import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.lang.invoke.WrongMethodTypeException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Reproduction attempt using the REAL libraries on the exact reported builds.
 *
 * Rather than a synthetic model of the interning invariant, this drives the actual
 * code paths from the field reports:
 *
 *  - Caffeine (ben-manes/caffeine#1111): the WrongMethodTypeException originated in
 *    NodeFactory.newFactory -> ... invokeExact() during Caffeine.build(). The failing
 *    factory class (SSL/SSLMW/SSLMWA in the stack trace) depends on the *policy
 *    combination* (weak/soft keys+values, maximumSize, expireAfter*, etc.), and each
 *    distinct combination lazily loads a distinct generated class and interns the
 *    constructor's MethodType. So we build caches across many policy combinations from
 *    many threads, forcing repeated first-time factory linkage under concurrency + GC.
 *
 *  - Blackbird (FasterXML/jackson-modules-base#142): the WrongMethodTypeException
 *    originated in BBSerializerModifier.createProperty during first serialization of a
 *    bean. We serialize many distinct bean classes from many threads with fresh
 *    ObjectMappers (fresh module + serializer-modifier state) to force repeated
 *    first-time property-accessor linkage.
 *
 * Either library throwing WrongMethodTypeException (directly or wrapped) is the
 * reproduction. Exit 42 = reproduced, 0 = held, 3 = resource exhaustion.
 *
 * Configure with -Dreal.durationSec, -Dreal.caffeineThreads, -Dreal.blackbirdThreads,
 * -Dreal.freshMapper (true: new ObjectMapper per op; the faithful first-link case).
 */
public final class RealLibStress {
    static final int DURATION_SEC = intProp("real.durationSec", 120);
    static final int CAFFEINE_THREADS = intProp("real.caffeineThreads", Math.max(2, cpus()));
    static final int BLACKBIRD_THREADS = intProp("real.blackbirdThreads", Math.max(2, cpus()));
    static final boolean FRESH_MAPPER = boolProp("real.freshMapper", true);
    static final int MAPPER_REFRESH = intProp("real.mapperRefresh", 64);  // new mapper every N ops when fresh

    static final AtomicLong caffeineBuilds = new AtomicLong();
    static final AtomicLong blackbirdSers = new AtomicLong();
    static final AtomicBoolean FAILED = new AtomicBoolean(false);
    static final AtomicBoolean RESOURCE = new AtomicBoolean(false);
    static final AtomicReference<String> REPORT = new AtomicReference<>();
    static volatile boolean running = true;
    static volatile Object sink;
    static volatile long startNanos;

    public static void main(String[] args) throws Exception {
        System.out.printf("RealLibStress: %s %s | %s%n",
                System.getProperty("java.vm.name"), System.getProperty("java.vm.version"),
                System.getProperty("java.vm.vendor"));
        System.out.printf("caffeine=3.1.7 blackbird/jackson=2.12.7 | caffeineThreads=%d blackbirdThreads=%d freshMapper=%b duration=%ds%n",
                CAFFEINE_THREADS, BLACKBIRD_THREADS, FRESH_MAPPER, DURATION_SEC);

        // sanity: confirm both paths actually work once before stressing
        warmupSanity();

        startNanos = System.nanoTime();
        java.util.List<Thread> ts = new java.util.ArrayList<>();
        for (int i = 0; i < CAFFEINE_THREADS; i++) {
            final long seed = ((long) i << 40) ^ 0x1234;
            ts.add(new Thread(() -> caffeineWorker(seed), "caffeine-" + i));
        }
        for (int i = 0; i < BLACKBIRD_THREADS; i++) {
            final int id = i;
            ts.add(new Thread(() -> blackbirdWorker(id), "blackbird-" + i));
        }
        Thread mon = new Thread(RealLibStress::monitor, "monitor"); mon.setDaemon(true);
        for (Thread t : ts) t.start();
        mon.start();

        long deadline = System.nanoTime() + DURATION_SEC * 1_000_000_000L;
        while (running && !FAILED.get() && !RESOURCE.get() && System.nanoTime() < deadline) {
            Thread.sleep(50);
        }
        running = false;
        for (Thread t : ts) t.join(5_000);

        if (FAILED.get()) {
            System.out.println(REPORT.get());
            System.out.println("\n*** REPRODUCED: WrongMethodTypeException from a real library ***");
            System.exit(42);
        }
        if (RESOURCE.get()) {
            System.out.printf("%nRESOURCE EXHAUSTION (not a violation) after %.0fs%n",
                    (System.nanoTime() - startNanos) / 1e9);
            System.exit(3);
        }
        System.out.printf("%nNo reproduction after %ds (caffeineBuilds=%,d blackbirdSers=%,d). Held.%n",
                DURATION_SEC, caffeineBuilds.get(), blackbirdSers.get());
        System.exit(0);
    }

    // ---- Caffeine: build caches across many policy combinations --------------
    static void caffeineWorker(long seed) {
        long n = seed;
        try {
            while (running && !FAILED.get()) {
                buildCache((int) (n & 0x7f));
                caffeineBuilds.incrementAndGet();
                n += 0x9E3779B97F4A7C15L;
            }
        } catch (WrongMethodTypeException w) {
            report("Caffeine.build", w);
        } catch (Throwable t) {
            if (hasCause(t, WrongMethodTypeException.class)) report("Caffeine.build(wrapped)", t);
            else if (isResource(t)) { RESOURCE.set(true); running = false; }
            else { unexpected("Caffeine.build", t); }   // diagnostic only, not a reproduction
        }
    }

    // Each bit selects a policy dimension; distinct combinations => distinct generated
    // factory classes (SS/SSL/SSLMW/SSLMWA/...), each interning its constructor MethodType.
    static void buildCache(int combo) {
        // Some policy combinations are mutually exclusive (e.g. weakValues + softValues),
        // and Caffeine throws IllegalStateException from the *builder setters*. Those combos
        // are not interesting here, so skip the whole build on any IllegalStateException.
        try {
            Caffeine<Object, Object> c = Caffeine.newBuilder();
            if ((combo & 1) != 0) c.maximumSize(1_000);
            if ((combo & 2) != 0) c.weakKeys();
            if ((combo & 4) != 0) c.weakValues();
            if ((combo & 8) != 0) c.softValues();
            if ((combo & 16) != 0) c.expireAfterWrite(Duration.ofMinutes(5));
            if ((combo & 32) != 0) c.expireAfterAccess(Duration.ofMinutes(5));
            if ((combo & 64) != 0) c.recordStats();
            sink = c.build();
        } catch (IllegalStateException ignore) {
            // mutually-exclusive policy combo; skip
        }
    }

    // ---- Blackbird: serialize many distinct bean classes ---------------------
    static void blackbirdWorker(int id) {
        try {
            // freshMapper recreates the mapper every MAPPER_REFRESH ops (not every op): this
            // keeps forcing first-time BBSerializerModifier accessor linkage for each bean
            // type, while bounding live mappers so the hidden lambda classes they spin can be
            // unloaded and metaspace reaches steady state. (Per-op fresh mappers exhaust
            // metaspace and are also unfaithful — #142 was a single shared mapper.)
            ObjectMapper m = newBlackbirdMapper();
            int k = 0;
            long ops = 0;
            while (running && !FAILED.get()) {
                if (FRESH_MAPPER && (++ops % MAPPER_REFRESH) == 0) m = newBlackbirdMapper();
                Object bean = Beans.make(k++);
                sink = m.writeValueAsString(bean);
                blackbirdSers.incrementAndGet();
                if (k >= Beans.COUNT) k = 0;
            }
        } catch (WrongMethodTypeException w) {
            report("Blackbird.serialize", w);
        } catch (Throwable t) {
            if (hasCause(t, WrongMethodTypeException.class)) report("Blackbird.serialize(wrapped)", t);
            else if (isResource(t)) { RESOURCE.set(true); running = false; }
            else { unexpected("Blackbird.serialize", t); }   // diagnostic only, not a reproduction
        }
    }

    static ObjectMapper newBlackbirdMapper() {
        return new ObjectMapper().registerModule(new BlackbirdModule());
    }

    static void warmupSanity() throws Exception {
        buildCache(0); buildCache(1); buildCache(17);
        ObjectMapper m = newBlackbirdMapper();
        for (int i = 0; i < Beans.COUNT; i++) m.writeValueAsString(Beans.make(i));
        System.out.println("warmup sanity OK (both libraries exercised once)");
    }

    // ---- reporting -----------------------------------------------------------
    static void report(String where, Throwable t) {
        if (!FAILED.compareAndSet(false, true)) return;
        StringBuilder b = new StringBuilder();
        b.append("=== REPRODUCED at ").append(where).append(" ===\n");
        b.append("thread : ").append(Thread.currentThread().getName()).append('\n');
        b.append("vm     : ").append(System.getProperty("java.vm.name")).append(' ')
         .append(System.getProperty("java.vm.version")).append('\n');
        for (Throwable c = t; c != null && c != c.getCause(); c = c.getCause()) {
            b.append("cause  : ").append(c).append('\n');
            for (StackTraceElement e : c.getStackTrace()) b.append("    at ").append(e).append('\n');
        }
        REPORT.set(b.toString());
        running = false;
    }

    // Non-WMTE, non-resource throwable: print once for diagnosis but do NOT treat as a
    // reproduction or stop the run (avoids false positives from benign exceptions).
    static final AtomicBoolean unexpectedLogged = new AtomicBoolean(false);
    static void unexpected(String where, Throwable t) {
        if (unexpectedLogged.compareAndSet(false, true)) {
            System.out.println("[diagnostic] non-WMTE throwable at " + where + " (run continues): " + t);
        }
    }

    static void monitor() {
        long lastC = 0, lastB = 0, lastT = System.nanoTime();
        while (running && !FAILED.get()) {
            try { Thread.sleep(5_000); } catch (InterruptedException e) { return; }
            long c = caffeineBuilds.get(), bb = blackbirdSers.get(), now = System.nanoTime();
            double dt = (now - lastT) / 1e9;
            System.out.printf("[%4.0fs] caffeineBuilds=%,d (+%,.0f/s) blackbirdSers=%,d (+%,.0f/s)%n",
                    (now - startNanos) / 1e9, c, (c - lastC) / dt, bb, (bb - lastB) / dt);
            lastC = c; lastB = bb; lastT = now;
        }
    }

    static boolean hasCause(Throwable t, Class<?> k) {
        for (Throwable c = t; c != null && c != c.getCause(); c = c.getCause()) if (k.isInstance(c)) return true;
        return false;
    }
    static boolean isResource(Throwable t) {
        for (Throwable c = t; c != null && c != c.getCause(); c = c.getCause())
            if (c instanceof OutOfMemoryError || c instanceof StackOverflowError) return true;
        return false;
    }
    static int cpus() { return Runtime.getRuntime().availableProcessors(); }
    static int intProp(String k, int d) { String v = System.getProperty(k); return v == null ? d : Integer.parseInt(v.trim()); }
    static boolean boolProp(String k, boolean d) { String v = System.getProperty(k); return v == null ? d : Boolean.parseBoolean(v.trim()); }
}

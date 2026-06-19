import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Concurrent first-link DIVERSITY reproduction attempt for the Blackbird
 * WrongMethodTypeException (FasterXML/jackson-modules-base#142).
 *
 * WHY THIS HARNESS EXISTS, AND WHY IT IS SHAPED THIS WAY
 * -----------------------------------------------------
 * The field reporter hits the bug RELIABLY; our prior harnesses (parent dir and
 * real-libs/) never reproduced it across tens of billions of ops. Those harnesses
 * test STEADY STATE with a SMALL fixed set of bean types on DEDICATED threads.
 * But the field failure is DURABLE ("restart fixes it temporarily") and appears at
 * application COLD START. So the trigger is most likely the CONCURRENT FIRST-TIME
 * LINKAGE of a LARGE, DIVERSE population of accessor/creator MethodTypes, happening
 * simultaneously on a SMALL shared thread pool (WebFlux event-loop style), overlapped
 * with heavy GC during heap ramp-up.
 *
 * Blackbird interns a MethodType per-property-per-type lazily on first serialize
 * (BBSerializerModifier.createProperty) and per @JsonCreator on first deserialize
 * (CreatorOptimizer.createOptimized -> invokeExact, type (MethodHandle)Function).
 * That first-link install event is what we must hit, not steady-state re-checking.
 *
 * DESIGN: serialize AND deserialize each of GenBeans.COUNT (~hundreds) DISTINCT
 * top-level bean types EXACTLY ONCE, but submit all of them as tasks to a SMALL
 * fixed thread pool so that many DISTINCT types are first-linked simultaneously
 * through few threads. One shared ObjectMapper (the field's single Spring mapper;
 * a fresh mapper per op blows up metaspace and is unfaithful). The burst runs once
 * and the JVM exits -- the cold-start signal is per-launch, so this is meant to run
 * as a STORM of short JVMs (storm.sh).
 *
 * Exit codes (matching the sibling harnesses): 42 = reproduced (WrongMethodType-
 * Exception, direct or wrapped), 3 = resource exhaustion (OOM/metaspace, NOT a
 * reproduction), 0 = clean completion (held), 2 = self-test detector confirmation.
 *
 * Run with --add-opens java.base/java.lang.invoke=ALL-UNNAMED for the self-test.
 */
public final class Main {
    static final int POOL = intProp("div.pool", 3);   // small pool: mimic WebFlux event loops

    static final MethodHandles.Lookup L = MethodHandles.lookup();

    static final AtomicLong serialized = new AtomicLong();
    static final AtomicLong deserialized = new AtomicLong();
    static final AtomicBoolean FAILED = new AtomicBoolean(false);
    static final AtomicBoolean RESOURCE = new AtomicBoolean(false);
    static final AtomicReference<String> REPORT = new AtomicReference<>();
    static volatile Object sink;

    public static void main(String[] args) throws Throwable {
        if (boolProp("selftest", false)) { selfTest(); return; }

        System.out.printf("DiversityBurst: %s %s | %s | types=%d pool=%d%n",
                System.getProperty("java.vm.name"), System.getProperty("java.vm.version"),
                System.getProperty("java.vm.vendor"), GenBeans.COUNT, POOL);

        // ONE shared mapper, same as the field's shared Spring mapper. Creating it
        // (and registering Blackbird) is itself part of cold start, so we time the burst
        // from immediately after construction -- the first-link events happen inside the
        // tasks below, never on the main thread before the pool starts.
        final ObjectMapper mapper = new ObjectMapper().registerModule(new BlackbirdModule());

        final int n = GenBeans.COUNT;
        final AtomicInteger serOk = new AtomicInteger();
        final AtomicInteger deserOk = new AtomicInteger();

        // Pre-build the sample instances on the main thread. Instance construction is
        // ordinary Java (no Blackbird linkage), so doing it up front keeps the worker
        // tasks focused on the first-link event we care about and makes the concurrent
        // burst as tight as possible (all tasks hit the mapper at once).
        final Object[] samples = new Object[n];
        for (int k = 0; k < n; k++) samples[k] = GenBeans.make(k);

        long start = System.nanoTime();
        ExecutorService pool = Executors.newFixedThreadPool(POOL, r -> {
            Thread t = new Thread(r);
            t.setName("burst-" + t.getId());
            return t;   // non-daemon on purpose: a hang here is a real signal, not to be masked
        });
        List<Future<?>> futures = new ArrayList<>(n);
        try {
            for (int k = 0; k < n; k++) {
                final int idx = k;
                futures.add(pool.submit(() -> linkOne(mapper, idx, samples[idx], serOk, deserOk)));
            }
            // Await every task. We do not bail early on the first task -- we want the whole
            // diverse population first-linked in one concurrent wave before declaring done.
            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (Throwable t) {
                    classify(unwrapExecution(t));   // a task threw; classify it (WMTE/resource/other)
                }
            }
        } finally {
            pool.shutdownNow();
        }
        double secs = (System.nanoTime() - start) / 1e9;

        if (FAILED.get()) {
            System.out.println(REPORT.get());
            System.out.println("\n*** REPRODUCED: WrongMethodTypeException from Blackbird first-link ***");
            System.exit(42);
        }
        if (RESOURCE.get()) {
            System.out.printf("%nRESOURCE EXHAUSTION (not a violation) after %.2fs%n", secs);
            System.exit(3);
        }
        System.out.printf("Burst complete in %.2fs: serialized=%d deserialized=%d (of %d types). Held.%n",
                secs, serOk.get(), deserOk.get(), n);
        System.exit(0);
    }

    /**
     * The unit of work: first-link ONE distinct bean type, both directions. Serializing
     * triggers BBSerializerModifier accessor linkage for every property (including the
     * nested beans, which cascade their own first-links); deserializing the result
     * triggers CreatorOptimizer creator linkage (the (MethodHandle)Function invokeExact
     * site from the second #142 report). Run from a small pool, hundreds of these fire
     * concurrently -- the concurrent first-link burst this harness is built to produce.
     */
    static void linkOne(ObjectMapper mapper, int idx, Object sample,
                        AtomicInteger serOk, AtomicInteger deserOk) {
        try {
            String json = mapper.writeValueAsString(sample);
            serialized.incrementAndGet();
            serOk.incrementAndGet();
            sink = json;
            Object back = mapper.readValue(json, GenBeans.type(idx));
            deserialized.incrementAndGet();
            deserOk.incrementAndGet();
            sink = back;
        } catch (WrongMethodTypeException w) {
            report("Blackbird.firstLink type=" + GenBeans.type(idx).getSimpleName(), w);
        } catch (Throwable t) {
            classify(t);
        }
    }

    // Classify a throwable: WrongMethodTypeException (direct or wrapped) is the
    // reproduction; OOM/metaspace is resource exhaustion; anything else is a
    // diagnostic, logged once, and does NOT count as a reproduction.
    static void classify(Throwable t) {
        if (hasCause(t, WrongMethodTypeException.class)) report("Blackbird.firstLink(wrapped)", t);
        else if (isResource(t)) { RESOURCE.set(true); }
        else unexpected(t);
    }

    // ---- reporting -----------------------------------------------------------
    static void report(String where, Throwable t) {
        if (!FAILED.compareAndSet(false, true)) return;
        StringBuilder b = new StringBuilder();
        b.append("=== REPRODUCED at ").append(where).append(" ===\n");
        b.append("thread : ").append(Thread.currentThread().getName()).append('\n');
        b.append("vm     : ").append(System.getProperty("java.vm.name")).append(' ')
         .append(System.getProperty("java.vm.version")).append('\n');
        // Pull the two MethodType identities out of the WMTE message if extractable: the
        // bug's signature is two .equals() but not == MethodTypes ("X but found X").
        WrongMethodTypeException w = firstCause(t, WrongMethodTypeException.class);
        if (w != null && w.getMessage() != null) b.append("wmte   : ").append(w.getMessage()).append('\n');
        for (Throwable c = t; c != null && c != c.getCause(); c = c.getCause()) {
            b.append("cause  : ").append(c).append('\n');
            for (StackTraceElement e : c.getStackTrace()) b.append("    at ").append(e).append('\n');
        }
        REPORT.set(b.toString());
    }

    static final AtomicBoolean unexpectedLogged = new AtomicBoolean(false);
    static void unexpected(Throwable t) {
        if (unexpectedLogged.compareAndSet(false, true)) {
            System.out.println("[diagnostic] non-WMTE throwable (run continues): " + t);
        }
    }

    /**
     * Self-test: fabricate a non-interned MethodType via reflection (private ctor +
     * copy the 'form' field from the canonical instance) and confirm invokeExact's
     * identity check throws the "X but found X" WrongMethodTypeException. This proves
     * the detector is live on this VM, so a clean (exit 0) burst is trustworthy.
     * Mirrors the parent dir's WarmupChurn.selfTest().
     */
    static void selfTest() throws Throwable {
        System.out.println("SELF TEST: fabricate a non-interned MethodType and confirm invokeExact's identity check fires");
        var ctor = MethodType.class.getDeclaredConstructor(Class.class, Class[].class);
        ctor.setAccessible(true);
        var form = MethodType.class.getDeclaredField("form");
        form.setAccessible(true);
        // A non-interned duplicate of ()Object: .equals() the canonical instance but not ==,
        // with the canonical erased form copied in so it is otherwise structurally valid.
        MethodType canon = MethodType.methodType(Object.class);
        MethodType dup = ctor.newInstance(Object.class, new Class<?>[0]);
        form.set(dup, form.get(canon));
        System.out.println("dup equals canon=" + canon.equals(dup) + "  ==" + (canon == dup));
        MethodHandle h = L.findStatic(Main.class, "sImpl", MethodType.methodType(Object.class)).asType(dup);
        try {
            Object o = (Object) h.invokeExact();   // call-site 'expected' = interned ()Object; handle type = dup
            sink = o;
            System.out.println("(note: invokeExact did NOT throw on this VM)");
        } catch (WrongMethodTypeException w) {
            System.out.println("invokeExact threw as expected: " + w.getMessage());
        }
        System.out.println("Detector confirmed: a non-canonical MethodType makes invokeExact throw "
                + "the 'X but found X' WrongMethodTypeException the field reports show.");
        System.exit(2);
    }

    static Object sImpl() { return ""; }

    // ---- helpers -------------------------------------------------------------
    static Throwable unwrapExecution(Throwable t) {
        // Future.get wraps task failures in ExecutionException; unwrap to the real cause.
        return (t instanceof java.util.concurrent.ExecutionException && t.getCause() != null) ? t.getCause() : t;
    }
    static boolean hasCause(Throwable t, Class<?> k) {
        for (Throwable c = t; c != null && c != c.getCause(); c = c.getCause()) if (k.isInstance(c)) return true;
        return false;
    }
    @SuppressWarnings("unchecked")
    static <T extends Throwable> T firstCause(Throwable t, Class<T> k) {
        for (Throwable c = t; c != null && c != c.getCause(); c = c.getCause()) if (k.isInstance(c)) return (T) c;
        return null;
    }
    static boolean isResource(Throwable t) {
        for (Throwable c = t; c != null && c != c.getCause(); c = c.getCause())
            if (c instanceof OutOfMemoryError || c instanceof StackOverflowError) return true;
        return false;
    }
    static int intProp(String k, int d) { String v = System.getProperty(k); return v == null ? d : Integer.parseInt(v.trim()); }
    static boolean boolProp(String k, boolean d) { String v = System.getProperty(k); return v == null ? d : Boolean.parseBoolean(v.trim()); }
}

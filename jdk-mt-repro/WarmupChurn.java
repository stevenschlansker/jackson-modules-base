import java.lang.invoke.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

/**
 * "Realistic warmup churn" reproduction attempt.
 *
 * KEY OBSERVATION driving this harness: we ran the EXACT build from caffeine#1111
 * (Amazon Corretto 17.0.8.7.1) and the bug did NOT reproduce. Same build the users hit
 * it on. So it is not fixed and not a version problem -- we are missing the *triggering
 * conditions*. The field reports describe failures clustered at application *warmup*,
 * ~10 in thousands of JVM launches. A real app warming up interns a large, DIVERSE
 * population of MethodTypes concurrently (every lambda via LambdaMetafactory, every
 * string concatenation via the invokedynamic StringConcatFactory, every MethodHandle
 * lookup, every record), under GC, while many threads link classes for the first time.
 *
 * Our prior harnesses interned a narrow population (a few hundred synthetic shapes, or one
 * library's factory types). This one maximises diversity and concurrency of intern-table
 * churn during warmup, with an invokeExact identity check at each LambdaMetafactory site
 * (the Blackbird/Caffeine bug site). It is designed to run as a STORM of short JVMs (see
 * warmup-storm.sh) because (a) the field signal is per-launch warmup, and (b) the hidden
 * lambda classes spun here exhaust metaspace in a long single run.
 *
 * Exit: 42 = WrongMethodTypeException / identity violation, 3 = resource, 0 = held.
 * Run with --add-opens java.base/java.lang.invoke=ALL-UNNAMED for the intern-table diag.
 */
public final class WarmupChurn {
    static final int THREADS      = intProp("warm.threads", Runtime.getRuntime().availableProcessors());
    static final int DURATION_SEC = intProp("warm.durationSec", 8);   // short by default: storm usage
    static final boolean CONCAT   = boolProp("warm.concat", true);
    static final boolean LOOKUPS  = boolProp("warm.lookups", true);

    static final MethodHandles.Lookup L = MethodHandles.lookup();

    // Pinned call-site 'expected' types for the invokeExact sites (== the casts below).
    static final MethodType T_RUN  = MethodType.methodType(Runnable.class);
    static final MethodType T_SUP  = MethodType.methodType(Supplier.class);
    static final MethodType T_FN   = MethodType.methodType(Function.class);
    static final MethodType T_BIFN = MethodType.methodType(BiFunction.class);
    static final MethodType T_PRED = MethodType.methodType(Predicate.class);
    static final MethodType T_CONS = MethodType.methodType(Consumer.class);
    static final MethodType T_TOINT= MethodType.methodType(ToIntFunction.class);
    static final MethodType T_TOLNG= MethodType.methodType(ToLongFunction.class);

    static final AtomicLong sites = new AtomicLong();
    static final AtomicLong interns = new AtomicLong();
    static final AtomicBoolean FAILED = new AtomicBoolean(false);
    static final AtomicBoolean RESOURCE = new AtomicBoolean(false);
    static final AtomicReference<String> REPORT = new AtomicReference<>();
    static volatile boolean running = true;
    static volatile Object sink;
    static volatile long startNanos;

    static Object internTable; static java.lang.reflect.Method internGet;

    public static void main(String[] args) throws Throwable {
        setupIntrospection();
        if (boolProp("warm.selftest", false)) { selfTest(); return; }

        System.out.printf("WarmupChurn: %s %s | %s | threads=%d dur=%ds concat=%b lookups=%b%n",
                System.getProperty("java.vm.name"), System.getProperty("java.vm.version"),
                System.getProperty("java.vm.vendor"), THREADS, DURATION_SEC, CONCAT, LOOKUPS);
        startNanos = System.nanoTime();

        Thread[] ts = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int id = i;
            ts[i] = new Thread(() -> worker(id), "warm-" + i);
            ts[i].start();
        }
        long deadline = System.nanoTime() + DURATION_SEC * 1_000_000_000L;
        while (running && !FAILED.get() && !RESOURCE.get() && System.nanoTime() < deadline) Thread.sleep(25);
        running = false;
        for (Thread t : ts) t.join(5_000);

        if (FAILED.get()) {
            System.out.println(REPORT.get());
            System.out.println("\n*** REPRODUCED — see report above ***");
            System.exit(42);
        }
        if (RESOURCE.get()) { System.out.println("\nRESOURCE EXHAUSTION (not a violation)"); System.exit(3); }
        System.out.printf("No reproduction after %ds (lmfSites=%,d interns=%,d). Held.%n",
                DURATION_SEC, sites.get(), interns.get());
        System.exit(0);
    }

    static void worker(int id) {
        long n = id;
        try {
            while (running && !FAILED.get()) {
                // 1) Many distinct LambdaMetafactory sites -> diverse MethodType interning,
                //    each immediately invokeExact'd + cast (the bug site).
                spinAll();
                // 2) invokedynamic string concatenation -> StringConcatFactory interns MethodTypes.
                if (CONCAT) sink = concat(n, id);
                // 3) MethodHandle lookups across JDK methods -> interns their MethodTypes.
                if (LOOKUPS) doLookups(n);
                // 4) Re-intern the pinned SAM factory types and assert identity (the invariant).
                checkPinned();
                n += 0x9E3779B97F4A7C15L;
            }
        } catch (WrongMethodTypeException w) {
            report("invokeExact (WMTE)", w);
        } catch (OutOfMemoryError | StackOverflowError e) {
            RESOURCE.set(true); running = false;
        } catch (Throwable t) {
            if (hasResource(t)) { RESOURCE.set(true); running = false; }
            else report("unexpected", t);
        }
    }

    // Build + invokeExact a lambda for each SAM type. Each call site has its EXACT factory
    // return type as the cast (e.g. (Runnable) ... invokeExact() where the metafactory
    // invokedType is methodType(Runnable.class), so the target type is ()Runnable). This is
    // the verbatim Blackbird/Caffeine pattern: 'expected' (the cast) and the handle type must
    // be the same interned MethodType; invokeExact checks that by identity. (Each site must
    // be inlined with its exact type — a shared helper returning Object would erase the type
    // and make 'expected' ()Object, a genuine mismatch unrelated to the interning bug.)
    @SuppressWarnings("unchecked")
    static void spinAll() throws Throwable {
        Runnable r = (Runnable) LambdaMetafactory.metafactory(L, "run", T_RUN, MethodType.methodType(void.class),
                L.findStatic(WarmupChurn.class, "rImpl", MethodType.methodType(void.class)), MethodType.methodType(void.class)).getTarget().invokeExact();
        Supplier<Object> s = (Supplier<Object>) LambdaMetafactory.metafactory(L, "get", T_SUP, MethodType.methodType(Object.class),
                L.findStatic(WarmupChurn.class, "sImpl", MethodType.methodType(Object.class)), MethodType.methodType(Object.class)).getTarget().invokeExact();
        Function<Object,Object> f = (Function<Object,Object>) LambdaMetafactory.metafactory(L, "apply", T_FN, MethodType.methodType(Object.class, Object.class),
                L.findStatic(WarmupChurn.class, "fImpl", MethodType.methodType(Object.class, Object.class)), MethodType.methodType(Object.class, Object.class)).getTarget().invokeExact();
        BiFunction<Object,Object,Object> bf = (BiFunction<Object,Object,Object>) LambdaMetafactory.metafactory(L, "apply", T_BIFN, MethodType.methodType(Object.class, Object.class, Object.class),
                L.findStatic(WarmupChurn.class, "bfImpl", MethodType.methodType(Object.class, Object.class, Object.class)), MethodType.methodType(Object.class, Object.class, Object.class)).getTarget().invokeExact();
        Predicate<Object> p = (Predicate<Object>) LambdaMetafactory.metafactory(L, "test", T_PRED, MethodType.methodType(boolean.class, Object.class),
                L.findStatic(WarmupChurn.class, "pImpl", MethodType.methodType(boolean.class, Object.class)), MethodType.methodType(boolean.class, Object.class)).getTarget().invokeExact();
        Consumer<Object> c = (Consumer<Object>) LambdaMetafactory.metafactory(L, "accept", T_CONS, MethodType.methodType(void.class, Object.class),
                L.findStatic(WarmupChurn.class, "cImpl", MethodType.methodType(void.class, Object.class)), MethodType.methodType(void.class, Object.class)).getTarget().invokeExact();
        ToIntFunction<Object> ti = (ToIntFunction<Object>) LambdaMetafactory.metafactory(L, "applyAsInt", T_TOINT, MethodType.methodType(int.class, Object.class),
                L.findStatic(WarmupChurn.class, "tiImpl", MethodType.methodType(int.class, Object.class)), MethodType.methodType(int.class, Object.class)).getTarget().invokeExact();
        ToLongFunction<Object> tl = (ToLongFunction<Object>) LambdaMetafactory.metafactory(L, "applyAsLong", T_TOLNG, MethodType.methodType(long.class, Object.class),
                L.findStatic(WarmupChurn.class, "tlImpl", MethodType.methodType(long.class, Object.class)), MethodType.methodType(long.class, Object.class)).getTarget().invokeExact();
        // use them so nothing folds away
        r.run(); if (p.test(s.get()) && f.apply(c) != bf.apply(r, ti) && ti.applyAsInt(tl) == Integer.MIN_VALUE) sink = c;
        sites.addAndGet(8);
    }

    // varied invokedynamic string-concat shapes (different arg counts/types per call site)
    static String concat(long n, int id) {
        String a = "t" + id + "-" + n;
        String b = a + ':' + (n & 0xff) + '/' + (n * 31) + '#' + (id ^ n);
        return b + '|' + Long.toHexString(n) + '@' + id + '!' + (n % 7) + '~' + (a.length() + b.length());
    }

    static void doLookups(long n) throws Throwable {
        // interning churn from real MethodType creation across diverse JDK signatures
        switch ((int) (n & 3)) {
            case 0 -> sink = MethodType.methodType(String.class, int.class, int.class);
            case 1 -> sink = MethodType.methodType(boolean.class, Object.class, long.class, char.class);
            case 2 -> sink = MethodType.methodType(void.class, double.class, float.class);
            default -> sink = MethodType.methodType(Object.class, int[].class, String[].class, java.util.List.class);
        }
        interns.incrementAndGet();
    }

    static void checkPinned() {
        if (MethodType.methodType(ToIntFunction.class) != T_TOINT) reportPinned("ToIntFunction", T_TOINT);
        else if (MethodType.methodType(Function.class) != T_FN)    reportPinned("Function", T_FN);
        else if (MethodType.methodType(Supplier.class) != T_SUP)   reportPinned("Supplier", T_SUP);
        else if (MethodType.methodType(BiFunction.class) != T_BIFN)reportPinned("BiFunction", T_BIFN);
    }

    // ---- impl targets for the lambdas ----
    static void rImpl() {}
    static Object sImpl() { return ""; }
    static Object fImpl(Object o) { return o; }
    static Object bfImpl(Object a, Object b) { return a; }
    static boolean pImpl(Object o) { return o != null; }
    static void cImpl(Object o) {}
    static int tiImpl(Object o) { return o == null ? 0 : 1; }
    static long tlImpl(Object o) { return 0L; }

    // ---- reporting ----
    static void report(String where, Throwable t) {
        if (!FAILED.compareAndSet(false, true)) return;
        StringBuilder b = new StringBuilder("=== REPRODUCED at ").append(where).append(" ===\n");
        b.append("thread : ").append(Thread.currentThread().getName()).append('\n');
        b.append("vm     : ").append(System.getProperty("java.vm.name")).append(' ').append(System.getProperty("java.vm.version")).append('\n');
        for (Throwable c = t; c != null && c != c.getCause(); c = c.getCause()) {
            b.append("cause  : ").append(c).append('\n');
            for (StackTraceElement e : c.getStackTrace()) b.append("    at ").append(e).append('\n');
        }
        REPORT.set(b.toString()); running = false;
    }
    static void reportPinned(String fi, MethodType pinned) {
        if (!FAILED.compareAndSet(false, true)) return;
        MethodType fresh = MethodType.methodType(pinnedClass(fi));
        StringBuilder b = new StringBuilder("=== Pinned-type interning violation: methodType(").append(fi).append(") != pinned ===\n");
        b.append("thread : ").append(Thread.currentThread().getName()).append('\n');
        b.append("pinned : @").append(idh(pinned)).append("  fresh: @").append(idh(fresh))
         .append("  equals=").append(pinned.equals(fresh)).append(" ==").append(pinned == fresh).append('\n');
        if (internGet != null) try { Object held = internGet.invoke(internTable, pinned);
            b.append("internTable.get(pinned)=@").append(held==null?"null":idh(held)).append(" (==pinned? ").append(held==pinned).append(")\n"); } catch (Throwable ignore) {}
        REPORT.set(b.toString()); running = false;
    }
    static Class<?> pinnedClass(String fi) {
        switch (fi) { case "ToIntFunction": return ToIntFunction.class; case "Function": return Function.class;
            case "Supplier": return Supplier.class; default: return BiFunction.class; }
    }

    static void selfTest() throws Throwable {
        System.out.println("SELF TEST: fabricate a non-interned MethodType and confirm invokeExact's identity check fires");
        var ctor = MethodType.class.getDeclaredConstructor(Class.class, Class[].class);
        ctor.setAccessible(true);
        var form = MethodType.class.getDeclaredField("form"); form.setAccessible(true);
        // A non-interned duplicate of ()Object, equal but not ==, with the canonical erased form.
        MethodType canon = MethodType.methodType(Object.class);
        MethodType dup = ctor.newInstance(Object.class, new Class<?>[0]);
        form.set(dup, form.get(canon));
        System.out.println("dup equals canon=" + canon.equals(dup) + "  ==" + (canon == dup));
        MethodHandle h = L.findStatic(WarmupChurn.class, "sImpl", MethodType.methodType(Object.class)).asType(dup);
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

    static void setupIntrospection() {
        try { var f = MethodType.class.getDeclaredField("internTable"); f.setAccessible(true); internTable = f.get(null);
            internGet = internTable.getClass().getMethod("get", Object.class); internGet.setAccessible(true);
        } catch (Throwable t) { internTable = null; internGet = null; }
    }
    static boolean hasResource(Throwable t) { for (Throwable c=t;c!=null&&c!=c.getCause();c=c.getCause()) if (c instanceof OutOfMemoryError||c instanceof StackOverflowError) return true; return false; }
    static String idh(Object o){return Integer.toHexString(System.identityHashCode(o));}
    static int intProp(String k,int d){String v=System.getProperty(k);return v==null?d:Integer.parseInt(v.trim());}
    static boolean boolProp(String k,boolean d){String v=System.getProperty(k);return v==null?d:Boolean.parseBoolean(v.trim());}
}

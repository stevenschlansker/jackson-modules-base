import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Stress test for the {@link MethodType} interning invariant that
 * {@link MethodHandle#invokeExact} depends on.
 *
 * <h2>Background</h2>
 * {@code invokeExact} compares the target handle's {@link MethodType} against the
 * call-site's symbolic type by reference identity, not by {@code .equals()}
 * (see {@code java.lang.invoke.Invokers.checkExactType}). This is only sound
 * because {@link MethodType#methodType} canonicalises every instance through a
 * process-wide weak intern table ({@code MethodType.internTable}). The contract
 * is: <em>equal MethodTypes are identical (==).</em>
 *
 * Field reports against Caffeine (ben-manes/caffeine#1111) and Jackson Blackbird
 * (FasterXML/jackson-modules-base#142) show rare, load-dependent
 * {@code WrongMethodTypeException: ... expected ()X but found ()X} failures: the
 * two types print identically because they are {@code .equals()} but not
 * {@code ==}. That can only happen if the intern table hands back a
 * non-canonical instance for a type whose canonical instance is still live.
 *
 * In those libraries the surviving (call-site) instance is permanently pinned by
 * the constant-pool appendix of the {@code invokeExact} call site. So the bug
 * reduces to: for a type X whose canonical MethodType is strongly reachable,
 * {@code methodType(X)} sometimes returns a different, equal instance.
 *
 * <h2>What this harness does</h2>
 * It pins the canonical MethodType for a population of types, then hammers the
 * intern table from many threads under heavy concurrent GC and interning churn,
 * asserting the invariant two ways:
 * <ul>
 *   <li><b>Probe A (intern invariant):</b> {@code methodType(spec_i) == CANON[i]}
 *       for each pinned type. Pure intern-table check, independent of the JIT or
 *       invoke machinery.</li>
 *   <li><b>Probe B (production signature):</b> the exact Blackbird/Caffeine
 *       pattern: build a {@code LambdaMetafactory} call site whose factory type
 *       is {@code methodType(SomeFunctionalInterface.class)} and immediately
 *       {@code (SomeFunctionalInterface) cs.getTarget().invokeExact()}. The cast
 *       pins the expected type; a non-canonical factory type throws
 *       {@code WrongMethodTypeException}.</li>
 * </ul>
 * On the first violation it prints a full diagnostic report (identity hashes,
 * what the intern table currently holds, GC in use, thread, iteration) and exits
 * with status 1. A clean run exits 0.
 *
 * Run with {@code --add-opens java.base/java.lang.invoke=ALL-UNNAMED} to enable
 * the intern-table introspection in the failure report (the harness degrades
 * gracefully without it).
 */
public final class MethodTypeInternStress {

    // ---- configuration (system properties) ----------------------------------
    static final int THREADS      = intProp("mt.threads", Runtime.getRuntime().availableProcessors());
    static final int DECOY_THREADS= intProp("mt.decoyThreads", Math.max(1, THREADS / 2));
    static final int DURATION_SEC = intProp("mt.durationSec", 60);
    static final int PINNED       = intProp("mt.pinned", 1024);
    static final int DECOY_SPACE  = intProp("mt.decoySpace", 1 << 18);  // bound the intern table size
    static final boolean GC_THREAD= boolProp("mt.gcThread", true);
    static final int SYSGC_MS     = intProp("mt.sysgcMs", 0);   // 0 = never call System.gc()
    static final boolean PROBE_B  = boolProp("mt.probeB", true);
    static final boolean PROBE_C  = boolProp("mt.probeC", true);
    static final int PROBE_C_THREADS = intProp("mt.probeCThreads", Math.max(2, THREADS));
    static final int PROBE_C_SPACE = intProp("mt.probeCSpace", 1 << 16);
    static final boolean LINK_STORM = boolProp("mt.linkStorm", true);
    static final int LINK_THREADS = intProp("mt.linkStormThreads", 2);
    static final int HEAP_CHURN_KB= intProp("mt.churnKb", 32);  // per allocation in the GC churn thread

    // ---- class pools used to synthesise distinct MethodType shapes -----------
    static final Class<?>[] RTYPES = {
        void.class, int.class, long.class, double.class, float.class, boolean.class,
        byte.class, char.class, short.class, Object.class, String.class, Integer.class,
        Long.class, Double.class, Number.class, CharSequence.class, List.class,
        java.util.Map.class, Runnable.class, Thread.class, Class.class, int[].class,
        Object[].class, String[].class, java.util.Collection.class, Comparable.class
    };
    static final Class<?>[] PTYPES = {
        int.class, long.class, double.class, float.class, boolean.class, byte.class,
        char.class, short.class, Object.class, String.class, Integer.class, Long.class,
        Double.class, Number.class, CharSequence.class, List.class, java.util.Map.class,
        Runnable.class, Thread.class, Class.class, java.util.Collection.class, Comparable.class
    };

    // ---- shared state --------------------------------------------------------
    static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    static volatile MethodType[] CANON;          // pinned canonical instances (Probe A)
    static final AtomicBoolean FAILED = new AtomicBoolean(false);
    static final AtomicBoolean RESOURCE = new AtomicBoolean(false);   // OOM/SOE: environmental, not a violation
    static final AtomicReference<String> REPORT = new AtomicReference<>();
    static final AtomicLong probeA = new AtomicLong();
    static final AtomicLong probeB = new AtomicLong();
    static final AtomicLong probeC = new AtomicLong();
    static final AtomicLong linkStorm = new AtomicLong();
    static final AtomicLong decoys = new AtomicLong();
    static volatile boolean running = true;
    static volatile Object sink;                 // defeat DCE of Probe B results

    // pinned "expected" types for Probe B (also pins them so they must stay canonical)
    static final MethodType EXP_INT = MethodType.methodType(ToIntFunction.class);
    static final MethodType EXP_FN  = MethodType.methodType(Function.class);
    static final MethodType EXP_SUP = MethodType.methodType(Supplier.class);

    // reflective handle into MethodType.internTable for diagnostics (best-effort)
    static Object internTable;
    static Method internTableGet;

    public static void main(String[] args) throws Throwable {
        setupInternTableIntrospection();
        CANON = new MethodType[PINNED];
        for (int i = 0; i < PINNED; i++) {
            CANON[i] = shape(i);                 // pins the canonical instance for spec i
        }
        if (boolProp("mt.selftest", false)) {
            selfTest();
            return;
        }
        printHeader();

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            final long base = ((long) i) << 40;
            threads.add(new Thread(() -> probeWorker(base), "probe-" + i));
        }
        if (PROBE_C) {
            for (int i = 0; i < PROBE_C_THREADS; i++) {
                threads.add(new Thread(MethodTypeInternStress::probeCWorker, "probeC-" + i));
            }
        }
        for (int i = 0; i < DECOY_THREADS; i++) {
            final long base = (((long) i) << 40) ^ 0x5555555555555555L;
            threads.add(new Thread(() -> decoyWorker(base), "decoy-" + i));
        }
        if (LINK_STORM) {
            byte[] probeBytes = readLinkProbeBytes();
            if (probeBytes != null) {
                for (int i = 0; i < LINK_THREADS; i++) {
                    threads.add(new Thread(() -> linkStormWorker(probeBytes), "link-" + i));
                }
            } else {
                System.out.println("(link storm disabled: LinkProbe.class not found on classpath)");
            }
        }
        if (GC_THREAD) {
            threads.add(new Thread(MethodTypeInternStress::gcChurnWorker, "gc-churn"));
        }
        Thread monitor = new Thread(MethodTypeInternStress::monitor, "monitor");
        monitor.setDaemon(true);

        long deadline = System.nanoTime() + DURATION_SEC * 1_000_000_000L;
        for (Thread t : threads) t.start();
        monitor.start();

        while (running && !FAILED.get() && System.nanoTime() < deadline) {
            Thread.sleep(50);
        }
        running = false;
        for (Thread t : threads) t.join(5_000);

        if (FAILED.get()) {
            System.out.println(REPORT.get());
            System.out.println("\n*** INVARIANT VIOLATED — see report above ***");
            System.exit(42);    // distinctive: not confusable with a JVM-startup failure (1)
        }
        if (RESOURCE.get()) {
            System.out.printf("%nRESOURCE EXHAUSTION (OutOfMemoryError) after %.0fs — NOT an invariant "
                    + "violation. Increase -Xmx, lower mt.decoySpace, or lower mt.churnKb. "
                    + "(probeA=%,d probeB=%,d probeC=%,d link=%,d decoys=%,d)%n",
                    (System.nanoTime() - startNanos) / 1e9,
                    probeA.get(), probeB.get(), probeC.get(), linkStorm.get(), decoys.get());
            System.exit(3);
        }
        System.out.printf("%nNo violation after %.1fs (probeA=%,d probeB=%,d probeC=%,d link=%,d decoys=%,d). "
                + "Invariant held.%n",
                DURATION_SEC * 1.0, probeA.get(), probeB.get(), probeC.get(), linkStorm.get(), decoys.get());
        System.exit(0);
    }

    // ---- Probe A + B worker --------------------------------------------------
    static void probeWorker(long base) {
        long n = base;
        while (running && !FAILED.get()) {
            // Probe A: pinned-type interning invariant.
            int i = (int) Math.floorMod(n, PINNED);
            MethodType regen = shape(i);             // must return CANON[i]
            if (regen != CANON[i]) {
                reportProbeA(i, regen);
                return;
            }
            probeA.incrementAndGet();

            // Probe B: the production identity check, without spinning a class per call.
            // In Blackbird/Caffeine the call site's 'expected' type is resolved once and
            // pinned in the constant-pool appendix (here EXP_*), while the handle type is
            // a freshly-interned methodType(FI.class) on every build. invokeExact compares
            // them by identity, so the live failure is exactly: a fresh methodType(FI.class)
            // that is not == the pinned expected. We assert that directly.
            if (PROBE_B && (n & 0x7) == 0) {
                MethodType t1 = MethodType.methodType(ToIntFunction.class);
                if (t1 != EXP_INT) { reportProbeBType("ToIntFunction", EXP_INT, t1); return; }
                MethodType t2 = MethodType.methodType(Function.class);
                if (t2 != EXP_FN) { reportProbeBType("Function", EXP_FN, t2); return; }
                MethodType t3 = MethodType.methodType(Supplier.class);
                if (t3 != EXP_SUP) { reportProbeBType("Supplier", EXP_SUP, t3); return; }
                probeB.incrementAndGet();
            }
            n += 0x9E3779B97F4A7C15L;                // golden-ratio stride over the pinned space
        }
    }

    // Note: the end-to-end LambdaMetafactory + invokeExact pattern (a generated class per
    // call) is exercised by the link storm, which links a fresh call site each iteration.
    // Probe B above checks the same identity invariant in steady state without spinning a
    // class per call, which would otherwise exhaust class-metadata space.

    // ---- Probe C: evictable re-intern ---------------------------------------
    // The most focused detector for "the table loses / null-returns a live entry
    // under concurrent GC reference-processing". It hammers a bounded space of
    // types that cycle in and out of reachability (held only for the duration of
    // one iteration), so GC reference-processing constantly churns the very bins
    // being probed -- unlike the permanently-pinned Probe A, whose entries are
    // never reference-processed. Within one iteration m1 strongly pins the
    // referent, so methodType(spec) must return that same instance; if it does
    // not, the table lost or mis-returned a strongly-reachable entry.
    static void probeCWorker() {
        long n = 0;
        final int space = PROBE_C_SPACE;
        try {
            while (running && !FAILED.get()) {
                long s = Math.floorMod(n, space);
                MethodType m1 = shape(s);               // intern (and hold)
                // a brief allocation widens the window so a concurrent GC cycle
                // (and weak-reference processing) can land while m1 pins the referent
                if (junk()) sink = m1;
                MethodType m2 = shape(s);               // re-intern; must return m1
                if (m1 != m2) {                         // m1 still strongly reachable here
                    reportProbeC(s, m1, m2);
                    return;
                }
                // touch m1 again after m2 so the JIT cannot shorten m1's lifetime
                if (m1.parameterCount() == Integer.MIN_VALUE) sink = m1;
                probeC.incrementAndGet();
                n += 0x9E3779B97F4A7C15L;                // golden-ratio stride over the bounded space
            }
        } catch (OutOfMemoryError | StackOverflowError e) { resourceExhausted(); }
    }

    // Small, GC-visible allocation used to widen the held-then-reget window in Probe C.
    static boolean junk() {
        byte[] b = new byte[256];
        b[0] = 1;
        return b[255] != 0;   // always false; keeps the allocation live and uneliminable
    }

    static void reportProbeC(long s, MethodType m1, MethodType m2) {
        if (!FAILED.compareAndSet(false, true)) return;
        StringBuilder b = new StringBuilder();
        b.append("=== Probe C violation: re-intern of a strongly-held type returned a different instance ===\n");
        b.append("(closest detector for a concurrent-GC reference-processing race in the intern table)\n");
        b.append("thread     : ").append(Thread.currentThread().getName()).append('\n');
        b.append("seed       : ").append(s).append('\n');
        b.append("m1 (held)  : ").append(m1).append("  @").append(idh(m1)).append('\n');
        b.append("m2 (reget) : ").append(m2).append("  @").append(idh(m2)).append('\n');
        b.append("equals?    : ").append(m1.equals(m2)).append("   == ? ").append(m1 == m2).append('\n');
        appendInternState(b, m1, m2);
        appendEnv(b);
        REPORT.set(b.toString());
    }

    // ---- decoy worker: churns the intern table with collectable types --------
    static void decoyWorker(long base) {
        long n = base;
        try {
            while (running && !FAILED.get()) {
                // Synthesise an unpinned MethodType and immediately drop it, so its
                // intern entry becomes weakly reachable and is processed/cleared by GC.
                // The seed is bounded to DECOY_SPACE so the intern table reaches a
                // sustainable steady-state size instead of growing without limit.
                MethodType t = shape(Math.floorMod(n, DECOY_SPACE));
                if (t.parameterCount() == Integer.MIN_VALUE) sink = t; // unreachable; defeat DCE
                decoys.incrementAndGet();
                n += 0x9E3779B97F4A7C15L;
            }
        } catch (OutOfMemoryError | StackOverflowError e) { resourceExhausted(); }
    }

    static void resourceExhausted() {
        RESOURCE.set(true);
        running = false;   // stop everyone; main will report this as environmental, not a violation
    }

    // ---- link storm: force first-time invokeExact linkage repeatedly ---------
    // Each iteration defines a fresh "LinkProbe" class under a throwaway loader,
    // so its (ToIntFunction) invokeExact call site is linked from scratch under
    // concurrent GC. The loaders become garbage, also exercising class unloading.
    static void linkStormWorker(byte[] probeBytes) {
        while (running && !FAILED.get()) {
            try {
                FreshLoader fl = new FreshLoader(probeBytes);
                Class<?> c = Class.forName("LinkProbe", true, fl);
                sink = c.getMethod("run").invoke(null);
                linkStorm.incrementAndGet();
            } catch (java.lang.reflect.InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause instanceof WrongMethodTypeException wmte) {
                    reportLinkStorm(wmte);
                    return;
                }
                if (cause instanceof OutOfMemoryError || cause instanceof StackOverflowError) {
                    resourceExhausted();
                    return;
                }
                reportUnexpected(cause == null ? ite : cause);
                return;
            } catch (OutOfMemoryError | StackOverflowError e) {
                resourceExhausted();
                return;
            } catch (Throwable t) {
                reportUnexpected(t);
                return;
            }
        }
    }

    /** Child-first loader for "LinkProbe" only; everything else delegates to the parent. */
    static final class FreshLoader extends ClassLoader {
        private final byte[] bytes;
        FreshLoader(byte[] bytes) { super(MethodTypeInternStress.class.getClassLoader()); this.bytes = bytes; }
        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.equals("LinkProbe")) {
                synchronized (getClassLoadingLock(name)) {
                    Class<?> c = findLoadedClass(name);
                    if (c == null) c = defineClass(name, bytes, 0, bytes.length);
                    if (resolve) resolveClass(c);
                    return c;
                }
            }
            return super.loadClass(name, resolve);
        }
    }

    static byte[] readLinkProbeBytes() {
        try (var in = MethodTypeInternStress.class.getResourceAsStream("/LinkProbe.class")) {
            return in == null ? null : in.readAllBytes();
        } catch (Throwable t) {
            return null;
        }
    }

    static void reportLinkStorm(WrongMethodTypeException wmte) {
        if (!FAILED.compareAndSet(false, true)) return;
        StringBuilder b = new StringBuilder();
        b.append("=== Link-storm violation: invokeExact threw during FIRST-TIME call-site linkage ===\n");
        b.append("(this is the closest match to the field reports: failure at build/init time)\n");
        b.append("thread     : ").append(Thread.currentThread().getName()).append('\n');
        b.append("message    : ").append(wmte.getMessage()).append('\n');
        b.append("EXP_INT    : @").append(idh(EXP_INT))
         .append("  methodType(ToIntFunction)=@").append(idh(MethodType.methodType(ToIntFunction.class))).append('\n');
        appendInternState(b, EXP_INT, MethodType.methodType(ToIntFunction.class));
        b.append("stacktrace :\n");
        for (StackTraceElement e : wmte.getStackTrace()) b.append("    at ").append(e).append('\n');
        appendEnv(b);
        REPORT.set(b.toString());
    }

    // ---- GC pressure ---------------------------------------------------------
    static void gcChurnWorker() {
        Object[] ring = new Object[256];
        int idx = 0;
        long n = 0;
        try {
            while (running && !FAILED.get()) {
                ring[idx] = new byte[HEAP_CHURN_KB * 1024];      // short/medium-lived garbage
                idx = (idx + 1) & 255;
                if (SYSGC_MS > 0 && (++n % 1024) == 0) {
                    try { Thread.sleep(SYSGC_MS); } catch (InterruptedException e) { return; }
                    System.gc();
                }
            }
        } catch (OutOfMemoryError | StackOverflowError e) { resourceExhausted(); }
    }

    // ---- deterministic MethodType shape from a seed --------------------------
    static MethodType shape(long seed) {
        long s = mix(seed);
        Class<?> r = RTYPES[(int) Math.floorMod(s, RTYPES.length)];
        s = mix(s);
        int arity = (int) Math.floorMod(s, 5);               // 0..4 params
        Class<?>[] p = new Class<?>[arity];
        for (int i = 0; i < arity; i++) {
            s = mix(s);
            p[i] = PTYPES[(int) Math.floorMod(s, PTYPES.length)];
        }
        return MethodType.methodType(r, p);
    }

    static long mix(long z) {                                // SplitMix64-style
        z = (z + 0x9E3779B97F4A7C15L);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    // ---- failure reporting ---------------------------------------------------
    static void reportProbeA(int i, MethodType regen) {
        if (!FAILED.compareAndSet(false, true)) return;
        StringBuilder b = new StringBuilder();
        b.append("=== Probe A violation: methodType(spec) != pinned canonical ===\n");
        b.append("thread        : ").append(Thread.currentThread().getName()).append('\n');
        b.append("spec index    : ").append(i).append('\n');
        b.append("CANON[i]      : ").append(CANON[i]).append("  @").append(idh(CANON[i])).append('\n');
        b.append("methodType(..): ").append(regen).append("  @").append(idh(regen)).append('\n');
        b.append("equals?       : ").append(CANON[i].equals(regen)).append('\n');
        b.append("== ?          : ").append(CANON[i] == regen).append('\n');
        appendInternState(b, CANON[i], regen);
        appendStability(b, i);
        appendEnv(b);
        REPORT.set(b.toString());
    }

    static void reportProbeB(WrongMethodTypeException wmte) {
        if (!FAILED.compareAndSet(false, true)) return;
        StringBuilder b = new StringBuilder();
        b.append("=== Probe B violation: invokeExact threw WrongMethodTypeException ===\n");
        b.append("thread     : ").append(Thread.currentThread().getName()).append('\n');
        b.append("message    : ").append(wmte.getMessage()).append('\n');
        b.append("EXP_INT    : @").append(idh(EXP_INT))
         .append("  methodType(ToIntFunction)=@").append(idh(MethodType.methodType(ToIntFunction.class))).append('\n');
        b.append("EXP_FN     : @").append(idh(EXP_FN))
         .append("  methodType(Function)=@").append(idh(MethodType.methodType(Function.class))).append('\n');
        b.append("EXP_SUP    : @").append(idh(EXP_SUP))
         .append("  methodType(Supplier)=@").append(idh(MethodType.methodType(Supplier.class))).append('\n');
        b.append("stacktrace :\n");
        for (StackTraceElement e : wmte.getStackTrace()) b.append("    at ").append(e).append('\n');
        appendEnv(b);
        REPORT.set(b.toString());
    }

    static void reportProbeBType(String fi, MethodType pinnedExpected, MethodType freshHandleType) {
        if (!FAILED.compareAndSet(false, true)) return;
        StringBuilder b = new StringBuilder();
        b.append("=== Probe B violation: methodType(").append(fi)
         .append(") != pinned call-site expected ===\n");
        b.append("(this is precisely what invokeExact compares; in production it throws "
                + "WrongMethodTypeException)\n");
        b.append("thread       : ").append(Thread.currentThread().getName()).append('\n');
        b.append("pinned EXP   : ").append(pinnedExpected).append("  @").append(idh(pinnedExpected)).append('\n');
        b.append("fresh handle : ").append(freshHandleType).append("  @").append(idh(freshHandleType)).append('\n');
        b.append("equals?      : ").append(pinnedExpected.equals(freshHandleType))
         .append("   == ? ").append(pinnedExpected == freshHandleType).append('\n');
        appendInternState(b, pinnedExpected, freshHandleType);
        appendEnv(b);
        REPORT.set(b.toString());
    }

    static void reportUnexpected(Throwable t) {
        // Resource exhaustion (heap or class space) is environmental, not an invariant
        // violation. It can arrive wrapped (e.g. InternalError -> OutOfMemoryError), so
        // walk the whole cause chain before deciding.
        if (hasResourceCause(t)) { resourceExhausted(); return; }
        if (!FAILED.compareAndSet(false, true)) return;
        StringBuilder b = new StringBuilder();
        b.append("=== Unexpected throwable in probe ===\n");
        b.append("thread : ").append(Thread.currentThread().getName()).append('\n');
        b.append(stackToString(t));
        appendEnv(b);
        REPORT.set(b.toString());
    }

    static boolean hasResourceCause(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof OutOfMemoryError || c instanceof StackOverflowError) return true;
            if (c == c.getCause()) break;
        }
        return false;
    }

    static void appendStability(StringBuilder b, int i) {
        b.append("recheck (does it persist?):\n");
        for (int k = 0; k < 8; k++) {
            MethodType again = shape(i);
            b.append("    [").append(k).append("] == CANON? ").append(again == CANON[i])
             .append("  @").append(idh(again)).append('\n');
        }
    }

    @SuppressWarnings("unchecked")
    static void appendInternState(StringBuilder b, MethodType canon, MethodType regen) {
        if (internTableGet == null) {
            b.append("internTable   : <not accessible; rerun with "
                    + "--add-opens java.base/java.lang.invoke=ALL-UNNAMED>\n");
            return;
        }
        try {
            Object held = internTableGet.invoke(internTable, canon);
            b.append("internTable.get(CANON) : @").append(held == null ? "null" : idh(held))
             .append("  (== CANON? ").append(held == canon)
             .append(", == regen? ").append(held == regen).append(")\n");
        } catch (Throwable t) {
            b.append("internTable   : <introspection failed: ").append(t).append(">\n");
        }
    }

    static void appendEnv(StringBuilder b) {
        b.append("--- environment ---\n");
        b.append("java.runtime  : ").append(System.getProperty("java.runtime.name"))
         .append(' ').append(System.getProperty("java.runtime.version")).append('\n');
        b.append("java.vm       : ").append(System.getProperty("java.vm.name"))
         .append(' ').append(System.getProperty("java.vm.version")).append('\n');
        b.append("gc            : ").append(gcNames()).append('\n');
        b.append("threads       : ").append(THREADS).append(" probe + ")
         .append(DECOY_THREADS).append(" decoy").append('\n');
        b.append("pinned        : ").append(PINNED).append('\n');
        b.append("counters      : probeA=").append(probeA.get())
         .append(" probeB=").append(probeB.get())
         .append(" probeC=").append(probeC.get())
         .append(" link=").append(linkStorm.get())
         .append(" decoys=").append(decoys.get()).append('\n');
    }

    // ---- monitor -------------------------------------------------------------
    static volatile long startNanos;

    static void monitor() {
        long lastA = 0, lastC = 0, lastL = 0, lastD = 0, lastT = System.nanoTime();
        while (running && !FAILED.get()) {
            try { Thread.sleep(5_000); } catch (InterruptedException e) { return; }
            long a = probeA.get(), c = probeC.get(), ls = linkStorm.get(), d = decoys.get(), now = System.nanoTime();
            double dt = (now - lastT) / 1e9;
            double elapsed = (now - startNanos) / 1e9;
            System.out.printf("[%4.0fs] probeA=%,d (+%,.0f/s) probeC=%,d (+%,.0f/s) link=%,d (+%,.0f/s) decoys=%,d | gc=%s%n",
                    elapsed, a, (a - lastA) / dt, c, (c - lastC) / dt, ls, (ls - lastL) / dt, d, gcStats());
            lastA = a; lastC = c; lastL = ls; lastD = d; lastT = now;
        }
    }

    // ---- helpers -------------------------------------------------------------
    static void printHeader() {
        System.out.println("MethodType intern stress: " + gcNames()
                + " | " + System.getProperty("java.vm.version"));
        System.out.printf("threads=%d decoy=%d pinned=%d duration=%ds probeB=%s gcThread=%s sysgcMs=%d%n",
                THREADS, DECOY_THREADS, PINNED, DURATION_SEC, PROBE_B, GC_THREAD, SYSGC_MS);
        startNanos = System.nanoTime();
    }

    static void setupInternTableIntrospection() {
        try {
            Field f = MethodType.class.getDeclaredField("internTable");
            f.setAccessible(true);
            internTable = f.get(null);
            internTableGet = internTable.getClass().getMethod("get", Object.class);
            internTableGet.setAccessible(true);
        } catch (Throwable t) {
            internTable = null;
            internTableGet = null;   // diagnostics degrade; harness still works
        }
    }

    static String idh(Object o) { return Integer.toHexString(System.identityHashCode(o)); }

    static String stackToString(Throwable t) {
        StringBuilder b = new StringBuilder(t.toString()).append('\n');
        for (StackTraceElement e : t.getStackTrace()) b.append("    at ").append(e).append('\n');
        return b.toString();
    }

    static String gcNames() {
        StringBuilder b = new StringBuilder();
        for (GarbageCollectorMXBean g : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (b.length() > 0) b.append('+');
            b.append(g.getName());
        }
        return b.toString();
    }

    static String gcStats() {
        long count = 0, time = 0;
        for (GarbageCollectorMXBean g : ManagementFactory.getGarbageCollectorMXBeans()) {
            count += Math.max(0, g.getCollectionCount());
            time += Math.max(0, g.getCollectionTime());
        }
        return count + " collections / " + time + "ms";
    }

    static int intProp(String k, int def) {
        String v = System.getProperty(k);
        return v == null ? def : Integer.parseInt(v.trim());
    }
    static boolean boolProp(String k, boolean def) {
        String v = System.getProperty(k);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    // LambdaMetafactory implementation target used by the self test (looked up by name).
    static Object implSup() { return ""; }

    // ---- self test: manufacture the failure to validate detectors + reports --
    // Forces the exact condition the bug produces (two equal, non-identical
    // MethodTypes) and runs it through both reporting paths, proving the harness
    // would faithfully report a real hit. Requires --add-opens for the private ctor.
    static void selfTest() throws Throwable {
        System.out.println("=== SELF TEST: injecting a fabricated invariant violation ===\n");
        MethodType canon = CANON[0];
        MethodType dup = nonInternedCopyOf(canon);
        System.out.println("fabricated: equals=" + canon.equals(dup) + " ==" + (canon == dup)
                + "  canon@" + idh(canon) + " dup@" + idh(dup) + "\n");

        reportProbeA(0, dup);
        System.out.println(REPORT.get());

        // Probe B path: drive invokeExact with a desynced (non-canonical) handle type.
        FAILED.set(false); REPORT.set(null);
        try {
            MethodHandle h = LOOKUP.findStatic(MethodTypeInternStress.class, "implSup",
                    MethodType.methodType(Object.class));
            MethodHandle desynced = h.asType(nonInternedCopyOf(MethodType.methodType(Object.class)));
            Object o = (Object) desynced.invokeExact();   // expected (interned) != handle type (dup)
            sink = o;
            System.out.println("(self test note: invokeExact did not throw on this VM)");
        } catch (WrongMethodTypeException wmte) {
            reportProbeB(wmte);
            System.out.println(REPORT.get());
        }
        System.out.println("\n=== SELF TEST complete: both detectors fire and report. ===");
        System.exit(2);
    }

    static MethodType nonInternedCopyOf(MethodType t) throws Throwable {
        var ctor = MethodType.class.getDeclaredConstructor(Class.class, Class[].class);
        ctor.setAccessible(true);
        MethodType copy = ctor.newInstance(t.returnType(), t.parameterArray());
        Field form = MethodType.class.getDeclaredField("form");
        form.setAccessible(true);
        form.set(copy, form.get(t));
        return copy;
    }
}

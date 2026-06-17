import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * JIT-focused stress test for {@link MethodHandle#invokeExact}'s identity check.
 *
 * The intern-table stress harness ({@code MethodTypeInternStress}) proves the failure is
 * not in the Java intern logic. The remaining VM-level hypothesis a Java test can target
 * is a C2 miscompile at a hot {@code invokeExact} call site: {@code Invokers.checkExactType}
 * is {@code @ForceInline} and the call site's {@code expected} type and the handle's
 * {@code type()} field are {@code @Stable}. A scheduling/folding bug could make the
 * identity compare {@code targetType != expected} spuriously true and throw
 * {@code WrongMethodTypeException} even though the types are the canonical, interned one.
 *
 * Design choices that make this likely to surface such a bug:
 *  - the handle for each call site is held in a NON-constant ({@code volatile}) field, so
 *    C2 must emit the real {@code checkExactType} comparison instead of folding it away;
 *  - several distinct call sites are hammered so multiple compiled invokers exist;
 *  - the handle is periodically rebuilt (a fresh LambdaMetafactory target of the same,
 *    canonical type) to force recompilation churn and re-exercise the type identity;
 *  - intended to run with C2 stress flags and the -Xint control (see RUN below).
 *
 * RUN (compare the two; a JIT bug fails under C2 but never under -Xint):
 *   java -XX:+UnlockDiagnosticVMOptions -XX:+StressLCM -XX:+StressGCM -XX:+StressIGVN \
 *        -XX:+DeoptimizeALot JitInvokeExactStress
 *   java -Xint JitInvokeExactStress       # control: must always pass
 *
 * Exit: 0 = held, 42 = invokeExact identity violation, 3 = resource exhaustion.
 */
public final class JitInvokeExactStress {
    static final int THREADS      = intProp("jit.threads", Runtime.getRuntime().availableProcessors());
    static final int DURATION_SEC = intProp("jit.durationSec", 60);
    static final long REBUILD_MASK = (1L << intProp("jit.rebuildShift", 18)) - 1;  // rebuild handle every 2^n iters

    static final MethodHandles.Lookup L = MethodHandles.lookup();

    // Pinned call-site 'expected' types (== the casts in the call sites below).
    static final MethodType T_INT  = MethodType.methodType(ToIntFunction.class);
    static final MethodType T_FN   = MethodType.methodType(Function.class);
    static final MethodType T_SUP  = MethodType.methodType(Supplier.class);
    static final MethodType T_PRED = MethodType.methodType(Predicate.class);

    // NON-constant handle holders: forces C2 to emit the actual identity check.
    static volatile MethodHandle hInt, hFn, hSup, hPred;

    static final AtomicLong calls = new AtomicLong();
    static final AtomicBoolean FAILED = new AtomicBoolean(false);
    static final AtomicBoolean RESOURCE = new AtomicBoolean(false);
    static final AtomicReference<String> REPORT = new AtomicReference<>();
    static volatile boolean running = true;
    static volatile Object sink;
    static volatile long startNanos;

    public static void main(String[] args) throws Throwable {
        hInt  = buildInt();
        hFn   = buildFn();
        hSup  = buildSup();
        hPred = buildPred();

        System.out.printf("JIT invokeExact stress: %s | %s%nthreads=%d duration=%ds rebuildEvery=%d%n",
                System.getProperty("java.vm.name"), System.getProperty("java.vm.version"),
                THREADS, DURATION_SEC, REBUILD_MASK + 1);
        startNanos = System.nanoTime();

        Thread[] ts = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final long base = ((long) i) << 32;
            ts[i] = new Thread(() -> worker(base), "jit-" + i);
            ts[i].start();
        }
        Thread mon = new Thread(JitInvokeExactStress::monitor, "monitor");
        mon.setDaemon(true);
        mon.start();

        long deadline = System.nanoTime() + DURATION_SEC * 1_000_000_000L;
        while (running && !FAILED.get() && !RESOURCE.get() && System.nanoTime() < deadline) {
            Thread.sleep(50);
        }
        running = false;
        for (Thread t : ts) t.join(5_000);

        if (FAILED.get()) {
            System.out.println(REPORT.get());
            System.out.println("\n*** invokeExact IDENTITY VIOLATION — see report above ***");
            System.exit(42);
        }
        if (RESOURCE.get()) {
            System.out.printf("%nRESOURCE EXHAUSTION (not a violation) after %.0fs%n",
                    (System.nanoTime() - startNanos) / 1e9);
            System.exit(3);
        }
        System.out.printf("%nNo violation after %ds (%,d invokeExact calls). Held.%n",
                DURATION_SEC, calls.get());
        System.exit(0);
    }

    static void worker(long base) {
        long n = base;
        try {
            while (running && !FAILED.get()) {
                // Each is (FI) <volatile handle>.invokeExact(): C2 emits checkExactType.
                ToIntFunction<Object> a = (ToIntFunction<Object>) hInt.invokeExact();
                Function<Object, Object> b = (Function<Object, Object>) hFn.invokeExact();
                Supplier<Object> c = (Supplier<Object>) hSup.invokeExact();
                Predicate<Object> d = (Predicate<Object>) hPred.invokeExact();
                // use the results so nothing is optimized away
                if (a.applyAsInt(b) == Integer.MIN_VALUE && d.test(c.get())) sink = a;
                calls.incrementAndGet();

                if ((n & REBUILD_MASK) == 0) {     // recompilation churn + fresh-type re-check
                    hInt = buildInt(); hFn = buildFn(); hSup = buildSup(); hPred = buildPred();
                }
                n++;
            }
        } catch (WrongMethodTypeException wmte) {
            reportWmte(wmte);
        } catch (OutOfMemoryError | StackOverflowError e) {
            RESOURCE.set(true); running = false;
        } catch (Throwable t) {
            if (hasResourceCause(t)) { RESOURCE.set(true); running = false; return; }
            reportThrowable(t);
        }
    }

    static void reportWmte(WrongMethodTypeException wmte) {
        if (!FAILED.compareAndSet(false, true)) return;
        StringBuilder b = new StringBuilder();
        b.append("=== invokeExact threw WrongMethodTypeException (handle type != pinned expected) ===\n");
        b.append("thread  : ").append(Thread.currentThread().getName()).append('\n');
        b.append("message : ").append(wmte.getMessage()).append('\n');
        b.append("expected types pinned: ToIntFunction@").append(idh(T_INT))
         .append(" Function@").append(idh(T_FN))
         .append(" Supplier@").append(idh(T_SUP))
         .append(" Predicate@").append(idh(T_PRED)).append('\n');
        b.append("current handle types : hInt=").append(typeId(hInt))
         .append(" hFn=").append(typeId(hFn)).append(" hSup=").append(typeId(hSup))
         .append(" hPred=").append(typeId(hPred)).append('\n');
        b.append("vm      : ").append(System.getProperty("java.vm.name")).append(' ')
         .append(System.getProperty("java.vm.version")).append('\n');
        for (StackTraceElement e : wmte.getStackTrace()) b.append("    at ").append(e).append('\n');
        REPORT.set(b.toString());
    }

    static void reportThrowable(Throwable t) {
        if (!FAILED.compareAndSet(false, true)) return;
        StringBuilder b = new StringBuilder("=== Unexpected throwable ===\n");
        b.append(t).append('\n');
        for (StackTraceElement e : t.getStackTrace()) b.append("    at ").append(e).append('\n');
        REPORT.set(b.toString());
    }

    static void monitor() {
        long last = 0, lastT = System.nanoTime();
        while (running && !FAILED.get()) {
            try { Thread.sleep(5_000); } catch (InterruptedException e) { return; }
            long c = calls.get(), now = System.nanoTime();
            System.out.printf("[%4.0fs] invokeExact=%,d (+%,.0f/s)%n",
                    (now - startNanos) / 1e9, c, (c - last) / ((now - lastT) / 1e9));
            last = c; lastT = now;
        }
    }

    // ---- call-site builders (LambdaMetafactory targets, type ()FI) -----------
    static MethodHandle buildInt() throws Throwable {
        return LambdaMetafactory.metafactory(L, "applyAsInt", T_INT,
                MethodType.methodType(int.class, Object.class),
                L.findStatic(JitInvokeExactStress.class, "iImpl", MethodType.methodType(int.class, Object.class)),
                MethodType.methodType(int.class, Object.class)).getTarget();
    }
    static MethodHandle buildFn() throws Throwable {
        return LambdaMetafactory.metafactory(L, "apply", T_FN,
                MethodType.methodType(Object.class, Object.class),
                L.findStatic(JitInvokeExactStress.class, "fImpl", MethodType.methodType(Object.class, Object.class)),
                MethodType.methodType(Object.class, Object.class)).getTarget();
    }
    static MethodHandle buildSup() throws Throwable {
        return LambdaMetafactory.metafactory(L, "get", T_SUP,
                MethodType.methodType(Object.class),
                L.findStatic(JitInvokeExactStress.class, "sImpl", MethodType.methodType(Object.class)),
                MethodType.methodType(Object.class)).getTarget();
    }
    static MethodHandle buildPred() throws Throwable {
        return LambdaMetafactory.metafactory(L, "test", T_PRED,
                MethodType.methodType(boolean.class, Object.class),
                L.findStatic(JitInvokeExactStress.class, "pImpl", MethodType.methodType(boolean.class, Object.class)),
                MethodType.methodType(boolean.class, Object.class)).getTarget();
    }
    static int iImpl(Object o)      { return o == null ? 0 : 1; }
    static Object fImpl(Object o)   { return o; }
    static Object sImpl()           { return ""; }
    static boolean pImpl(Object o)  { return o != null; }

    static boolean hasResourceCause(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof OutOfMemoryError || c instanceof StackOverflowError) return true;
            if (c == c.getCause()) break;
        }
        return false;
    }
    static String idh(Object o) { return Integer.toHexString(System.identityHashCode(o)); }
    static String typeId(MethodHandle h) { return h.type() + "@" + idh(h.type()); }
    static int intProp(String k, int d) { String v = System.getProperty(k); return v == null ? d : Integer.parseInt(v.trim()); }
}

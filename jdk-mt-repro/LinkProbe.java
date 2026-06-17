import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.ToIntFunction;

/**
 * A self-contained copy of the Blackbird/Caffeine pattern. The harness loads
 * fresh instances of this class under throwaway classloaders so that the
 * {@code (ToIntFunction) ... invokeExact()} call site is linked for the FIRST
 * TIME on every invocation — the moment the field reports point at (serializer /
 * cache build). First-time linkage resolves the call-site 'expected' MethodType
 * via the VM, concurrently with the Java-side {@code methodType(ToIntFunction.class)},
 * which is exactly when a non-canonical instance would surface.
 */
public final class LinkProbe {
    public static int run() throws Throwable {
        MethodHandles.Lookup l = MethodHandles.lookup();
        MethodHandle impl = l.findStatic(LinkProbe.class, "impl",
                MethodType.methodType(int.class, Object.class));
        @SuppressWarnings("unchecked")
        ToIntFunction<Object> f = (ToIntFunction<Object>) LambdaMetafactory.metafactory(
                l, "applyAsInt",
                MethodType.methodType(ToIntFunction.class),
                MethodType.methodType(int.class, Object.class),
                impl, impl.type())
            .getTarget().invokeExact();          // first-time-linked invokeExact site
        return f.applyAsInt("x");
    }

    static int impl(Object o) { return o == null ? 0 : o.hashCode(); }
}

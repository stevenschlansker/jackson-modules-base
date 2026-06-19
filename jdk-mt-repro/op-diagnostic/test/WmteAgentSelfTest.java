import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.ToIntFunction;

/**
 * Validation harness for the wmtediag agent. It fabricates the exact invariant violation the
 * bug produces — two {@code MethodType} instances that are {@code .equals()} but not {@code ==},
 * sharing the same internal {@code form} object — and drives {@code invokeExact} so the VM throws
 * {@code WrongMethodTypeException} with an identical "X but found X" message.
 *
 * <p>This mirrors {@code MethodTypeInternStress.nonInternedCopyOf} / {@code selfTest}. Run it under
 * the agent and confirm the dump file is written with the intern-table identity, both type
 * identities, thread, and stack.
 *
 * <p>Requires {@code --add-opens java.base/java.lang.invoke=ALL-UNNAMED}.
 */
public final class WmteAgentSelfTest {

    static int target() {
        return 0;
    }

    public static void main(String[] args) throws Throwable {
        if (args.length > 0 && "ordinary".equals(args[0])) {
            // No reflection into MethodType internals -> runs WITHOUT --add-opens.
            // Used to confirm the agent still dumps (with the intern-table section degraded)
            // when the opens are absent. The types differ for real, so it is not "X but found X".
            ordinaryWmte();
            return;
        }

        System.out.println("=== wmtediag agent self-test: fabricating the WMTE ===");

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle h = lookup.findStatic(WmteAgentSelfTest.class, "target",
                MethodType.methodType(int.class));

        // Build a non-interned duplicate of the handle's type and retype the handle to it, so the
        // call-site 'expected' type (interned) differs by IDENTITY from the handle's type.
        MethodType canonical = MethodType.methodType(int.class);
        MethodType duplicate = nonInternedCopyOf(canonical);

        System.out.println("canonical @" + Integer.toHexString(System.identityHashCode(canonical))
                + "  duplicate @" + Integer.toHexString(System.identityHashCode(duplicate))
                + "  equals=" + canonical.equals(duplicate)
                + "  ==" + (canonical == duplicate));

        MethodHandle desynced = h.asType(duplicate);

        boolean threw = false;
        try {
            int r = (int) desynced.invokeExact();   // handle's type (duplicate) != call-site expected (canonical)
            System.out.println("NOTE: invokeExact did not throw on this VM (got " + r + ")");
        } catch (WrongMethodTypeException wmte) {
            threw = true;
            System.out.println("caught (expected): " + wmte);
        }

        if (!threw) {
            // Fallback: drive the same construction path the JDK uses, directly, so the agent's
            // instrumentation point still fires and we validate the dump on any VM.
            System.out.println("driving Invokers.newWrongMethodTypeException directly as a fallback");
            forceNewWrongMethodTypeException(MethodType.methodType(ToIntFunction.class),
                    nonInternedCopyOf(MethodType.methodType(ToIntFunction.class)));
        }

        System.out.println("=== self-test done; check the dump file printed above ===");
    }

    static void ordinaryWmte() throws Throwable {
        System.out.println("=== wmtediag agent self-test: ordinary WMTE (no opens needed) ===");
        MethodHandle h = MethodHandles.lookup().findStatic(WmteAgentSelfTest.class, "target",
                MethodType.methodType(int.class));
        try {
            int r = (int) h.invokeExact((Object) null);   // wrong exact type on purpose
            System.out.println("NOTE: did not throw (got " + r + ")");
        } catch (WrongMethodTypeException wmte) {
            System.out.println("caught (expected): " + wmte);
        }
        System.out.println("=== done; agent should have dumped with intern-table marked unavailable ===");
    }

    /** Same technique as MethodTypeInternStress.nonInternedCopyOf. */
    static MethodType nonInternedCopyOf(MethodType t) throws Throwable {
        Constructor<MethodType> ctor =
                MethodType.class.getDeclaredConstructor(Class.class, Class[].class);
        ctor.setAccessible(true);
        MethodType copy = ctor.newInstance(t.returnType(), t.parameterArray());
        Field form = MethodType.class.getDeclaredField("form");
        form.setAccessible(true);
        form.set(copy, form.get(t));     // share the canonical form -> equals() true, == false
        return copy;
    }

    static void forceNewWrongMethodTypeException(MethodType a0, MethodType a1) throws Throwable {
        Class<?> invokers = Class.forName("java.lang.invoke.Invokers");
        var m = invokers.getDeclaredMethod("newWrongMethodTypeException",
                MethodType.class, MethodType.class);
        m.setAccessible(true);
        Object ex = m.invoke(null, a0, a1);
        System.out.println("constructed: " + ex);
    }
}

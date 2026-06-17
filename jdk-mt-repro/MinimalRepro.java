import java.lang.invoke.*;
import java.lang.reflect.*;

public class Repro {
    static String make() { return "hi"; }

    public static void main(String[] args) throws Throwable {
        MethodHandles.Lookup l = MethodHandles.lookup();
        MethodHandle h = l.findStatic(Repro.class, "make", MethodType.methodType(String.class));

        MethodType t1 = MethodType.methodType(String.class);        // interned
        // Build a non-interned duplicate via the private ctor:
        Constructor<MethodType> ctor =
            MethodType.class.getDeclaredConstructor(Class.class, Class[].class);
        ctor.setAccessible(true);
        MethodType t2 = ctor.newInstance(String.class, new Class<?>[0]);
        // copy the erased form so the handle machinery is happy
        Field form = MethodType.class.getDeclaredField("form");
        form.setAccessible(true);
        form.set(t2, form.get(t1));

        System.out.println("t1 == t2 ? " + (t1 == t2));
        System.out.println("t1.equals(t2) ? " + t1.equals(t2));
        System.out.println("t1: " + t1 + "   t2: " + t2);

        MethodHandle h2 = h.asType(t2);
        System.out.println("h2.type() == t2 ? " + (h2.type() == t2));
        System.out.println("h2.type() == t1 ? " + (h2.type() == t1));

        // The cast (String) makes the call-site 'expected' type = interned methodType(String)
        System.out.println("\n--- invokeExact (identity check) ---");
        try {
            String s = (String) h2.invokeExact();
            System.out.println("invokeExact OK: " + s);
        } catch (WrongMethodTypeException e) {
            System.out.println("WrongMethodTypeException: " + e.getMessage());
        }

        System.out.println("\n--- invoke (asType / equals path) ---");
        String s2 = (String) h2.invoke();
        System.out.println("invoke OK: " + s2);
    }
}

/**
 * A set of distinct bean classes with getters covering every accessor branch in
 * Blackbird's BBSerializerModifier.createProperty: int (ToIntFunction), long
 * (ToLongFunction), boolean (ToBooleanFunction), String (Function<.,String>), and
 * Object (Function). Distinct classes force distinct first-time property-accessor
 * linkage, which is where jackson-modules-base#142 failed.
 */
public final class Beans {
    public static final int COUNT = 8;

    public static Object make(int k) {
        switch (Math.floorMod(k, COUNT)) {
            case 0:  return new A(k);
            case 1:  return new B(k);
            case 2:  return new C(k);
            case 3:  return new D(k);
            case 4:  return new E(k);
            case 5:  return new F(k);
            case 6:  return new G(k);
            default: return new H(k);
        }
    }

    public static final class A { private final int v; A(int v){this.v=v;} public int getV(){return v;} public String getName(){return "a"+v;} }
    public static final class B { private final long v; B(long v){this.v=v;} public long getV(){return v;} public boolean isFlag(){return v%2==0;} }
    public static final class C { private final String s; C(int v){this.s="c"+v;} public String getS(){return s;} public int getLen(){return s.length();} }
    public static final class D { private final Object o; D(int v){this.o=Integer.valueOf(v);} public Object getO(){return o;} public boolean isNull(){return o==null;} }
    public static final class E { private final int a; private final long b; private final boolean c; E(int v){a=v;b=v*2L;c=v>0;} public int getA(){return a;} public long getB(){return b;} public boolean isC(){return c;} }
    public static final class F { private final String x; private final String y; F(int v){x="x"+v;y="y"+v;} public String getX(){return x;} public String getY(){return y;} }
    public static final class G { private final int i; private final String s; private final Object o; G(int v){i=v;s="g"+v;o=s;} public int getI(){return i;} public String getS(){return s;} public Object getO(){return o;} }
    public static final class H { private final boolean b; private final double d; H(int v){b=v>0;d=v+0.5;} public boolean isB(){return b;} public double getD(){return d;} }
}

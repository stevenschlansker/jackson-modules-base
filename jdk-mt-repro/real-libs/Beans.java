import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A set of distinct bean classes with getters covering every accessor branch in
 * Blackbird's BBSerializerModifier.createProperty: int (ToIntFunction), long
 * (ToLongFunction), boolean (ToBooleanFunction), String (Function<.,String>), and
 * Object (Function). Distinct classes force distinct first-time property-accessor
 * linkage, which is where jackson-modules-base#142 failed during serialization.
 *
 * The bean classes below (A..H) are serialize-only: all-args constructors and no
 * setters, so Blackbird's CreatorOptimizer has nothing to optimize for them. To
 * cover the *deserialization* report from #142 (CreatorOptimizer.createOptimized ->
 * invokeExact, type (MethodHandle)Function) we add a parallel set of @JsonCreator
 * beans (DA..DH) below. Each has a distinct annotated constructor, so each forces a
 * distinct first-time creator linkage. We deliberately include a single-arg primitive
 * boolean creator (DBool, the (.)ToBooleanFunction shape) and several multi-arg
 * creators (the (MethodHandle)Function shape the field reporter hit).
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

    // ---- Deserialization beans: each has a distinct @JsonCreator constructor -----
    // These exercise Blackbird's CreatorOptimizer.createOptimized first-time linkage.
    // Property accessors (getters) are present so the round-trip is symmetric.
    public static final int DESER_COUNT = 8;

    // Distinct singleton JSON per deser type, filled once by serializing a sample
    // instance (see RealLibStress.warmupSanity / deser worker setup) so the hot loop
    // does not re-serialize on every iteration.
    public static final String[] DESER_JSON = new String[DESER_COUNT];

    public static Class<?> deserType(int k) {
        switch (Math.floorMod(k, DESER_COUNT)) {
            case 0:  return DA.class;
            case 1:  return DB.class;
            case 2:  return DC.class;
            case 3:  return DD.class;
            case 4:  return DE.class;
            case 5:  return DF.class;
            case 6:  return DG.class;
            default: return DBool.class;
        }
    }

    // A sample instance per deser type, used once to produce DESER_JSON.
    public static Object deserSample(int k) {
        int v = k + 1;
        switch (Math.floorMod(k, DESER_COUNT)) {
            case 0:  return new DA(v);
            case 1:  return new DB((long) v);
            case 2:  return new DC("c" + v);
            case 3:  return new DD(v, "d" + v);
            case 4:  return new DE(v, v * 2L, v > 0);
            case 5:  return new DF("x" + v, "y" + v);
            case 6:  return new DG(v, "g" + v, v > 0);
            default: return new DBool(v % 2 == 0);
        }
    }

    // Single int arg.
    public static final class DA {
        private final int v;
        @JsonCreator public DA(@JsonProperty("v") int v){this.v=v;}
        public int getV(){return v;}
    }
    // Single long arg.
    public static final class DB {
        private final long v;
        @JsonCreator public DB(@JsonProperty("v") long v){this.v=v;}
        public long getV(){return v;}
    }
    // Single String arg.
    public static final class DC {
        private final String s;
        @JsonCreator public DC(@JsonProperty("s") String s){this.s=s;}
        public String getS(){return s;}
    }
    // Two args (int + String): the multi-arg (MethodHandle)Function creator shape.
    public static final class DD {
        private final int i; private final String s;
        @JsonCreator public DD(@JsonProperty("i") int i, @JsonProperty("s") String s){this.i=i;this.s=s;}
        public int getI(){return i;} public String getS(){return s;}
    }
    // Three args mixing primitives and boolean.
    public static final class DE {
        private final int a; private final long b; private final boolean c;
        @JsonCreator public DE(@JsonProperty("a") int a, @JsonProperty("b") long b, @JsonProperty("c") boolean c){this.a=a;this.b=b;this.c=c;}
        public int getA(){return a;} public long getB(){return b;} public boolean isC(){return c;}
    }
    // Two String args.
    public static final class DF {
        private final String x; private final String y;
        @JsonCreator public DF(@JsonProperty("x") String x, @JsonProperty("y") String y){this.x=x;this.y=y;}
        public String getX(){return x;} public String getY(){return y;}
    }
    // Three args including boolean and Object.
    public static final class DG {
        private final int i; private final String s; private final boolean b;
        @JsonCreator public DG(@JsonProperty("i") int i, @JsonProperty("s") String s, @JsonProperty("b") boolean b){this.i=i;this.s=s;this.b=b;}
        public int getI(){return i;} public String getS(){return s;} public boolean isB(){return b;}
    }
    // Single primitive boolean arg: the (.)ToBooleanFunction creator shape.
    public static final class DBool {
        private final boolean flag;
        @JsonCreator public DBool(@JsonProperty("flag") boolean flag){this.flag=flag;}
        public boolean isFlag(){return flag;}
    }
}

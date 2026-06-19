package com.wholesail.wmtediag;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Java agent that captures decisive evidence for the rare
 * {@code WrongMethodTypeException} in FasterXML/jackson-modules-base#142.
 *
 * <p>It instruments {@code java.lang.invoke.Invokers.newWrongMethodTypeException(MethodType,
 * MethodType)} so that, at the instant that exception is constructed (and only then), it calls
 * {@link WmteDiagnostics#onWrongMethodType(java.lang.invoke.MethodType, java.lang.invoke.MethodType)}
 * with the two method types, before the exception propagates.
 *
 * <p>{@code newWrongMethodTypeException} runs only on the failure path, so the agent adds zero
 * overhead to successful {@code invokeExact} calls.
 *
 * <h2>Why retransform</h2>
 * {@code Invokers} is part of {@code java.base} and is loaded very early, almost certainly before
 * {@code premain} runs. A passive {@link ClassFileTransformer} (which only sees classes at load
 * time) would never see it. So the agent registers the transformer with {@code canRetransform=true}
 * and then explicitly calls {@link Instrumentation#retransformClasses} on {@code Invokers}.
 *
 * <h2>Why the diagnostics class is on the boot classpath</h2>
 * The injected call lives inside a {@code java.base} method, so the callee
 * ({@link WmteDiagnostics}) must be resolvable from the bootstrap loader. The agent calls
 * {@link Instrumentation#appendToBootstrapClassLoaderSearch} on its own jar before retransforming.
 */
public final class WmteAgent {

    private static final String INVOKERS_INTERNAL = "java/lang/invoke/Invokers";
    private static final String TARGET_METHOD = "newWrongMethodTypeException";
    private static final String DIAG_INTERNAL = "com/wholesail/wmtediag/WmteDiagnostics";
    private static final String DIAG_METHOD = "onWrongMethodType";
    // (MethodType, MethodType)V  -- the two args of newWrongMethodTypeException, returns void.
    private static final String DIAG_DESC =
            "(Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;)V";

    private WmteAgent() {}

    public static void premain(String args, Instrumentation inst) {
        run(args, inst, false);
    }

    /** Allow dynamic attach (-XX:+EnableDynamicAgentLoading) as well as -javaagent. */
    public static void agentmain(String args, Instrumentation inst) {
        run(args, inst, true);
    }

    private static void run(String args, Instrumentation inst, boolean dynamicAttach) {
        log("starting (" + (dynamicAttach ? "dynamic attach" : "premain")
                + "); output=" + WmteDiagnostics.resolveOutputForLog());

        // Make WmteDiagnostics visible to the bootstrap loader so the injected call inside java.base
        // resolves. Under -javaagent the manifest's "Boot-Class-Path: wmtediag-agent.jar" already
        // does this, and appending a second time would force-disable CDS ("Sharing is only supported
        // for boot loader classes..."). So we only append explicitly for dynamic attach, where the
        // manifest is NOT honored.
        if (dynamicAttach) {
            java.util.jar.JarFile jar = agentJarOrNull();
            if (jar != null) {
                try {
                    inst.appendToBootstrapClassLoaderSearch(jar);
                } catch (Throwable t) {
                    log("WARNING: could not append agent jar to bootstrap search: " + t);
                }
            } else {
                log("WARNING: could not locate the agent jar to add to the bootstrap search. "
                        + "Under dynamic attach the injected call may fail to resolve WmteDiagnostics. "
                        + "Prefer launching with -javaagent:/path/to/wmtediag-agent.jar.");
            }
        }

        Transformer transformer = new Transformer();
        inst.addTransformer(transformer, true);

        try {
            Class<?> invokers = Class.forName("java.lang.invoke.Invokers", false, null);
            if (inst.isModifiableClass(invokers)) {
                inst.retransformClasses(invokers);
            } else {
                log("ERROR: java.lang.invoke.Invokers is not modifiable on this VM");
            }
        } catch (ClassNotFoundException e) {
            log("ERROR: java.lang.invoke.Invokers not found: " + e);
        } catch (UnmodifiableClassException e) {
            log("ERROR: could not retransform Invokers: " + e);
        } catch (Throwable t) {
            log("ERROR: unexpected failure during retransform: " + t);
        }

        if (transformer.instrumented) {
            log("instrumented java.lang.invoke.Invokers#" + TARGET_METHOD
                    + " -- diagnostics armed.");
        } else {
            log("WARNING: transformer ran but did NOT instrument " + TARGET_METHOD
                    + "; the method signature may have changed on this build. "
                    + "Nothing will be captured. Please report your exact 'java -version'.");
        }
    }

    /** Best-effort location of the running agent jar; null if it cannot be resolved as a file. */
    private static java.util.jar.JarFile agentJarOrNull() {
        // 1) Code source of this class (works for dynamic attach where the agent loads in an app loader).
        try {
            java.security.CodeSource cs = WmteAgent.class.getProtectionDomain().getCodeSource();
            if (cs != null && cs.getLocation() != null) {
                java.io.File f = new java.io.File(cs.getLocation().toURI());
                if (f.isFile()) {
                    return new java.util.jar.JarFile(f);
                }
            }
        } catch (Throwable ignore) {
            // fall through to URL-based resolution
        }
        // 2) Resolve from this class's own resource URL (jar:file:.../wmtediag-agent.jar!/...).
        try {
            java.net.URL self = WmteAgent.class.getResource("WmteAgent.class");
            if (self != null && "jar".equals(self.getProtocol())) {
                String p = self.getPath();              // file:/path/to.jar!/com/...
                int bang = p.indexOf('!');
                if (bang > 0) {
                    java.net.URI uri = new java.net.URI(p.substring(0, bang));
                    java.io.File f = new java.io.File(uri);
                    if (f.isFile()) {
                        return new java.util.jar.JarFile(f);
                    }
                }
            }
        } catch (Throwable ignore) {
            // give up; caller logs appropriately
        }
        return null;
    }

    private static void log(String msg) {
        System.err.println("[wmtediag] " + msg);
    }

    // ---- transformer ---------------------------------------------------------

    private static final class Transformer implements ClassFileTransformer {
        volatile boolean instrumented;

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws IllegalClassFormatException {
            if (!INVOKERS_INTERNAL.equals(className)) {
                return null; // unchanged
            }
            try {
                ClassReader reader = new ClassReader(classfileBuffer);
                // COMPUTE_MAXS so we don't have to hand-track the (modest) stack growth.
                ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
                InjectingVisitor visitor = new InjectingVisitor(writer);
                reader.accept(visitor, 0);
                instrumented = visitor.injected;
                return visitor.injected ? writer.toByteArray() : null;
            } catch (Throwable t) {
                // Never break class loading. Return null => original bytes used.
                log("ERROR transforming Invokers (leaving it unmodified): " + t);
                return null;
            }
        }
    }

    private static final class InjectingVisitor extends ClassVisitor {
        boolean injected;

        InjectingVisitor(ClassVisitor cv) {
            super(Opcodes.ASM9, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (TARGET_METHOD.equals(name) && DIAG_DESC_MATCHES(descriptor)) {
                injected = true;
                return new HeadCallInserter(mv);
            }
            return mv;
        }

        private static boolean DIAG_DESC_MATCHES(String descriptor) {
            // newWrongMethodTypeException(MethodType, MethodType) returns WrongMethodTypeException.
            return descriptor != null
                    && descriptor.startsWith("(Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;)");
        }
    }

    /**
     * Inserts, at the very head of the method body:
     * {@code WmteDiagnostics.onWrongMethodType(arg0, arg1);}
     * Both args are object references (slots 0 and 1, since the method is static).
     */
    private static final class HeadCallInserter extends MethodVisitor {
        HeadCallInserter(MethodVisitor mv) {
            super(Opcodes.ASM9, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            // load arg0 (MethodType), arg1 (MethodType); call diagnostics; original body follows.
            super.visitVarInsn(Opcodes.ALOAD, 0);
            super.visitVarInsn(Opcodes.ALOAD, 1);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, DIAG_INTERNAL, DIAG_METHOD, DIAG_DESC, false);
        }
    }
}

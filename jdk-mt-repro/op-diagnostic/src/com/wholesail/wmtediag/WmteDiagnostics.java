package com.wholesail.wmtediag;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.invoke.MethodType;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Diagnostic sink for the rare {@code WrongMethodTypeException} described in
 * FasterXML/jackson-modules-base#142.
 *
 * <p>This class is installed on the <em>bootstrap</em> class loader search path by the
 * accompanying java agent, so that instrumented bytecode inside {@code java.lang.invoke.Invokers}
 * (a {@code java.base} class) can call into it. The agent injects a single static call to
 * {@link #onWrongMethodType(MethodType, MethodType)} at the head of
 * {@code Invokers.newWrongMethodTypeException(MethodType, MethodType)}.
 *
 * <p>That construction method runs <em>only on failure</em>, so the success path of every
 * {@code invokeExact} carries zero added cost. When it fires we capture the decisive evidence
 * that distinguishes a GC/reference-processing bug from a JIT/VM bug:
 * what {@code MethodType.internTable.get(canonical)} currently returns, by reference identity.
 *
 * <p>Everything here is best-effort and exception-safe: the diagnostic must never alter program
 * behavior nor throw back into the VM's exception-construction path. On any internal error we fall
 * back to whatever we managed to collect and let the original exception propagate untouched.
 */
public final class WmteDiagnostics {

    /** System property naming the dump file. Defaults to a temp file if unset. */
    public static final String OUTPUT_PROP = "wmtediag.output";
    /** System property; when "true", only dump when arg0 and arg1 stringify identically. */
    public static final String ONLY_IDENTICAL_PROP = "wmtediag.onlyIdentical";
    /** System property capping how many dumps we write (the failure is durable; we don't need many). */
    public static final String MAX_DUMPS_PROP = "wmtediag.maxDumps";

    private static final long JVM_START_MILLIS = startMillisBestEffort();
    private static final AtomicInteger DUMPS_WRITTEN = new AtomicInteger();
    private static final int MAX_DUMPS = intProp(MAX_DUMPS_PROP, 25);
    private static final boolean ONLY_IDENTICAL = Boolean.parseBoolean(System.getProperty(ONLY_IDENTICAL_PROP, "false"));

    // Reflective handles into MethodType internals, set up once. All optional: if the required
    // --add-opens are missing these stay null and the dump degrades gracefully.
    private static final Object INTERN_TABLE;
    private static final Method INTERN_TABLE_GET;
    private static final Field MT_FORM_FIELD;
    private static final String INTROSPECTION_NOTE;

    static {
        Object table = null;
        Method get = null;
        Field form = null;
        String note = "ok";
        try {
            Field tableField = MethodType.class.getDeclaredField("internTable");
            tableField.setAccessible(true);
            table = tableField.get(null);
            // internTable is jdk.internal.util.ReferencedKeyMap on JDK 21; .get(Object) is the lookup.
            get = table.getClass().getMethod("get", Object.class);
            get.setAccessible(true);
        } catch (Throwable t) {
            note = "internTable not accessible (" + t.getClass().getSimpleName() + ": " + t.getMessage()
                    + "); rerun with --add-opens java.base/java.lang.invoke=ALL-UNNAMED "
                    + "--add-opens java.base/jdk.internal.util=ALL-UNNAMED";
            table = null;
            get = null;
        }
        try {
            form = MethodType.class.getDeclaredField("form");
            form.setAccessible(true);
        } catch (Throwable t) {
            form = null;
            if ("ok".equals(note)) {
                note = "MethodType.form not accessible (" + t.getClass().getSimpleName() + ")";
            }
        }
        INTERN_TABLE = table;
        INTERN_TABLE_GET = get;
        MT_FORM_FIELD = form;
        INTROSPECTION_NOTE = note;
    }

    private WmteDiagnostics() {}

    /**
     * Invoked by injected bytecode at the head of
     * {@code Invokers.newWrongMethodTypeException(expectedFromHandle, foundAtCallSite)}.
     *
     * <p>Argument naming mirrors the JDK message text. For
     * {@code newWrongMethodTypeException(a0, a1)} the message reads
     * {@code "handle's method type <a0> but found <a1>"}. In the {@code checkExactType(mh, expected)}
     * path that Blackbird's {@code invokeExact} reaches, {@code a0} is the handle's real
     * {@code mh.type()} and {@code a1} is the call-site {@code expected} type. The bug under
     * investigation makes those two stringify identically while being non-identical references.
     *
     * @param handleType  arg0: the handle's method type (message "handle's method type ...")
     * @param expectedType arg1: the call-site expected type (message "but found ...")
     */
    public static void onWrongMethodType(MethodType handleType, MethodType expectedType) {
        try {
            if (DUMPS_WRITTEN.get() >= MAX_DUMPS) {
                return;
            }
            if (ONLY_IDENTICAL && !sameString(handleType, expectedType)) {
                return; // ordinary user WMTE (genuinely different signatures); not our bug.
            }
            if (DUMPS_WRITTEN.incrementAndGet() > MAX_DUMPS) {
                return;
            }
            String report = buildReport(handleType, expectedType);
            writeReport(report);
        } catch (Throwable ignore) {
            // Never let the diagnostic disturb the VM's exception construction.
        }
    }

    private static boolean sameString(MethodType a, MethodType b) {
        String sa = safeToString(a);
        String sb = safeToString(b);
        return sa.equals(sb);
    }

    private static String buildReport(MethodType handleType, MethodType expectedType) {
        StringBuilder b = new StringBuilder(2048);
        b.append("================ WrongMethodTypeException diagnostic ================\n");
        b.append("captured by wmtediag java agent at exception construction time\n");
        b.append("issue : FasterXML/jackson-modules-base#142\n\n");

        appendTiming(b);
        appendTypePair(b, handleType, expectedType);
        appendKeyDatum(b, handleType, expectedType);
        appendThreadAndStack(b);
        appendGc(b);
        appendEnv(b);

        b.append("====================================================================\n");
        return b.toString();
    }

    private static void appendTiming(StringBuilder b) {
        long now = System.currentTimeMillis();
        long sinceStart = JVM_START_MILLIS > 0 ? now - JVM_START_MILLIS : -1;
        b.append("--- timing ---\n");
        b.append("timestamp        : ").append(Instant.ofEpochMilli(now)).append('\n');
        b.append("ms since JVM start: ").append(sinceStart).append(
                sinceStart >= 0 ? "  (" + (sinceStart / 1000.0) + "s)" : "  (uptime unavailable)").append('\n');
        b.append('\n');
    }

    private static void appendTypePair(StringBuilder b, MethodType handleType, MethodType expectedType) {
        b.append("--- the two MethodType instances ---\n");
        b.append("arg0 handle's type : ").append(safeToString(handleType))
                .append("  @").append(idh(handleType)).append('\n');
        b.append("arg1 expected type : ").append(safeToString(expectedType))
                .append("  @").append(idh(expectedType)).append('\n');
        b.append("toString identical : ").append(sameString(handleType, expectedType)).append('\n');
        boolean eq = false;
        try {
            eq = handleType != null && handleType.equals(expectedType);
        } catch (Throwable ignore) {
        }
        b.append(".equals()          : ").append(eq).append('\n');
        b.append("== (reference)     : ").append(handleType == expectedType).append('\n');
        b.append("internal 'form' id : arg0.form @").append(formIdh(handleType))
                .append("   arg1.form @").append(formIdh(expectedType)).append('\n');
        b.append("\nINTERPRETATION: equals==true with ==false is the invariant violation. "
                + "Identical 'form' ids on two non-identical MethodTypes means the duplicate "
                + "shares the canonical form object (consistent with a lost/replaced intern entry).\n\n");
    }

    /** THE KEY DATUM: what the live intern table currently maps the canonical type to, by identity. */
    private static void appendKeyDatum(StringBuilder b, MethodType handleType, MethodType expectedType) {
        b.append("--- KEY DATUM: live intern table lookup (by reference identity) ---\n");
        if (INTERN_TABLE == null || INTERN_TABLE_GET == null) {
            b.append("internTable.get(arg0): <unavailable> ").append(INTROSPECTION_NOTE).append('\n');
            b.append("internTable.get(arg1): <unavailable>\n\n");
            return;
        }
        appendOneLookup(b, "internTable.get(arg0 handle's type)  ", handleType, handleType, expectedType);
        appendOneLookup(b, "internTable.get(arg1 expected type)  ", expectedType, handleType, expectedType);
        b.append("\nINTERPRETATION:\n");
        b.append("  - returns arg0 OR arg1 (one of them is canonical, the other is the orphan)\n");
        b.append("        -> a NON-CANONICAL duplicate bypassed/escaped the table (points at JIT/VM:\n");
        b.append("           a C2 miscompile or a stale cached MethodType reached invokeExact).\n");
        b.append("  - returns a THIRD distinct instance\n");
        b.append("        -> the table now holds a re-created entry; the live duplicates predate it\n");
        b.append("           (consistent with GC weak-ref processing dropping a still-reachable entry).\n");
        b.append("  - returns null\n");
        b.append("        -> the canonical entry was lost from the table entirely (GC/reference\n");
        b.append("           processing bug on ReferencedKeyMap).\n\n");
    }

    @SuppressWarnings("unchecked")
    private static void appendOneLookup(StringBuilder b, String label, MethodType key,
                                        MethodType arg0, MethodType arg1) {
        try {
            Object held = INTERN_TABLE_GET.invoke(INTERN_TABLE, key);
            b.append(label).append(": @").append(held == null ? "null" : idh(held));
            if (held != null) {
                b.append("  (== arg0? ").append(held == arg0)
                        .append(", == arg1? ").append(held == arg1)
                        .append(", form @").append(formIdh((MethodType) held)).append(')');
            }
            b.append('\n');
        } catch (Throwable t) {
            b.append(label).append(": <lookup failed: ").append(t).append(">\n");
        }
    }

    private static void appendThreadAndStack(StringBuilder b) {
        Thread cur = Thread.currentThread();
        b.append("--- thread & stack ---\n");
        b.append("thread : ").append(cur.getName()).append("  (id=").append(cur.threadId())
                .append(", group=").append(cur.getThreadGroup() == null ? "?" : cur.getThreadGroup().getName())
                .append(")\n");
        b.append("stack  :\n");
        // Skip our own diagnostic frames; start at the JDK exception-construction frame.
        StackTraceElement[] st = cur.getStackTrace();
        for (StackTraceElement e : st) {
            String cn = e.getClassName();
            if (cn.equals(WmteDiagnostics.class.getName()) || cn.equals(Thread.class.getName())) {
                continue;
            }
            b.append("    at ").append(e).append('\n');
        }
        b.append('\n');
    }

    private static void appendGc(StringBuilder b) {
        b.append("--- GC state ---\n");
        try {
            StringBuilder names = new StringBuilder();
            long count = 0;
            long time = 0;
            for (GarbageCollectorMXBean g : ManagementFactory.getGarbageCollectorMXBeans()) {
                if (names.length() > 0) {
                    names.append('+');
                }
                names.append(g.getName());
                count += Math.max(0, g.getCollectionCount());
                time += Math.max(0, g.getCollectionTime());
            }
            b.append("collectors : ").append(names).append('\n');
            b.append("collections: ").append(count).append("   total GC time: ").append(time).append("ms\n");
        } catch (Throwable t) {
            b.append("<GC introspection failed: ").append(t).append(">\n");
        }
        b.append('\n');
    }

    private static void appendEnv(StringBuilder b) {
        b.append("--- environment ---\n");
        b.append("java.runtime : ").append(System.getProperty("java.runtime.name"))
                .append(' ').append(System.getProperty("java.runtime.version")).append('\n');
        b.append("java.vm      : ").append(System.getProperty("java.vm.name"))
                .append(' ').append(System.getProperty("java.vm.version")).append('\n');
        b.append("java.vendor  : ").append(System.getProperty("java.vendor")).append('\n');
        b.append("introspection: ").append(INTROSPECTION_NOTE).append('\n');
        b.append("dumps so far : ").append(DUMPS_WRITTEN.get()).append(" / max ").append(MAX_DUMPS).append('\n');
        b.append('\n');
    }

    private static void writeReport(String report) {
        // Always to stderr (visible even if the file write fails).
        PrintStream err = System.err;
        if (err != null) {
            err.print("\n" + report);
            err.flush();
        }
        Path out = resolveOutputPath();
        if (out == null) {
            return;
        }
        try {
            if (out.getParent() != null) {
                Files.createDirectories(out.getParent());
            }
            try (Writer w = Files.newBufferedWriter(out, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                w.write(report);
                w.write('\n');
            }
            if (err != null) {
                err.println("[wmtediag] dump appended to " + out.toAbsolutePath());
                err.flush();
            }
        } catch (IOException e) {
            if (err != null) {
                err.println("[wmtediag] could not write dump file " + out + ": " + e);
            }
        }
    }

    /** Human-readable description of where dumps will go, for the agent's startup log. */
    public static String resolveOutputForLog() {
        Path p = resolveOutputPath();
        return p == null ? "<unresolved>" : p.toAbsolutePath().toString();
    }

    private static Path resolveOutputPath() {
        String configured = System.getProperty(OUTPUT_PROP);
        try {
            if (configured != null && !configured.isBlank()) {
                return Path.of(configured.trim());
            }
            return Path.of(System.getProperty("java.io.tmpdir", "."), "wmtediag.log");
        } catch (Throwable t) {
            return null;
        }
    }

    // ---- small helpers -------------------------------------------------------

    private static String idh(Object o) {
        return o == null ? "null" : Integer.toHexString(System.identityHashCode(o));
    }

    private static String formIdh(MethodType t) {
        if (t == null || MT_FORM_FIELD == null) {
            return "?";
        }
        try {
            return idh(MT_FORM_FIELD.get(t));
        } catch (Throwable e) {
            return "?";
        }
    }

    private static String safeToString(Object o) {
        if (o == null) {
            return "null";
        }
        try {
            return String.valueOf(o);
        } catch (Throwable t) {
            return "<toString failed>";
        }
    }

    private static long startMillisBestEffort() {
        try {
            return ManagementFactory.getRuntimeMXBean().getStartTime();
        } catch (Throwable t) {
            return -1;
        }
    }

    private static int intProp(String key, int def) {
        try {
            String v = System.getProperty(key);
            return v == null ? def : Integer.parseInt(v.trim());
        } catch (Throwable t) {
            return def;
        }
    }
}

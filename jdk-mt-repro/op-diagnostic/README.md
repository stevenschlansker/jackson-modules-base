# WrongMethodTypeException diagnostic agent (jackson-modules-base#142)

Hi Noitcereon — thank you for the detailed report. We have studied the
`WrongMethodTypeException` you are hitting in Blackbird and we believe it is a JVM bug:
two `java.lang.invoke.MethodType` instances exist that are `.equals()` but not `==`,
which violates `MethodType`'s interning invariant. `invokeExact` compares the handle's
type to the call-site's expected type by **reference identity**, so two equal-but-distinct
types make it throw the confusing "handle's method type `()ToBooleanFunction` but found
`()ToBooleanFunction`" message — identical text on both sides.

We can reason about the cause from the source, but we cannot reproduce it. You can.
This package is a tiny java agent that captures the decisive evidence **at the instant the
exception is created**, before it propagates. One `-javaagent` flag, no app changes, no
rebuild. When you next see the failure, send us the dump file.

The single most valuable fact it records is what the live `MethodType` intern table maps
the canonical type to, by identity. That one datum tells us whether the table lost a live
entry (a GC / reference-processing bug) or a non-canonical duplicate slipped past the table
(a JIT / VM bug).

---

## What's in this package

- `wmtediag-agent.jar` — the agent (self-contained; ASM is bundled inside it, no extra deps).
- `build.sh` — rebuilds the jar from source if you'd rather not run our prebuilt binary.
- `validate.sh` — proves the agent works by fabricating the exact failure.
- `src/` — agent source, if you want to read exactly what it does.
- `test/` — the validation harness.

The agent only instruments `java.lang.invoke.Invokers.newWrongMethodTypeException(...)`,
which the JVM calls **only when it is about to throw this exception**. So it adds zero
overhead to your normal, successful `invokeExact` calls. It never throws and never changes
behavior — on any internal error it silently writes whatever it managed to collect and lets
your original exception propagate untouched.

---

## How to run it

Add two things to your existing launch command:

1. `-javaagent:/path/to/wmtediag-agent.jar`
2. the two `--add-opens` flags below (needed to read the intern table; without them the agent
   still dumps everything else and tells you in the file that the key datum was skipped).

Using your environment (Shenandoah, `-Xms32m -Xmx6g`):

```
java \
  -Xms32m -Xmx6g \
  -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC \
  -javaagent:/path/to/wmtediag-agent.jar \
  -Dwmtediag.output=/var/log/wmtediag.log \
  --add-opens java.base/java.lang.invoke=ALL-UNNAMED \
  --add-opens java.base/jdk.internal.util=ALL-UNNAMED \
  -jar your-spring-boot-app.jar
```

- `-Dwmtediag.output=...` chooses the dump file. If you omit it, the agent writes to
  `${java.io.tmpdir}/wmtediag.log`.
- If you launch with `-javaagent` (recommended), that is all you need. The agent puts its
  diagnostic class on the boot classpath via its manifest, so no `EnableDynamicAgentLoading`
  is required. If for some reason you attach the agent at runtime instead of at launch, add
  `-XX:+EnableDynamicAgentLoading`.

On startup you'll see one line confirming it armed:

```
[wmtediag] instrumented java.lang.invoke.Invokers#newWrongMethodTypeException -- diagnostics armed.
```

If you instead see a `WARNING: ... did NOT instrument`, the method signature changed on your
build — please send us your exact `java -version` and we'll adjust.

---

## What the output looks like

When the failure fires, the agent prints the dump to stderr **and** appends it to your
output file. It looks like this (captured during our validation against a fabricated copy of
your exact failure on Red Hat OpenJDK 21.0.10 + Shenandoah):

```
================ WrongMethodTypeException diagnostic ================
--- the two MethodType instances ---
arg0 handle's type : ()int  @5f4da5c3
arg1 expected type : ()int  @5ef04b5
toString identical : true
.equals()          : true
== (reference)     : false
internal 'form' id : arg0.form @621be5d1   arg1.form @621be5d1

--- KEY DATUM: live intern table lookup (by reference identity) ---
internTable.get(arg0 handle's type)  : @5ef04b5  (== arg0? false, == arg1? true, form @621be5d1)
internTable.get(arg1 expected type)  : @5ef04b5  (== arg0? false, == arg1? true, form @621be5d1)

--- thread & stack ---
thread : main  (id=1, group=main)
stack  :
    at java.base/java.lang.invoke.Invokers.newWrongMethodTypeException(Invokers.java)
    at java.base/java.lang.invoke.Invokers.checkExactType(Invokers.java:531)
    ...

--- GC state ---
collectors : Shenandoah Pauses+Shenandoah Cycles
collections: 0   total GC time: 0ms

--- environment ---
java.runtime : OpenJDK Runtime Environment 21.0.10+7-LTS
...
```

**Please send us the whole dump file.** In your real app the interesting line is the
`internTable.get(...)` result and whether it equals arg0, arg1, a third instance, or `null` —
that is what discriminates the GC hypothesis from the JIT hypothesis. The thread name (e.g.
`reactor-http-epoll-N`) and the "ms since JVM start" also confirm the startup-clustering
pattern you described.

---

## Verify the agent on your machine first (optional, ~2 seconds)

If you want to confirm it works before deploying:

```
./validate.sh                 # or point it at a specific JDK: ./validate.sh $(which java)
```

It fabricates the exact "X but found X" failure and prints the dump it captured. (The
validation harness needs the `--add-opens` flags, which `validate.sh` already passes.)

To rebuild the jar from source:

```
./build.sh                    # set ASM_JAR=/path/to/asm-9.x.jar if ours isn't found
```

---

## Cheap fallback tests (if you can't run the agent)

Each of these narrows the cause on its own. Run your app the way you normally reproduce the
failure, with one change at a time, and tell us whether the failure still appears.

a) **Disable the JIT.** Add `-Xint`.
   - Failure **disappears** -> the JIT (C2) is implicated.
   - Failure **still happens** -> points away from the JIT, toward GC / reference processing.
   - Note: `-Xint` is much slower. This is a diagnostic run only, not a fix.

b) **Switch the garbage collector.** Replace your Shenandoah flags with `-XX:+UseG1GC`.
   - Failure **disappears under G1** -> Shenandoah specifically matters (strong signal toward
     a reference-processing interaction in Shenandoah).
   - Failure **still happens** -> not Shenandoah-specific.

c) **Capture VM logs around the failure window.** Add:
   ```
   -Xlog:gc*:file=gc.log -Xlog:class+load=info:file=classload.log
   ```
   Send us `gc.log` and `classload.log` covering the minute before and after the failure.
   GC cycle timing next to the failure timestamp helps test the reference-processing theory.

d) **Try other versions.** cowtowncoder asked about this:
   - Does it reproduce on Jackson **2.21.4** and **2.22.0** (blackbird)? Same app, just bump
     the jackson-module-blackbird version.
   - If your Blackbird/`ObjectMapper` config lets you switch the dispatch from `invokeExact`
     to plain `invoke`, does the failure disappear? `invoke` does an asType conversion instead
     of an identity check, so it would mask an interning violation rather than trip on it.

---

## What we'll do with the dump

We believe this is a JVM bug, not a Jackson bug. The evidence the agent captures — in
particular the intern-table identity result — is intended to go into an OpenJDK bug report so
the right team (GC reference processing or HotSpot C2) can find the root cause. Your dump is
the piece we cannot produce ourselves. Thank you.

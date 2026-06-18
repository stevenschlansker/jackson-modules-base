# Handoff: x86_64 fastdebug reproduction push

**For the next agent, on an x86_64 host with a fastdebug JDK.** Read `HANDOFF.md` first for
the full history; this doc is the focused next push. We are NOT going upstream empty-handed —
the users reproduced this in production, so it is reproducible, and we are going to keep
trying to match the conditions.

## The reframing that matters

We ran the **exact build from caffeine#1111 — Amazon Corretto 17.0.8.7.1 — and it HELD**
(~10.8 B real `Caffeine.build()` calls; see `real-libs/`). Same build the users hit it on.

Therefore: **this is not fixed and not a version problem. We are missing the triggering
conditions.** Every "held" result so far points the same way — our workloads don't recreate
what a real, large application does. That is the gap to close, and it is closable.

What the field reports actually describe (and we have under-replicated):
- **Warmup clustering.** ~10 failures across *thousands of JVM launches*, at application
  startup — not steady state. (carterkozak, caffeine#1111.)
- **A real app's intern-table churn.** A starting JVM interns a huge, *diverse* population of
  `MethodType`s: every lambda (LambdaMetafactory), every string concatenation (invokedynamic
  `StringConcatFactory`), every `MethodHandle` lookup, every record. Our harnesses interned a
  narrow population (a few hundred synthetic shapes, or one library's factory types).
- **"Some piece of class-loading/lookup state."** carterkozak's own hunch. Suggests CDS/AOT
  archived metadata or first-link ordering — axes we have barely touched.

## Environment setup

```sh
# fastdebug x86_64 JDK. Try the distro first:
dnf install -y java-21-openjdk-fastdebug java-17-openjdk-fastdebug 2>/dev/null
# If unavailable, a fastdebug build is what unlocks GC verification + develop-only C2 stress.
# (Corretto/Temurin do NOT ship fastdebug; you may need an Adoptium debug build or to build
#  OpenJDK 17/21 with --with-debug-level=fastdebug. A fastdebug 21 is fine to start.)

# Also fetch the exact field builds for product-VM runs (no root needed):
curl -fsSL https://corretto.aws/downloads/resources/17.0.8.7.1/amazon-corretto-17.0.8.7.1-linux-x64.tar.gz | tar xz
git checkout claude/jdk-methodtype-invokeexact-repro
cd jdk-mt-repro
```

## Experiments, ranked by expected yield

Run each until it hits (exit 42, or exit >=128 = VM assert + `hs_err_pid*.log`) or is
clearly exhausted. **The fastdebug VM is the multiplier**: its internal asserts + GC
verification turn a silent 1-in-billions inconsistency into a hard abort. Apply it to
whichever workload below you run.

### 1. Realistic warmup storm under fastdebug (NEW — highest yield)
`WarmupChurn.java` + `warmup-storm.sh` maximise *diverse* intern-table churn during warmup
(many distinct LambdaMetafactory sites + invokedynamic string concat + MethodHandle lookups,
with an `invokeExact` identity check at each lambda site). Designed for short JVMs (the
warmup signal); long single runs exhaust metaspace by design.
```sh
JAVA=<fastdebug>/bin/java N=20000 SECS=4 PAR=8 ./warmup-storm.sh
# then add verification to each JVM by editing META in the script, or run a long-ish single
# JVM under fastdebug verify for a few hundred launches:
<fastdebug>/bin/java --add-opens java.base/java.lang.invoke=ALL-UNNAMED \
  -XX:+UnlockDiagnosticVMOptions -XX:+VerifyBeforeGC -XX:+VerifyAfterGC -XX:+UseG1GC \
  -Dwarm.durationSec=6 -Dwarm.threads=8 -cp out WarmupChurn
```
Validated on aarch64 product: self-test fires, ~500K lambda sites in 4s, no false positive.
Scale `N` to tens of thousands of launches — that is the closest analogue to the field rate.

### 2. The real libraries under fastdebug (faithful + verified)
`real-libs/` runs actual Caffeine 3.1.7 `build()` (the verbatim #1111 `invokeExact` path) and
Blackbird 2.12.7. It held on product Corretto 17.0.8, but never ran under a fastdebug VM with
GC verification. Re-run `real-libs/real-hunt.sh` with `JAVA=<fastdebug>/bin/java` and add
`-XX:+UnlockDiagnosticVMOptions -XX:+VerifyBeforeGC -XX:+VerifyAfterGC` to the long phases.

### 3. CDS / AOT discriminator (NEW, cheap, untried)
Our runs all used default CDS (on). Never tried: CDS off, or AppCDS archiving the app's own
generated classes (which is what carterkozak's "class-loading state" hint points at, and what
JDK-8354897 "weak refs in AOT cache" touches). Run experiments 1–2 three ways and compare:
```sh
... -Xshare:off ...                 # CDS disabled
... -Xshare:on  ...                 # default archive (baseline)
# AppCDS: -XX:ArchiveClassesAtExit=app.jsa on a first run, then -XX:SharedArchiveFile=app.jsa
```
If it reproduces under exactly one CDS mode, that *is* the mechanism — a high-value finding.

### 4. develop-only C2 stress under fastdebug (the JIT hypothesis)
`jit-debug-hunt.sh` runs `JitInvokeExactStress` with `+StressLCM/GCM/IGVN +DeoptimizeALot`
(develop flags → debug VM only) and the `-Xint` control. It held on aarch64 fastdebug; the
x86_64 codegen/memory-model path is different and untested. `./jit-debug-hunt.sh`.

### 5. A real Dropwizard/Spring app, launched repeatedly (most faithful)
caffeine#1111 was a Dropwizard microservice. Take any real Caffeine+Jackson app (a Dropwizard
or Spring Boot sample that builds caches and serializes at startup), and launch it a few
thousand times under fastdebug, concurrently, on x86_64. This is the literal field scenario.

### 6. Older / other builds
JDK 16 (the jackson-modules-base#142 build) is untested; so is a sweep of 17.0.x to find a
version boundary. Corretto 17.0.8 is done (held).

## When it reproduces

Capture: the full harness report (it prints `internTable.get(...)` identity — discriminates
"GC lost a live entry" from "instance bypassed the table"), the `hs_err_pid*.log` if the
fastdebug VM aborted, the exact `java -version` + all flags + CDS mode, and whether `-Xint`
suppresses it. Then update `UPSTREAM-REPORT.md` (it is otherwise ready to file) and we go
upstream with a reproduction, not just analysis.

## Honest expectation

A clean run is still not proof of absence (field rate ~1 in hundreds-to-thousands of
launches). But the fastdebug VM + the warmup-diversity workload + the CDS axis are three
genuinely new levers we have never combined, and the fastdebug asserts make a near-miss
*visible* in a way product runs cannot. This is the best remaining shot before we hand
OpenJDK the analysis-only package.

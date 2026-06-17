# Handoff: `MethodType` interning / `invokeExact` `WrongMethodTypeException`

**Status:** not reproduced on this machine (aarch64). All harnesses are built, validated,
and exhausted here. The leading remaining hypothesis is that the bug is **x86_64-specific**
(a HotSpot GC/JIT phenomenon), so the next step is to run the *same artifacts* on an
**x86_64** host. This document is the orientation for that successor agent and the basis
for an eventual OpenJDK report.

Everything referenced lives in this directory (`jdk-mt-repro/`).

---

## 1. The bug

Two unrelated libraries report a rare, load-dependent failure:

- Caffeine `ben-manes/caffeine#1111`: `WrongMethodTypeException: expected ()NodeFactory but found ()NodeFactory`
- Jackson Blackbird `FasterXML/jackson-modules-base#142`: `... expected ()ToIntFunction but found ()ToIntFunction`

Both print the **same type on both sides**. That is the whole tell: `Invokers.checkExactType`
compares the handle's `MethodType` to the call site's symbolic `MethodType` by **reference
identity** (`targetType != expected`), not `.equals()`. So the failure means two
`MethodType` instances exist that are `.equals()` but not `==` — a violation of the
interning invariant that `MethodType.methodType(...)` canonicalises every instance through
a process-wide weak intern table.

`MinimalRepro.java` demonstrates the *consequence* deterministically by fabricating a
non-interned duplicate via reflection: `invokeExact` throws `handle's method type ()String
but found ()String`, while `invoke` (which uses `asType`/`.equals()`) tolerates it. This
is also why Caffeine's `invokeExact`→`invoke` workaround "papers over" it without fixing
the root cause.

In production the surviving instance is the call site's constant-pool appendix (`expected`),
pinned for the life of the class. The 25–40% failure rate Blackbird saw means a
non-canonical instance becomes *durably* installed (appendix vs. table canonical diverge),
so every subsequent build fails until the process restarts.

---

## 2. Key analytical result (this is the important part)

We read the intern implementation in full on **both** JDK lines and proved the failure
**cannot originate in the Java intern logic**:

- **JDK 21** `jdk.internal.util.ReferencedKeyMap` (`@since 21`, rewrote the old set):
  race-losers call `unused()`→`WeakReference.clear()` which does **not** enqueue, so a
  loser can never pollute the stale queue; `removeStaleReferences()` removes a polled key
  by identity, or by `equals` only when both referents are dead — it can never match a
  *live* entry (`Objects.equals(null, liveReferent)` is false).
- **JDK 17** `MethodType.ConcurrentWeakInternSet`: `WeakEntry.equals` falls back to
  **identity** when either referent is null:
  `return (that == null || mine == null) ? (this == obj) : mine.equals(that);`
  so `expungeStaleElements()`'s `map.remove(staleEntry)` can only remove the *exact* stale
  entry, never a live equal one. **This disproves the "expunge removes a live entry"
  hypothesis** raised in caffeine#1111.

Conclusion: a live duplicate of a reachable type cannot be produced by pure-Java races on
either version. The field failures are therefore necessarily **VM-level**:
1. **GC reference-processing** — a reachable `WeakReference` transiently cleared/enqueued,
   or a stale `get()` under concurrent reference processing; or
2. **C2 miscompile** of the `@ForceInline checkExactType` identity compare at a hot
   `invokeExact` site (the `@Stable` `MethodType` fields could be mis-folded/mis-scheduled).

A Java-level stress test can *assert* the invariant and provide a substrate to run under a
debug VM, but it cannot *force* a VM bug. That matches our results below.

---

## 3. Environment of THIS investigation (and why arch matters)

- Host: **Rocky Linux 10.2, aarch64**, 8 cores (capped to `-XX:ActiveProcessorCount=4` via
  `JDK_JAVA_OPTIONS`), 8 GB RAM.
- JDKs used:
  - OpenJDK **21.0.11** (Red Hat) product — `/usr/lib/jvm/java-21-openjdk`
  - OpenJDK **21.0.11 fastdebug** + slowdebug — `/usr/lib/jvm/java-21-openjdk-fastdebug`
  - Amazon **Corretto 17.0.19** — downloaded to `/tmp/jdk17` (matches caffeine#1111's
    Corretto 17.0.8 family; has the old `ConcurrentWeakInternSet`)

**The x86_64 hypothesis.** The original reports were almost certainly on x86_64. GC
reference-processing and C2 codegen differ materially by architecture (memory model:
aarch64 is weakly ordered, x86 is TSO; different load-barrier / IGVN / scheduling code
paths). A heisenbug of this kind can be present on one arch and absent on another. We
exhausted aarch64; **the single highest-value next action is to run the identical harnesses
on x86_64** (ideally Linux x86_64 with JDK 17 G1, the closest match to the field reports).

---

## 4. What's here (artifacts)

| file | purpose |
|---|---|
| `MethodTypeInternStress.java` | main harness. Probe A (pinned-type intern invariant), Probe B (handle type == pinned call-site expected), Probe C (evictable re-intern under concurrent GC — the focused detector), link storm (fresh-classloader first-time `invokeExact` linkage), decoy/GC churn. Self-test (`-Dmt.selftest=true`) proves the detectors fire. Exit 0=held, 42=violation, 3=resource. |
| `LinkProbe.java` | the Blackbird/Caffeine pattern in a class loaded fresh per iteration (first-time linkage). |
| `JitInvokeExactStress.java` | JIT-focused harness: non-constant (`volatile`) handles hammered through `invokeExact` so C2 must emit the real `checkExactType`; built to run under C2 stress flags. |
| `MinimalRepro.java` | deterministic demonstration of the `invokeExact` identity check vs `invoke`. |
| `run-sweep.sh` | sweeps GCs + compilation modes on one JDK. |
| `repro-hunt.sh` | JDK 21 hunt (G1-first, ZGC, Shenandoah, launch storm). |
| `repro-hunt-17.sh` | JDK 17 hunt (G1 production config, high contention + thread oversubscription). |
| `debug-hunt.sh` | runs the intern harness under **fastdebug** with GC verification (`VerifyBeforeGC/AfterGC`, `ShenandoahVerify`, `ZVerifyObjects/Forwarding`). |
| `jit-debug-hunt.sh` | runs `JitInvokeExactStress` under **fastdebug** with C2 stress (`StressLCM/GCM/IGVN`, `DeoptimizeALot`) and the `-Xint` control. These are *develop* flags → debug VM only. |
| `results-*.log` | full captured output of the three big hunts (see §5). |
| `README.md` | harness usage, config properties, triage notes. |

Build (any of these javacs work; sources are Java 17+ compatible):
```
javac MethodTypeInternStress.java LinkProbe.java JitInvokeExactStress.java
```
Run needs `--add-opens java.base/java.lang.invoke=ALL-UNNAMED` and (JDK 21 only)
`--add-opens java.base/jdk.internal.util=ALL-UNNAMED` for the intern-table diagnostic.

---

## 5. What we tried, and the results (all on aarch64 — all HELD)

| JDK | config | metric | result |
|---|---|---|---|
| Corretto 17.0.19 | G1, 24 threads, 300s (production-like) | Probe C 2.06 B | held |
| Corretto 17.0.19 | ZGC, 180s | Probe C 531 M | held |
| Corretto 17.0.19 | Shenandoah, 180s | Probe C 1.22 B | held |
| Corretto 17.0.19 | G1, 100-JVM warmup launch storm | — | held |
| OpenJDK 21.0.11 | G1 / generational ZGC / Shenandoah | Probe C 1.2 B+ | held |
| 21 **fastdebug** | G1 `+VerifyBeforeGC +VerifyAfterGC` | Probe C 71 M | held, no assert |
| 21 **fastdebug** | Shenandoah `+ShenandoahVerify` | Probe C 82 M | held, no assert |
| 21 **fastdebug** | gen-ZGC `+ZVerifyObjects +ZVerifyForwarding` | Probe C 1.21 B | held, no assert |
| 21 **fastdebug** | G1 + link storm | Probe C 337 M | held, no assert |
| 21 **fastdebug** | `-Xint` control (JIT harness) | 303 M invokeExact | held |
| 21 **fastdebug** | C2 `+StressLCM/GCM/IGVN +DeoptimizeALot` | 494 M invokeExact | held |
| 21 **fastdebug** | `-XX:-TieredCompilation +DeoptimizeALot`, freq. rebuild | 44 M invokeExact | held |
| 21 **fastdebug** | C2 `+StressLCM/GCM/IGVN`, hot | **3.73 B** invokeExact | held |

Aggregate: ~5.6 B Probe C re-intern checks + ~4.3 B JIT `invokeExact` calls, across both
JDK lines, all four collectors, fastdebug verification, and C2 scheduling stress — **zero
violations, zero VM asserts**.

Notes / gotchas already handled:
- Two earlier "exit 42" hits were **false positives** (heap OOM from unbounded decoy types;
  class-metadata OOM from Probe B spinning a `LambdaMetafactory` class per call). Fixed:
  decoy space bounded (`mt.decoySpace`), Probe B no longer spins classes, and resource
  exhaustion now walks the cause chain → exit 3, never 42.
- `repro-hunt-17.sh` phase 2 (`g1/youngsmall`) needs `-XX:+UnlockExperimentalVMOptions`
  before `G1NewSizePercent` (fixed in the script; the stale `results-corretto17-hunt.log`
  shows that phase as `rc=1` because it predates the fix).
- C2 stress + `DeoptimizeALot` are **develop** flags: debug VM only.

---

## 6. What the x86_64 successor should do (in order)

0. `git checkout` this branch; `cd jdk-mt-repro`; confirm `uname -m` = `x86_64`.
1. **Fast first pass — product JDK 17 + G1** (closest to the field reports). Get a JDK 17
   (Corretto 17 tarball works without root:
   `curl -fsSL https://corretto.aws/downloads/latest/amazon-corretto-17-x64-linux-jdk.tar.gz`),
   then `JAVA=<jdk17>/bin/java ./repro-hunt-17.sh`. Also run on the platform default JDK.
2. **fastdebug 17 if available** (`dnf install java-17-openjdk-fastdebug` if the distro has
   it; otherwise a fastdebug 21 is fine to start): `./debug-hunt.sh` and `./jit-debug-hunt.sh`.
   The asserts + GC verification are the strongest catch mechanism. A hit shows up as exit
   42 (probe) or exit ≥128 (VM assert + `hs_err_pid*.log`).
3. **Scale the launch storm.** Field rate was ~10 in thousands of *JVM launches*, clustered
   at warmup. `./launch-storm.sh 5000 4 8` (or higher) is the closest analogue and may be
   more effective than one long run. Run across several x86_64 machines if possible.
4. **Vary the knobs** that differ by arch/timing: more `mt.probeCThreads` (oversubscribe),
   smaller `mt.probeCSpace` (more contention on the same bins), `-Dmt.sysgcMs=1`, and
   different `-XX:` GC ergonomics. Try Parallel/Serial too as a contrast.
5. If a hit occurs: capture the full report (it prints `internTable.get(CANON)` identity —
   that discriminates "table lost a live entry" (GC) from "bad instance bypassed the table"
   (JIT/VM)), the `hs_err` file if any, and the exact `java -version` + flags. Re-run under
   `-Xint`: if it vanishes, it's the JIT.

---

## 7. If still not reproduced anywhere

Then either it was fixed between the reported versions and now, or it needs a specific
workload/timing we haven't matched. Before concluding, try: real Caffeine/Blackbird under
load on x86_64 (the actual reproducers from the issues), and older exact builds
(Corretto 17.0.8, the version in caffeine#1111). A clean run is not proof of absence — the
production rate is ~1 in hundreds-to-thousands of launches.

---

## 8. Draft OpenJDK report (fill in once x86_64 is tried)

> **Component:** hotspot/gc or core-libs/java.lang.invoke (TBD by where it reproduces)
> **Synopsis:** Rare `WrongMethodTypeException` from `MethodHandle.invokeExact` where the
> handle type and call-site `expected` type are `.equals()` but not `==` (printed
> identically), i.e. a violated `MethodType` interning invariant.
>
> **Downstream reports:** ben-manes/caffeine#1111 (Corretto 17, G1), FasterXML/jackson-modules-base#142 (JDK 16).
>
> **Analysis:** `Invokers.checkExactType` compares `MethodType` by identity. The intern
> implementations on JDK 17 (`ConcurrentWeakInternSet`) and JDK 21 (`ReferencedKeyMap`) are
> both race-free for producing live duplicates (see §2), so the duplicate must arise below
> the Java layer — GC reference processing clearing/returning-stale a reachable weak intern
> entry, or a C2 miscompile of the identity check. Likely architecture-specific.
>
> **Reproduction:** [attach this harness + the platform/arch where it tripped, the report
> output incl. `internTable.get(CANON)` identity, `hs_err` if any, `java -version`, flags,
> and whether `-Xint` suppresses it]. Note: extensive stress on aarch64 (JDK 17 + 21,
> product + fastdebug w/ GC verification, C2 stress; billions of checks) did NOT reproduce.
>
> **Questions for OpenJDK:** is this a known/fixed issue (search MethodType intern / weak
> ref processing / invokeExact); is it x86_64-specific; which builds are affected.

Do **not** file until there is either an x86_64 reproduction or, at minimum, a documented
x86_64 attempt — OpenJDK will close an unreproducible report, as the Jackson team's own
issue was closed.

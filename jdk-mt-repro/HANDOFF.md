# Handoff: `MethodType` interning / `invokeExact` `WrongMethodTypeException`

**Status:** NOT reproduced anywhere we have tried. Synthetic stress (intern-invariant + C2
`invokeExact`) and the **real libraries on the exact reported build** (Corretto 17.0.8.7.1)
both held, on **both aarch64 and x86_64**, across all four collectors, product + fastdebug
(aarch64) with GC verification, and ~22 billion combined checks/builds. The "x86_64-specific"
hypothesis was **refuted** (§5a). Source analysis proves the Java intern logic is race-free
on both JDK 17 and 21 (§2), so the field failures are a VM-level (GC ref-processing or C2)
event that black-box stress from outside the VM does not force.

**Next push:** because we ran the EXACT #1111 build (Corretto 17.0.8.7.1) and it held, this
is a *conditions* problem, not a version problem — so there is more to try before going
upstream. See **`HANDOFF-FASTDEBUG-X86.md`** for the focused x86_64 fastdebug plan: a new
realistic-warmup-diversity harness (`WarmupChurn.java` / `warmup-storm.sh`), a CDS/AOT
discriminator axis, fastdebug GC verification + develop-only C2 stress, and a real-app
launch loop. `UPSTREAM-REPORT.md` is the analysis-only package if those also come up empty.

**Reading order:** §1 (bug) → §2 (why it's VM-level) → §5/§5a/§5b (what was tried) →
`HANDOFF-FASTDEBUG-X86.md` (next push) → §8 / `UPSTREAM-REPORT.md`.

Everything referenced lives in this directory (`jdk-mt-repro/`); real-library artifacts are
in `real-libs/`.

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

## 5a. x86_64 attempt (2026-06-17) — also HELD, no reproduction

The x86_64 pass called for in this handoff is now done. Same artifacts, x86_64 host.

- Host: **Rocky Linux 10.2, x86_64**, 4 cores, 31 GB RAM (kernel 6.12).
- JDKs:
  - Amazon **Corretto 17.0.19** — `/tmp/jdk17` (old `ConcurrentWeakInternSet`; closest to
    the field reports). Shenandoah **and** ZGC both available in this build.
  - OpenJDK **21.0.11** (Red Hat) product — `/usr/lib/jvm/java-21-openjdk`.
- No fastdebug VM was available on this host, so `debug-hunt.sh` / `jit-debug-hunt.sh`
  (GC verification + C2 `develop` stress) could **not** be run. *However*, this Red Hat
  21.0.11 build exposes `StressLCM/StressGCM/StressIGVN` as **diagnostic** (not develop)
  flags, so real C2 scheduling stress was still applied on the product VM — see the new
  `jit-product-hunt.sh`. `DeoptimizeALot` (develop-only) was substituted with heavy
  recompilation churn (frequent handle rebuild) + a C2-only (no-tier) phase.

| JDK | config | metric | result |
|---|---|---|---|
| Corretto 17.0.19 | G1, 24 threads, space512, 300s (production-like) | Probe C 676 M | held |
| Corretto 17.0.19 | G1 youngsmall + `sysgcMs=1`, 240s | Probe C 36 M | held |
| Corretto 17.0.19 | ZGC, 180s | Probe C 235 M | held |
| Corretto 17.0.19 | Shenandoah, 180s | Probe C 223 M | held |
| Corretto 17.0.19 | G1, 100-JVM launch storm | — | held |
| Corretto 17.0.19 | G1, **2000-JVM** launch storm (3s, par 6) | — | held |
| OpenJDK 21.0.11 | G1, 240s | Probe C 275 M | held |
| OpenJDK 21.0.11 | G1 youngsmall + `sysgcMs=2`, 240s (5158 GC cycles) | Probe C 158 M | held |
| OpenJDK 21.0.11 | generational ZGC, 180s | Probe C 346 M | held |
| OpenJDK 21.0.11 | Shenandoah, 180s | Probe C 237 M | held |
| OpenJDK 21.0.11 | G1, 120-JVM launch storm | — | held |
| 21 (product) | `-Xint` control (JIT harness), 60s | 98 M invokeExact | held |
| 21 (product) | C2 `+StressLCM/GCM/IGVN` + rebuild, 8t, 180s | 781 M invokeExact | held |
| 21 (product) | `-TieredCompilation` + rebuild, 8t, 180s | 552 M invokeExact | held |
| 21 (product) | C2 `+StressLCM/GCM/IGVN`, 12t hot, 180s | **4.29 B** invokeExact | held |

Aggregate (x86_64): ~2.2 B Probe C re-intern checks, ~4.9 B Probe A intern checks,
~5.7 B JIT compiled `invokeExact` calls, and ~3220 short-JVM launches, across **both**
JDK lines, **all four** collectors (G1, ZGC, generational-ZGC, Shenandoah), and product-VM
C2 scheduling stress — **zero violations, zero VM aborts**. The self-test (`-Dmt.selftest=true`)
confirmed both detectors fire on x86_64 before the runs.

Gaps vs. the aarch64 pass: **no fastdebug VM** here, so GC verification
(`VerifyBeforeGC/AfterGC`, `ShenandoahVerify`, `ZVerify*`) and the develop-only C2 stress
(`DeoptimizeALot`) were not exercised on x86_64. That is the single most valuable remaining
gap — a fastdebug 17/21 x86_64 run of `debug-hunt.sh` + `jit-debug-hunt.sh` is the strongest
catch mechanism and has now been tried on *neither* arch's product limitation... it ran on
aarch64 fastdebug (held) but not x86_64. Get a fastdebug x86_64 VM and run those two scripts.

Bugs fixed in the artifacts during this pass:
- `repro-hunt.sh` phase 2 (`g1/probeC/youngsmall`) was missing `-XX:+UnlockExperimentalVMOptions`
  before `-XX:G1NewSizePercent`, so it failed to start (rc=1) — the same bug the handoff noted
  was fixed in `repro-hunt-17.sh` but had been left unfixed here. Now fixed; the patched config
  was re-run standalone (held, 158 M Probe C, 5158 GC cycles).
- Added `jit-product-hunt.sh`: a product-VM variant of `jit-debug-hunt.sh` for hosts without a
  fastdebug build (uses the diagnostic C2 stress flags this build exposes).

Net: the bug is **not** reproduced on x86_64 either, on these builds. It was not the simple
"x86_64 vs aarch64" split the leading hypothesis guessed. Next levers: a **fastdebug x86_64**
run (highest value), older exact builds (Corretto 17.0.8 per caffeine#1111), generational ZGC
on a build that has it, and the real Caffeine/Blackbird workloads under load (§7).

---

## 5b. Real-library attempt on the EXACT reported build (2026-06-18) — also HELD

The most faithful attempt: drive the **real libraries' actual code paths** on **Amazon
Corretto 17.0.8.7.1** — the precise JDK build named in caffeine#1111 — instead of a
synthetic model. Artifacts live in `real-libs/` (sources + `real-hunt.sh` + README +
result log; jars/JDK are fetched, not committed).

- **Caffeine 3.1.7** `Caffeine.build()` across many policy combinations. Confirmed in the
  3.1.7 bytecode that `NodeFactory.newFactory` is exactly
  `findConstructor → type() → changeReturnType → asType → invokeExact()` — a fresh
  `MethodType` per build, checked by identity against the pinned `expected`. This is the
  #1111 path verbatim, sustainable at ~12M builds/s (factory classes bounded by ~policy
  combos, no metaspace growth).
- **Blackbird/Jackson 2.12.7** serializing varied bean types (covers every
  `BBSerializerModifier` accessor branch: int/long/boolean/String/Object) — the #142 path.

| JDK / GC | config | metric | result |
|---|---|---|---|
| Corretto 17.0.8.7.1, G1 | Caffeine, 16 threads, 300s | 4.14 B builds | held |
| Corretto 17.0.8.7.1, G1 youngsmall | Caffeine, 14 threads, 240s | 2.70 B builds | held |
| Corretto 17.0.8.7.1, ZGC | Caffeine, 14 threads, 180s | 2.05 B builds | held |
| Corretto 17.0.8.7.1, Shenandoah | Caffeine, 14 threads, 180s | 1.93 B builds | held |
| Corretto 17.0.8.7.1, G1 | launch storm A (both libs, 500 short JVMs) | — | held |
| Corretto 17.0.8.7.1, G1 | launch storm B (Blackbird fresh-mapper, 500 short JVMs) | — | held |

Aggregate: **~10.8 billion real `Caffeine.build()` calls** through the verbatim #1111
`invokeExact` path, plus ~1000 short-JVM warmup launches covering Blackbird first-use —
**zero reproductions**.

Design/fidelity notes for the next agent:
- The Caffeine path is the sustainable, high-rate, best-documented repro; long phases use it.
- Blackbird `fresh-mapper-per-op` spins a hidden lambda class per accessor and exhausts
  metaspace in a long run (caught correctly as exit 3, *not* a violation). It is therefore
  exercised only in the **launch storms** (short JVMs that exit before classes accumulate) —
  which is also *more faithful*, since #142 was concurrent startup first-use, not steady state.
- `real.freshMapper`/`real.mapperRefresh` control mapper churn; `real.caffeineThreads` /
  `real.blackbirdThreads` size each side (set blackbird=0 for a pure, indefinitely-sustainable
  Caffeine run).

**Conclusion after both synthetic and real attempts, on two arches:** the bug is a genuine
rare VM-level event (GC reference processing or C2) that needs either the original
production workload/timing or VM-internal tooling. Black-box stress from outside the VM —
synthetic or real-library — does not force it. The recommendation has shifted from "keep
hunting" to "engage upstream" (§8); a fastdebug **x86_64** run and the exact older builds
(Corretto 17.0.8 ✓ done, JDK 16 from #142 — not yet) remain the only untried black-box levers.

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

## 8. Upstream report

A complete, self-contained draft for core-libs-dev / hotspot-dev (and for cross-linking to
the Caffeine/Jackson maintainers and to uschindler/ben-manes, who already suspected a JDK
threading bug) is in **`UPSTREAM-REPORT.md`**. It folds in the decoded symptom, the
race-free proof for both intern implementations, the full negative-results table across both
architectures, the JBS "is this known?" search, and four specific questions for OpenJDK.

The terse skeleton below is kept for reference; `UPSTREAM-REPORT.md` supersedes it.

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

The x86_64 attempt is now documented (§5a) and the real-library attempt on the exact #1111
build (§5b) is done, so the report can be filed as a "here is the analysis + a faithful
harness + exhaustive negative results, please advise" issue. It is framed as a question
(is this known/fixed/arch-specific?) precisely because we cannot hand OpenJDK a one-command
reproduction — be explicit about that so it is not closed as not-reproducible the way the
Jackson issue was.

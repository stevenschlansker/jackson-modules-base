# MethodType interning / `invokeExact` stress reproducer

A self-contained harness that stresses the `java.lang.invoke.MethodType` interning
invariant that `MethodHandle.invokeExact` depends on, and asserts it from several
angles under heavy concurrent GC and class-loading churn.

## The bug being chased

Field reports against two unrelated libraries show rare, load-dependent failures:

- Caffeine: `ben-manes/caffeine#1111` — `WrongMethodTypeException: expected ()NodeFactory but found ()NodeFactory`
- Jackson Blackbird: `FasterXML/jackson-modules-base#142` — `... expected ()ToIntFunction but found ()ToIntFunction`

The two types print identically because `Invokers.checkExactType` compares the
handle's `MethodType` against the call-site's symbolic `MethodType` by **reference
identity** (`targetType != expected`), not `.equals()`:

```java
// java.lang.invoke.Invokers (JDK 21)
static void checkExactType(MethodHandle mh, MethodType expected) {
    MethodType targetType = mh.type();
    if (targetType != expected)                 // identity, not equals
        throw newWrongMethodTypeException(targetType, expected);
}
```

This is sound only because `MethodType.methodType(...)` canonicalises every
instance through a process-wide weak intern table (`MethodType.internTable`).
The contract is: **equal MethodTypes are identical**. The failures mean that
invariant is being violated — two `.equals()` but non-`==` instances of the same
type exist at once.

A minimal demonstration of the *consequence* (fabricating the duplicate via the
private constructor) is in `../mt/Repro.java`; run the harness here with
`-Dmt.selftest=true` to see the same thing through these detectors.

## Why this is below the Java library layer

The intern table was rewritten in JDK 21 (`jdk.internal.util.ReferencedKeySet` /
`ReferencedKeyMap`, `@since 21`), replacing the JDK 17 `ConcurrentWeakInternSet`.
Walking the 21 code:

- losing threads in `internKey` call `unused()` → `WeakReference.clear()`, which
  does **not** enqueue, so race-losers cannot pollute the stale queue;
- `removeStaleReferences()` removes a polled key by identity, or by `equals` only
  when both referents are already dead — it cannot match a *live* entry
  (`Objects.equals(null, liveReferent)` is false);
- a key is enqueued only after GC clears a *weakly-unreachable* referent, and a
  `MethodType` pinned by a live handle or a committed call-site appendix is not
  weakly unreachable.

The pure-Java logic preserves the invariant, yet the reports say it still happens
on 21. So the suspect is below the library:

1. a GC reference-processing window where `WeakReference.get()` on a reachable
   referent transiently yields `null` (or a live entry is cleared), causing
   `makeImpl` to intern a duplicate;
2. C2 mis-speculation at the heavily customised, `@ForceInline` `invokeExact`
   site (`@Stable` `MethodType` fields, LambdaForm customisation);
3. the VM-side call-site `expected` resolution (`MethodHandleNatives.
   findMethodHandleType` → `makeImpl`) installing a non-canonical instance.

Crucially, in both libraries the surviving (call-site `expected`) instance is
**permanently pinned** by the constant-pool appendix of the `invokeExact` site.
So the bug reduces to a clean assertion this harness makes directly: *for a type
whose canonical `MethodType` is strongly reachable, `methodType(X)` must always
return that same instance.*

## What the harness asserts

- **Probe A (intern invariant).** Pins the canonical `MethodType` for a population
  of synthesised type shapes, then checks `methodType(spec_i) == CANON[i]` from
  many threads. Pure intern-table check; independent of the JIT and invoke
  machinery.
- **Probe B (production signature, steady state).** The literal Blackbird/Caffeine
  pattern: `LambdaMetafactory.metafactory(..., methodType(FI.class), ...)
  .getTarget().invokeExact()` with the `(FI)` cast pinning `expected`.
- **Link storm (production signature, first-time linkage).** Defines a fresh
  `LinkProbe` class under a throwaway classloader every iteration, so the
  `invokeExact` site is linked from scratch each time — matching the field
  reports' clustering at build/init time. Also churns class unloading.
- **Decoy churn + GC pressure.** Many threads intern-and-drop unpinned shapes;
  a churn thread drives allocation. Small heap → frequent GC → wide
  reference-processing windows overlapping the probe lookups.

On the first violation it prints identity hashes of both instances, what the
intern table currently holds for the type (run with the `--add-opens` below),
the offending stack, GC in use, and counters; then exits with status **42**
(distinctive, so it is not confused with a code-1 JVM-startup failure when a GC
is unavailable). A clean run exits **0**. `-Dmt.selftest=true` exits **2** after
demonstrating that both detectors fire on a fabricated duplicate.

## Build

```
javac MethodTypeInternStress.java LinkProbe.java
```

## Run

Single config (recommended flags enable the intern-table diagnostic):

```
java --add-opens java.base/java.lang.invoke=ALL-UNNAMED \
     --add-opens java.base/jdk.internal.util=ALL-UNNAMED \
     -Xmx512m -Xms512m -XX:+UseG1GC \
     -Dmt.durationSec=120 MethodTypeInternStress
```

Self-test (proves the detectors and report formatting):

```
java --add-opens java.base/java.lang.invoke=ALL-UNNAMED \
     --add-opens java.base/jdk.internal.util=ALL-UNNAMED \
     -Dmt.selftest=true MethodTypeInternStress
```

Sweep GC algorithms and compilation modes (`-Xint`, C1-only, C2-only), stopping
on first hit:

```
./run-sweep.sh 300        # 300s per config
```

Launch storm — many short JVMs, biased toward warmup-time linkage (closest to the
field signal of failures-per-launch):

```
GC="-XX:+UseZGC" ./launch-storm.sh 5000 4 8     # 5000 launches, 4s each, 8 parallel
```

## Configuration (system properties)

| property | default | meaning |
|---|---|---|
| `mt.threads` | CPUs | Probe A/B worker threads |
| `mt.decoyThreads` | threads/2 | intern-churn threads |
| `mt.linkStormThreads` | 2 | fresh-linkage threads |
| `mt.pinned` | 1024 | number of pinned canonical types |
| `mt.durationSec` | 60 | run length |
| `mt.gcThread` | true | allocation-pressure thread |
| `mt.sysgcMs` | 0 | if >0, periodic `System.gc()` every N ms |
| `mt.probeB` | true | enable steady-state invokeExact probe |
| `mt.linkStorm` | true | enable first-time-linkage probe |
| `mt.churnKb` | 32 | KB per GC-churn allocation |
| `mt.selftest` | false | inject a fabricated violation and exit |

## Results so far (this investigation)

The interning invariant **held** under extreme stress on every configuration tried;
no non-canonical `MethodType` / `WrongMethodTypeException` was observed.

| JDK | collector | Probe C checks | result |
|---|---|---|---|
| Corretto 17.0.19 | G1 (production config, 24 threads, 300s) | 2.06 billion | held |
| Corretto 17.0.19 | ZGC (180s) | 531 million | held |
| Corretto 17.0.19 | Shenandoah (180s) | 1.22 billion | held |
| Corretto 17.0.19 | G1, 100-JVM warmup launch storm | — | held |
| OpenJDK 21.0.11 | G1 / generational ZGC / Shenandoah | 1.2 billion+ | held |

Total: ~5.6 billion Probe C re-intern checks, plus billions of Probe A/B, no violation.

Combined with a full reading of the intern source on both versions, the failure
cannot be produced by the intern library logic:

- JDK 21 `ReferencedKeyMap`: race-losers `clear()` without enqueueing; stale removal
  matches a live entry neither by identity nor by `equals` (a live referent never
  equals a null one).
- JDK 17 `ConcurrentWeakInternSet`: `WeakEntry.equals` falls back to **identity** when
  either referent is null, so `expungeStaleElements()`'s `map.remove(staleEntry)` can
  only remove the exact stale entry, never a live equal one. This disproves the
  "expunge removes a live entry" hypothesis from caffeine#1111.

So the field failures are necessarily a **VM-level** phenomenon (GC reference
processing or JIT), not the Java intern logic. A Java-level stress test like this one
structurally cannot force them; it can only assert the invariant and serve as a
substrate to run under a debug VM / verification flags.

## Notes for triage

- The intern-table diagnostic line `internTable.get(CANON) : @... (== CANON? ..)`
  discriminates hypotheses: if the table still holds the pinned canonical, the
  bad instance came from a path that *bypassed or raced* the table (favouring the
  JIT/VM hypotheses); if it holds something else, a live entry was lost
  (favouring the GC/ref-processing hypothesis).
- Compare `-Xint` vs C2: if the violation vanishes under `-Xint`, it is the JIT.
- Concurrent collectors (G1 concurrent cycle, ZGC, Shenandoah) process references
  concurrently with mutators and are the most likely to expose a ref-processing
  window; STW collectors (Parallel, Serial) are a useful contrast.
- A clean run is not proof of absence — the production rate is ~1 in hundreds of
  JVM launches under real workloads. Treat this as a probability amplifier and a
  diagnostic, and run it long / wide / across platforms.
```

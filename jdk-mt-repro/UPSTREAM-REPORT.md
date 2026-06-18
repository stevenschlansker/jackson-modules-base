# Rare `WrongMethodTypeException` from `MethodHandle.invokeExact`: a violated `MethodType` interning invariant

Draft for core-libs-dev / hotspot-dev (and for cross-linking to the Caffeine and Jackson
maintainers). Everything below is reproducible from the harnesses in this directory.

## Summary

Two unrelated libraries have independently reported a rare, load-dependent failure in
production:

- Caffeine — [ben-manes/caffeine#1111](https://github.com/ben-manes/caffeine/issues/1111):
  `java.lang.invoke.WrongMethodTypeException: expected ()NodeFactory but found ()NodeFactory`
  (Amazon Corretto 17.0.8, G1, Dropwizard-style microservices).
- Jackson Blackbird — [FasterXML/jackson-modules-base#142](https://github.com/FasterXML/jackson-modules-base/issues/142):
  `... expected ()ToIntFunction but found ()ToIntFunction` (JDK 16, Jackson 2.12).

In both, the exception message prints **the same type on both sides**. Both issues were
closed without a root cause; Caffeine shipped a workaround (replacing `invokeExact` with
`invoke`) that masks the symptom without explaining it.

## Why the message prints identical types

`java.lang.invoke.Invokers.checkExactType` compares the target handle's `MethodType` against
the call site's symbolic `MethodType` by **reference identity**, not `.equals()`:

```java
// java.lang.invoke.Invokers (JDK 21)
static void checkExactType(MethodHandle mh, MethodType expected) {
    MethodType targetType = mh.type();
    if (targetType != expected)                 // identity, not equals
        throw newWrongMethodTypeException(targetType, expected);
}
```

This is sound only because `MethodType.methodType(...)` canonicalises every instance through
a process-wide weak intern table (`MethodType.internTable`). The invariant `invokeExact`
relies on is: **two equal `MethodType`s are the same object.** A "X but found X" message
therefore means two instances exist that are `.equals()` but not `==` — the interning
invariant has been violated. (The HotSpot wiki notes this identity check works *"because
instances of MethodType are interned as an implementation choice, though the spec does not
require it and it could change."*)

`MinimalRepro.java` demonstrates the consequence deterministically: fabricate a
non-interned duplicate via reflection, and `invokeExact` throws `handle's method type
()String but found ()String` while `invoke` (which routes through `asType`/`.equals()`)
succeeds. That is exactly why Caffeine's `invoke` workaround hides the bug.

## The duplicate cannot originate in the Java intern logic

We read the intern implementation in full on both JDK lines:

- **JDK ≥22 / 21.0.x** `jdk.internal.util.ReferencedKeyMap` (`MethodType.internTable`):
  intern race-losers call `unused()` → `WeakReference.clear()`, which does **not** enqueue,
  so a loser cannot pollute the stale queue. `removeStaleReferences()` removes a polled key
  by identity, or by `equals` only when both referents are already dead — it can never match
  a *live* entry (`Objects.equals(null, liveReferent)` is false).
- **JDK 17** `MethodType.ConcurrentWeakInternSet`: `WeakEntry.equals` falls back to
  **identity** when either referent is null:
  `return (that == null || mine == null) ? (this == obj) : mine.equals(that);`
  so `expungeStaleElements()`'s `map.remove(staleEntry)` can only remove the exact stale
  entry, never a live equal one. (This specifically refutes the "expunge removes a live
  entry" hypothesis floated in caffeine#1111.)

On both versions, a live duplicate of a strongly-reachable type cannot be produced by
pure-Java races in the intern table. The failure must therefore arise **below the Java
layer**:

1. **GC reference processing** — a `WeakReference` to a still-reachable `MethodType` being
   transiently cleared / its `get()` returning null / its entry enqueued, causing `makeImpl`
   to re-intern a duplicate; or
2. **C2 miscompile** of the `@ForceInline` identity compare at a hot `invokeExact` site
   (the call site's `expected` and the handle's `@Stable` `type()` field mis-folded or
   mis-scheduled).

In production the surviving (call-site `expected`) instance is pinned permanently by the
constant-pool appendix, which explains why Blackbird saw a *durable* ~25–40% failure rate
once triggered: a non-canonical instance becomes installed and diverges from the table
canonical for the life of the class.

## What we tried (and could not reproduce)

We built harnesses that (a) assert the interning invariant directly under heavy concurrent
GC and class-loading churn, (b) hammer hot `invokeExact` sites under C2 stress, and (c) run
the **real libraries** on the **exact reported build**. All are in this directory.

| Track | JDK / build | coverage | result |
|---|---|---|---|
| Intern-invariant stress | OpenJDK 21.0.11 + Corretto 17.0.19, aarch64 & x86_64 | G1, ZGC(+gen), Shenandoah; ~7.8 B re-intern checks | held |
| Same, under fastdebug | OpenJDK 21.0.11 fastdebug, aarch64 | `+VerifyBeforeGC/AfterGC`, `+ShenandoahVerify`, `+ZVerifyObjects/Forwarding` | held, no VM assert |
| Hot `invokeExact` / C2 stress | 21 fastdebug (aarch64) + 21 product (x86_64) | `+StressLCM/GCM/IGVN`, `+DeoptimizeALot`, `-Xint` control | held (~10 B calls) |
| **Real libraries** | **Corretto 17.0.8.7.1** (the #1111 build), aarch64 | Caffeine 3.1.7 `build()` (verbatim #1111 path) + Blackbird 2.12.7; G1/ZGC/Shenandoah + 1000 launch JVMs | **held (~10.8 B builds)** |

Combined, ~22 billion checks/builds across two architectures, all four collectors, product
and fastdebug VMs — zero reproductions. This is consistent with a true heisenbug that
requires production timing or VM-internal observation; it is not reproducible by black-box
stress from outside the VM, which matches both library teams' experience.

## Questions for OpenJDK

1. Is this a known or already-fixed issue? We searched JBS (MethodType intern / weak
   reference processing / `WrongMethodTypeException` / `invokeExact`) and found no matching
   issue. Note: [JDK-8310913](https://bugs.openjdk.org/browse/JDK-8310913) (the
   `ReferencedKeyMap` switch, fixed in 22) is an explicit *refactor* "to be shared," not a
   correctness fix, so it does not obviously explain a fix between the reported builds and now.
2. Can the weak intern table briefly return a non-canonical instance during concurrent
   reference processing on any collector (we suspect G1 specifically, the field collector)?
3. Could C2 mis-handle the `@ForceInline checkExactType` identity comparison given the
   `@Stable` `MethodType` fields and LambdaForm customization?
4. Is this plausibly architecture- or build-specific in a way our testing (aarch64 + x86_64,
   17.0.8 → 21.0.11) would have missed?

## Reproduce / inspect

See `README.md` (intern + JIT harnesses) and `real-libs/README.md` (real-library harness;
includes the one-shot bootstrap to fetch the exact Corretto 17.0.8 build and the library
jars). All harnesses print a full diagnostic on a hit, including
`internTable.get(CANON)` identity — which discriminates "table lost a live entry" (GC) from
"bad instance bypassed the table" (JIT) — and exit 42 (probe violation) / ≥128 (VM assert).

If anyone can reproduce under a debug VM, the `-Xint`-vs-C2 contrast is the decisive split:
a failure that vanishes under `-Xint` is the JIT; one that survives is GC/reference
processing.

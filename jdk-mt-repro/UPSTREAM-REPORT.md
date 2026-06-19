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
  originally `... expected ()ToIntFunction but found ()ToIntFunction` (JDK 16, Jackson 2.12).
  Reopened in 2026 with a fresh field report (commenter Noitcereon) carrying more detail:
  - **Build:** Red Hat OpenJDK **21.0.10.0.7-1**; **GC: Shenandoah** (`-XX:+UseShenandoahGC`);
    heap `-Xms32m -Xmx6g`; Spring Boot 3.5.4 (WebFlux/reactor); jackson 2.19.2. No custom
    classloaders. Started only **after upgrading 17 → 21**, and is "cached" — it recurs until
    the app is restarted.
  - **Two distinct call sites**, both printing identical types:
    serialization `()ToBooleanFunction` (a primitive-`boolean` accessor) at
    `BBSerializerModifier.createProperty`, and deserialization
    `(MethodHandle)Function` at `CreatorOptimizer.createOptimized`.

In every case the exception message prints **the same type on both sides**. Both issues were
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

- **JDK ≥22 / 21.0.x** — `MethodType.internTable` is a `jdk.internal.util.ReferencedKeySet`
  backed by a `jdk.internal.util.ReferencedKeyMap` (verified by reflection on Red Hat 21.0.10:
  the field's runtime type is `ReferencedKeySet`, whose `map` field is a `ReferencedKeyMap`).
  Intern race-losers call `unused()` → `WeakReference.clear()`, which does **not** enqueue,
  so a loser cannot pollute the stale queue. `ReferencedKeyMap.removeStaleReferences()` removes
  a polled key by identity, or by `equals` only when both referents are already dead — it can
  never match a *live* entry (`Objects.equals(null, liveReferent)` is false).
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
| Real libraries | Corretto 17.0.8.7.1 (the #1111 build), aarch64 | Caffeine 3.1.7 `build()` (verbatim #1111 path) + Blackbird 2.12.7; G1/ZGC/Shenandoah + 1000 launch JVMs | held (~10.8 B builds) |

### Targeting the new Blackbird field report (x86_64, Shenandoah, the exact field build)

After #142 was reopened we re-ran focused on the now-known field conditions: **Shenandoah**,
the field heap (`-Xms32m -Xmx6g`), the **exact build Red Hat 21.0.10.0.7-1** (and 21.0.11
fastdebug for VM asserts), and **both** failure sites. We also deliberately oversubscribed
(threads ≫ cores) to widen interleavings, since this is a concurrency/GC-timing bug.

| Track | JDK / build | coverage | result |
|---|---|---|---|
| Intern-invariant (Probe A/B/C) + fastdebug verify | 21.0.11 **fastdebug**, x86_64 | **Shenandoah** `+ShenandoahVerify +VerifyBeforeGC/AfterGC`, field heap; 78 verified GC cycles; 164 M re-intern checks | held, no VM assert |
| Intern-invariant, oversubscribed contention | **21.0.10.0.7-1** (exact field build), x86_64 | Shenandoah, field heap, 16+24 threads on a 2 K-entry table, `sysgcMs=1`; **3.7 B re-intern checks / 2.6 K GC cycles** | held |
| First-time linkage (fresh-classloader `invokeExact`) | 21.0.10.0.7-1, x86_64 | Shenandoah, field heap, 10 link threads; 2.26 M fresh linkages | held |
| **Real libraries, serialize + deserialize** | **21.0.10.0.7-1** (exact field build), x86_64 | **Shenandoah**, field heap, oversubscribed; Caffeine 3.1.7 `build()` + Blackbird 2.12.7 **both the `BBSerializerModifier` (serialize) and `CreatorOptimizer` (deserialize) paths** — the two field sites — **1.32 B builds + 1.80 B serializes + 1.33 B deserializes** | held |
| Same, under fastdebug + verify | 21.0.11 fastdebug, x86_64 | Shenandoah `+ShenandoahVerify +VerifyBeforeGC/AfterGC`, field heap; both field sites; 21.9 M builds + 59.3 M ser + 58.9 M deser | held, no VM assert |

Combined across both pushes: tens of billions of checks/builds across two architectures, all
four collectors, product and fastdebug VMs, the exact field build and the exact field
collector, both failure sites, and heavy oversubscription — **zero reproductions, zero VM
asserts/aborts**. This is consistent with a true heisenbug that requires production timing or
VM-internal observation; it is not reproducible by black-box stress from outside the VM,
which matches both library teams' experience.

**Not a 21.0.10 → present version artifact.** We read all 160 commits between the exact field
build (`jdk-21.0.10-ga`) and our newer build (`jdk-21.0.11-ga`) and found nothing that touches
`java.lang.invoke`, the `MethodType` intern table, GC weak-reference processing, or the C2
path compiling `checkExactType`. [JDK-8358751](https://bugs.openjdk.org/browse/JDK-8358751)
("C2: Recursive inlining check for compiled lambda forms is broken") superficially mentions
compiled lambda forms, but it is a **different bug**: its symptom is a hard `SIGSEGV` crash in
`Node::uncast` (hs_err + core dump), not a Java-level `WrongMethodTypeException`; it reproduces
under G1 via Groovy/Velocity compilation back to JDK 8. So the clean result on 21.0.11 is a
faithful proxy for 21.0.10's intern/GC behavior — we are not merely observing a build where the
bug was already fixed.

## Questions for OpenJDK

1. Is this a known or already-fixed issue? We searched JBS (MethodType intern / weak
   reference processing / `WrongMethodTypeException` / `invokeExact`) and found no matching
   issue. Note: [JDK-8310913](https://bugs.openjdk.org/browse/JDK-8310913) (the
   `ReferencedKeyMap` switch, fixed in 22) is an explicit *refactor* "to be shared," not a
   correctness fix, so it does not obviously explain a fix between the reported builds and now.
2. Can the weak intern table briefly return a non-canonical instance during concurrent
   reference processing? The two field collectors are different — **G1** (caffeine#1111) and
   **Shenandoah** (jackson#142) — which argues against a single collector-specific bug and
   toward something in the shared weak-reference / intern path, or in C2.
3. Could C2 mis-handle the `@ForceInline checkExactType` identity comparison given the
   `@Stable` `MethodType` fields and LambdaForm customization?
4. Is this plausibly architecture- or build-specific in a way our testing (aarch64 + x86_64;
   exact builds Corretto 17.0.8.7.1 and Red Hat 21.0.10.0.7-1; both field collectors) missed?
5. The Jackson reporter saw it appear only after **17 → 21** and saw it become *durable*
   until restart. Is there a known interaction (intern-table rewrite, LambdaForm caching,
   AOT/CDS archived metadata) that could make a non-canonical instance persist once created?

## Reproduce / inspect

See `README.md` (intern + JIT harnesses) and `real-libs/README.md` (real-library harness;
includes the one-shot bootstrap to fetch the exact Corretto 17.0.8 build and the library
jars). All harnesses print a full diagnostic on a hit, including
`internTable.get(CANON)` identity — which discriminates "table lost a live entry" (GC) from
"bad instance bypassed the table" (JIT) — and exit 42 (probe violation) / ≥128 (VM assert).

If anyone can reproduce under a debug VM, the `-Xint`-vs-C2 contrast is the decisive split:
a failure that vanishes under `-Xint` is the JIT; one that survives is GC/reference
processing.

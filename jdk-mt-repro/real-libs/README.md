# Real-library reproduction (Caffeine 3.1.7 + Blackbird/Jackson 2.12.7)

The most faithful reproduction attempt: instead of a synthetic model of the `MethodType`
interning invariant, this drives the **actual code paths** from the field reports, on the
**exact reported build** (Amazon Corretto 17.0.8.7.1, the JDK in ben-manes/caffeine#1111).

## Why this is more faithful than the synthetic harness

The Caffeine stack trace in #1111 is:
```
NodeFactory.newFactory -> findConstructor -> type() -> changeReturnType -> asType -> invokeExact()
```
Confirmed in the 3.1.7 bytecode (`javap -c NodeFactory`): each `newFactory` call builds a
**fresh** `MethodType` via `changeReturnType`/`asType`, then `invokeExact()` checks it by
identity against the constant-pool `expected`. The failing generated factory class
(`SS`/`SSL`/`SSLMW`/`SSLMWA` in the trace) depends on the **policy combination**
(weak/soft keys+values, maximumSize, expireAfter*), so distinct combinations lazily load
distinct classes and intern distinct constructor `MethodType`s.

`RealLibStress` therefore:
- builds Caffeine caches across many policy combinations from many threads (forces repeated
  first-time factory linkage), and
- serializes many distinct bean classes with fresh `ObjectMapper`s through Blackbird
  (forces repeated first-time `BBSerializerModifier.createProperty` accessor linkage —
  where jackson-modules-base#142 failed).

A `WrongMethodTypeException` from either library (direct or wrapped) is the reproduction
(exit 42). Non-WMTE exceptions are logged as diagnostics and do **not** stop the run.
`warmupSanity()` exercises both paths once at startup so we know the detectors are live.

## Bootstrap (jars + JDK are NOT committed — fetch them)

```sh
# 1. exact reported JDK (no root needed)
curl -fsSL -o /tmp/c1708.tgz \
  https://corretto.aws/downloads/resources/17.0.8.7.1/amazon-corretto-17.0.8.7.1-linux-<ARCH>.tar.gz
mkdir -p /tmp/jdk1708 && tar xzf /tmp/c1708.tgz -C /tmp/jdk1708 --strip-components=1
# (<ARCH> = aarch64 or x64)

# 2. library jars into ./lib
mkdir -p lib && cd lib
B=https://repo1.maven.org/maven2
curl -fsSO $B/com/github/ben-manes/caffeine/caffeine/3.1.7/caffeine-3.1.7.jar
curl -fsSL -o blackbird.jar   $B/com/fasterxml/jackson/module/jackson-module-blackbird/2.12.7/jackson-module-blackbird-2.12.7.jar
curl -fsSL -o databind.jar    $B/com/fasterxml/jackson/core/jackson-databind/2.12.7/jackson-databind-2.12.7.jar
curl -fsSL -o core.jar        $B/com/fasterxml/jackson/core/jackson-core/2.12.7/jackson-core-2.12.7.jar
curl -fsSL -o annotations.jar $B/com/fasterxml/jackson/core/jackson-annotations/2.12.7/jackson-annotations-2.12.7.jar
cd ..

# 3. build + run the hunt
CP="lib/caffeine-3.1.7.jar:lib/blackbird.jar:lib/databind.jar:lib/core.jar:lib/annotations.jar"
/tmp/jdk1708/bin/javac --release 11 -cp "$CP" -d out RealLibStress.java Beans.java
JAVA=/tmp/jdk1708/bin/java ./real-hunt.sh
```

`pom.xml` is an alternative (Maven) way to resolve the same dependencies.

## Config (system properties)

| property | default | meaning |
|---|---|---|
| `real.durationSec` | 120 | run length |
| `real.caffeineThreads` | #cpus | concurrent `Caffeine.build()` threads |
| `real.blackbirdThreads` | #cpus | concurrent Blackbird serialize threads |
| `real.freshMapper` | true | new `ObjectMapper` per op (faithful first-link); false reuses one |

## Results (aarch64, Corretto 17.0.8.7.1)

Sanity run: ~100M `Caffeine.build()` + ~560K Blackbird serializations in 15s, **held**.
Full-hunt results are captured alongside the other `results-*.log` files in the parent
directory. See `../HANDOFF.md` for the consolidated status.

> Note: this is the closest match to the field reports that we can run without the exact
> production workload. A clean run still does not prove absence — the field rate was ~10
> failures across thousands of JVM launches.
```

# Blackbird benchmarks

JMH benchmarks that compare stock databind, the classic Blackbird engine
(LambdaMetafactory, release 3.2.2) and the generated-codec engine of this
build.

## Benchmarks

Each benchmark class has a `mode` parameter: `stock` (no module),
`blackbird` or `afterburner`. The default run measures `stock` and
`blackbird`.

- `PojoBenchmark`: setter POJOs. Arrays of 1000 small beans, the same beans
  through a property-based `@JsonCreator`, the media item bean, and arrays
  of 1000 beans with 8 `int` properties.
- `RecordBenchmark`: a record with a nested record and a list of 3 records.
- `BuilderBenchmark`: a bean with 8 properties and a fluent builder
  (`@JsonPOJOBuilder`).
- `WideBeanBenchmark`: setter beans with 40 and 200 properties.
- `ColdStartBenchmark`: the first read and write of 500 distinct types
  (250 records, 250 setter beans) in a fresh JVM, with 20 forks. The score
  is the time for all 500 types.

## How to run

Blackbird needs JDK 25 or later. The benchmark module is not part of the
reactor, so install the modules first:

```
./mvnw -pl blackbird,afterburner -am install -DskipTests
cd blackbird/benchmarks
../../mvnw package
../../mvnw package -Pclassic-blackbird
```

The first `package` writes `target/benchmarks.jar` (this build). The
`classic-blackbird` profile writes `target/classic/benchmarks-classic.jar`,
which has Blackbird 3.2.2 and the same databind. Run the two jars with the
same JMH options:

```
java -jar target/benchmarks.jar -f 3 -wi 5 -w 1s -i 5 -r 1s \
    'Pojo|Record|Builder|WideBean' -rf json -rff new.json
java -jar target/classic/benchmarks-classic.jar -f 3 -wi 5 -w 1s -i 5 -r 1s \
    'Pojo|Record|Builder|WideBean' -rf json -rff classic.json
java -jar target/benchmarks.jar ColdStart -rf json -rff cold-new.json
java -jar target/classic/benchmarks-classic.jar ColdStart \
    -rf json -rff cold-classic.json
```

Each run measures stock databind too. Divide each Blackbird score by the
stock score of the same run. A ratio across two runs includes any change
in the load of the host between the runs.

Do not give `-wi` or `-i` to `ColdStartBenchmark`. A warmup iteration
makes the measured iteration warm.

To see which beans get a generated codec, add
`-jvmArgsAppend -Dblackbird.debug.codegen=true`.

## Results

JMH 1.37, JDK 25.0.4, Linux on x86_64 (AMD Ryzen 7 5800X3D, 8 cores and 16
threads), with the options above and `-Xms512m -Xmx512m` in each fork.
Other processes used less than 0.4 CPU during the runs. Scores are ops/ms
with the 99.9% confidence interval. Classic is Blackbird 3.2.2, and new is
this build. Each Blackbird column has the stock score of its own run.

| Benchmark | Stock (classic run) | Classic | Stock (new run) | New |
|---|---:|---:|---:|---:|
| BuilderBenchmark.builderDeser | 2180 ± 20 | 2165 ± 57 | 2172 ± 29 | 2768 ± 33 |
| BuilderBenchmark.builderSer | 3287 ± 108 | 3857 ± 144 | 3185 ± 33 | 4446 ± 107 |
| PojoBenchmark.beanArrayDeser | 2.74 ± 0.04 | 2.93 ± 0.04 | 2.82 ± 0.03 | 3.34 ± 0.03 |
| PojoBenchmark.beanArraySer | 4.93 ± 0.06 | 5.03 ± 0.10 | 4.91 ± 0.06 | 5.59 ± 0.10 |
| PojoBenchmark.classicBeanDeser | 2.55 ± 0.05 | 2.68 ± 0.03 | 2.52 ± 0.07 | 3.45 ± 0.04 |
| PojoBenchmark.classicBeanSer | 4.15 ± 0.07 | 4.76 ± 0.06 | 4.18 ± 0.05 | 6.63 ± 0.12 |
| PojoBenchmark.constructorArrayDeser | 1.93 ± 0.02 | 2.42 ± 0.06 | 1.96 ± 0.01 | 2.00 ± 0.02 |
| PojoBenchmark.mediaItemDeser | 758 ± 17 | 766 ± 11 | 757 ± 13 | 950 ± 27 |
| PojoBenchmark.mediaItemSer | 1270 ± 24 | 1280 ± 5 | 1260 ± 26 | 1480 ± 22 |
| RecordBenchmark.recordDeser | 627 ± 14 | 843 ± 19 | 634 ± 7 | 1429 ± 14 |
| RecordBenchmark.recordSer | 1888 ± 55 | 2169 ± 79 | 1927 ± 69 | 2083 ± 8 |
| WideBeanBenchmark.wideDeser (40) | 555 ± 10 | 579 ± 10 | 555 ± 12 | 789 ± 5 |
| WideBeanBenchmark.wideDeser (200) | 91.4 ± 1.9 | 95.0 ± 2.3 | 93.3 ± 1.7 | 93.2 ± 1.3 |
| WideBeanBenchmark.wideSer (40) | 856 ± 13 | 1061 ± 12 | 867 ± 24 | 1482 ± 28 |
| WideBeanBenchmark.wideSer (200) | 143 ± 6 | 148 ± 6 | 142 ± 8 | 297 ± 7 |

`ColdStartBenchmark.firstUse`, ms for 500 types (lower is better): classic
run stock 384 ± 7 and classic 856 ± 10, new run stock 381 ± 6 and new
911 ± 14.

The new engine does not generate a reader for `BeanWithPropertyConstructor`
(property-based creator), so `constructorArrayDeser` measures the stock
deserializer in the new column. It also does not generate a reader for
`Wide200`: the generated method would be larger than 7000 bytes, which
HotSpot does not compile.

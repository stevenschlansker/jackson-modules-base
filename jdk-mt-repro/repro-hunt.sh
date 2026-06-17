#!/usr/bin/env bash
# Sustained reproduction hunt. Runs the most promising configurations in sequence
# and stops the instant any of them reproduces the invariant violation (exit 42).
# Concurrent collectors (generational ZGC, Shenandoah) process weak references
# concurrently with mutators, so Probe C (evictable re-intern) is most likely to
# expose a reference-processing race there. The launch storm covers the
# startup/first-linkage window the field reports point at.
set -u
JAVA="${JAVA:-/usr/lib/jvm/java-21-openjdk/bin/java}"
HERE="$(cd "$(dirname "$0")" && pwd)"
OPENS=(--add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/jdk.internal.util=ALL-UNNAMED)

run() {
  local name="$1"; shift
  echo "######## $name : $* ########"
  "$JAVA" "${OPENS[@]}" -cp "$HERE" "$@" MethodTypeInternStress
  local rc=$?
  if [ "$rc" = "42" ]; then echo "!!!! REPRODUCED under $name !!!!"; exit 42; fi
  echo "---- $name done (rc=$rc) ----"
}

# Class-metadata headroom for the link storm (it spins a fresh class per iteration).
META="-XX:CompressedClassSpaceSize=512m -XX:MaxMetaspaceSize=1g"

# Phase 1: G1 (the collector the field reports actually ran on), Probe C heavy.
run "g1/probeC/space16k" -Xmx1g -Xms1g $META -XX:+UseG1GC -XX:+ParallelRefProcEnabled \
    -Dmt.durationSec=240 -Dmt.threads=10 -Dmt.probeCThreads=10 -Dmt.probeCSpace=16384

# Phase 2: G1, small young gen => very frequent collections, concurrent class unloading.
# (G1NewSizePercent is experimental, so UnlockExperimentalVMOptions must precede it.)
run "g1/probeC/youngsmall" -Xmx1g -Xms1g $META -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=5 -XX:G1MaxNewSizePercent=10 \
    -XX:+ClassUnloadingWithConcurrentMark -XX:+ExplicitGCInvokesConcurrent \
    -Dmt.durationSec=240 -Dmt.threads=10 -Dmt.probeCThreads=10 -Dmt.probeCSpace=65536 -Dmt.sysgcMs=2

# Phase 3: generational ZGC (concurrent reference processing), Probe C heavy.
run "zgc-gen/probeC/space16k" -Xmx1g -Xms1g $META -XX:+UseZGC -XX:+ZGenerational \
    -Dmt.durationSec=180 -Dmt.threads=10 -Dmt.probeCThreads=10 -Dmt.probeCSpace=16384

# Phase 4: Shenandoah (concurrent reference processing).
run "shenandoah/probeC/space16k" -Xmx1g -Xms1g $META -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC \
    -Dmt.durationSec=180 -Dmt.threads=10 -Dmt.probeCThreads=10 -Dmt.probeCSpace=16384

echo "######## launch storm: G1, 120 short JVMs (first-time linkage at warmup) ########"
i=0
while [ "$i" -lt 120 ]; do
  out="$("$JAVA" "${OPENS[@]}" -Xmx1g -Xms1g $META -XX:+UseG1GC \
        -Dmt.durationSec=3 -Dmt.threads=6 -Dmt.probeCThreads=6 -Dmt.probeCSpace=8192 \
        -Dmt.linkStormThreads=4 -cp "$HERE" MethodTypeInternStress 2>&1)"
  if [ "$?" = "42" ]; then echo "!!!! REPRODUCED in launch storm (run $i) !!!!"; echo "$out"; exit 42; fi
  i=$((i+1))
  [ $((i % 20)) -eq 0 ] && echo "  launch storm: $i/120"
done

echo "######## HUNT COMPLETE: no violation reproduced ########"
exit 0

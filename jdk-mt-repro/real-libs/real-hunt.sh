#!/usr/bin/env bash
# Reproduction hunt using the REAL libraries (Caffeine 3.1.7, Blackbird/Jackson 2.12.7)
# on the EXACT build from caffeine#1111: Amazon Corretto 17.0.8.7.1.
#
# Structure:
#  - Long phases are Caffeine-HEAVY. Caffeine.build() runs the exact #1111 path
#    (NodeFactory.newFactory: fresh MethodType via changeReturnType/asType + invokeExact)
#    and is sustainable at ~12M builds/s with bounded factory classes (no metaspace growth).
#    A small, slow Blackbird presence (few threads, high mapperRefresh) rides along without
#    exhausting metaspace.
#  - The launch storm covers Blackbird faithfully: short JVMs do fresh-mapper first-use
#    serialization (the jackson-modules-base#142 startup scenario) and exit before hidden
#    lambda classes accumulate.
#
# Exit 42 = WrongMethodTypeException reproduced; 3 = resource; 0 = held.
set -u
JAVA="${JAVA:-/tmp/jdk1708/bin/java}"
HERE="$(cd "$(dirname "$0")" && pwd)"
CP="$HERE/out:$HERE/lib/caffeine-3.1.7.jar:$HERE/lib/blackbird.jar:$HERE/lib/databind.jar:$HERE/lib/core.jar:$HERE/lib/annotations.jar"
META="-XX:CompressedClassSpaceSize=1g -XX:MaxMetaspaceSize=2g -XX:+ClassUnloadingWithConcurrentMark"

run() {
  local name="$1"; shift
  echo "######## $name : $* ########"
  "$JAVA" -cp "$CP" "$@" RealLibStress
  local rc=$?
  if [ "$rc" = "42" ]; then echo "!!!! REPRODUCED under $name !!!!"; exit 42; fi
  if [ "$rc" -ge 128 ] 2>/dev/null; then echo "!!!! VM ABORT rc=$rc under $name !!!!"; exit "$rc"; fi
  echo "---- $name done (rc=$rc) ----"
}

"$JAVA" -version 2>&1 | sed 's/^/  /'

# --- Long phases: Caffeine-only (the #1111 path; sustainable, no metaspace growth). ---
# Phase 1: G1 (production default), oversubscribed, long.
run "g1/caffeine" -Xmx1g -Xms1g $META -XX:+UseG1GC \
    -Dreal.durationSec=300 -Dreal.caffeineThreads=16 -Dreal.blackbirdThreads=0

# Phase 2: G1, small young gen + concurrent class unloading (factory class churn).
run "g1/youngsmall" -Xmx1g -Xms1g $META -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions \
    -XX:G1NewSizePercent=5 -XX:G1MaxNewSizePercent=10 \
    -Dreal.durationSec=240 -Dreal.caffeineThreads=14 -Dreal.blackbirdThreads=0

# Phase 3: ZGC (concurrent ref processing) if available.
if "$JAVA" -XX:+UseZGC -version >/dev/null 2>&1; then
  run "zgc/caffeine" -Xmx1g -Xms1g $META -XX:+UseZGC \
      -Dreal.durationSec=180 -Dreal.caffeineThreads=14 -Dreal.blackbirdThreads=0
fi

# Phase 4: Shenandoah if available.
if "$JAVA" -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC -version >/dev/null 2>&1; then
  run "shenandoah/caffeine" -Xmx1g -Xms1g $META -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC \
      -Dreal.durationSec=180 -Dreal.caffeineThreads=14 -Dreal.blackbirdThreads=0
fi

# --- Launch storms: short JVMs, warmup first-use. This is the faithful BLACKBIRD test
#     (#142 was concurrent startup first-use), and also covers Caffeine warmup. Short JVM
#     lifetime means fresh-mapper first-linkage with no metaspace accumulation. ---
echo "######## launch storm A: both libs, 500 short JVMs (4s each) ########"
i=0
while [ "$i" -lt 500 ]; do
  out="$("$JAVA" -cp "$CP" -Xmx512m -Xms512m $META -XX:+UseG1GC \
        -Dreal.durationSec=4 -Dreal.caffeineThreads=4 -Dreal.blackbirdThreads=4 \
        -Dreal.mapperRefresh=8 -Dreal.freshMapper=true RealLibStress 2>&1)"
  rc=$?
  if [ "$rc" = "42" ]; then echo "!!!! REPRODUCED in launch storm A (run $i) !!!!"; echo "$out"; exit 42; fi
  i=$((i+1))
  [ $((i % 50)) -eq 0 ] && echo "  launch storm A: $i/500"
done

echo "######## launch storm B: Blackbird-focused, 500 short JVMs (3s, fresh mapper per op) ########"
i=0
while [ "$i" -lt 500 ]; do
  out="$("$JAVA" -cp "$CP" -Xmx512m -Xms512m $META -XX:+UseG1GC \
        -Dreal.durationSec=3 -Dreal.caffeineThreads=1 -Dreal.blackbirdThreads=6 \
        -Dreal.mapperRefresh=1 -Dreal.freshMapper=true RealLibStress 2>&1)"
  rc=$?
  if [ "$rc" = "42" ]; then echo "!!!! REPRODUCED in launch storm B (run $i) !!!!"; echo "$out"; exit 42; fi
  i=$((i+1))
  [ $((i % 50)) -eq 0 ] && echo "  launch storm B: $i/500"
done

echo "######## REAL-LIB HUNT COMPLETE (Corretto 17.0.8): no reproduction ########"
exit 0

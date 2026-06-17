#!/usr/bin/env bash
# Reproduction hunt against JDK 17 (Corretto), the version family the field reports
# ran on, which still has the old ConcurrentWeakInternSet intern implementation.
# Source analysis shows that logic is race-free for live duplicates, so this targets
# the residual sub-Java (GC / JIT) manifestation under the *production* collector (G1).
#
# Tuning rationale:
#  - small mt.probeCSpace  => many threads contend on the SAME few intern entries
#    (maximises concurrent intern + eviction on identical bins);
#  - thread oversubscription (more threads than cores) => frequent preemption in the
#    middle of the non-atomic intern sequence, widening any race window.
set -u
JAVA="${JAVA:-/tmp/jdk17/bin/java}"
HERE="$(cd "$(dirname "$0")" && pwd)"
OPENS=(--add-opens java.base/java.lang.invoke=ALL-UNNAMED)
META="-XX:CompressedClassSpaceSize=512m -XX:MaxMetaspaceSize=1g"

run() {
  local name="$1"; shift
  echo "######## $name : $* ########"
  "$JAVA" "${OPENS[@]}" -cp "$HERE" "$@" MethodTypeInternStress
  local rc=$?
  if [ "$rc" = "42" ]; then echo "!!!! REPRODUCED under $name (jdk17) !!!!"; exit 42; fi
  echo "---- $name done (rc=$rc) ----"
}

"$JAVA" -version 2>&1 | sed 's/^/  /'

# Phase 1: G1 (production collector), maximal contention + oversubscription.
run "g1/contention/space512/24t" -Xmx1g -Xms1g $META -XX:+UseG1GC -XX:+ParallelRefProcEnabled \
    -Dmt.durationSec=300 -Dmt.threads=24 -Dmt.probeCThreads=24 -Dmt.probeCSpace=512

# Phase 2: G1, small young gen + frequent System.gc(), medium space.
run "g1/youngsmall/space4k" -Xmx1g -Xms1g $META -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions \
    -XX:G1NewSizePercent=5 -XX:G1MaxNewSizePercent=10 -XX:+ClassUnloadingWithConcurrentMark \
    -Dmt.durationSec=240 -Dmt.threads=20 -Dmt.probeCThreads=20 -Dmt.probeCSpace=4096 -Dmt.sysgcMs=1

# Phase 3: ZGC (concurrent reference processing on 17, single generation).
run "zgc/space16k" -Xmx1g -Xms1g $META -XX:+UseZGC \
    -Dmt.durationSec=180 -Dmt.threads=16 -Dmt.probeCThreads=16 -Dmt.probeCSpace=16384

# Phase 4: Shenandoah (concurrent reference processing).
run "shenandoah/space16k" -Xmx1g -Xms1g $META -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC \
    -Dmt.durationSec=180 -Dmt.threads=16 -Dmt.probeCThreads=16 -Dmt.probeCSpace=16384

echo "######## launch storm: G1, 100 short JVMs (warmup-time first-linkage) ########"
i=0
while [ "$i" -lt 100 ]; do
  out="$("$JAVA" "${OPENS[@]}" -Xmx1g -Xms1g $META -XX:+UseG1GC \
        -Dmt.durationSec=3 -Dmt.threads=8 -Dmt.probeCThreads=8 -Dmt.probeCSpace=2048 \
        -Dmt.linkStormThreads=4 -cp "$HERE" MethodTypeInternStress 2>&1)"
  if [ "$?" = "42" ]; then echo "!!!! REPRODUCED in launch storm (jdk17 run $i) !!!!"; echo "$out"; exit 42; fi
  i=$((i+1))
  [ $((i % 20)) -eq 0 ] && echo "  launch storm: $i/100"
done

echo "######## JDK17 HUNT COMPLETE: no violation reproduced ########"
exit 0

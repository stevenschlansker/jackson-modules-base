#!/usr/bin/env bash
# JIT-focused hunt: run JitInvokeExactStress under a FASTDEBUG JVM with C2 stress flags
# (StressLCM/StressGCM/StressIGVN randomise scheduling/IGVN; DeoptimizeALot churns
# recompilation). These are 'develop' flags, available only in a debug VM. The -Xint
# phase is the control: it must always hold. A C2 miscompile of invokeExact's identity
# check would fail under the compiled phases but never under -Xint.
#
# Exit: 42 = invokeExact identity violation; >=128 = VM assert/crash; 0 = held.
set -u
JAVA="${JAVA:-/usr/lib/jvm/java-21-openjdk-fastdebug/bin/java}"
HERE="$(cd "$(dirname "$0")" && pwd)"
DIAG="-XX:+UnlockDiagnosticVMOptions"
META="-XX:CompressedClassSpaceSize=512m -XX:MaxMetaspaceSize=1g"

run() {
  local name="$1"; shift
  echo "######## $name : $* ########"
  "$JAVA" $DIAG $META -cp "$HERE" "$@" JitInvokeExactStress
  local rc=$?
  if [ "$rc" = "42" ]; then echo "!!!! JIT REPRODUCED (identity violation) under $name !!!!"; exit 42; fi
  if [ "$rc" -ge 128 ] 2>/dev/null; then echo "!!!! VM ABORT (rc=$rc) under $name !!!!"; exit "$rc"; fi
  echo "---- $name done (rc=$rc) ----"
}

"$JAVA" -version 2>&1 | sed 's/^/  /'

# Control: interpreted. Must hold. If this ever fails, the bug is NOT the JIT.
run "xint-control" -Xint -Djit.durationSec=60 -Djit.threads=4

# C2 scheduling/IGVN stress + recompilation churn.
run "c2-stress+deopt" -XX:+StressLCM -XX:+StressGCM -XX:+StressIGVN -XX:+DeoptimizeALot \
    -Djit.durationSec=180 -Djit.threads=8 -Djit.rebuildShift=16

# Straight to C2 (no tier-1), heavy deopt churn, frequent handle rebuild.
run "c2only+deopt+rebuild" -XX:-TieredCompilation -XX:+DeoptimizeALot \
    -Djit.durationSec=180 -Djit.threads=8 -Djit.rebuildShift=12

# C2 stress, more threads, rare rebuild (let sites stay hot/compiled longer).
run "c2-stress-hot" -XX:+StressLCM -XX:+StressGCM -XX:+StressIGVN \
    -Djit.durationSec=180 -Djit.threads=12 -Djit.rebuildShift=22

echo "######## JIT FASTDEBUG HUNT COMPLETE: no violation, no VM assert ########"
exit 0

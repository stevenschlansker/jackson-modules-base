#!/usr/bin/env bash
# JIT-focused hunt on a PRODUCT VM (no fastdebug available on this x86_64 host).
# The original jit-debug-hunt.sh needs a fastdebug VM for the C2 'develop' stress
# flags + DeoptimizeALot. This variant runs the SAME JitInvokeExactStress harness
# but uses only the C2 scheduling-stress flags that this build exposes as DIAGNOSTIC
# (StressLCM/StressGCM/StressIGVN). DeoptimizeALot is develop-only and omitted; we
# substitute heavy recompilation churn via frequent handle rebuild (jit.rebuildShift)
# and a C2-only (no tiered) phase. -Xint is the control: it must always hold.
#
# Exit: 42 = invokeExact identity violation; >=128 = VM abort; 0 = held.
set -u
JAVA="${JAVA:-$(command -v java)}"
HERE="$(cd "$(dirname "$0")" && pwd)"
DIAG="-XX:+UnlockDiagnosticVMOptions"
META="-XX:CompressedClassSpaceSize=512m -XX:MaxMetaspaceSize=1g"

run() {
  local name="$1"; shift
  echo "######## $name : $* ########"
  "$JAVA" $META -cp "$HERE" "$@" JitInvokeExactStress
  local rc=$?
  if [ "$rc" = "42" ]; then echo "!!!! JIT REPRODUCED (identity violation) under $name !!!!"; exit 42; fi
  if [ "$rc" -ge 128 ] 2>/dev/null; then echo "!!!! VM ABORT (rc=$rc) under $name !!!!"; exit "$rc"; fi
  echo "---- $name done (rc=$rc) ----"
}

"$JAVA" -version 2>&1 | sed 's/^/  /'

# Control: interpreted. Must hold. If this ever fails, the bug is NOT the JIT.
run "xint-control" -Xint -Djit.durationSec=60 -Djit.threads=4

# C2 scheduling/IGVN stress (diagnostic on this build) + frequent rebuild churn.
run "c2-stress+rebuild" $DIAG -XX:+StressLCM -XX:+StressGCM -XX:+StressIGVN \
    -Djit.durationSec=180 -Djit.threads=8 -Djit.rebuildShift=14

# Straight to C2 (no tier-1), frequent handle rebuild => recompilation churn.
run "c2only+rebuild" -XX:-TieredCompilation \
    -Djit.durationSec=180 -Djit.threads=8 -Djit.rebuildShift=12

# C2 stress, oversubscribed threads, rare rebuild (sites stay hot/compiled longer).
run "c2-stress-hot" $DIAG -XX:+StressLCM -XX:+StressGCM -XX:+StressIGVN \
    -Djit.durationSec=180 -Djit.threads=12 -Djit.rebuildShift=22

echo "######## JIT PRODUCT HUNT COMPLETE: no violation, no VM abort ########"
exit 0

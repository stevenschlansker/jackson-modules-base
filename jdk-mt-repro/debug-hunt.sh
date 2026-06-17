#!/usr/bin/env bash
# Run the intern stress harness under a FASTDEBUG JVM with GC verification enabled.
# Rationale: the intern logic is provably race-free, so any real failure is a VM-level
# reference-processing/JIT inconsistency. A product VM tolerates a rare inconsistency
# silently; a fastdebug VM has internal asserts, and the verification flags fully verify
# the heap / reference state around every GC. Either should abort precisely on a real
# inconsistency. Throughput is ~40x lower than product, which is fine: we want many
# *verified* GC cycles, not raw iteration count.
#
# A fastdebug abort (assert/SIGABRT, exit ~134) or a Probe violation (exit 42) is a hit.
set -u
JAVA="${JAVA:-/usr/lib/jvm/java-21-openjdk-fastdebug/bin/java}"
HERE="$(cd "$(dirname "$0")" && pwd)"
OPENS=(--add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/jdk.internal.util=ALL-UNNAMED)
DIAG="-XX:+UnlockDiagnosticVMOptions"

run() {
  local name="$1"; shift
  echo "######## $name : $* ########"
  "$JAVA" "${OPENS[@]}" $DIAG -cp "$HERE" "$@" MethodTypeInternStress
  local rc=$?
  if [ "$rc" = "42" ]; then echo "!!!! REPRODUCED (probe violation) under $name !!!!"; exit 42; fi
  if [ "$rc" -ge 128 ] 2>/dev/null; then echo "!!!! VM ABORT (signal/assert, rc=$rc) under $name -- inspect hs_err / stderr !!!!"; exit "$rc"; fi
  echo "---- $name done (rc=$rc) ----"
}

"$JAVA" -version 2>&1 | sed 's/^/  /'

# Phase 1: G1 with full heap verification before and after every GC.
run "fastdebug/g1/verify" -Xmx1g -Xms1g -XX:+UseG1GC -XX:+VerifyBeforeGC -XX:+VerifyAfterGC \
    -Dmt.durationSec=150 -Dmt.threads=6 -Dmt.probeCThreads=6 -Dmt.probeCSpace=4096 -Dmt.linkStorm=false

# Phase 2: Shenandoah with full verification (level 4) -- concurrent ref processing.
run "fastdebug/shenandoah/verify" -Xmx1g -Xms1g -XX:+UseShenandoahGC -XX:+ShenandoahVerify \
    -Dmt.durationSec=150 -Dmt.threads=6 -Dmt.probeCThreads=6 -Dmt.probeCSpace=4096 -Dmt.linkStorm=false

# Phase 3: generational ZGC with object/forwarding verification -- concurrent ref processing.
run "fastdebug/zgc/verify" -Xmx1g -Xms1g -XX:+UseZGC -XX:+ZGenerational -XX:+ZVerifyObjects -XX:+ZVerifyForwarding \
    -Dmt.durationSec=150 -Dmt.threads=6 -Dmt.probeCThreads=6 -Dmt.probeCSpace=16384 -Dmt.linkStorm=false

# Phase 4: G1 with asserts only (no heap verify, so faster), link storm ON to exercise
# first-time call-site linkage + lambda class spinning under fastdebug asserts.
run "fastdebug/g1/linkstorm" -Xmx1g -Xms1g -XX:CompressedClassSpaceSize=512m -XX:MaxMetaspaceSize=1g -XX:+UseG1GC \
    -Dmt.durationSec=150 -Dmt.threads=8 -Dmt.probeCThreads=8 -Dmt.probeCSpace=4096 -Dmt.linkStormThreads=4

echo "######## FASTDEBUG HUNT COMPLETE: no violation, no VM assert ########"
exit 0

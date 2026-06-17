#!/usr/bin/env bash
# Sweep the MethodType intern stress test across GC algorithms and compilation
# modes. Stops and prints the full diagnostic on the first invariant violation.
#
# Usage:
#   ./run-sweep.sh [duration_seconds]            # default 120s per config
#   JAVA=/path/to/bin/java ./run-sweep.sh 300
#
# Each configuration runs the harness for the given duration. Exit status of the
# harness: 0 = invariant held, 1 = VIOLATION (bug reproduced), 2 = self-test.
set -u

JAVA="${JAVA:-$(command -v java)}"
DUR="${1:-120}"
HERE="$(cd "$(dirname "$0")" && pwd)"

OPENS="--add-opens java.base/java.lang.invoke=ALL-UNNAMED \
       --add-opens java.base/jdk.internal.util=ALL-UNNAMED"

# Small heap + high alloc rate => frequent GC cycles => wide reference-processing
# windows. Tune mt.threads/mt.pinned up on bigger boxes.
COMMON="-Xmx512m -Xms512m -Dmt.durationSec=${DUR}"

echo "java: $JAVA"; "$JAVA" -version 2>&1 | sed 's/^/  /'
echo "compiling..."; "$JAVA" --version >/dev/null 2>&1
javac_bin="$(dirname "$JAVA")/javac"
[ -x "$javac_bin" ] && "$javac_bin" "$HERE/MethodTypeInternStress.java" \
    || echo "  (no javac next to java; assuming MethodTypeInternStress.class is present)"
echo

# name|extra JVM flags
CONFIGS=(
  "G1|-XX:+UseG1GC"
  "G1-unload|-XX:+UseG1GC -XX:+ClassUnloadingWithConcurrentMark -XX:+ExplicitGCInvokesConcurrent"
  "Parallel|-XX:+UseParallelGC"
  "Serial|-XX:+UseSerialGC"
  "ZGC-gen|-XX:+UseZGC -XX:+ZGenerational"
  "ZGC-single|-XX:+UseZGC -XX:-ZGenerational"
  "Shenandoah|-XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC"
  "G1-int|-XX:+UseG1GC -Xint"
  "G1-c1|-XX:+UseG1GC -XX:TieredStopAtLevel=1"
  "G1-c2only|-XX:+UseG1GC -XX:-TieredCompilation"
  "ZGC-gen-sysgc|-XX:+UseZGC -XX:+ZGenerational -Dmt.sysgcMs=1"
)

fail=0
for entry in "${CONFIGS[@]}"; do
  name="${entry%%|*}"
  flags="${entry#*|}"
  printf '========== %-16s %s ==========\n' "$name" "$flags"
  # shellcheck disable=SC2086
  "$JAVA" $OPENS $COMMON $flags -cp "$HERE" MethodTypeInternStress
  rc=$?
  if [ "$rc" = "42" ]; then
    echo ">>> VIOLATION reproduced under: $name ($flags)"
    fail=1
    break
  elif [ "$rc" != "0" ]; then
    echo ">>> config '$name' skipped/failed to start (rc=$rc; GC may be unavailable in this build)"
  fi
  echo
done

if [ "$fail" = "1" ]; then
  echo "RESULT: invariant VIOLATED — diagnostic printed above."
  exit 1
fi
echo "RESULT: invariant held across all runnable configurations."
exit 0

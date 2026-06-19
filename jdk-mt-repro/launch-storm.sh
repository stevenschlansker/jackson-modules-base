#!/usr/bin/env bash
# The field reports describe ~10 failures across *thousands of JVM launches*,
# clustered at cache/serializer build time. That suggests first-time linkage
# during warmup is the trigger, so launching many short-lived JVMs may catch the
# bug faster than one long run. This script does exactly that.
#
# Usage:
#   ./launch-storm.sh [num_launches] [seconds_each] [parallelism]
#   JAVA=/path/to/bin/java GC="-XX:+UseZGC" ./launch-storm.sh 2000 4 8
set -u

JAVA="${JAVA:-$(command -v java)}"
N="${1:-1000}"
SECS="${2:-4}"
PAR="${3:-$(getconf _NPROCESSORS_ONLN 2>/dev/null || echo 4)}"
GC="${GC:--XX:+UseG1GC}"
HERE="$(cd "$(dirname "$0")" && pwd)"
OUT="${OUT:-$HERE/out}"
# Heap sizing; default matches the field report (tiny initial, large max).
HEAP="${HEAP:--Xms32m -Xmx6g}"

OPENS="--add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/jdk.internal.util=ALL-UNNAMED"
# Bias each short JVM toward warmup-time linkage churn under GC pressure.
PROPS="-Dmt.durationSec=${SECS} -Dmt.linkStormThreads=4 -Dmt.pinned=2048 -Dmt.threads=6"

echo "launch storm: N=$N secs=$SECS parallelism=$PAR gc=$GC"
echo "java: $JAVA"
hits=0
run_one() {
  # shellcheck disable=SC2086
  out="$("$JAVA" $OPENS $HEAP $GC $PROPS -cp "$OUT" MethodTypeInternStress 2>&1)"
  rc=$?
  if [ "$rc" = "42" ]; then
    echo "=========== VIOLATION (launch $1) ==========="
    echo "$out"
    echo "$out" > "$HERE/violation-$$-$1.log"
    return 1
  fi
  return 0
}
export -f run_one
export JAVA OPENS PROPS GC HEAP OUT HERE

# Run with bounded parallelism; abort the whole storm on first violation.
seq 1 "$N" | xargs -P "$PAR" -I{} bash -c 'run_one {} || exit 255' 2>/dev/null
status=$?
if [ "$status" != "0" ]; then
  echo "RESULT: violation reproduced — see violation-*.log in $HERE"
  exit 1
fi
echo "RESULT: $N launches completed, no violation."
exit 0

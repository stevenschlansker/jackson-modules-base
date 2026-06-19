#!/usr/bin/env bash
# Storm launcher for the concurrent-first-link DIVERSITY harness.
#
# The Blackbird WrongMethodTypeException (jackson-modules-base#142) is a durable
# COLD-START failure -- the field signal is per-JVM-launch, not per-op. So we launch
# many short JVMs, each of which does ONE concurrent first-link burst over the full
# diverse bean population and exits. Aborts the whole storm on the first reproduction
# (exit 42) or on any JVM that dies on a signal (exit >= 128).
#
# Usage:
#   ./storm.sh                 # defaults: N=2000 launches, PAR=4 parallel
#   N=5000 PAR=8 ./storm.sh
#   JAVA=/path/to/bin/java GC="-XX:+UseZGC" HEAP="-Xms32m -Xmx4g" ./storm.sh
#
# Env vars:
#   JAVA  java binary (default: fastdebug 21, falls back to PATH java)
#   N     number of JVM launches (default 2000)
#   PAR   parallel launches (default 4)
#   GC    GC flags (default Shenandoah, matching the field/handoff config)
#   HEAP  heap flags (default "-Xms32m -Xmx6g", matching the field report's tiny->large)
set -u

HERE="$(cd "$(dirname "$0")" && pwd)"
LIB="$HERE/../real-libs/lib"
CP="$HERE/out:$LIB/blackbird.jar:$LIB/databind.jar:$LIB/core.jar:$LIB/annotations.jar"

FASTDEBUG="/usr/lib/jvm/java-21-openjdk-fastdebug/bin/java"
JAVA="${JAVA:-$([ -x "$FASTDEBUG" ] && echo "$FASTDEBUG" || command -v java)}"
N="${N:-2000}"
PAR="${PAR:-4}"
GC="${GC:--XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC}"
HEAP="${HEAP:--Xms32m -Xmx6g}"

OPENS="--add-opens java.base/java.lang.invoke=ALL-UNNAMED"
PROPS="${PROPS:-}"

echo "diversity storm: N=$N parallelism=$PAR"
echo "java: $JAVA"
echo "gc:   $GC"
echo "heap: $HEAP"

run_one() {
  # shellcheck disable=SC2086
  out="$("$JAVA" $OPENS $HEAP $GC $PROPS -cp "$CP" Main 2>&1)"
  rc=$?
  if [ "$rc" = "42" ]; then
    echo "=========== REPRODUCED (launch $1) ==========="
    echo "$out"
    echo "$out" > "$HERE/violation-$$-$1.log"
    return 1
  fi
  if [ "$rc" -ge 128 ]; then
    echo "=========== JVM died on signal rc=$rc (launch $1) ==========="
    echo "$out"
    echo "$out" > "$HERE/crash-$$-$1.log"
    return 1
  fi
  return 0
}
export -f run_one
export JAVA OPENS HEAP GC PROPS CP HERE

# Bounded parallelism; abort the whole storm on the first reproduction/crash.
seq 1 "$N" | xargs -P "$PAR" -I{} bash -c 'run_one {} || exit 255' 2>/dev/null
status=$?
if [ "$status" != "0" ]; then
  echo "RESULT: reproduced or crashed -- see violation-*.log / crash-*.log in $HERE"
  exit 1
fi
echo "RESULT: $N launches completed, no reproduction."
exit 0

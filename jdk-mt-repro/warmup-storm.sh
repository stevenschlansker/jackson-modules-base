#!/usr/bin/env bash
# Warmup-storm: many short JVMs, each maximising DIVERSE MethodType intern-table churn
# during startup (the field signal: ~10 failures in thousands of JVM launches, at warmup).
# Each JVM spins many distinct LambdaMetafactory sites + invokedynamic string concat +
# MethodHandle lookups concurrently, with an invokeExact identity check at each lambda site.
# Short lifetime avoids the metaspace exhaustion a long single run hits.
#
# Usage: [JAVA=...] [N=2000] [SECS=4] [PAR=4] [GC=-XX:+UseG1GC] ./warmup-storm.sh
# Exit 42 = reproduced; >=128 = VM assert (fastdebug); 0 = all launches held.
set -u
JAVA="${JAVA:-$(command -v java)}"
HERE="$(cd "$(dirname "$0")" && pwd)"
N="${N:-2000}"; SECS="${SECS:-4}"; PAR="${PAR:-4}"; GC="${GC:--XX:+UseG1GC}"
OPENS="--add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/jdk.internal.util=ALL-UNNAMED"
META="-XX:CompressedClassSpaceSize=1g -XX:MaxMetaspaceSize=2g -XX:+ClassUnloadingWithConcurrentMark"

# Build classpath dir (expects WarmupChurn.class next to this script's compiled output).
OUT="${OUT:-$HERE/out}"
if [ ! -f "$OUT/WarmupChurn.class" ]; then
  echo "compiling WarmupChurn into $OUT ..."
  mkdir -p "$OUT"
  "$(dirname "$JAVA")/javac" -d "$OUT" "$HERE/WarmupChurn.java" || { echo "compile failed"; exit 1; }
fi

echo "warmup-storm: N=$N secs=$SECS par=$PAR gc=$GC java=$JAVA"
"$JAVA" -version 2>&1 | sed 's/^/  /'

one() {
  # shellcheck disable=SC2086
  out="$("$JAVA" $OPENS $META $GC -Xmx512m -Xms512m -cp "$OUT" \
        -Dwarm.durationSec=$SECS -Dwarm.threads=6 WarmupChurn 2>&1)"
  rc=$?
  if [ "$rc" = "42" ] || [ "$rc" -ge 128 ] 2>/dev/null; then
    echo "=========== HIT (launch $1, rc=$rc) ==========="
    echo "$out"
    echo "$out" > "$HERE/warmup-hit-$$-$1.log"
    return 1
  fi
  return 0
}
export -f one; export JAVA OPENS META GC OUT SECS HERE

seq 1 "$N" | xargs -P "$PAR" -I{} bash -c 'one {} || exit 255' 2>/dev/null
if [ "$?" != "0" ]; then echo "RESULT: reproduced — see warmup-hit-*.log in $HERE"; exit 42; fi
echo "RESULT: $N launches, no reproduction."
exit 0

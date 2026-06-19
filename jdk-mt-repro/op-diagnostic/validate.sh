#!/usr/bin/env bash
#
# Validates wmtediag-agent.jar by fabricating the exact WrongMethodTypeException the bug produces
# and confirming the agent writes a dump with the intern-table identity, both type identities,
# thread, and stack.
#
# Usage:  ./validate.sh [path-to-java]
# Default java: the fastdebug JDK 21 used during development.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AGENT="$HERE/wmtediag-agent.jar"
TESTSRC="$HERE/test/WmteAgentSelfTest.java"
TESTOUT="$HERE/build/test"
DUMP="${WMTEDIAG_DUMP:-$HERE/build/wmtediag-validate.log}"

JAVA="${1:-/usr/lib/jvm/java-21-openjdk-fastdebug/bin/java}"
JAVAC="${JAVAC:-javac}"

[ -f "$AGENT" ] || { echo "agent jar missing; run ./build.sh first" >&2; exit 1; }

mkdir -p "$TESTOUT"
"$JAVAC" --release 21 -d "$TESTOUT" "$TESTSRC"
rm -f "$DUMP"

echo "[validate] java: $("$JAVA" -version 2>&1 | head -1)"
"$JAVA" \
  -javaagent:"$AGENT" \
  -Dwmtediag.output="$DUMP" \
  --add-opens java.base/java.lang.invoke=ALL-UNNAMED \
  --add-opens java.base/jdk.internal.util=ALL-UNNAMED \
  -cp "$TESTOUT" \
  WmteAgentSelfTest

echo
echo "===================== captured dump ($DUMP) ====================="
cat "$DUMP"

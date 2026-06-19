#!/usr/bin/env bash
#
# Builds wmtediag-agent.jar — a self-contained java agent (no runtime deps) that
# captures decisive evidence for FasterXML/jackson-modules-base#142.
#
# Reproducible build. Requires:
#   - a JDK 21 javac (we use --release 21)
#   - ASM 9.x classes to bundle (shaded) into the agent jar
#
# Override the toolchain by exporting JAVAC / JAR / ASM_JAR before running.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC="$HERE/src"
BUILD="$HERE/build"
OUT="$HERE/wmtediag-agent.jar"

JAVAC="${JAVAC:-javac}"
JAR="${JAR:-jar}"
# ASM is needed only at BUILD time (to write the transformer) and is bundled into the
# agent jar so the OP needs nothing on their classpath. Default to the copy maven ships.
ASM_JAR="${ASM_JAR:-/opt/apache-maven/lib/asm-9.9.1.jar}"

echo "[build] javac : $("$JAVAC" -version 2>&1)"
echo "[build] asm   : $ASM_JAR"

if [ ! -f "$ASM_JAR" ]; then
  echo "[build] ERROR: ASM jar not found at $ASM_JAR (set ASM_JAR=/path/to/asm-9.x.jar)" >&2
  exit 1
fi

rm -rf "$BUILD/classes" "$BUILD/MANIFEST.MF"
mkdir -p "$BUILD/classes"

echo "[build] compiling sources (--release 21)"
"$JAVAC" --release 21 \
  -cp "$ASM_JAR" \
  -d "$BUILD/classes" \
  "$SRC/com/wholesail/wmtediag/WmteDiagnostics.java" \
  "$SRC/com/wholesail/wmtediag/WmteAgent.java"

echo "[build] unpacking ASM (org/objectweb/asm) for shading"
( cd "$BUILD/classes" && "$JAR" xf "$ASM_JAR" org/objectweb/asm )
# Drop ASM's own manifest/metadata so it does not collide with ours.
rm -rf "$BUILD/classes/META-INF"

echo "[build] writing manifest"
cat > "$BUILD/MANIFEST.MF" <<'EOF'
Manifest-Version: 1.0
Premain-Class: com.wholesail.wmtediag.WmteAgent
Agent-Class: com.wholesail.wmtediag.WmteAgent
Can-Retransform-Classes: true
Can-Redefine-Classes: true
Boot-Class-Path: wmtediag-agent.jar
Implementation-Title: wmtediag-agent
Implementation-Vendor: Wholesail
EOF

echo "[build] assembling $OUT"
"$JAR" cfm "$OUT" "$BUILD/MANIFEST.MF" -C "$BUILD/classes" .

echo "[build] done: $OUT"
echo "[build] agent classes in jar:"
"$JAR" tf "$OUT" | grep -E 'wmtediag|MANIFEST' | sort
echo "[build] ASM classes bundled: $("$JAR" tf "$OUT" | grep -c '^org/objectweb/asm/')"

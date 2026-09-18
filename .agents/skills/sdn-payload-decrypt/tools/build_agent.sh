#!/bin/zsh
# Build sdn-capture-agent.jar: agent classes + ASM shaded in (the agent runs
# before the app classpath exists, so it must carry its own ASM).
set -e
cd "$(dirname "$0")"

R="${R:-$HOME/DreamBot/BotData/repository2}"
[ -f "$R/asm-8.0.1.jar" ] || { echo "asm-8.0.1.jar not found in $R"; exit 1; }

BUILD=$(mktemp -d)
trap 'rm -rf "$BUILD"' EXIT

mkdir -p "$BUILD/classes"
javac -cp "$R/asm-8.0.1.jar:$R/asm-tree-8.0.1.jar" CaptureAgent.java CaptureHook.java -d "$BUILD/classes"

# shade ASM (core + tree) into the agent jar
mkdir -p "$BUILD/asm"
unzip -q -o "$R/asm-8.0.1.jar"      -d "$BUILD/asm"
unzip -q -o "$R/asm-tree-8.0.1.jar" -d "$BUILD/asm"
rm -rf "$BUILD/asm/META-INF"

cat > "$BUILD/MANIFEST.MF" <<EOF
Premain-Class: CaptureAgent
Agent-Class: CaptureAgent
Can-Retransform-Classes: true
EOF

jar cfm sdn-capture-agent.jar "$BUILD/MANIFEST.MF" -C "$BUILD/classes" . -C "$BUILD" asm
echo "built sdn-capture-agent.jar"

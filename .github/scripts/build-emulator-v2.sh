#!/usr/bin/env bash
# Build Che779 EmulatorV2; requires JDK 8+ and no Maven.
set -euo pipefail
# Script hỗ trợ CI/Linux (không dành cho người dùng Windows): build jar V2 từ mã nguồn.
cd "$(dirname "$0")/../../apps/emulator-v2/source"
command -v javac >/dev/null 2>&1 || { echo "[ERROR] javac not found; install JDK 8+ and configure JAVA_HOME/PATH." >&2; exit 1; }
command -v jar >/dev/null 2>&1 || { echo "[ERROR] jar not found; install a full JDK." >&2; exit 1; }
version=$(javac -version 2>&1 | awk '/^javac/ {print $2; exit}')
major=${version%%.*}; [[ "$version" == 1.* ]] && major=${version#1.} && major=${major%%.*}
(( major >= 8 )) || { echo "[ERROR] JDK 8+ required; found $version" >&2; exit 1; }
rm -rf out stubout jarroot
mkdir -p out stubout jarroot
find stubs -name '*.java' -print0 | xargs -0 javac -source 8 -target 8 -nowarn -encoding UTF-8 -d stubout
find src -name '*.java' -print0 | xargs -0 javac -source 8 -target 8 -nowarn -proc:none -encoding UTF-8 -d out -cp "lib/asm-3.2.jar:stubout"
cp -R out/. jarroot/
cp -R res/. jarroot/
rm -rf asmx; mkdir asmx; (cd asmx && jar xf ../lib/asm-3.2.jar); rm -rf asmx/META-INF; cp -r asmx/. jarroot/; rm -rf asmx   # dung jar cua JDK, khong can unzip
printf 'Manifest-Version: 1.0\nMain-Class: com.che779.emu.Che779Main\nClass-Path: lib/jna.jar\nImplementation-Title: Che779 EmulatorV2\nImplementation-Version: 1.3.1\nCreated-By: Che779 Team\n' > manifest.txt
jar cfm Che779_EmulatorV2.jar manifest.txt -C jarroot .
echo "OK: $(pwd)/Che779_EmulatorV2.jar"

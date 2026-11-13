#!/usr/bin/env bash
# Build Che779 EmulatorV2; requires JDK 8+ and no Maven.
set -euo pipefail
cd "$(dirname "$0")"
command -v javac >/dev/null 2>&1 || { echo "[ERROR] javac not found; install JDK 8+ and configure JAVA_HOME/PATH." >&2; exit 1; }
command -v jar >/dev/null 2>&1 || { echo "[ERROR] jar not found; install a full JDK." >&2; exit 1; }
version=$(javac -version 2>&1 | awk '{print $2}')
major=${version%%.*}; [[ "$version" == 1.* ]] && major=${version#1.} && major=${major%%.*}
(( major >= 8 )) || { echo "[ERROR] JDK 8+ required; found $version" >&2; exit 1; }
rm -rf out stubout jarroot
mkdir -p out stubout jarroot
find stubs -name '*.java' -print0 | xargs -0 javac -source 8 -target 8 -nowarn -encoding UTF-8 -d stubout
find src -name '*.java' -print0 | xargs -0 javac -source 8 -target 8 -nowarn -proc:none -encoding UTF-8 -d out -cp "lib/asm-3.2.jar:stubout"
cp -R out/. jarroot/
cp -R res/. jarroot/
(cd jarroot && unzip -qo ../lib/asm-3.2.jar -x 'META-INF/*')
printf 'Manifest-Version: 1.0\nMain-Class: com.che779.emu.Che779Main\nClass-Path: lib/jna.jar\nImplementation-Title: Che779 EmulatorV2\nImplementation-Version: 0.0.0.2 custom\nCreated-By: Che779 Team\n' > manifest.txt
jar cfm ../Che779_EmulatorV2.jar manifest.txt -C jarroot .
echo "OK: $(cd .. && pwd)/Che779_EmulatorV2.jar"

#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/apps/emulator-v2/source"
bash ./build.sh
mkdir -p test-out
javac -source 8 -target 8 -encoding UTF-8 -cp "out:stubout:lib/asm-3.2.jar" -d test-out test/com/che779/emu/IpcServerParserTest.java
java -cp "test-out:out:stubout:lib/asm-3.2.jar" com.che779.emu.IpcServerParserTest
python3 "$ROOT/tests/verify_project2.py"

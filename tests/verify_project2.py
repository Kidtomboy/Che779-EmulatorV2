#!/usr/bin/env python3
"""Kiểm tra cấu trúc repo Project 2 và (nếu có) jar vừa build."""
from pathlib import Path
import sys, zipfile

root = Path(__file__).resolve().parents[1]
SRC = "apps/emulator-v2/source"
required = [
    "README.md", "CONTRIBUTING.md", "SECURITY.md", "LICENSE", "CHANGELOG.md", "VERSION",
    "THIRD_PARTY_NOTICES.md",
    "docs/README.md", "docs/AI_HANDOFF.md", "docs/ARCHITECTURE.md", "docs/BUILD_AND_RUN.md",
    "docs/DEBUG_LOG.md", "docs/DECISIONS.md", "docs/IPC_PROTOCOL.md", "docs/PROJECT_STATE.md",
    "docs/ROADMAP.md", "docs/SOURCE_INVENTORY.md", "docs/TESTING.md", "docs/VARIANTS_AND_CLONES.md",
    f"{SRC}/build.sh", f"{SRC}/build_emulatorV2.cmd", f"{SRC}/build_emulatorV2.ps1",
    f"{SRC}/src/com/che779/emu/IpcServer.java",
    f"{SRC}/test/com/che779/emu/IpcServerParserTest.java",
    ".github/workflows/ci.yml", ".github/workflows/release.yml",
]
missing = [x for x in required if not (root / x).is_file()]
if missing:
    print("Thiếu file bắt buộc:", ", ".join(missing)); sys.exit(1)

version = (root / "VERSION").read_text().strip()
jar = root / SRC / "Che779_EmulatorV2.jar"          # jar vừa build
if not jar.is_file():
    jar = root / "apps/emulator-v2/prebuilt/Che779_EmulatorV2.jar"   # jar dựng sẵn (không kiểm phiên bản)
    check_version = False
else:
    check_version = True
with zipfile.ZipFile(jar) as z:
    bad = z.testzip()
    if bad:
        print("Mục jar bị hỏng:", bad); sys.exit(1)
    names = set(z.namelist())
    for cls in ["com/che779/emu/Che779Main.class", "com/che779/emu/IpcServer.class", "org/microemu/app/V2Main.class"]:
        if cls not in names:
            print("Thiếu lớp:", cls); sys.exit(1)
    if check_version:
        mf = z.read("META-INF/MANIFEST.MF").decode("utf-8", "replace")
        if f"Implementation-Version: {version}" not in mf:
            print(f"Phiên bản trong jar khác VERSION ({version})"); sys.exit(1)
print("Project 2: kiểm tra cấu trúc và jar ĐẠT")

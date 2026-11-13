#!/usr/bin/env python3
from pathlib import Path
import sys, zipfile
root = Path(__file__).resolve().parents[1]
required = [
    'README.md', 'CHANGELOG.md', 'docs/PROJECT_STATE.md', 'docs/AI_HANDOFF.md',
    'docs/BUILD_AND_TEST.md', 'docs/IPC_PROTOCOL.md', 'docs/SECURITY.md',
    'emulator-src/src/com/che779/emu/IpcServer.java',
    'emulator-src/test/com/che779/emu/IpcServerParserTest.java',
    '.github/workflows/ci.yml', 'Che779_EmulatorV2.jar'
]
missing = [x for x in required if not (root/x).is_file()]
if missing:
    print('Missing required files:', ', '.join(missing)); sys.exit(1)
jar = root/'Che779_EmulatorV2.jar'
with zipfile.ZipFile(jar) as z:
    bad = z.testzip()
    if bad: print('Corrupt JAR entry:', bad); sys.exit(1)
    names = set(z.namelist())
    for cls in ['com/che779/emu/Che779Main.class','com/che779/emu/IpcServer.class','org/microemu/app/V2Main.class']:
        if cls not in names: print('Missing class:', cls); sys.exit(1)
print('Project 2 repository checks: PASS')

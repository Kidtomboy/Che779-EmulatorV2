# Build and Test

## Toolchain

- Runtime target: Java 8 bytecode.
- Build JDK: JDK 8+ (đã xác minh JDK 21 trên Linux).
- Python 3 dùng cho repository integrity check.
- `unzip` cần có để giải nén ASM classes trong build.

## Build

```bash
cd emulator-src
./build.sh
```

Kết quả: `Che779_EmulatorV2.jar` tại repository root.

Windows:

```cmd
emulator-src\build_emulatorV2.cmd
```

```powershell
powershell -ExecutionPolicy Bypass -File emulator-src\build_emulatorV2.ps1
```

## Tests

```bash
bash tests/run-tests.sh
```

Bộ test hiện tại kiểm tra strict flat JSON parser và cấu trúc JAR/repository. Đây chưa phải bộ integration tests cho game MIDlet, RMS, proxy hoặc đồng bộ nhiều tab.

## Lưu ý

`-source 8 -target 8` cho bytecode Java 8 nhưng JDK mới có thể cảnh báo bootstrap class path. Để kiểm tra API compatibility nghiêm ngặt với Java 8, cần chạy build bằng JDK 8 hoặc bổ sung Java 8 bootclasspath tương ứng.

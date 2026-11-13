# Che779 Emulator V2

**Phiên bản nguồn đầu vào:** `0.0.0.2 custom`  
**Repository:** Project 2 độc lập trong hệ sinh thái Che779  
**Chủ sở hữu / người duyệt:** Cherry

Che779 Emulator V2 là emulator Java ME nhiều tab trong một cửa sổ và một tiến trình Java. Dự án bắt nguồn từ MicroEmulator; các thay đổi Che779 bổ sung giao diện nhiều tab, proxy theo tab, đồng bộ thao tác và IPC cho công cụ điều khiển ngoài.

## Cấu trúc

- `emulator-src/src/` — mã nguồn Java.
- `emulator-src/res/` — tài nguyên giao diện/thiết bị.
- `emulator-src/stubs/` — stub JNA dùng ở compile-time.
- `emulator-src/lib/asm-3.2.jar` — dependency ASM.
- `Che779_EmulatorV2.jar` — JAR build gần nhất, có thể chạy.
- `docs/` — kiến trúc, giao thức, build, bảo mật, trạng thái và bàn giao.
- `emulator-src/test/` và `tests/` — regression tests và kiểm tra cấu trúc.
- `.github/workflows/ci.yml` — CI Linux + Windows.

## Chạy

```bash
java -jar Che779_EmulatorV2.jar
```

Mở game trực tiếp:

```bash
java -jar Che779_EmulatorV2.jar /path/to/game.jar
```

Yêu cầu runtime: Java 8 trở lên. JNA native integration là tùy chọn; xem `lib/DOC_TOI_DAY.txt` và `docs/BUILD_AND_TEST.md`.

## Build từ source

- Linux/macOS: `cd emulator-src && ./build.sh`
- Windows CMD: `emulator-src\build_emulatorV2.cmd`
- Windows PowerShell: `powershell -ExecutionPolicy Bypass -File emulator-src\build_emulatorV2.ps1`

Các script nhắm bytecode Java 8 bằng `-source 8 -target 8`, phù hợp JDK 8 và JDK mới. Lưu ý JDK mới có thể in cảnh báo bootstrap class path; đây không phải lỗi build.

Chạy kiểm tra:

```bash
bash tests/run-tests.sh
```

## IPC

IPC bind loopback `127.0.0.1`, mặc định từ cổng 7790 và thử tối đa 200 cổng kế tiếp nếu bận. Giao thức là JSON Lines, mỗi dòng một object phẳng; xem `docs/IPC_PROTOCOL.md`. Server giới hạn 16 client, 64 KiB mỗi dòng và timeout đọc 10 giây.

## Tài liệu quan trọng

- [Trạng thái dự án](docs/PROJECT_STATE.md)
- [AI handoff](docs/AI_HANDOFF.md)
- [Build và kiểm thử](docs/BUILD_AND_TEST.md)
- [IPC protocol](docs/IPC_PROTOCOL.md)
- [Security notes](docs/SECURITY.md)
- [Source inventory và đối chiếu baseline](docs/SOURCE_INVENTORY.md)
- [Third-party notices](THIRD_PARTY_NOTICES.md)
- [Changelog](CHANGELOG.md)

## Phạm vi xác minh

Linux build và regression tests được chạy trong môi trường bàn giao. Windows build chỉ được xác minh khi workflow Windows thực sự chạy; không suy diễn kết quả Windows/Android/Termux từ build Linux. Chưa có kiểm thử tương thích với toàn bộ bộ sưu tập game JAR của Cherry.

## CHE779 maintenance follow-up — 2026-11-13

- `tests/run-tests.sh` now invokes the build script through Bash, so the test runner does not depend on an executable bit for `build.sh`.
- `emulator-src/src/org/microemu/app/util/FileRecordStoreManager.java` writes persistence data to a sibling temporary file and replaces the destination only after the stream closes; it uses atomic move when supported and a replace fallback otherwise.

Run `./tests/run-tests.sh` from the project root. These checks compile the emulator, run IPC parser tests and verify repository/artifact structure. They do not constitute a multi-slot runtime stress test or a power-loss persistence test.

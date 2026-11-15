# Build và chạy

## Chạy

Cần **Java 8 trở lên**.

```bash
java -jar Che779_EmulatorV2.jar [game1.jar game2.jar ...]
```

Không truyền game thì V2 mở lại các tab của lần trước (`tabs.txt`). Bản phát hành có sẵn `chay.cmd` (Windows) và `chay.sh` (Linux).

## Giao diện nhanh

Mỗi ô tab có `Tab N: [tên game]`, ô **Sync**, nút **P** (proxy riêng: Trực tiếp / Dùng chung / từng proxy / Quản lý danh sách) và nút × đóng game. Thanh dưới có CPU/HEAP và nút Resize (kích thước màn hình mọi tab). Lần đầu mở sẵn 12 tab.

## Dữ liệu

| | Windows | Linux |
|---|---|---|
| Thư mục | `%APPDATA%\Che779\Che779-EmulatorV2` | `~/.che779` |
| Nội dung | `Che779V2.xml` (cấu hình), `proxies.txt`, `servers.txt`, `tabs.txt`, `tab_proxies.txt`, RMS từng tab, `Che779V2.log` | như Windows |

Tab báo "Game đã thoát" hoặc "Tab gặp sự cố" có nút **Xem log**; gửi file `Che779V2.log` khi báo lỗi.

## Build từ mã nguồn

Cần **JDK 8 trở lên** (đã kiểm chứng với OpenJDK 21), không cần Maven và không cần `unzip`. Mã được biên dịch cho Java 8 (`-source 8 -target 8`); JDK mới có thể in cảnh báo bootstrap class path — không phải lỗi.

| Hệ điều hành | Lệnh |
|---|---|
| Linux / macOS | `cd apps/emulator-v2/source && ./build.sh` |
| Windows CMD | `apps\emulator-v2\source\build_emulatorV2.cmd` |
| Windows PowerShell | `powershell -ExecutionPolicy Bypass -File apps\emulator-v2\source\build_emulatorV2.ps1` |

Kết quả: `apps/emulator-v2/source/Che779_EmulatorV2.jar`. Nếu báo "jar đang được sử dụng", hãy đóng ứng dụng đang chạy rồi build lại. Kiểm thử đầy đủ: `bash tests/run-tests.sh`.

## Thư viện tùy chọn

JNA là tùy chọn: đặt `jna.jar` vào `apps/emulator-v2/source/lib/` (cạnh jar khi chạy, thư mục `lib/`) thì Clean RAM dùng JNA; không có thì dùng PowerShell. Nếu phân phối kèm JNA phải kèm đúng giấy phép.

## Bản dựng sẵn

`apps/emulator-v2/prebuilt/Che779_EmulatorV2.jar` là jar gốc chủ dự án đã dùng và thử, giữ để tham chiếu — **không** phải bản 1.2.0. Bản 1.2.0 lấy ở mục Releases.

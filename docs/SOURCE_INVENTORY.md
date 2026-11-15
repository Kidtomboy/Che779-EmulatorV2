# Nguồn gốc mã và kiểm kê

## Nguồn

- Mã nguồn gốc do chủ dự án cung cấp ở dạng gói `Che779_EmulatorV2_0.0.0.2_custom(1).zip` (SHA-256 `1830f850004ac9a5a241e6868954a6376e6bad6c337d71c9774e863e3933af47`, 411 mục, kiểm tra CRC đạt). Đây là gói đầu vào lịch sử; phiên bản phát hành hiện tại là `1.2.0`.
- Nền tảng: **MicroEmulator 3.0.0** (Apache License 2.0). Một số ý tưởng chức năng tham khảo HERO-Emulator-V2 của Hùng Hero với sự đồng ý của tác giả; mã Che779 được viết lại.

## Kiểm kê hiện tại (`apps/emulator-v2/source/`)

| Thành phần | Số lượng |
|---|---|
| File Java trong `src/` | 320 |
| File Java stub JNA dùng khi biên dịch (`stubs/`) | 3 |
| File tài nguyên (`res/`) | 14 |
| Thư viện đi kèm | ASM 3.2 (`lib/asm-3.2.jar`) |
| Jar dựng sẵn (`apps/emulator-v2/prebuilt/`) | 1 — jar chủ dự án đã dùng |

## Đối chiếu jar

Jar dựng lại từ mã nguồn có cùng 655 mục so với jar đầu vào; chỉ 4 mục thay đổi nội dung (`IpcServer.class` và 3 lớp con ẩn danh), đúng phạm vi phần IPC đã được sửa. Phiên bản lớp là 52 (Java 8). Phép đối chiếu này chỉ chứng minh danh sách mục và phạm vi thay đổi, **không** chứng minh hai bản hoạt động giống hệt nhau.

Giấy phép bên thứ ba: xem [`THIRD_PARTY_NOTICES.md`](../THIRD_PARTY_NOTICES.md).

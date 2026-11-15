# Đóng góp cho Che779 Emulator V2

Cảm ơn bạn đã quan tâm! Tài liệu này giúp bạn gửi thay đổi nhanh và ít phải sửa lại.

## Trước khi bắt đầu

1. Đọc `docs/PROJECT_STATE.md`, `docs/ARCHITECTURE.md` và `docs/IPC_PROTOCOL.md`.
2. Mã nguồn nằm ở `apps/emulator-v2/source/src`, tài nguyên ở `res`, thư viện ASM 3.2 ở `lib`.
3. Không đưa vào repo: file build, dữ liệu người dùng (`tabs.txt`, `Che779V2.xml`, log), mật khẩu proxy, khóa ký.

## Cách gửi thay đổi

1. Fork repo, tạo nhánh từ `main`.
2. Sửa, rồi chạy `bash tests/run-tests.sh` (build + test bộ đọc IPC + kiểm tra cấu trúc repo).
3. Cập nhật `docs/` và `CHANGELOG.md` nếu hành vi thay đổi.
4. Mở pull request và điền mẫu có sẵn. Ghi rõ **đã chạy gì, trên hệ điều hành nào, gì chưa chạy**.

## Nguyên tắc

- Không ghi "đã kiểm tra" khi chưa chạy thật.
- Giữ nguyên thông báo bản quyền và giấy phép của MicroEmulator (Apache 2.0) và ASM.
- Báo lỗi bằng mẫu Issue; thông tin nhạy cảm xem `SECURITY.md`.

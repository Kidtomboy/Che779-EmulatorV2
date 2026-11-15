# Trạng thái dự án — Che779 Emulator V2

**Phiên bản:** `1.2.0` (file `VERSION`)

## Tóm tắt

Emulator nhiều tab trong một tiến trình Java. Chủ dự án xác nhận chạy tốt trên Windows với đủ 12 tab; Claude đã chạy trên Linux và mở được game thật. Theo quyết định của chủ dự án, phần mã được giữ nguyên, chỉ sửa khi có người dùng yêu cầu.

## Đã có

- Bộ build đủ cho Linux (`.sh`) và Windows (`.cmd`, `.ps1`), không cần `unzip`.
- CI build trên Linux và Windows; kiểm thử bộ đọc IPC.
- Phát hành tự động: gắn thẻ `vX.Y.Z` → zip + `SHA256SUMS.txt`.

## Chưa kiểm chứng

Xem [TESTING](TESTING.md): workflow Phát hành chưa chạy thật; Android/Termux trực tiếp; chịu tải nhiều tab; proxy xoay vòng.

## Rủi ro còn lại

Xem [DEBUG_LOG](DEBUG_LOG.md) và [SECURITY](../SECURITY.md): dọn luồng khi đóng tab, IPC chưa xác thực, mật khẩu proxy chữ thường.

## Nguồn sự thật

Mã nguồn và commit là bằng chứng về cách làm; log CI là bằng chứng về build. File này chỉ tóm tắt — khi mâu thuẫn, tin mã nguồn và CI.

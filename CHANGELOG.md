# Nhật ký thay đổi

Chỉ ghi những gì **phiên bản hiện tại** đang làm được. Lịch sử các bản trước không còn được lưu ở đây.

## [Chưa phát hành]

### Sửa
- Hình game bớt **mờ/nhoè**: trước đây khi tab thu phóng không nguyên (ví dụ 1,37×) hoặc Windows đặt scale 125%/150%, ảnh bị kéo giãn trực tiếp nên vỡ hạt hoặc nhoè. Nay phóng nguyên lần bằng nearest rồi thu về đúng kích thước bằng bilinear, có tính hệ số DPI của Windows. Chưa kiểm chứng trên màn hình Windows thật.

## [1.3.0] — Bản phát hành (chỉ Windows)

> Từ 1.3.0 repo này chỉ dành cho Windows. Bản Linux nằm ở Che779-Linux.

### Che779 Emulator V2
- Nhiều tab trong **một cửa sổ, một tiến trình Java**; mở sẵn 12 tab ở lần chạy đầu. Chủ dự án xác nhận chạy tốt với đủ 12 tab trên Windows.
- Mỗi tab có thiết bị, RMS (dữ liệu lưu game), class loader và proxy riêng; tab lỗi hoặc thoát không kéo sập tab khác, có nút **Khởi động lại game** và **Xem log**.
- Chế độ **Tab** và **Lưới**; kích thước màn hình tab từ 176×208 đến 360×640; game tự thu phóng vừa ô.
- Đồng bộ phím và chuột (kể cả cuộn chuột) giữa các tab được chọn.
- Proxy HTTP/SOCKS5 riêng từng tab hoặc chia vòng từ kho proxy; lưu và khôi phục khi mở lại.
- Thay IP máy chủ: `server.txt` trong jar game, rồi máy chủ chọn từ `servers.txt`, rồi tự nhập.
- Thanh dưới hiện CPU và HEAP; dọn RAM ngay hoặc tự dọn theo ngưỡng HEAP 60–90%.
- Menu **Che779**: Sleep, tự Sleep, giảm FPS, tốc độ game, giảm đồ họa, màu.
- IPC TCP `127.0.0.1` (có giới hạn 16 kết nối, 64 KiB mỗi dòng, chờ đọc 10 giây, bộ đọc JSON chặt), thêm lệnh `add_tab`, `open`, `slots`.
- Dữ liệu lưu ngoài thư mục chương trình (`%APPDATA%\Che779\Che779-EmulatorV2` hoặc `~/.che779`).

### Phát hành và build
- Mỗi lần gắn thẻ `vX.Y.Z`, GitHub tự build, chạy kiểm thử và tạo `Che779-EmulatorV2-<phiên bản>.zip` cùng `SHA256SUMS.txt`.
- Script build Windows (`.cmd`, `.ps1`); không cần `unzip`. Gắn thẻ `vX.Y.Z` → Actions build trên Windows, đóng zip và tạo `SHA256SUMS.txt` (mã băm tính sau cùng trên đúng file zip).

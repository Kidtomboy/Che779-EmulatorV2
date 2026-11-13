# Che779 EmulatorV2 — phiên bản 0.0.0.2 custom

Emulator nhiều tab trong **một cửa sổ / một tiến trình Java**, xây lại từ MicroEmulator 3.0.0 sạch (Apache 2.0) cùng bộ tính năng Che779.
Tham khảo chức năng từ HERO-Emulator-V2 (theo sự đồng ý của tác giả Hùng Hero); mã nguồn viết lại, không dùng mã đã làm rối.

## Chạy
`java -jar Che779_EmulatorV2.jar [game1.jar game2.jar ...]` (Java 8+). Không truyền game thì mở lại các tab lần trước (lưu ở `tabs.txt`).
Dữ liệu: `%APPDATA%\Che779\Che779-EmulatorV2` (cấu hình **Che779V2.xml**, proxies.txt, servers.txt, tabs.txt, RMS riêng từng tab).

## Tự build (đã thêm file build)
- Windows: chạy `emulator-src\build_emulatorV2.cmd` (hoặc `powershell -ExecutionPolicy Bypass -File emulator-src\build_emulatorV2.ps1`). Cần JDK 8+. Script đã quote đường dẫn để hỗ trợ thư mục có dấu cách.
- Linux/mac: `emulator-src/build.sh`. Các script dùng `-source 8 -target 8` để nhắm bytecode Java 8; JDK mới có thể in cảnh báo tương thích source/target.
Kết quả: `Che779_EmulatorV2.jar` nằm ngoài thư mục `emulator-src`.
Clean RAM qua JNA: đặt `jna.jar` vào `lib/` (xem lib/DOC_TOI_DAY.txt).

## Giao diện
Nền tối giống bản gốc: mỗi ô tab có `Tab N: [tên game]`, tích **Sync**, nút **P** (proxy riêng của tab: Trực tiếp / Dùng chung / từng proxy / Quản lý danh sách), nút × đóng game; ô trống là "+ Thêm Game". Góc phải: ✕ đóng tất cả game, ☰ chuyển Lưới/Tab, + thêm tab, − xóa tab đang chọn (luôn giữ ít nhất 1 tab). Lưới tự co giãn để vừa khung cửa sổ (tab trống nhỏ dần giống bản gốc); chỉ có thanh cuộn khi có game đang chạy mà không thể thu nhỏ thêm. Số thứ tự tab luôn là 1..N (tự đánh lại khi xóa/thêm; dữ liệu RMS của tab vẫn giữ nguyên theo tab). Ít tab thì ô giãn lấp kín cửa sổ, nhiều tab thì tự chia lưới. Menu **Options** (thay cho menu Che779 của bản thường) gồm: Dạng Tab/Lưới, kích thước màn hình, Sleep, FPS, tốc độ, đồ họa, màu, Proxy Management, mạng, Log console.
Thanh dưới: CPU/HEAP + nút Resize (kích thước màn hình mọi tab). Lần đầu mở sẵn 12 tab.

## Tính năng
- **Tab**: Thêm Game / tab trống, mở game vào tab, Đóng game, Xóa tab, Đóng tất cả, danh sách tab. Hiển thị **Dạng Tab** hoặc **Dạng Lưới** (xem tất cả cùng lúc).
- Mỗi tab có **thiết bị, RMS, class loader, proxy riêng**; một tab lỗi/thoát không kéo sập tab khác. Báo "Đang nạp game...", "Tab gặp sự cố" và nút **Khởi động lại game**.
- **Đồng bộ** phím + chuột sang các tab (menu Đồng bộ: bật/tắt tất cả, chọn tab tham gia, tích "Đồng bộ" từng tab).
- **Proxy tab**: gán proxy riêng cho từng tab hoặc chia vòng tròn từ kho proxy (thêm/nhập/kiểm tra ở Che779 > Quản lý Proxy). Áp dụng cho kết nối mới.
- **Bộ nhớ**: hiển thị CPU / HEAP trên thanh trạng thái, dọn RAM ngay, tự dọn khi HEAP vượt ngưỡng (60–90%).
- Giữ toàn bộ menu Che779: Sleep, tự Sleep, giảm FPS, tốc độ game, giảm đồ họa, màu. Sleep/FPS/tốc độ áp dụng cho mọi tab.
- Kích thước màn hình tab: 176x208 … 360x640 (menu Hiển thị).
- IPC (cho Remoter): các lệnh cũ (keypress/keydown/keyup/text/sleep/clean_ram/screenshot/status...) gửi tới **mọi tab đang chạy**; thêm `add_tab`, `open` (tab, path), `slots`.

## Màn hình game tự thu phóng
Game trong mỗi ô được thu nhỏ/phóng to vừa ô (như bản gốc), chuột bấm đúng tọa độ game. Nhiều tab → ô nhỏ lại để tất cả vừa cửa sổ, không cần cuộn.

## Log khi game lỗi
Tab "Game đã thoát"/"Tab gặp sự cố" có nút **Xem log** (hoặc File > Xem log tab đang chọn). File đầy đủ: `%APPDATA%\Che779\Che779-EmulatorV2\Che779V2.log` (ghi lại mỗi lần chạy) — gửi file này khi báo lỗi.

- Proxy riêng từng tab được lưu (`tab_proxies.txt`) và tự khôi phục khi mở lại. Cuộn chuột trong game hoạt động và cũng được đồng bộ.

## Chưa làm / lưu ý
- Ghi chú build: nếu báo "jar đang được sử dụng" thì hãy đóng Che779 EmulatorV2 đang chạy rồi build lại.
- Chưa có quay GIF và Quản lý RMS dạng bảng của V2 gốc (có Log console).
- V2 không cần Remoter (mọi thứ nằm trong một cửa sổ); Remoter không đổi.
- Đồng bộ chuột/phím chỉ giữa các tab có kích thước tương tự mới trùng toạ độ.

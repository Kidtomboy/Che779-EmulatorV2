# Kiến trúc

Che779 Emulator V2 là **một tiến trình Java** chứa nhiều tab. Mỗi tab (gọi là *slot*) chạy một game J2ME độc lập.

## Thành phần chính

| Lớp | Vai trò |
|---|---|
| `com.che779.emu.Che779Main` | Điểm vào; khởi động V2, đọc tham số game |
| `org.microemu.app.V2Main` | Cửa sổ chính (~2.100 dòng): thanh tab, chế độ Tab/Lưới, menu, thanh CPU/HEAP |
| `org.microemu.app.v2.Slot` | Một tab: nạp game, đóng game, thiết bị, RMS, class loader riêng |
| `SlotCommon / SlotContext` | Trạng thái và ngữ cảnh dùng chung của slot; gắn slot với luồng game |
| `ui.swing.SlotBinder`, `SyncHook` | Nối giao diện với slot; đồng bộ phím và chuột giữa các tab |
| `org.microemu.app.Che779`, `ProxyManager`, `ProxyPanel`, `Servers` | Cấu hình Che779, quản lý proxy, danh sách máy chủ |
| `com.che779.emu.CleanRam`, `JnaClean` | Dọn RAM (JNA tùy chọn, dự phòng PowerShell trên Windows) |
| `com.che779.emu.IpcServer` | Cổng điều khiển TCP loopback |
| `org.microemu.*` | MicroEmulator 3.0.0 gốc (Apache 2.0) |

## Cách tách biệt giữa các tab

- Mỗi slot có class loader, RMS và proxy riêng; ngữ cảnh slot được gắn vào luồng game (luồng con kế thừa).
- Tab lỗi hoặc thoát được cô lập: các tab khác vẫn chạy, tab lỗi có nút **Khởi động lại game**.
- Đổi lại, tất cả tab dùng chung một JVM: lỗi nặng ở tầng Java có thể ảnh hưởng cả cửa sổ. Muốn cách ly hoàn toàn, dùng [Che779-Emulator](https://github.com/Kidtomboy/Che779-Emulator) (mỗi game một tiến trình).

## IPC

Xem [IPC_PROTOCOL](IPC_PROTOCOL.md). Các lệnh phím/chuột/ngủ gửi tới **mọi tab đang chạy**; `open` và `add_tab` chỉ cho biết yêu cầu đã được đưa vào hàng đợi giao diện.

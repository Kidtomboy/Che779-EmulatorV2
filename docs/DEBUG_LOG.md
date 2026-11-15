# Lỗi và rủi ro đã biết

Đây là nhận định từ đọc mã, **chưa tái hiện bằng chạy thật**. Chủ dự án quyết định: chỉ sửa khi có người dùng yêu cầu.

| # | Vấn đề | Mức | Ghi chú |
|---|---|---|---|
| 1 | Đóng tab chưa dọn luồng game | Cao | `Slot.closeGameInternal` gọi `destroyApp` rồi xóa ngữ cảnh, nhưng luồng game (vòng lặp mạng, Timer) có thể vẫn chạy ngầm. Gợi ý: mỗi game một `ThreadGroup`, ngắt nhóm khi đóng; hủy Timer; đóng socket. |
| 2 | IPC chưa có xác thực | Trung bình | Xem [SECURITY](../SECURITY.md). |
| 3 | `open`/`add_tab` trả về khi mới xếp hàng | Thấp | `true` nghĩa là yêu cầu đã nhận, không phải game đã nạp xong. |
| 4 | Mật khẩu proxy lưu chữ thường | Trung bình | `tab_proxies.txt`, `proxies.txt`. |
| 5 | Callback `onSlotChange` có thể chạy ngoài luồng giao diện | Trung bình | Nên bọc `SwingUtilities.invokeLater`. |
| 6 | `Slot.close()` sửa trạng thái ngoài khóa `LOAD_LOCK` | Trung bình | Có thể đua với thao tác mở game cùng tab ngay sau đó. |
| 7 | `MIDletBridge` giữ bảng ngữ cảnh dùng chung | Trung bình | `midletContexts` là `WeakHashMap` static, không đồng bộ; cần xác minh `clear()` có xóa toàn cục không. |
| 8 | Tự dọn RAM mỗi 2 giây, không có thời gian nghỉ | Thấp | Có thể dọn dồn dập khi HEAP sát ngưỡng. |
| 9 | `saveTabs()` ghi đè trực tiếp | Thấp | Mất điện giữa chừng có thể hỏng `tabs.txt`. |
| 10 | `platformRequest` chỉ hiện thông báo | Thấp | Game không mở được liên kết ngoài. |
| 11 | Dọn RAM chỉ thu nhỏ vùng nhớ hiển thị (Windows) | Thấp | Không giải phóng bộ nhớ Java thật. |

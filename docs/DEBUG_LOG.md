# Lỗi và rủi ro đã biết

Đây là nhận định từ đọc mã, **chưa tái hiện bằng chạy thật**. Chủ dự án quyết định: chỉ sửa khi có người dùng yêu cầu.

| # | Vấn đề | Mức | Ghi chú |
|---|---|---|---|
| 1 | Đóng tab chưa dọn luồng game | Cao (**đã sửa một phần 2026-11-24, chưa commit/chưa chạy thật:** mỗi lần nạp có `ThreadGroup` riêng, ngắt luồng khi đóng; chỉ ngắt được luồng sinh từ nhóm, game dùng cách khác vẫn có thể còn chạy) | `Slot.closeGameInternal` gọi `destroyApp` rồi xóa ngữ cảnh, nhưng luồng game (vòng lặp mạng, Timer) có thể vẫn chạy ngầm. Gợi ý: mỗi game một `ThreadGroup`, ngắt nhóm khi đóng; hủy Timer; đóng socket. |
| 2 | IPC chưa có xác thực | Trung bình | Xem [SECURITY](../SECURITY.md). |
| 3 | `open`/`add_tab` trả về khi mới xếp hàng | Thấp | `true` nghĩa là yêu cầu đã nhận, không phải game đã nạp xong. |
| 4 | Mật khẩu proxy lưu chữ thường | Trung bình | `tab_proxies.txt`, `proxies.txt`. |
| 5 | Callback `onSlotChange` có thể chạy ngoài luồng giao diện | Trung bình | **Đã sửa 2026-11-24 (chưa commit/chưa chạy thật):** bọc `SwingUtilities.invokeLater`. `BUILD_VERIFIED` bằng `javac`, GUI `NOT_RUN`. |
| 6 | `Slot.close()` sửa trạng thái ngoài khóa `LOAD_LOCK` | Trung bình (**đã sửa 2026-11-24, chưa commit/chưa chạy thật:** đổi trạng thái trong khoá) | Có thể đua với thao tác mở game cùng tab ngay sau đó. |
| 7 | `MIDletBridge` giữ bảng ngữ cảnh dùng chung | Trung bình (đọc lại 2026-11-24: `clear()` đã `synchronized` và chỉ xoá theo slot; không cần sửa) | `midletContexts` là `WeakHashMap` static, không đồng bộ; cần xác minh `clear()` có xóa toàn cục không. |
| 8 | Tự dọn RAM mỗi 2 giây, không có thời gian nghỉ | Thấp | **Đã sửa 2026-11-24 (chưa commit/chưa chạy thật):** nghỉ tối thiểu 30 giây giữa hai lần dọn. |
| 9 | `saveTabs()` ghi đè trực tiếp | Thấp | **Đã sửa 2026-11-24 (chưa commit/chưa chạy thật):** ghi tệp tạm rồi đổi tên (atomic khi hỗ trợ). |
| 10 | `platformRequest` chỉ hiện thông báo | Thấp | Game không mở được liên kết ngoài. |
| 11 | Dọn RAM chỉ thu nhỏ vùng nhớ hiển thị (Windows) | Thấp | Không giải phóng bộ nhớ Java thật. |

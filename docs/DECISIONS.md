# Quyết định thiết kế

| Quyết định | Lý do |
|---|---|
| Một tiến trình, nhiều tab (khác Project 1) | Nhẹ hơn và mở là dùng, không cần chương trình điều khiển riêng. Đổi lại kém cách ly hơn. |
| Xây lại từ MicroEmulator 3.0.0 sạch (Apache 2.0) | Mã rõ nguồn gốc và giấy phép. Một số ý tưởng chức năng tham khảo HERO-Emulator-V2 **theo sự đồng ý của Hùng Hero**; mã được viết lại, không dùng mã đã làm rối. |
| IPC chỉ trên `127.0.0.1`, JSON một dòng | Đơn giản, dùng được từ mọi ngôn ngữ; không mở ra mạng. |
| Mở sẵn 12 tab ở lần chạy đầu | Phù hợp người treo nhiều tài khoản. Số tab luôn đánh số 1..N. |
| Dữ liệu lưu ngoài thư mục chương trình | Cập nhật bản mới không mất dữ liệu. |
| JNA là tùy chọn | Không bắt buộc kèm thư viện native; có dự phòng PowerShell trên Windows. |
| Chỉ sửa lỗi khi có yêu cầu | Chủ dự án xác nhận bản hiện tại chạy tốt; tránh thay đổi không cần thiết. |

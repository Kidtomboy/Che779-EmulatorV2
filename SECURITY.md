# Chính sách bảo mật

## Báo lỗi bảo mật

Nếu bạn tìm thấy lỗ hổng, **đừng đăng công khai** trong Issue. Hãy dùng mục *Security → Report a vulnerability* của repo trên GitHub để báo riêng. Khi báo, ghi rõ phiên bản, hệ điều hành, các bước tái hiện, và xóa mật khẩu proxy hoặc thông tin cá nhân khỏi log.

## Đã có

- IPC chỉ mở trên `127.0.0.1`.
- Tối đa 16 kết nối cùng lúc; mỗi dòng tối đa 64 KiB; ngắt kết nối sau 10 giây không hoạt động.
- Bộ đọc JSON từ chối JSON sai cú pháp, khóa trùng, giá trị lồng nhau và dữ liệu thừa phía sau.

## Hạn chế đã biết

| Vấn đề | Mức độ | Ghi chú |
|---|---|---|
| IPC chưa có xác thực | Trung bình | Tiến trình khác của cùng người dùng biết cổng thì gửi được lệnh (`quit`, `text`, `proxy`, `screenshot`...). |
| Lệnh `screenshot` ghi file | Trung bình | Ghi ảnh ra đường dẫn do bên gọi chỉ định, theo quyền người dùng. |
| Mật khẩu proxy lưu dạng chữ thường | Trung bình | Lưu trong `tab_proxies.txt` và `proxies.txt`; chưa kiểm quyền file trên Windows. |

Khuyến nghị: không mở cổng IPC ra mạng (không bind `0.0.0.0`, không đưa qua đường hầm công cộng). Kế hoạch xử lý: `docs/ROADMAP.md`.

## Tải bản chính thức

Chỉ tải file từ mục **Releases** của repo chính thức và kiểm tra bằng `SHA256SUMS.txt`.

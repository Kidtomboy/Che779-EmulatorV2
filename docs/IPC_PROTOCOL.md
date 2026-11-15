# Giao thức IPC

## Kết nối

- TCP loopback, chỉ `127.0.0.1`. Cổng mặc định **7790**; nếu bận, thử 199 cổng kế tiếp.
- JSON một dòng, mã hóa UTF-8: mỗi dòng một đối tượng JSON phẳng, mỗi lệnh nhận lại một dòng JSON.
- Tối đa 65.536 ký tự mỗi dòng; tối đa 16 kết nối cùng lúc (kết nối thừa bị đóng); ngắt nếu 10 giây không có dữ liệu.

## Dạng lệnh

Giá trị chỉ có thể là chuỗi, số, boolean hoặc null. Đối tượng/mảng lồng nhau **không** được hỗ trợ; khóa trùng, escape sai, dữ liệu thừa phía sau bị từ chối.

```json
{"action":"ping"}
{"action":"open","tab":1,"path":"/games/demo.jar"}
```

## Các lệnh

`ping`, `status`, `version`, `slots`, `keypress`, `keydown`, `keyup`, `text`, `clean_ram`, `sleep`, `hide`, `show`, `mouse`, `proxy`, `proxy_get`, `add_tab`, `open`, `screenshot`, `quit` / `close`.

Các lệnh phím, chuột, ngủ, dọn RAM gửi tới **mọi tab đang chạy**. `add_tab` thêm tab; `open` (kèm `tab`, `path`) nạp game vào tab; `slots` liệt kê tab.

## Ý nghĩa phản hồi

Hầu hết lệnh trả `{"ok":true}` hoặc `{"ok":false,...}`. Với `open` và `add_tab`, `ok:true` nghĩa là yêu cầu đã được nhận và xếp hàng, **chưa** có nghĩa game đã nạp xong; hãy kiểm tra lại bằng `slots` hoặc giao diện.

## Bảo mật

Chỉ bind loopback nên không truy cập được từ mạng, nhưng **không xác thực** tiến trình trên cùng máy. Không đưa cổng này ra công cộng và không bind `0.0.0.0`. Xem [`SECURITY.md`](../SECURITY.md).

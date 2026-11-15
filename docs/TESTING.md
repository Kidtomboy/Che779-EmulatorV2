# Kiểm thử

## Tự động (CI, mỗi lần push)

| Việc | Chạy ở đâu |
|---|---|
| `bash tests/run-tests.sh`: build, test bộ đọc IPC, kiểm tra cấu trúc repo và jar | Linux |
| Build bằng `build_emulatorV2.cmd` | Windows |
| Build bằng `build_emulatorV2.ps1` | Windows |

## Đã kiểm chứng

| Hạng mục | Cách kiểm | Kết quả |
|---|---|---|
| Chạy trên Windows với đủ 12 tab | Chủ dự án tự chạy | Chạy tốt |
| Chạy trên Linux (màn hình ảo Xvfb), mở game thật | Claude chạy | Mở được game |
| IPC: `ping`, `status`, 12 slot ban đầu, `add_tab` lên 13, `quit` | Claude gửi lệnh | Đúng |
| Build trong đường dẫn có dấu cách, OpenJDK 21 | Claude chạy | Thành công |
| Bộ đọc IPC: JSON sai, khóa trùng, lồng nhau, dòng quá dài | Test hồi quy | Đạt |

## Chưa kiểm chứng

- Workflow **Phát hành** chưa chạy thật lần nào (sẽ biết khi gắn thẻ đầu tiên).
- Chạy trên Android/Termux trực tiếp.
- Kiểm thử chịu tải nhiều tab, mất điện giữa lúc ghi dữ liệu, bộ game đa dạng.
- Proxy xoay vòng, đồng bộ chuột giữa các tab khác kích thước.

Không đánh dấu một mục là đã kiểm chứng khi chưa có kết quả chạy thật.

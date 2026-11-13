# AI Handoff — Che779 Emulator V2

## Chủ sở hữu và nguyên tắc

- Chủ sở hữu/direction owner: Cherry.
- Đọc `README.md`, `docs/PROJECT_STATE.md`, `docs/IPC_PROTOCOL.md` và `docs/SECURITY.md` trước khi sửa.
- Không tuyên bố test/build chưa chạy. Phân biệt build Linux, Windows CI, runtime UI và kiểm thử game thực tế.
- Mọi thay đổi phải cập nhật `CHANGELOG.md`, `docs/PROJECT_STATE.md` và tài liệu bị ảnh hưởng.
- Không chuyển Project 3 trước khi Cherry duyệt Project 2.

## Nhận diện

- Project: Che779 Emulator V2, baseline `0.0.0.2 custom`.
- Một tiến trình Java, nhiều tab/slots, mỗi tab có device/classloader/RMS/proxy riêng theo thiết kế hiện tại.
- Source: `emulator-src/src`; tài nguyên: `emulator-src/res`; dependency: ASM 3.2.
- JAR đầu vào được giữ nguyên trong lịch sử gói gốc; JAR bàn giao được build từ source.

## Thay đổi của revision bàn giao

- Đồng bộ build scripts về Java 8 bytecode target.
- Linux script build ra `../Che779_EmulatorV2.jar`.
- IPC server giới hạn 16 client, 64 KiB mỗi dòng, socket read timeout 10 giây.
- Parser IPC mới chỉ chấp nhận JSON object phẳng với string/number/boolean/null; từ chối key trùng, escape sai, trailing data và JSON lồng nhau.
- Thêm parser regression tests và CI.

## Kết quả xác minh

- Build Linux OpenJDK 21: đã chạy thành công; build trong đường dẫn có dấu cách cũng thành công.
- Runtime Xvfb: `ping`, `status`, 12 slots, `add_tab` lên 13 slots và `quit` đã chạy thành công.
- JAR baseline và JAR mới: cùng 655 entries, không có entry thiếu/thừa; 4 entry `IpcServer*.class` thay đổi nội dung, phù hợp thay đổi IPC.
- Windows build, Android/Termux và kiểm thử đầy đủ game chưa xác minh.

## Cẩn trọng

- IPC chỉ bind loopback; hiện chưa có authentication token.
- `open` và `add_tab` enqueue Swing EDT; `true` chỉ biểu thị yêu cầu được nhận/đưa vào hàng đợi, không phải game load thành công.
- Proxy credentials có thể được lưu trong file cấu hình; cần kiểm tra quyền và mã hóa/credential store trước khi tuyên bố an toàn.
- Không tự nhận ASM license dựa trên Manifest; cần xác minh license upstream chính thức.

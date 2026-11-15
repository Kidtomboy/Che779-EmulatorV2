# Ghi chú bàn giao

Dành cho người hoặc AI tiếp tục làm việc trên Project 2.

## Nguyên tắc

- Chủ dự án: **Cherry**. Trả lời và viết tài liệu bằng tiếng Việt.
- Đọc `README.md`, `docs/PROJECT_STATE.md`, `docs/IPC_PROTOCOL.md`, `SECURITY.md` trước khi sửa.
- **Không ghi "đã kiểm tra" khi chưa chạy.** Phân biệt: build Linux, build Windows trên CI, chạy giao diện thật, thử game thật.
- Mọi thay đổi phải cập nhật `CHANGELOG.md` (chỉ ghi phiên bản hiện tại), `docs/PROJECT_STATE.md` và tài liệu bị ảnh hưởng.
- Chủ dự án quyết định: giữ nguyên mã đã chạy tốt, chỉ sửa khi có yêu cầu cụ thể.

## Nhận diện

- Phiên bản `1.3.0`. Một tiến trình Java, nhiều tab (slot); mỗi tab có thiết bị, class loader, RMS, proxy riêng.
- Mã: `apps/emulator-v2/source/src`; tài nguyên: `res`; thư viện: ASM 3.2.
- Jar phát hành được **build từ mã nguồn** bởi workflow Phát hành; `prebuilt/` chỉ là jar tham chiếu.

## Lưu ý

- IPC chỉ bind loopback, chưa có xác thực.
- `open` và `add_tab` trả về khi yêu cầu được xếp hàng giao diện, không phải khi game đã nạp xong.
- Mật khẩu proxy có thể lưu trong file cấu hình; cần kiểm tra quyền file trước khi tuyên bố an toàn.
- Không tự gán giấy phép cho ASM dựa trên manifest; cần đối chiếu nguồn chính thức.

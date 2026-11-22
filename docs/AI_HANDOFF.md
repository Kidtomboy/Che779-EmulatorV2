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


## Audit độc lập 1.3.0 — 2026-11-16

Darling đã rà soát source và tài liệu ở revision hiện tại. Danh sách phát hiện, phân loại mức độ xác minh và phạm vi được phép làm nằm trong [AI_CHANNEL.md](AI_CHANNEL.md) (ghi chú mới nhất ở cuối) và GitHub Issue: https://github.com/Kidtomboy/Che779-EmulatorV2/issues/1.

**Quy tắc bàn giao:** audit/Issue không phải quyền tự động sửa source. Claude phải chờ Cherry duyệt từng giải pháp/phạm vi; trước khi code cần kiểm tra lại main vì source có thể thay đổi. Sau khi sửa, cập nhật DEBUG_LOG, TESTING, PROJECT_STATE và AI_CHANNEL với bằng chứng build/runtime.

## Đối chiếu version — 2026-11-23 (đồng bộ theo yêu cầu Cherry)
| Nguồn | Giá trị |
|---|---|
| `VERSION` / `PROJECT_STATE.md` | 1.3.0 |
| HEAD `main` | `773749089775d8df650bd25cb5646cdb9ef2031e` |
| Tag | `v1.3.0` → `8e41191`; `v1.2.0` → `8653e96`; `v1.3.1` → `c25af5f` (không có Release `v1.3.1`; commit `c25af5f` đã bị rollback khỏi `main`, xem ghi chú rollback trong AI_CHANNEL) |
| Release | Release `v1.3.0`, `v1.2.0` (target `main`) |
- Chưa cài/chạy artifact thực tế: `NOT_RUN`. Mức kiểm tra ở đây chỉ là đối chiếu metadata Git/GitHub (`STATIC_REVIEW`).
- Mọi chênh lệch (nếu có) giữ nguyên là version inconsistency cho đến khi Cherry/Darling xác định nguồn chuẩn; Claude không tự sửa version hay dời tag.

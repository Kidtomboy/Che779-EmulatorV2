# Changelog

## 0.0.0.2-custom-che779.1 — Implementation continuation

- Chuẩn hóa script Linux/CMD/PowerShell để nhắm bytecode Java 8 nhất quán.
- Sửa script Linux để tạo JAR tại thư mục gốc repository như hướng dẫn.
- Thêm giới hạn client IPC (16), giới hạn dòng (64 KiB), timeout đọc (10 giây) và kiểm tra lỗi ghi socket.
- Thay parser IPC regex lỏng bằng parser JSON object phẳng có kiểm tra escape, kiểu primitive, key trùng và dữ liệu thừa.
- Thêm regression test cho parser IPC và workflow CI Linux/Windows.
- Bổ sung README, tài liệu giao thức, bảo mật, build, trạng thái và AI handoff.
- Không thay đổi tính năng giao diện/game runtime ngoài lớp IPC/build.

## 0.0.0.2 custom — Baseline đầu vào

- Bản nguồn và JAR do Cherry cung cấp; xem `docs/PROJECT_STATE.md` để biết giới hạn kiểm thử baseline.

# Project State — Che779 Emulator V2

## Trạng thái

**Implementation pass tiếp theo đã được áp dụng trên source ZIP gốc; chờ Cherry nghiệm thu.** Không được gọi đây là bản phát hành production hoặc tuyên bố tương thích mọi hệ điều hành.

## Baseline

- Archive đầu vào: `Che779_EmulatorV2_0.0.0.2_custom(1).zip`
- SHA-256: `1830f850004ac9a5a241e6868954a6376e6bad6c337d71c9774e863e3933af47`
- ZIP có 411 entries, `testzip()` không báo lỗi.
- 323 Java source files theo inventory ban đầu.
- JAR baseline có 655 entries.

## Revision hiện tại

- Linux build script dùng JDK 8-compatible `-source 8 -target 8` và tạo JAR ở root.
- Windows CMD/PowerShell cũng dùng source/target 8; Windows runtime build chưa chạy trong môi trường này.
- IPC có bounded line, timeout, giới hạn số client, strict flat JSON parser.
- Có regression test parser và CI workflow.
- JAR mới có 655 entries; so với JAR baseline: không thiếu/thừa entry; chỉ 4 entry `IpcServer*.class` thay đổi nội dung, đúng phạm vi code IPC được chỉnh.

## Kiểm thử đã chạy

- [x] Giải nén ZIP gốc và kiểm tra CRC.
- [x] Runtime smoke test dưới Xvfb: `ping`, `status`, 12 slot ban đầu, `add_tab` tăng lên 13 và `quit`.
- [x] Build lại từ đường dẫn có dấu cách trên Linux.
- [x] Build Linux bằng OpenJDK 21.
- [x] Đối chiếu cấu trúc JAR baseline/new.
- [ ] Chạy Windows CMD build thực tế.
- [ ] Chạy PowerShell build thực tế.
- [ ] Chạy Android/Termux.
- [ ] Kiểm thử game JAR đa dạng, RMS, proxy, đồng bộ và lifecycle nhiều tab.
- [ ] Kiểm tra GitHub Actions sau khi Cherry push repository.

## Backlog còn lại

1. Xác minh license/NOTICE chính xác của toàn bộ source và ASM 3.2; chưa tự gán một license chung khi chưa đủ chứng cứ.
2. Xác thực IPC (token hoặc secret local) và quản lý secret an toàn; loopback không thay thế authentication trước process local không đáng tin.
3. `ipcAddTab`/`ipcOpen` hiện trả về sau khi enqueue Swing EDT, không chứng minh game load thành công. Cần protocol async/result nếu muốn ACK chính xác mà không phá client cũ.
4. Đánh giá lưu mật khẩu proxy trong `tab_proxies.txt` và file quyền trên Windows.
5. Kiểm thử lỗi classloader, RMS isolation, crash recovery, memory pressure và proxy rotation.
6. Cần xác minh yêu cầu JNA native library cho clean RAM theo từng OS/architecture.

## Quy tắc phê duyệt

Cherry là người duyệt cuối. Không bắt đầu Project 3 khi Project 2 chưa được Cherry chấp thuận.

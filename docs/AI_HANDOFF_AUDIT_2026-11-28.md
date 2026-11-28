# AI Audit Handoff — Project 2 (2026-11-28)

> Snapshot bàn giao cho Claude; không đồng nhất commit với runtime verification.

## Repository baseline
- Repo: Kidtomboy/Che779-EmulatorV2
- VERSION: 1.3.1
- main HEAD đã đọc: 8e35567c59f7ea8ac3ef6df96096e726a2fe27ec
- Audit issue mới: https://github.com/Kidtomboy/Che779-EmulatorV2/issues/2
- Issue cũ được thay đầu mối: https://github.com/Kidtomboy/Che779-EmulatorV2/issues/1
- Key lifecycle commit: 313b99078f99883fab787c5a967d65c0edea01c1 (24/11).

## Bằng chứng source/docs
- docs/DEBUG_LOG.md blob 4ebde238fd2a91aa102e4f842802370fade88af2 vẫn ghi các thay đổi 24/11 là “chưa commit/chưa chạy thật”; trạng thái “chưa commit” đã lỗi thời do commit 313b990... tồn tại trong lịch sử.
- docs/TESTING.md blob 541dde64812e611cc4b0512a515dc56c0ee6f502: IPC reader/build tests; Windows 12 tabs và Linux Xvfb có kết quả ghi nhận; release workflow, persistence failure, multi-tab stress và proxy/mouse scenarios chưa xác minh.
- Source V2Main.java blob 85b4b86e673a76465df38f9bc2138c271e8b0e6e đã được đọc; saveTabs có temp + ATOMIC_MOVE/fallback.
- Theo source review trước đó trong phiên audit: Slot dùng ThreadGroup best-effort interrupt, LOAD_LOCK cho close state; MIDletBridge synchronized map/clear theo slot; UI callbacks marshal EDT; saveSlotProxies vẫn lưu user/pass plaintext trong tab_proxies.txt.

## Việc còn mở
1. Thread/Timer/socket sau đóng tab; tab khác vẫn chạy.
2. Cách ly hai game có class name trùng: Display/EventQueue/RMS/proxy/session.
3. Race close/open cùng slot.
4. EDT callback regression.
5. Mô phỏng lỗi ghi/mất điện cho tabs persistence.
6. Plaintext proxy credentials.
7. IPC authentication và platformRequest limitation.
8. Đồng bộ DEBUG_LOG/TESTING với commit history.

## Test status
- CI/script/IPC tests: có bằng chứng trong docs.
- GUI/lifecycle stress, crash persistence, isolation: NOT_RUN trong handoff này.
- Không có runtime multi-game verification mới được thực hiện trong phiên này.

## Handoff rules
Claude phải fetch main, đối chiếu commit 313b990..., sửa document drift bằng trạng thái chính xác (COMMITTED không đồng nghĩa TESTED), đề xuất test plan/acceptance criteria; không refactor lớn hoặc sửa source khi chưa được Cherry duyệt.

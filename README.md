<div align="center">

# 🖥️ Che779 Emulator V2

**Giả lập J2ME nhiều tab trong một cửa sổ — một tiến trình Java, không cần chương trình điều khiển riêng.**

Dựa trên MicroEmulator 3.0.0, thêm lớp Che779: tab, lưới, proxy theo tab, đồng bộ thao tác và dọn RAM.

[![Java 8+](https://img.shields.io/badge/Java-8%2B-ED8B00?logo=openjdk&logoColor=white)](#-chạy)
[![Phiên bản](https://img.shields.io/badge/phiên%20bản-0.0.0.2%20custom-blue)](CHANGELOG.md)
[![Giấy phép](https://img.shields.io/badge/giấy%20phép-Apache%202.0-orange)](LICENSE)
[![Trạng thái](https://img.shields.io/badge/trạng%20thái-thử%20nghiệm-yellow)](#-trạng-thái-và-giới-hạn)

[Tính năng](#-tính-năng) · [Chạy](#-chạy) · [Build](#-build-từ-mã-nguồn) · [IPC](#-ipc) · [Tài liệu](#-tài-liệu) · [Ghi công](#-ghi-công-và-giấy-phép)

</div>

---

## 🌐 Hệ sinh thái Che779

Che779 gồm 4 dự án, mỗi dự án một repo. Bạn đang xem **Project 2**.

| # | Dự án | Chạy trên | Dùng khi nào |
|:-:|---|---|---|
| 1 | [**Che779-Emulator**](https://github.com/Kidtomboy/Che779-Emulator) | Windows | Mỗi tab một cửa sổ, điều khiển bằng Remoter |
| 2 | **Che779-EmulatorV2** ← *bạn đang ở đây* | Windows / Java 8+ | Nhiều tab trong một cửa sổ, một tiến trình |
| 3 | [**Che779-Linux**](https://github.com/Kidtomboy/Che779-Linux) | Linux và Termux | Gói chạy trên Linux và Android (Termux) |
| 4 | [**Che779-Android**](https://github.com/Kidtomboy/Che779-Android) | Android | Chạy trực tiếp trên điện thoại bằng APK |

**V1 hay V2?** V1 cho mỗi game một tiến trình riêng nên cách ly tốt nhưng tốn RAM. V2 gom các tab vào một tiến trình nên nhẹ hơn và mở là dùng; đổi lại một lỗi nặng ở tầng Java có thể ảnh hưởng cả cửa sổ.

---

## ✨ Tính năng

| | Tính năng | Mô tả |
|:-:|---|---|
| 🗂️ | **Nhiều tab, một cửa sổ** | Mỗi tab có thiết bị, RMS (dữ liệu lưu game), class loader và proxy riêng |
| 🔲 | **Chế độ Tab / Lưới** | Mặc định mở sẵn 12 tab; kích thước màn hình tab từ 176×208 đến 360×640 |
| 🛡️ | **Tab lỗi không kéo sập tab khác** | Có nút *Khởi động lại game* cho tab bị lỗi |
| 🔁 | **Đồng bộ phím và chuột** | Chọn các tab tham gia, thao tác một tab được nhân ra các tab còn lại |
| 🌐 | **Proxy theo tab** | Mỗi tab một proxy, hoặc chia vòng từ kho proxy; HTTP và SOCKS5 |
| 🔀 | **Thay IP máy chủ** | Ưu tiên `server.txt` trong jar game, rồi máy chủ chọn từ `servers.txt`, rồi tự nhập |
| 🧹 | **Dọn RAM** | Dọn ngay hoặc tự dọn theo ngưỡng HEAP 60–90% |
| 😴 | **Menu Che779** | Sleep, tự Sleep, giảm FPS, tốc độ game, giảm đồ họa, màu |
| 📊 | **Thanh trạng thái** | CPU và HEAP của cả tiến trình, nút Resize |
| 🔌 | **IPC** | Điều khiển từ chương trình khác qua TCP loopback |

---

## ▶️ Chạy

Cần **Java 8 trở lên**.

```bash
java -jar Che779_EmulatorV2.jar
```

Mở sẵn game:

```bash
java -jar Che779_EmulatorV2.jar /duong/dan/game.jar
```

Không truyền game thì V2 mở lại các tab của lần trước (`tabs.txt`). Dữ liệu cấu hình, proxy và RMS nằm ngoài thư mục chương trình:

| Hệ điều hành | Thư mục dữ liệu |
|---|---|
| Windows | `%APPDATA%\Che779\Che779-EmulatorV2` |
| Linux | `~/.che779` |

Thư viện JNA là **tùy chọn** (để dọn RAM sâu hơn trên Windows); không có thì V2 dùng PowerShell.

> Muốn chạy trên Linux hoặc Termux với một lệnh duy nhất? Xem **[Che779-Linux](https://github.com/Kidtomboy/Che779-Linux)**.

---

## 🔧 Build từ mã nguồn

| Hệ điều hành | Lệnh |
|---|---|
| Linux / macOS | `cd emulator-src && ./build.sh` |
| Windows CMD | `emulator-src\build_emulatorV2.cmd` |
| Windows PowerShell | `powershell -ExecutionPolicy Bypass -File emulator-src\build_emulatorV2.ps1` |

Mã được biên dịch cho Java 8 (`-source 8 -target 8`), chạy được với JDK 8 đến JDK mới. JDK mới có thể in cảnh báo bootstrap class path; đây không phải lỗi.

Kiểm tra: `bash tests/run-tests.sh` — biên dịch emulator, chạy test bộ đọc IPC và kiểm tra cấu trúc repo.

<details>
<summary><b>Cấu trúc thư mục</b></summary>

| Đường dẫn | Nội dung |
|---|---|
| `emulator-src/src/` | Mã nguồn Java |
| `emulator-src/res/` | Tài nguyên giao diện và thiết bị |
| `emulator-src/stubs/` | Stub JNA dùng khi biên dịch |
| `emulator-src/lib/asm-3.2.jar` | Thư viện ASM |
| `Che779_EmulatorV2.jar` | Bản build gần nhất, chạy được ngay |
| `docs/` | Kiến trúc, giao thức, build, bảo mật, trạng thái, bàn giao |
| `tests/` | Kiểm tra hồi quy và cấu trúc |
| `.github/workflows/ci.yml` | CI trên Linux và Windows |

</details>

---

## 🔌 IPC

Cổng nghe chỉ trên `127.0.0.1`, mặc định từ **7790** (thử tối đa 200 cổng kế tiếp nếu bận). Giao thức là JSON một dòng, mỗi dòng một đối tượng phẳng. Giới hạn 16 kết nối, 64 KiB mỗi dòng, chờ đọc 10 giây. Chi tiết ở [`docs/IPC_PROTOCOL.md`](docs/IPC_PROTOCOL.md).

> Chưa có xác thực: tiến trình khác trên cùng máy biết cổng thì có thể gửi lệnh. IPC không mở ra mạng LAN.

---

## 🧭 Trạng thái và giới hạn

- Build và test hồi quy trên **Linux** đã chạy. Bản build **Windows** chỉ đáng tin khi workflow Windows chạy thật.
- Jar V2 đã được mở thử trên Linux (màn hình ảo) và nạp được game thật; chưa thử hết bộ game của cộng đồng.
- Chưa có kiểm thử chịu tải nhiều tab, kiểm thử mất điện giữa lúc ghi dữ liệu, hay kiểm thử Android/Termux trực tiếp.
- Còn một số trạng thái toàn cục dùng chung giữa các tab và hàm xóa tab chưa dùng chung khóa với hàm nạp game; hai điểm này nằm trong danh sách việc cần làm tiếp.
- Một vài game không mở được liên kết ngoài (`platformRequest` chỉ hiện thông báo).

---

## 📚 Tài liệu

[Trạng thái dự án](docs/PROJECT_STATE.md) · [Build và kiểm thử](docs/BUILD_AND_TEST.md) · [IPC](docs/IPC_PROTOCOL.md) · [Bảo mật](docs/SECURITY.md) · [Kiểm kê nguồn](docs/SOURCE_INVENTORY.md) · [Bàn giao](docs/AI_HANDOFF.md) · [Third-party notices](THIRD_PARTY_NOTICES.md) · [Changelog](CHANGELOG.md)

---

## 🙏 Ghi công và giấy phép

Dựa trên **[MicroEmulator](https://github.com/barteo/microemu)** 3.0.0 (Apache License 2.0). Một số ý tưởng chức năng được tham khảo từ HERO-Emulator-V2 của **Hùng Hero** với sự đồng ý của tác giả; mã Che779 được viết lại. Giữ nguyên thông báo bản quyền khi phân phối lại; xem [`LICENSE`](LICENSE) và [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md).

<div align="center">

Làm bởi **Cherry** · [Báo lỗi](../../issues)

</div>

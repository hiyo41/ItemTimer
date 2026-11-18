# 📦 ItemTimer

<div align="center">
  <img src="https://img.shields.io/badge/Version-1.3.1-orange.svg" alt="Version">
  <img src="https://img.shields.io/badge/Minecraft-1.21+-green.svg" alt="Minecraft Version">
  <img src="https://img.shields.io/badge/API-Paper-blue.svg" alt="API">
  <img src="https://img.shields.io/badge/Java-21-red.svg" alt="Java">
  <img src="https://img.shields.io/badge/License-Apache--2.0-lightgrey.svg" alt="License">
</div>

---

> ### ⚠️ ItemTimer chỉ để làm đẹp — không phải plugin giảm lag
>
> ItemTimer hiển thị đếm ngược trên item rơi và có thể xoá item sớm hơn vanilla.
> Nó **không giúp giảm lag đáng kể**. Plugin còn quét item mỗi giây để cập nhật
> name tag, nên có thể **tốn thêm CPU** trên server đông item.
>
> Nếu mục tiêu là giảm lag, hãy dùng plugin giới hạn entity/clearlag và profile
> server trước. ItemTimer không thay thế các plugin đó.
>
> **English:** ItemTimer is a **cosmetic** plugin. It does **not** meaningfully
> reduce lag — it scans dropped items every second and updates name tags, which
> costs CPU. Use a proper entity limiter/clearlag plugin if lag is your problem.

## 🌏 Language / Ngôn ngữ
[**English**](#english) | [**Tiếng Việt**](#tiếng-việt)

---

## English

### What it does
ItemTimer draws a countdown above dropped items and optionally removes them when
the timer reaches zero. It is a visual/quality-of-life plugin, not a performance
plugin.

### Performance
This is a cosmetic plugin, not a lag fix. The countdown task runs every second
and updates each tracked item's name tag, so a large number of dropped items
costs CPU. Early removal can reduce entity count slightly, but it is not a
substitute for an entity limiter/clearlag plugin.

### Features
- Countdown name tags with item name, amount and progress bar.
- Custom timers per material, plus a default timer.
- Whitelist/blacklist items and worlds.
- Pulsing color and low-time sound.
- Translatable item names (client language).
- `/it reload`, `/it status`, `/it check` and update notifications.
- All messages and display formatting are configurable.

### Requirements
- Paper 1.21+ (or a compatible fork)
- Java 21
- No database, no external dependencies

### Installation
1. Build or download `ItemTimer.jar`.
2. Put it in `plugins/`.
3. Start or restart the server.
4. Edit `plugins/ItemTimer/config.yml` and `messages.yml`.
5. Run `/it reload`.

### Commands & permissions
| Command | Description | Permission |
| :--- | :--- | :--- |
| `/it help` | Show help | *None* |
| `/it status` | Show tracked items and uptime | *None* |
| `/it reload` | Reload config and messages | `itemtimer.admin` |
| `/it check` | Check GitHub for a new release | `itemtimer.admin` |

### Configuration
| Key | Default | Description |
| :--- | :--- | :--- |
| `timers.default` | `60` | Default lifetime in seconds |
| `timers.custom` | `DIAMOND: 300` | Per-material overrides |
| `features.display-name-tags.enable` | `true` | Show countdown name tags |
| `features.progress-bar.enable` | `true` | Show the progress bar |
| `features.pulsing.enable` | `true` | Pulse when time is low |
| `features.item-removal.enable` | `true` | Remove items at zero |
| `features.low-time-sound.enable` | `true` | Play a sound near the end |
| `worlds.enabled` / `worlds.disabled` | `[]` | World filters |
| `items.excluded` / `items.whitelist` | see config | Item filters |
| `check-update` | `true` | Check GitHub on startup |

### Build
```bash
mvn -B package
# target/ItemTimer.jar
```

### Project status
This project is in maintenance mode: no new features are planned, but bug
reports and pull requests are welcome.

### Links
- GitHub: <https://github.com/hiyo41/ItemTimer>
- Issues: <https://github.com/hiyo41/ItemTimer/issues>

---

## Tiếng Việt

### Công dụng
ItemTimer vẽ đồng hồ đếm ngược trên item rơi và tùy chọn xoá item khi hết giờ.
Đây là plugin trang trí/tiện ích, **không phải plugin tăng hiệu năng**.

### Hiệu năng
Đây là plugin trang trí, không phải giải pháp chống lag. Task đếm ngược chạy mỗi
giây và cập nhật name tag của từng item, nên server có nhiều item rơi sẽ tốn
CPU. Việc xoá sớm có thể giảm số entity một chút, nhưng không thay thế được
plugin giới hạn entity/clearlag.

### Tính năng
- Name tag đếm ngược kèm tên item, số lượng và thanh tiến trình.
- Thời gian riêng cho từng material, cộng thời gian mặc định.
- Whitelist/blacklist item và world.
- Nhấp nháy màu và âm thanh khi sắp hết giờ.
- Tên item theo ngôn ngữ client (translatable).
- `/it reload`, `/it status`, `/it check` và thông báo cập nhật.
- Toàn bộ message và format hiển thị đều cấu hình được.

### Yêu cầu
- Paper 1.21+ (hoặc fork tương thích)
- Java 21
- Không cần database, không có dependency ngoài

### Cài đặt
1. Build hoặc tải `ItemTimer.jar`.
2. Bỏ vào `plugins/`.
3. Khởi động hoặc khởi động lại server.
4. Sửa `plugins/ItemTimer/config.yml` và `messages.yml`.
5. Chạy `/it reload`.

### Lệnh & quyền
| Lệnh | Mô tả | Quyền |
| :--- | :--- | :--- |
| `/it help` | Hiển thị trợ giúp | *Không có* |
| `/it status` | Xem số item đang theo dõi và uptime | *Không có* |
| `/it reload` | Tải lại config và messages | `itemtimer.admin` |
| `/it check` | Kiểm tra bản mới trên GitHub | `itemtimer.admin` |

### Cấu hình
| Key | Mặc định | Mô tả |
| :--- | :--- | :--- |
| `timers.default` | `60` | Thời gian sống mặc định (giây) |
| `timers.custom` | `DIAMOND: 300` | Ghi đè theo material |
| `features.display-name-tags.enable` | `true` | Hiện name tag đếm ngược |
| `features.progress-bar.enable` | `true` | Hiện thanh tiến trình |
| `features.pulsing.enable` | `true` | Nhấp nháy khi sắp hết giờ |
| `features.item-removal.enable` | `true` | Xoá item khi hết giờ |
| `features.low-time-sound.enable` | `true` | Phát âm thanh khi sắp hết giờ |
| `worlds.enabled` / `worlds.disabled` | `[]` | Lọc theo world |
| `items.excluded` / `items.whitelist` | xem config | Lọc theo item |
| `check-update` | `true` | Kiểm tra GitHub khi khởi động |

### Build
```bash
mvn -B package
# target/ItemTimer.jar
```

### Trạng thái dự án
Dự án đang ở trạng thái bảo trì: không có tính năng mới, nhưng vẫn nhận báo lỗi
và pull request.

### Liên kết
- GitHub: <https://github.com/hiyo41/ItemTimer>
- Issues: <https://github.com/hiyo41/ItemTimer/issues>

---

<div align="center">
  Developed with ❤️ by <b>Hiyo41</b>
</div>

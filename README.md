# Appxaml

⚡ Ứng dụng Android (Đèn pin pro max) — `com.NguyenHoang.appxaml`

---

## 🔧 Tổng quan

Ứng dụng Android được phát triển bằng Kotlin và Gradle (Kotlin DSL). Dự án này sử dụng Java 11, Material Components và ConstraintLayout. Namespace / applicationId: `com.NguyenHoang.appxaml`.

## 🧩 Yêu cầu

- JDK 11
- Android Studio Flamingo (hoặc tương đương hỗ trợ Gradle 8+) hoặc mới hơn
- Gradle Wrapper (đã kèm theo dự án)
- Android SDK (API 36)

## 🚀 Cách build & chạy

Trên Windows (PowerShell) từ thư mục gốc dự án:

- Cài dependencies và build debug:

```powershell
./gradlew.bat assembleDebug
```

- Chạy ứng dụng trên thiết bị/emulator:

```powershell
./gradlew.bat installDebug
```

- Chạy unit tests:

```powershell
./gradlew.bat test
```

- Chạy instrumentation tests (thiết bị/ emulator cần kết nối):

```powershell
./gradlew.bat connectedAndroidTest
```

> Gợi ý: Mở dự án bằng Android Studio: chọn `Open` -> chọn thư mục chứa `settings.gradle.kts`.

## 🧭 Cấu trúc chính

- `app/` — module ứng dụng chính
- `build.gradle.kts`, `settings.gradle.kts` — cấu hình Gradle ở mức workspace
- `gradle/` — cấu hình wrapper và phiên bản thư viện (`libs.versions.toml`)

## 📦 Thư viện chính

- Material Components (`com.google.android.material:material`)
- ConstraintLayout (`androidx.constraintlayout:constraintlayout`)
- ViewPager2
- AndroidX Core, AppCompat

(Thông tin chi tiết về version được quản lý trong `gradle/libs.versions.toml`.)

## 🧪 Testing

- Unit tests: `./gradlew.bat test`
- Instrumentation tests: `./gradlew.bat connectedAndroidTest`

## 👥 Contributing

- Mở issue để thảo luận tính năng hoặc bug
- Gửi Pull Request kèm mô tả chi tiết và steps để reproduce (nếu có)

## 📄 License

Chưa có `LICENSE`.

## 📝 Ghi chú

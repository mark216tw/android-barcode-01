# 簡單條碼

「簡單條碼」是一款以快速、離線及容易操作為目標的 Android 一維條碼工具。使用者可以建立及管理常用條碼，或將條碼放到桌面小工具，需要出示會員卡、產品碼或門禁條碼時即可快速開啟。

> [!WARNING]
> GitHub Release 提供的是 **Pre-release 測試版本**，僅供功能測試及體驗，不是正式發布版本。APK 已啟用 R8 壓縮，但使用 Debug 金鑰簽署，不建議用於正式環境或長期保存重要資料。

## 主要功能

- 建立、即時預覽及驗證一維條碼
- 搜尋、最愛、編輯及刪除已儲存的條碼
- 條碼詳細頁自動提高螢幕亮度並防止休眠
- 將條碼分享為 PNG 圖片
- 支援可調整大小的 `1 x N` Android 桌面小工具
- 每個桌面小工具可選擇不同條碼
- 六種活潑主題色與自訂 Hue 彩色滑桿，調整後立即套用
- 支援跟隨系統、淺色及深色模式
- 狀態列及系統導覽列會配合介面明暗調整
- 全程離線，不要求相機及網路權限

## 支援格式

| 格式 | 輸入內容 |
|---|---|
| Code 128 | 英文字母、數字及符號 |
| Code 39 | 大寫英文字母、數字及部分符號 |
| EAN-13 | 12 或 13 位數字 |
| EAN-8 | 7 或 8 位數字 |
| UPC-A | 11 或 12 位數字 |
| ITF | 偶數位數字 |
| Codabar | 數字、特定符號及起訖字元 |

## 安裝 Pre-release APK

1. 前往本專案的 [Releases](../../releases) 頁面。
2. 開啟標示為 `Pre-release` 的版本。
3. 下載 `app-prerelease.apk`。
4. 在 Android 裝置允許瀏覽器或檔案管理器安裝未知來源應用程式。
5. 開啟 APK 完成安裝。

完整操作方式請參閱[使用說明](docs/使用說明.md)。

## 系統需求

- Android 8.0（API 26）以上
- 桌面小工具功能需要支援 Android App Widget 的啟動器

## 開發環境

- Java 17
- Android SDK 36
- Kotlin
- Jetpack Compose 與 Material 3
- ZXing Core
- Gradle Wrapper 8.11.1

## 建置與驗證

Windows PowerShell：

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

macOS 或 Linux：

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Debug APK 產生於：

```text
app/build/outputs/apk/debug/app-debug.apk
```

使用 R8 壓縮及 Debug 金鑰簽署的 Pre-release APK：

```powershell
.\gradlew.bat assemblePrerelease
```

```text
app/build/outputs/apk/prerelease/app-prerelease.apk
```

更多架構及發布資訊請參閱[開發文件](docs/開發文件.md)。

## 隱私

- 條碼資料儲存在裝置本機的應用程式資料中。
- APP 不要求網路、相機、定位、聯絡人或儲存空間權限。
- 分享條碼時，圖片會暫存在 APP 快取目錄，並透過 Android 分享面板交由使用者選擇的應用程式處理。

## 授權

本專案依照 [MIT License](LICENSE) 公開授權。第三方套件仍適用各自的授權條款。

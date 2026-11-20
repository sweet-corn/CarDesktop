# CarDesk 车机桌面（自定义框架版）

依据用户提供的参考 APK 独立实现，不复制原应用代码、壁纸或图标。项目与参考 APK 包名不同，可并存安装。

## 已实现

- 原生 Android Activity；浅色几何背景、可选大时钟、应用顶部栏和底部半透明卡片布局。
- 全部应用列表、打开已安装应用；导航快捷尝试常见地图包名，找不到时打开应用列表。
- 本地图片和静音循环视频壁纸、持久化读取权限；时钟和上下栏开关、系统设置入口。
- 卡片显隐与左右排序；长按卡片管理。底栏按键可长按绑定应用、隐藏和恢复默认功能。
- 应用内沉浸模式请求隐藏系统栏，不禁用系统组件；DiLink 是否允许隐藏需真机确认。
- 使用普通 `LAUNCHER` 入口，额外声明原 APK 出现的非标准 `HOME_ONLY` 类别；**不声明系统 HOME、不改默认桌面、不请求特权或自动启动权限**。HOME_ONLY 的实际行为依赖车机，不能保证回到桌面或绕过任何厂商限制。
- 长按导航卡片选择默认高德／百度／腾讯地图；音乐卡片支持媒体会话封面、歌名、播放暂停、下一曲及播放器选择。首次控制需手动开启 CarDesk 通知使用权，并在音乐应用播放歌曲。
- DiLink 实验适配：桌面显示四轮胎压、最近百公里电耗／油耗和本次里程；底栏显示主副驾温度、空调开关和风量。仅在读到有效状态后开放需逐次确认的空调开关、风量操作。
- 导航卡片 `3:7`：左 30% 为 CarDesk 音乐面板，右 70% 为选中地图的虚拟屏；不调用系统画中画或系统分屏。地图启动、画面和触控受车机固件及地图版本限制，暂只尝试单指点击／滑动，不支持双指缩放，保留全屏地图入口。
- 不接管其他应用的导航栏，不读写驾驶控制、影像或方控。音乐兼容性取决于播放器是否提供标准媒体会话。

长按空白壁纸进入设置。最新桥接与分屏实测清单见 [docs/DILINK_VALIDATION.md](docs/DILINK_VALIDATION.md)；早期实现记录见 [docs/IMPLEMENTATION.md](docs/IMPLEMENTATION.md)。

## 应用内无线 ADB（实验）

默认每次启动自动尝试本机 `127.0.0.1:5555`，首次只需在系统调试授权弹窗勾选“始终允许”并确认。设置 → 系统设置可关闭自动连接；“无线ADB连接”页面支持手动重试和断开。仅默认连接失败时需要展开高级配置调整地址和端口。此应用使用自己的密钥，不继承参考APK授权，无需电脑执行命令。

仅支持 Android 10 风格的传统 TCP ADB，不支持配对码/TLS，不自行开启调试，只允许连接本机。车辆快照沿已授权的 ADB 会话回传，不再使用单独的本地 socket；会话上限为 30 分钟。断开时立即清除桌面读数。空调写入是单独的手动请求，不随连接自动下发。仅在停车和可信网络环境测试，不将 ADB 端口暴露到不可信网络。

ADB 认证成功不表示厂商接口有权限。“设置 → 系统设置 → 车辆只读桥接”分别显示启动阶段和每个厂商接口的读数／错误。普通手机没有 BYD SDK 或没有开放传统 TCP ADB，缺少车辆数据是预期行为。

## 构建

使用 Android Studio 打开本目录。需要 JDK 17、Android SDK Platform 34 和 Gradle 8.7；配置 `ANDROID_HOME` 或 Android Studio 的 SDK 路径后执行：

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

输出 `app/build/outputs/apk/debug/app-debug.apk`。Android Gradle Plugin 从 Google Maven 下载，需要配置网络；debug APK 使用本机调试签名，不覆盖原 APK。

本轮独立 Java 编译和 5 项单元测试通过；本机 Gradle 8.7 下载超时，未产出 APK，尚未在 DiLink 实车验证。可用仓库现有的 `Build Android APK` GitHub Actions 工作流构建。Android 11+ 的应用可见性及车机定制可能限制可查询、启动的应用。

## 安全测试

只在停车状态测试；保留原厂桌面及返回入口，不卸载原厂应用、不设置默认桌面。使用 `adb install -r app-debug.apk` 手动安装，启动：

```powershell
adb shell am start -n com.cardesk.home/.MainActivity
adb logcat -s AndroidRuntime ActivityTaskManager
```

如发现读数缺失或地图黑屏，请按实测清单记录诊断页面、地图版本和表现，以区分桥接启动、厂商接口权限与虚拟屏兼容性问题。

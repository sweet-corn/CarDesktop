# CarDesk 车机桌面（自定义框架版）

依据用户提供 APK 的布局结构独立实现，不包含原应用代码、壁纸、图标、品牌或车辆控制接口。项目与原 APK 包名不同，可并存安装。

## 已实现

- 原生 Android Activity；浅色几何背景、可选大时钟、应用顶部栏和底部半透明卡片布局。
- 全部应用列表、打开已安装应用；导航快捷尝试常见地图包名，找不到时打开应用列表。
- 本地图片和静音循环视频壁纸、持久化读取权限；时钟和上下栏开关、系统设置入口。
- 卡片显隐与左右排序；长按卡片管理。底栏按键可长按绑定应用、隐藏和恢复默认功能。
- 应用内沉浸模式请求隐藏系统栏，不禁用系统组件；DiLink 是否允许隐藏需真机确认。
- 使用普通 `LAUNCHER` 入口，额外声明原 APK 出现的非标准 `HOME_ONLY` 类别；**不声明系统 HOME、不改默认桌面、不请求特权或自动启动权限**。HOME_ONLY 的实际行为依赖车机，不能保证回到桌面或绕过任何厂商限制。
- 不跨应用接管系统导航栏，不实现车控、车速、影像、方控、媒体控制、开机自启及真实导航小组件；车辆数据明确标为未连接。默认底栏返回键进入桌面设置，并非全局返回。

长按空白壁纸进入设置。目标设备与 APK 分析记录、当前限制和测试清单见 [docs/IMPLEMENTATION.md](docs/IMPLEMENTATION.md)。当前版本为第一阶段框架，尚非原界面的完整视觉还原。

## 构建

使用 Android Studio 打开本目录。需要 JDK 17、Android SDK Platform 34 和 Gradle 8.7；配置 `ANDROID_HOME` 或 Android Studio 的 SDK 路径后执行：

```powershell
.\gradlew.bat :app:assembleDebug
```

输出 `app/build/outputs/apk/debug/app-debug.apk`。Android Gradle Plugin 从 Google Maven 下载，需要配置网络；debug APK 使用本机调试签名，不覆盖原 APK。

初版已由用户通过 GitHub Actions 构建并在手机验证。本轮自定义框架改动在本开发环境缺少 Android SDK/JDK 17，**尚未完成构建或真机验证**。安装与实际兼容性需要在目标车机测试。Android 11+ 的应用可见性及车机定制可能限制可查询、启动的应用。

## 安全测试

只在停车状态测试；保留原厂桌面及返回入口，不卸载原厂应用、不设置默认桌面。使用 `adb install -r app-debug.apk` 手动安装，启动：

```powershell
adb shell am start -n com.cardesk.home/.MainActivity
adb logcat -s AndroidRuntime ActivityTaskManager
```

请提供车型、DiLink 与 Android 版本、车机分辨率、原桌面截图及启动日志，才能进一步确认兼容性与还原实际布局。

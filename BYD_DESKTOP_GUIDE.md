# 比亚迪车机桌面 - 使用指南

## 🎨 新界面特性

### 界面1：主桌面（默认）

- **顶部状态栏**：PRND档位、油量（%）、电量（带进度条）、车外温度、系统状态图标、实时时间
- **中央区域**：背景图展示区（可替换为壁纸/3D模型）
- **信息卡片栏**：
  - 🚗 胎压监测卡片（四轮实时压力值）
  - 📊 本次行程信息（距离、电耗、时长）
  - 🔧 车辆控制面板（主驾锁、驾驶模式、后备箱）
  - ⚡ 快捷应用组（智能助手、蓝牙电话、视频、设置）
- **底部Dock栏**：主页、天气（带温度）、音乐、导航、应用抽屉、更多

### 界面2：时钟屏（可选）

- **超大时钟显示**：72sp大字体时间 + 完整日期
- **底部功能卡片**：
  - 导航快捷方式（回家/去公司）
  - 媒体播放器（播放控制）
  - 车辆控制（四门锁/后备箱）
  - 行程摘要 + 胎压信息

## 🎯 核心组件说明

### 1. BYDStatusBar (状态栏)

```kotlin
BYDStatusBar(
    gear = "P",           // 档位: P/R/N/D
    fuelLevel = 60,       // 油量 0-100%
    batteryLevel = 99,    // 电量 0-100%
    temperature = 23,     // 车外温度 °C
    time = "15:28"        // 时间
)
```

### 2. TirePressureCard (胎压卡片)

```kotlin
TirePressureCard(
    frontLeft = 282f,     // 左前 kPa
    frontRight = 282f,    // 右前 kPa
    rearLeft = 277f,      // 左后 kPa
    rearRight = 280f      // 右后 kPa
)
```

### 3. TripInfoCard (行程卡片)

```kotlin
TripInfoCard(
    distance = 0.0f,           // 行程距离 km
    energyConsumption = 0.0f,  // 电耗 kWh
    duration = "3分钟"         // 时长
)
```

### 4. VehicleControlPanel (车辆控制)

```kotlin
VehicleControlPanel(
    onVehicleClick = { /* 车辆总览 */ },
    onLockClick = { /* 主驾锁 */ },
    onModeClick = { /* 驾驶模式 */ }
)
```

### 5. QuickAppsRow (快捷应用)

```kotlin
QuickAppsRow(
    apps = listOf(
        QuickAppItem("智能助手", "💡"),
        QuickAppItem("蓝牙电话", "📞"),
        QuickAppItem("优酷视频", "▶️"),
        QuickAppItem("设置", "⚙️")
    ),
    onAppClick = { app -> /* 处理点击 */ }
)
```

### 6. BYDDockBar (底栏)

```kotlin
BYDDockBar(
    onHomeClick = {},
    onWeatherClick = {},
    onMusicClick = {},
    onNavClick = {},
    onAppDrawerClick = { /* 打开应用抽屉 */ },
    onSettingsClick = { /* 打开设置 */ }
)
```

## 🎨 主题配色

### 主要颜色

- **Primary**: #00D4FF (科技蓝)
- **Accent**: #FF6B35 (活力橙)

### 功能颜色

- **电量高** (>60%): 绿色 #4CAF50
- **电量中** (20-60%): 黄色 #FFC107
- **电量低** (<20%): 红色 #FF5252
- **胎压正常** (230-320kPa): 绿色
- **胎压异常**: 黄色警告

## 📐 尺寸规范

### 触摸目标（车机安全标准）

- **TouchTarget**: 72dp (最小触摸区域)
- **TouchTargetLarge**: 88dp (重要按钮)

### 字体大小

- **FontTimeLarge**: 72sp (时钟)
- **FontCardValue**: 32sp (数据卡片)
- **FontStatus**: 20sp (状态栏)
- **FontBody**: 18sp (正文)
- **FontCaption**: 15sp (辅助文字)

### 圆角

- **RadiusL**: 20dp (卡片)
- **RadiusM**: 16dp (按钮)
- **RadiusS**: 12dp (小元素)

## 🔧 自定义配置

### 切换界面模式

在 `MainActivity.kt` 中选择显示哪个屏幕：

```kotlin
// 方式1：主桌面（带背景图+信息卡片）
MainScreen()

// 方式2：时钟屏（大时钟+功能卡片）
ClockScreen()
```

### 替换背景图

在 `MainScreen.kt` 第78-91行修改背景：

```kotlin
// 当前：占位符
Text(text = "🏛️", fontSize = 120.sp, ...)

// 替换为：实际壁纸图片
Image(
    painter = painterResource(R.drawable.wallpaper),
    contentDescription = "壁纸",
    modifier = Modifier.fillMaxSize().alpha(0.3f),
    contentScale = ContentScale.Crop
)
```

### 连接真实车辆数据

目前所有数据都是模拟值，你可以通过以下方式接入真实数据：

1. **通过Vehicle Service获取**：

   ```kotlin
   // 在ViewModel中添加
   val vehicleService = VehicleService.getInstance()
   val tirePressure = vehicleService.getTirePressure()
   ```

2. **通过BroadcastReceiver接收**：
   ```kotlin
   // 注册车辆状态广播接收器
   val receiver = object : BroadcastReceiver() {
       override fun onReceive(context: Context?, intent: Intent?) {
           when(intent?.action) {
               "com.byd.vehicle.TIRE_PRESSURE" -> {
                   // 更新胎压数据
               }
           }
       }
   }
   ```

## 📱 构建与运行

### 前置要求

- Android Studio Hedgehog 或更高版本
- SDK 34 (Android 14)
- 目标设备：车机平板/中控屏

### 编译步骤

1. 打开 Android Studio
2. 导入项目 `CarDesktop`
3. 同步 Gradle
4. 连接真机或启动模拟器
5. 点击运行 ▶️

### 推荐测试设备

- 比亚迪 DiLink 3.0/4.0/5.0 系统
- 屏幕分辨率：1920x720 或更高
- Android 10+ 系统

## ✨ 下一步优化建议

1. **添加真实壁纸支持**
   - 从相册选择壁纸
   - 动态壁纸（视频/GIF）
   - 在线壁纸下载

2. **接入车辆CAN总线数据**
   - 实时车速、转速
   - 油量/电量精确数值
   - 四轮胎压监测
   - 车门/车窗状态

3. **增强交互体验**
   - 卡片滑动切换
   - 手势操作（上滑打开通知）
   - 语音助手集成
   - 多任务分屏

4. **个性化设置**
   - 主题色自定义
   - 布局调整（左舵/右舵）
   - Dock栏图标排序
   - 快捷应用编辑

5. **性能优化**
   - 懒加载组件
   - 内存管理
   - 动画流畅度优化
   - 低功耗模式

## 📄 文件结构说明

```
app/src/main/java/com/cardesktop/
├── ui/
│   ├── screen/
│   │   ├── MainScreen.kt          # 主桌面（界面1）⭐
│   │   ├── ClockScreen.kt         # 时钟屏（界面2）⭐ 新增
│   │   ├── MainActivity.kt        # Activity入口
│   │   ├── MainViewModel.kt       # 数据逻辑
│   │   ├── AppDrawerActivity.kt   # 应用抽屉
│   │   └── SettingsActivity.kt    # 设置页面
│   ├── widget/
│   │   ├── Widgets.kt             # UI组件库 ⭐ 重写
│   │   ├── DockBar.kt             # 底部栏 ⭐ 重写
│   │   └── AppIcon.kt             # 应用图标
│   └── theme/
│       ├── Color.kt               # 颜色主题 ⭐ 更新
│       └── Dimens.kt              # 尺寸规范 ⭐ 更新
├── data/
│   ├── model/
│   │   └── Models.kt             # 数据模型
│   └── repository/
│       └── AppRepository.kt      # 应用仓库
└── DesktopApp.kt                 # Application类
```

## 🎉 已完成的功能

✅ 比亚迪风格顶部状态栏（PRND档位、油量、电量、温度）
✅ 胎压监测卡片（四轮实时显示）
✅ 本次行程信息卡片
✅ 车辆快捷控制面板
✅ 快捷应用入口组
✅ 专业级底部Dock栏
✅ 大时钟第二屏界面
✅ 导航快捷方式
✅ 媒体播放器控件
✅ 车机适配的大字体和触摸目标
✅ 深色科技风主题配色
✅ 半透明玻璃态效果

## 🐛 已知限制

- 目前使用模拟数据（需接入真实车辆API）
- 图标使用Emoji（可替换为矢量图标/图片）
- 背景图为占位符（需添加实际壁纸）
- 部分功能为空实现（需补充业务逻辑）

---

**开发者提示**：如需进一步定制或有任何问题，请查看代码注释或联系开发团队！

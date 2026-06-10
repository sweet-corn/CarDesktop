package com.cardesktop.ui.screen

import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cardesktop.data.model.AppInfo
import com.cardesktop.ui.theme.*
import com.cardesktop.ui.widget.*

/**
 * 比亚迪车机桌面 - 主屏幕
 *
 * 布局结构（参考图片1）：
 * ┌──────────────────────────────────────────────┐
 * │  状态栏 (PRND | 油量 | 电量 | 温度 | 时间)   │
 * ├──────────────────────────────────────────────┤
 * │                                              │
 * │              背景图区域（建筑/车辆）           │
 * │                                              │
 * ├──────────────────────────────────────────────┤
 * │ [胎压] [行程] [车辆控制] [快捷应用]          │
 * ├──────────────────────────────────────────────┤
 * │  Dock栏 (主页|天气|音乐|导航|...|设置)       │
 * └──────────────────────────────────────────────┘
 */
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val time by viewModel.currentTime.collectAsState()
    val date by viewModel.currentDate.collectAsState()
    val apps by viewModel.apps.collectAsState()
    val shortcuts by viewModel.shortcuts.collectAsState()

    fun launchApp(app: AppInfo) {
        val intent = app.launchIntent?.let {
            Intent().apply {
                component = ComponentName(app.packageName, it)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } ?: context.packageManager.getLaunchIntentForPackage(app.packageName)

        intent?.let { context.startActivity(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ========== 1. 顶部状态栏（比亚迪风格）==========
            BYDStatusBar(
                gear = "P",
                fuelLevel = 60,
                batteryLevel = 99,
                temperature = 23,
                time = time
            )

            // ========== 2. 主内容区域 ==========
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // 背景占位（实际项目中可替换为壁纸）
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceDark.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    // 这里可以放置背景图片或3D渲染
                    // 暂时使用渐变色模拟
                    Text(
                        text = "🏛️",
                        fontSize = 120.sp,
                        color = TextPrimary.copy(alpha = 0.1f)
                    )
                }

                // 底部信息卡片层
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpaceXL, vertical = Dimens.SpaceL),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
                ) {
                    // 第一行：胎压 + 行程 + 车辆控制 + 快捷应用
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 胎压卡片
                        TirePressureCard(
                            frontLeft = 282f,
                            frontRight = 282f,
                            rearLeft = 277f,
                            rearRight = 280f,
                            modifier = Modifier.width(180.dp)
                        )

                        Spacer(modifier = Modifier.width(Dimens.SpaceS))

                        // 行程信息卡片
                        TripInfoCard(
                            distance = 0.0f,
                            energyConsumption = 0.0f,
                            duration = "3分钟",
                            modifier = Modifier.weight(1f).height(Dimens.CardHeightMedium)
                        )

                        Spacer(modifier = Modifier.width(Dimens.SpaceS))

                        // 车辆控制面板
                        VehicleControlPanel(
                            onVehicleClick = { /* 打开车辆总览 */ },
                            onLockClick = { /* 切换主驾锁 */ },
                            onModeClick = { /* 切换驾驶模式 */ },
                            modifier = Modifier.height(Dimens.CardHeightMedium)
                        )

                        Spacer(modifier = Modifier.width(Dimens.SpaceS))

                        // 快捷应用组
                        QuickAppsRow(
                            onAppClick = { app ->
                                when (app.name) {
                                    "智能助手" -> { /* 打开智能助手 */ }
                                    "蓝牙电话" -> { /* 打开电话 */ }
                                    "优酷视频" -> { /* 打开视频 */ }
                                    "设置" -> {
                                        context.startActivity(
                                            Intent(context, SettingsActivity::class.java)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.height(Dimens.CardHeightMedium)
                        )
                    }

                    // 第二行：右侧胎压补充信息（可选）
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 右侧小胎压卡片（简化版）
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SmallTirePressureDisplay(pressure = 257f, label = "")
                            CarIconSmall()
                            SmallTirePressureDisplay(pressure = 260f, label = "")
                        }
                    }
                }
            }

            // ========== 3. 底部 Dock 栏（比亚迪风格）==========
            BYDDockBar(
                onHomeClick = { /* 已在主页 */ },
                onWeatherClick = { /* 显示天气详情 */ },
                onMusicClick = { /* 打开音乐 */ },
                onNavClick = { /* 打开导航 */ },
                onAppDrawerClick = {
                    context.startActivity(
                        Intent(context, AppDrawerActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
                onSettingsClick = {
                    context.startActivity(
                        Intent(context, SettingsActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            )
        }
    }
}

/**
 * 小型胎压显示（用于底部右侧）
 */
@Composable
private fun SmallTirePressureDisplay(
    pressure: Float,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = Dimens.SpaceS)
    ) {
        Text(
            text = "${pressure.toInt()} kPa",
            color = TirePressureNormal,
            fontSize = Dimens.FontCaption,
            fontWeight = FontWeight.Medium
        )
        if (label.isNotEmpty()) {
            Text(
                text = label,
                color = TextHint,
                fontSize = Dimens.FontSmall
            )
        }
    }
}

/**
 * 小型车辆图标
 */
@Composable
private fun CarIconSmall() {
    Text(text = "🚗", fontSize = 36.sp)
}
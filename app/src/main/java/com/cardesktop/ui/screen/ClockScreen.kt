package com.cardesktop.ui.screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.cardesktop.ui.theme.*
import com.cardesktop.ui.widget.*

/**
 * 比亚迪车机桌面 - 时钟界面（第二屏）
 *
 * 布局结构（参考图片2）：
 * ┌──────────────────────────────────────────────┐
 * │  状态栏 (PRND | 油量 | 电量 | 温度 | 时间)   │
 * ├──────────────────────────────────────────────┤
 * │                                              │
 * │            21:14  （超大时钟）               │
 * │         4月14日 星期二 二月廿七              │
 * │                                              │
 * ├──────────────────────────────────────────────┤
 * │ [导航] [媒体] [车辆控制] [行程] [胎压]       │
 * ├──────────────────────────────────────────────┤
 * │  Dock栏 (主页|天气|音乐|导航|...|设置)       │
 * └──────────────────────────────────────────────┘
 */
@Composable
fun ClockScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val time by viewModel.currentTime.collectAsState()
    val date by viewModel.currentDate.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ========== 1. 顶部状态栏 ==========
            BYDStatusBar(
                gear = "P",
                fuelLevel = 30,
                batteryLevel = 52,
                temperature = 16,
                time = time
            )

            // ========== 2. 中央大时钟区域 ==========
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // 背景装饰（几何图形）
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceDark.copy(alpha = 0.5f))
                )

                // 大时钟组件
                LargeClockWidget(
                    time = time,
                    date = date,
                    modifier = Modifier.padding(Dimens.SpaceXXL)
                )
            }

            // ========== 3. 底部功能卡片区域 ==========
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceXL, vertical = Dimens.SpaceM),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
            ) {
                // 功能卡片行：导航 | 媒体播放器 | 车辆控制 | 行程 | 胎压
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 导航快捷方式
                    NavigationShortcuts(
                        onHomeClick = { /* 导航回家 */ },
                        onWorkClick = { /* 导航去公司 */ },
                        modifier = Modifier.weight(1f).height(Dimens.CardHeightSmall)
                    )

                    Spacer(modifier = Modifier.width(Dimens.SpaceS))

                    // 媒体播放器
                    MediaPlayerWidget(
                        isPlaying = false,
                        onPlayPause = { /* 播放/暂停 */ },
                        onNext = { /* 下一首 */ },
                        onPrevious = { /* 上一首 */ },
                        modifier = Modifier.weight(1.5f).height(Dimens.CardHeightSmall)
                    )

                    Spacer(modifier = Modifier.width(Dimens.SpaceS))

                    // 车辆控制（四门锁 + 后备箱）
                    VehicleControlPanel(
                        onVehicleClick = {},
                        onLockClick = { /* 四门锁 */ },
                        onModeClick = { /* 后备箱 */ },
                        modifier = Modifier.weight(1f).height(Dimens.CardHeightSmall)
                    )

                    Spacer(modifier = Modifier.width(Dimens.SpaceS))

                    // 本次行程
                    TripInfoCard(
                        distance = 0.0f,
                        energyConsumption = 0.0f,
                        duration = "3分钟",
                        modifier = Modifier.weight(1f).height(Dimens.CardHeightSmall)
                    )

                    Spacer(modifier = Modifier.width(Dimens.SpaceS))

                    // 胎压信息
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmallTirePressureDisplay(pressure = 257f, label = "")
                        SmallTirePressureDisplay(pressure = 260f, label = "")
                    }
                }
            }

            // ========== 4. 底部 Dock 栏 ==========
            BYDDockBar(
                onHomeClick = {
                    // 切换回主界面
                    // 可以通过Navigation或状态管理实现切换
                },
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
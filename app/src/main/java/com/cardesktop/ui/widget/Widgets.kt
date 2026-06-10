package com.cardesktop.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardesktop.ui.theme.*

/**
 * 比亚迪风格顶部状态栏
 * 包含：PRND档位 | 油量 | 电量 | 温度 | 信号图标 | 时间
 */
@Composable
fun BYDStatusBar(
    gear: String = "P",
    fuelLevel: Int = 60,
    batteryLevel: Int = 99,
    temperature: Int = 23,
    time: String = "15:28",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(StatusBarBg.copy(alpha = 0.6f))
            .padding(horizontal = Dimens.SpaceXL, vertical = Dimens.SpaceM),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：PRND + 油量 + 电量
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PRND 档位显示
            Text(
                text = gear,
                color = Primary,
                fontSize = Dimens.FontStatus,
                fontWeight = FontWeight.Bold
            )

            // 油量
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
            ) {
                Text(
                    text = "油量",
                    color = TextSecondary,
                    fontSize = Dimens.FontCaption
                )
                Text(
                    text = "$fuelLevel%",
                    color = if (fuelLevel > 20) FuelFull else FuelLow,
                    fontSize = Dimens.FontCaption,
                    fontWeight = FontWeight.Medium
                )
            }

            // 电量（带进度条）
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
            ) {
                Text(
                    text = "电量",
                    color = TextSecondary,
                    fontSize = Dimens.FontCaption
                )
                LinearProgressIndicator(
                    progress = batteryLevel / 100f,
                    modifier = Modifier.width(60.dp).height(6.dp),
                    color = when {
                        batteryLevel > 60 -> BatteryHigh
                        batteryLevel > 20 -> BatteryMedium
                        else -> BatteryLow
                    },
                    trackColor = SurfaceLight.copy(alpha = 0.5f)
                )
                Text(
                    text = "$batteryLevel%",
                    color = when {
                        batteryLevel > 60 -> BatteryHigh
                        batteryLevel > 20 -> BatteryMedium
                        else -> BatteryLow
                    },
                    fontSize = Dimens.FontCaption,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 右侧：温度 + 系统状态 + 时间
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 车外温度
            Text(
                text = "车外 ${temperature}°C",
                color = TextSecondary,
                fontSize = Dimens.FontCaption
            )

            // 系统状态图标（模拟）
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SystemStatusIcon(icon = "🔋", label = "")
                SystemStatusIcon(icon = "📶", label = "")
                SystemStatusIcon(icon = "📡", label = "")
                SystemStatusIcon(icon = "🔊", label = "静")
            }

            // 时间
            Text(
                text = time,
                color = TextPrimary,
                fontSize = Dimens.FontStatus,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SystemStatusIcon(icon: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 16.sp)
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
 * 胎压信息卡片 - 带车辆俯视图
 */
@Composable
fun TirePressureCard(
    frontLeft: Float = 282f,
    frontRight: Float = 282f,
    rearLeft: Float = 277f,
    rearRight: Float = 280f,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusL))
            .background(SurfaceDark.copy(alpha = 0.85f))
            .padding(Dimens.SpaceL),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 车辆俯视图（简化版）
        Box(
            modifier = Modifier
                .size(width = 120.dp, height = 80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceLight.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            // 车身轮廓
            Text(text = "🚗", fontSize = 48.sp)

            // 四个轮胎位置的压力值
            Column(modifier = Modifier.fillMaxSize()) {
                // 前轮
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TirePressureValue(value = frontLeft, position = "FL")
                    Spacer(modifier = Modifier.weight(1f))
                    TirePressureValue(value = frontRight, position = "FR")
                }
                Spacer(modifier = Modifier.weight(1f))
                // 后轮
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TirePressureValue(value = rearLeft, position = "RL")
                    Spacer(modifier = Modifier.weight(1f))
                    TirePressureValue(value = rearRight, position = "RR")
                }
            }
        }
    }
}

@Composable
private fun TirePressureValue(value: Float, position: String) {
    val isNormal = value in 230f..320f
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = "${value.toInt()} kPa",
            color = if (isNormal) TirePressureNormal else TirePressureWarning,
            fontSize = Dimens.FontSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 本次行程信息卡片
 */
@Composable
fun TripInfoCard(
    distance: Float = 0.0f,
    energyConsumption: Float = 0.0f,
    duration: String = "3分钟",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusL))
            .background(SurfaceDark.copy(alpha = 0.85f))
            .padding(Dimens.SpaceL)
    ) {
        Text(
            text = "本次行程",
            color = TextSecondary,
            fontSize = Dimens.FontCaption
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceS))

        // 行程距离（大字显示）
        Text(
            text = "${distance} km",
            color = TextPrimary,
            fontSize = Dimens.FontCardValue,
            fontWeight = FontWeight.Light
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceM))

        // 电耗和时长
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXL),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "电耗",
                    color = TextHint,
                    fontSize = Dimens.FontSmall
                )
                Text(
                    text = "${energyConsumption} kWh",
                    color = TextPrimary,
                    fontSize = Dimens.FontBody,
                    fontWeight = FontWeight.Medium
                )
            }

            Column {
                Text(
                    text = "",
                    color = TextHint,
                    fontSize = Dimens.FontSmall
                )
                Text(
                    text = duration,
                    color = TextSecondary,
                    fontSize = Dimens.FontBody
                )
            }
        }
    }
}

/**
 * 车辆控制面板
 * 包含：车辆图标、主驾锁、驾驶模式等
 */
@Composable
fun VehicleControlPanel(
    onVehicleClick: () -> Unit = {},
    onLockClick: () -> Unit = {},
    onModeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusL))
            .background(SurfaceDark.copy(alpha = 0.85f))
            .padding(Dimens.SpaceM),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 车辆总览按钮
        VehicleControlButton(
            icon = "🚙",
            label = "",
            onClick = onVehicleClick,
            iconSize = 56.sp
        )

        // 主驾锁
        VehicleControlButton(
            icon = "🔒",
            label = "主驾锁",
            onClick = onLockClick
        )

        // 驾驶模式
        VehicleControlButton(
            icon = "⚙️",
            label = "标准",
            onClick = onModeClick
        )

        // 后备箱
        VehicleControlButton(
            icon = "🚪",
            label = "后备箱",
            onClick = {}
        )
    }
}

@Composable
private fun VehicleControlButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    iconSize: androidx.compose.ui.unit.TextUnit = 28.sp
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.RadiusM))
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = iconSize)
        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = TextSecondary,
                fontSize = Dimens.FontSmall
            )
        }
    }
}

/**
 * 快捷应用卡片组
 */
@Composable
fun QuickAppsRow(
    apps: List<QuickAppItem> = defaultQuickApps(),
    onAppClick: (QuickAppItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusL))
            .background(SurfaceDark.copy(alpha = 0.85f))
            .padding(Dimens.SpaceM),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        apps.forEach { app ->
            QuickAppButton(
                app = app,
                onClick = { onAppClick(app) }
            )
        }
    }
}

data class QuickAppItem(
    val name: String,
    val icon: String,
    val packageName: String = ""
)

private fun defaultQuickApps(): List<QuickAppItem> = listOf(
    QuickAppItem("智能助手", "💡"),
    QuickAppItem("蓝牙电话", "📞"),
    QuickAppItem("优酷视频", "▶️"),
    QuickAppItem("设置", "⚙️")
)

@Composable
private fun QuickAppButton(
    app: QuickAppItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(width = Dimens.TouchTarget, height = Dimens.TouchTarget)
            .clip(CircleShape)
            .background(SurfaceLight.copy(alpha = 0.6f))
            .clickable(onClick = onClick)
            .padding(Dimens.SpaceS),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = app.icon, fontSize = 32.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.name,
            color = TextSecondary,
            fontSize = Dimens.FontSmall
        )
    }
}

/**
 * 大时钟组件（用于第二屏或主界面中央）
 */
@Composable
fun LargeClockWidget(
    time: String = "21:14",
    date: String = "4月14日 星期二 二月廿七",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = time,
            color = TextPrimary,
            fontSize = Dimens.FontTimeLarge,
            fontWeight = FontWeight.Light,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(Dimens.SpaceM))
        Text(
            text = date,
            color = TextSecondary,
            fontSize = Dimens.FontBody
        )
    }
}

/**
 * 导航快捷方式卡片
 */
@Composable
fun NavigationShortcuts(
    onHomeClick: () -> Unit = {},
    onWorkClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusL))
            .background(SurfaceDark.copy(alpha = 0.85f))
            .padding(Dimens.SpaceM),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavShortcutButton(icon = "🏠", label = "回家", onClick = onHomeClick)
        NavShortcutButton(icon = "🏢", label = "去公司", onClick = onWorkClick)

        // 分隔线
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(40.dp)
                .background(TextHint.copy(alpha = 0.3f))
        )

        NavShortcutButton(icon = "📍", label = "", onClick = {})
    }
}

@Composable
private fun NavShortcutButton(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.SpaceM),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 36.sp)
        if (label.isNotEmpty()) {
            Text(
                text = label,
                color = TextSecondary,
                fontSize = Dimens.FontCaption
            )
        }
    }
}

/**
 * 媒体播放器卡片
 */
@Composable
fun MediaPlayerWidget(
    isPlaying: Boolean = false,
    onPlayPause: () -> Unit = {},
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusL))
            .background(SurfaceDark.copy(alpha = 0.85f))
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 当前播放状态
        Column {
            Text(
                text = "等待播放",
                color = TextSecondary,
                fontSize = Dimens.FontCaption
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 播放控制按钮
        MediaControlButton(icon = "⏮️", onClick = onPrevious)
        MediaControlButton(
            icon = if (isPlaying) "⏸️" else "▶️",
            onClick = onPlayPause,
            isMain = true
        )
        MediaControlButton(icon = "⏭️", onClick = onNext)
    }
}

@Composable
private fun MediaControlButton(
    icon: String,
    onClick: () -> Unit,
    isMain: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(if (isMain) 56.dp else 48.dp)
            .clip(CircleShape)
            .background(if (isMain) Primary.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = if (isMain) 28.sp else 24.sp
        )
    }
}

/**
 * 小型胎压显示（用于底部右侧）
 */
@Composable
fun SmallTirePressureDisplay(
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
fun CarIconSmall() {
    Text(text = "🚗", fontSize = 36.sp)
}
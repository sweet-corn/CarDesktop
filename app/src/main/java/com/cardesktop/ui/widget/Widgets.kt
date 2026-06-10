package com.cardesktop.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardesktop.ui.theme.*

/**
 * 顶部状态栏 - 按照参考图：左侧时间 + 右侧图标
 */
@Composable
fun TopStatusBar(
    time: String = "20:44",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = Dimens.SpaceXL, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：时间显示
        Text(
            text = time,
            color = Color(0xFF4FC3F7),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        // 右侧：系统状态图标
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusIcon(icon = "📍", size = 18.sp)
            StatusIcon(icon = "🔇", size = 18.sp)
            StatusIcon(icon = "📶", size = 18.sp)
            StatusIcon(icon = "📡", size = 18.sp)
            StatusIcon(icon = "🔋", size = 18.sp)
        }
    }
}

@Composable
private fun StatusIcon(icon: String, size: sp) {
    Text(text = icon, fontSize = size, color = Color.White)
}

/**
 * 壁纸图片卡片 - 中央区域带圆角边框
 */
@Composable
fun WallpaperCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // 壁纸内容区域（模拟可爱的熊猫猫咪壁纸）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF87CEEB), // 天蓝色
                            Color(0xFFE0F7FA)  // 浅蓝白
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // 模拟壁纸内容（实际项目中替换为真实图片）
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🐼🎢🐱",
                    fontSize = 80.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "可爱壁纸区域",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * 功能卡片行 - 底部功能区域
 * 包含：导航 | 媒体播放器 | 车辆控制 | 胎压信息 | 设置按钮
 */
@Composable
fun FunctionCardsRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 导航卡片
        NavigationCard(
            modifier = Modifier.width(180.dp).height(90.dp)
        )

        // 2. 媒体播放器卡片
        MediaPlayerCard(
            modifier = Modifier.weight(1f).height(90.dp)
        )

        // 3. 车辆控制卡片
        VehicleControlCard(
            modifier = Modifier.width(200.dp).height(90.dp)
        )

        // 4. 胎压信息卡片
        TirePressureInfoCard(
            modifier = Modifier.width(200.dp).height(90.dp)
        )

        // 5. 设置按钮
        SettingsButton(
            modifier = Modifier.size(90.dp)
        )
    }
}

/**
 * 导航快捷方式卡片
 */
@Composable
fun NavigationCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x80000000)) // 半透明黑色
            .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 地图图标
        NavIconItem(icon = "🗺️")

        // 回家/去公司
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            NavTextItem(label = "回家")
            NavTextItem(label = "去公司")
        }

        // 公司图标
        NavIconItem(icon = "🏢")
    }
}

@Composable
private fun NavIconItem(icon: String) {
    Text(text = icon, fontSize = 28.sp)
}

@Composable
private fun NavTextItem(label: String) {
    Text(
        text = label,
        color = Color.White,
        fontSize = 13.sp
    )
}

/**
 * 媒体播放器卡片
 */
@Composable
fun MediaPlayerCard(
    isPlaying: Boolean = false,
    onPlayPause: () -> Unit = {},
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x80000000))
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 音乐图标
        Text(text = "🎵", fontSize = 36.sp)

        // 等待播放文字 + 收藏
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "等待播放",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(text = "❤️", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 播放控制按钮
        MediaButton(icon = "⏮️", onClick = onPrevious)
        MediaButton(
            icon = if (isPlaying) "⏸️" else "▶️",
            onClick = onPlayPause,
            isMain = true
        )
        MediaButton(icon = "⏭️", onClick = onNext)
    }
}

@Composable
private fun MediaButton(
    icon: String,
    onClick: () -> Unit,
    isMain: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(if (isMain) 48.dp else 40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = if (isMain) 24.sp else 20.sp,
            color = Color.White
        )
    }
}

/**
 * 车辆控制卡片
 */
@Composable
fun VehicleControlCard(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x80000000))
            .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 车辆图片
        Text(text = "🚙", fontSize = 48.sp)

        // 控制项：已开门 / 已开启
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VehicleControlItem(icon = "🔓", text = "已开门")
                VehicleControlItem(icon = "💡", text = "已开启")
            }
        }
    }
}

@Composable
private fun VehicleControlItem(icon: String, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 20.sp)
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

/**
 * 胎压信息卡片 - 带车辆俯视图和四轮压力值
 */
@Composable
fun TirePressureInfoCard(
    frontLeft: Float = 232f,
    frontRight: Float = 232f,
    rearLeft: Float = 232f,
    rearRight: Float = 235f,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x80000000))
            .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧两轮压力
        Column {
            TireValue(value = frontLeft)
            TireValue(value = rearLeft)
        }

        // 中间车辆俯视图
        Text(text = "🚗", fontSize = 40.sp)

        // 右侧两轮压力
        Column {
            TireValue(value = frontRight)
            TireValue(value = rearRight)
        }
    }
}

@Composable
private fun TireValue(value: Float) {
    Text(
        text = "${value.toInt()} kPa",
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
    )
}

/**
 * 设置按钮
 */
@Composable
fun SettingsButton(onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x80000000))
            .clickable(onClick = onClick)
            .padding(Dimens.SpaceS),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⚙️", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "设置",
            color = Color.White,
            fontSize = 12.sp
        )
    }
}
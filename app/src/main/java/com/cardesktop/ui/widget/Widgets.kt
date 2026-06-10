package com.cardesktop.ui.widget

import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardesktop.ui.theme.*

/**
 * 顶部状态栏 - 左侧时间 + 右侧图标
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
        Text(
            text = time,
            color = Color(0xFF4FC3F7),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

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
private fun StatusIcon(icon: String, size: TextUnit) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF87CEEB),
                            Color(0xFFE0F7FA)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🐼🎢🐱", fontSize = 80.sp)
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
 * 功能卡片行 - 严格按照顺序：导航 | 音乐 | 车辆控制 | 胎压显示 | 设置
 * 每个模块用半透明矩形包裹，卡片分隔开
 */
@Composable
fun FunctionCardsRow(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 导航卡片（可点击打开导航）
        NavigationCard(
            onClick = { openNavigationApp(context) },
            modifier = Modifier.width(180.dp).height(90.dp)
        )

        // 2. 音乐播放器卡片（可点击打开音乐）
        MediaPlayerCard(
            onPlayPause = { openMusicApp(context) },
            onNext = { openMusicApp(context) },
            onPrevious = { openMusicApp(context) },
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

        // 5. 设置按钮（点击打开原生设置）
        SettingsButton(
            onClick = { 
                context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            },
            modifier = Modifier.size(90.dp)
        )
    }
}

/**
 * 导航快捷方式卡片 - 半透明矩形包裹
 */
@Composable
fun NavigationCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x80000000)) // 半透明黑色背景
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavIconItem(icon = "🗺️")
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            NavTextItem(label = "回家")
            NavTextItem(label = "去公司")
        }

        NavIconItem(icon = "🏢")
    }
}

@Composable
private fun NavIconItem(icon: String) {
    Text(text = icon, fontSize = 28.sp)
}

@Composable
private fun NavTextItem(label: String) {
    Text(text = label, color = Color.White, fontSize = 13.sp)
}

/**
 * 媒体播放器卡片 - 半透明矩形包裹
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
            .background(Color(0x80000000)) // 半透明黑色背景
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🎵", fontSize = 36.sp)

        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "等待播放", color = Color.White, fontSize = 14.sp)
                Text(text = "❤️", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        MediaButton(icon = "⏮️", onClick = onPrevious)
        MediaButton(icon = if (isPlaying) "⏸️" else "▶️", onClick = onPlayPause, isMain = true)
        MediaButton(icon = "⏭️", onClick = onNext)
    }
}

@Composable
private fun MediaButton(icon: String, onClick: () -> Unit, isMain: Boolean = false) {
    Box(
        modifier = Modifier
            .size(if (isMain) 48.dp else 40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = if (isMain) 24.sp else 20.sp, color = Color.White)
    }
}

/**
 * 车辆控制卡片 - 半透明矩形包裹
 */
@Composable
fun VehicleControlCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x80000000)) // 半透明黑色背景
            .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🚙", fontSize = 48.sp)

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
        Text(text = text, color = Color.White, fontSize = 12.sp)
    }
}

/**
 * 胎压信息卡片 - 半透明矩形包裹
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
            .background(Color(0x80000000)) // 半透明黑色背景
            .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            TireValue(value = frontLeft)
            TireValue(value = rearLeft)
        }

        Text(text = "🚗", fontSize = 40.sp)

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
 * 设置按钮 - 半透明矩形包裹
 */
@Composable
fun SettingsButton(onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x80000000)) // 半透明黑色背景
            .clickable(onClick = onClick)
            .padding(Dimens.SpaceS),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⚙️", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "设置", color = Color.White, fontSize = 12.sp)
    }
}

/**
 * 打开导航应用
 */
private fun openNavigationApp(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            `package` = "com.autonavi.minimap" // 高德地图
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                `package` = "com.google.android.apps.maps" // Google Maps
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e2: Exception) {
            // 如果都没有安装，尝试打开系统地图选择器
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("geo:0,0?q=")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}

/**
 * 打开音乐应用
 */
private fun openMusicApp(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            `package` = "com.tencent.qqmusic" // QQ音乐
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                `package` = "com.kugou.android" // 酷狗音乐
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e2: Exception) {
            try {
                val intent = Intent("android.intent.action.MUSIC_PLAYER").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e3: Exception) {
                // 最后尝试打开系统音乐播放器
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_MUSIC)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
    }
}
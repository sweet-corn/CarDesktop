package com.cardesktop.ui.widget

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardesktop.ui.theme.*

/**
 * 赛博朋克风格顶部状态栏 - 霓虹青色时间 + 状态图标
 */
@Composable
fun CyberpunkStatusBar(
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
        // 左侧：霓虹青色时间
        Text(
            text = time,
            color = CyberpunkColors.NeonCyan,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )

        // 右侧：系统状态图标（白色）
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeonStatusIcon(icon = "📍", size = 18.sp)
            NeonStatusIcon(icon = "🔇", size = 18.sp)
            NeonStatusIcon(icon = "📶", size = 18.sp)
            NeonStatusIcon(icon = "📡", size = 18.sp)
            NeonStatusIcon(icon = "🔋", size = 18.sp)
        }
    }
}

@Composable
private fun NeonStatusIcon(icon: String, size: TextUnit) {
    Text(text = icon, fontSize = size, color = Color.White)
}

/**
 * 赛博朋克壁纸卡片 - 霓虹发光边框 + CYBERPUNK艺术字
 */
@Composable
fun CyberpunkWallpaperCard(modifier: Modifier = Modifier) {
    // 外层：霓虹发光边框容器
    Box(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(Dimens.RadiusXL),
                ambientColor = CyberpunkColors.NeonCyan,
                spotColor = CyberpunkColors.NeonPink
            )
            .border(
                width = 3.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        CyberpunkColors.NeonCyan,
                        CyberpunkColors.NeonPink,
                        CyberpunkColors.NeonCyan
                    )
                ),
                shape = RoundedCornerShape(Dimens.RadiusXL)
            )
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        // 内层：壁纸内容区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(Dimens.RadiusXL - 4.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // CYBERPUNK 艺术字（黄色霓虹）
                Text(
                    text = "CYBERPUNK",
                    color = CyberpunkColors.NeonYellow,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = CyberpunkColors.GlowYellow,
                            blurRadius = 20f,
                            offset = androidx.compose.ui.geometry.Offset(0f, 0f)
                        )
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "🌃 霓虹城市夜景壁纸",
                    color = CyberpunkColors.TextHint,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 霓虹功能卡片行 - 所有卡片带发光边框
 * 顺序：导航 | 音乐 | 车辆控制 | 胎压 | 设置
 */
@Composable
fun NeonFunctionCardsRow(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 导航卡片（青色霓虹边框）
        NeonCard(
            neonColor = CyberpunkColors.NeonCyan,
            onClick = { openNavigationApp(context) },
            content = { NavigationCardContent() },
            modifier = Modifier.width(180.dp).height(90.dp)
        )

        // 2. 音乐播放器卡片（粉色霓虹边框）
        NeonCard(
            neonColor = CyberpunkColors.NeonPink,
            onClick = { openMusicApp(context) },
            content = { MediaPlayerCardContent() },
            modifier = Modifier.weight(1f).height(90.dp)
        )

        // 3. 车辆控制卡片（紫色霓虹边框）
        NeonCard(
            neonColor = CyberpunkColors.NeonPurple,
            onClick = {},
            content = { VehicleControlCardContent() },
            modifier = Modifier.width(200.dp).height(90.dp)
        )

        // 4. 胎压信息卡片（蓝色霓虹边框）
        NeonCard(
            neonColor = CyberpunkColors.NeonBlue,
            onClick = {},
            content = { TirePressureCardContent() },
            modifier = Modifier.width(200.dp).height(90.dp)
        )

        // 5. 设置按钮（橙色霓虹边框）
        NeonCard(
            neonColor = CyberpunkColors.NeonOrange,
            onClick = { 
                context.startActivity(Intent(context, com.cardesktop.ui.screen.SettingsActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            },
            content = { SettingsButtonContent() },
            modifier = Modifier.size(90.dp)
        )
    }
}

/**
 * 霓虹发光卡片容器 - 核心组件
 */
@Composable
private fun NeonCard(
    neonColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(Dimens.RadiusL),
                ambientColor = neonColor.copy(alpha = 0.6f),
                spotColor = neonColor
            )
            .clip(RoundedCornerShape(Dimens.RadiusL))
            .background(CyberpunkColors.BackgroundCard)
            .border(
                width = 2.dp,
                color = neonColor.copy(alpha = 0.7f),
                shape = RoundedCornerShape(Dimens.RadiusL)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// ========== 卡片内容组件 ==========

/**
 * 导航卡片内容
 */
@Composable
private fun NavigationCardContent() {
    Row(
        modifier = Modifier.padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🗺️", fontSize = 28.sp)
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            NeonText(label = "回家")
            NeonText(label = "去公司")
        }

        Text(text = "🏢", fontSize = 28.sp)
    }
}

/**
 * 音乐播放器卡片内容
 */
@Composable
private fun MediaPlayerCardContent() {
    Row(
        modifier = Modifier.padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🎵", fontSize = 36.sp)

        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonText(label = "等待播放", color = Color.White)
                Text(text = "❤️", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        NeonMediaButton(icon = "⏮️")
        NeonMediaButton(icon = "▶️", isMain = true)
        NeonMediaButton(icon = "⏭️")
    }
}

@Composable
private fun NeonMediaButton(icon: String, isMain: Boolean = false) {
    Box(
        modifier = Modifier
            .size(if (isMain) 48.dp else 40.dp)
            .clip(CircleShape)
            .background(if (isMain) CyberpunkColors.NeonPink.copy(alpha = 0.3f) else Color.Transparent),
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
 * 车辆控制卡片内容
 */
@Composable
private fun VehicleControlCardContent() {
    Row(
        modifier = Modifier.padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
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
        NeonText(label = text)
    }
}

/**
 * 胎压信息卡片内容
 */
@Composable
private fun TirePressureCardContent() {
    Row(
        modifier = Modifier.padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            NeonTireValue(value = 222f)
            NeonTireValue(value = 232f)
        }

        Text(text = "🚗", fontSize = 40.sp)

        Column {
            NeonTireValue(value = 222f)
            NeonTireValue(value = 233f)
        }
    }
}

@Composable
private fun NeonTireValue(value: Float) {
    Text(
        text = "${value.toInt()} kPa",
        color = CyberpunkColors.NeonCyan,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
    )
}

/**
 * 设置按钮内容
 */
@Composable
private fun SettingsButtonContent() {
    Column(
        modifier = Modifier.padding(Dimens.SpaceS),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⚙️", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(4.dp))
        NeonText(label = "设置")
    }
}

/**
 * 霓虹风格文字标签
 */
@Composable
private fun NeonText(label: String, color: Color = CyberpunkColors.TextSecondary) {
    Text(
        text = label,
        color = color,
        fontSize = 13.sp
    )
}

/**
 * 打开导航应用 - 智能启动策略
 */
private fun openNavigationApp(context: Context) {
    try {
        val intent = context.packageManager.getLaunchIntentForPackage("com.autonavi.minimap")
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    try {
        val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.maps")
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("geo:0,0?q=")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 打开音乐应用
 */
private fun openMusicApp(context: Context) {
    try {
        val intent = context.packageManager.getLaunchIntentForPackage("com.tencent.qqmusic")
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    try {
        val intent = context.packageManager.getLaunchIntentForPackage("com.kugou.android")
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    try {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_MUSIC)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
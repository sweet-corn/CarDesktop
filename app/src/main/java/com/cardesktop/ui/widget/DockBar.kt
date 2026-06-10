package com.cardesktop.ui.widget

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardesktop.ui.theme.*

/**
 * 赛博朋克风格底部 Dock 栏 - 与功能卡片栏一致的霓虹风格
 * 
 * 布局结构：
 * [⚙️车辆设置] | [🏠主页] | [< 20°C >空调] | [🎵音乐] | [❄️空调] | ... | [⊞全应用]
 * 
 * 特点：
 * - 霓虹发光边框（与功能卡片一致）
 * - 渐变色分隔线
 * - 音乐播放器集成
 */
@Composable
fun FrostedGlassDockBar(
    onSettingsClick: () -> Unit = {},
    onAppDrawerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Dock栏整体容器 - 半透明背景
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x15000000)
                    )
                )
            )
            .padding(vertical = Dimens.SpaceS)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpaceM),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ========== 1. 车件设置按钮（霓虹橙色）==========
            NeonDockItem(
                icon = "⚙️",
                label = "车辆设置",
                neonColor = CyberpunkColors.NeonOrange,
                onClick = { 
                    context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }
            )

            // 渐变分隔线
            GradientSeparator()

            // ========== 2. 主页按钮（霓虹青色高亮）==========
            NeonDockItem(
                icon = "🏠",
                label = "主页",
                neonColor = CyberpunkColors.NeonCyan,
                onClick = {},
                isHighlight = true
            )

            // 渐变分隔线
            GradientSeparator()

            // ========== 3. 空调温度控制（霓虹蓝色）==========
            NeonTemperatureControl()

            // 渐变分隔线
            GradientSeparator()

            // ========== 4. 自定义app卡槽-音乐（霓虹粉色）==========
            NeonDockItem(
                icon = "🎵",
                label = "音乐",
                neonColor = CyberpunkColors.NeonPink,
                onClick = { openMusicApp(context) }
            )

            // 渐变分隔线
            GradientSeparator()

            // ========== 5. 空调按钮（霓虹紫色）==========
            NeonDockItem(
                icon = "❄️",
                label = "空调",
                neonColor = CyberpunkColors.NeonPurple,
                onClick = {}
            )

            // 弹性空间
            Spacer(modifier = Modifier.weight(1f))

            // 渐变分隔线
            GradientSeparator()

            // ========== 6. 全应用按钮（霓虹绿色）==========
            NeonDockItem(
                icon = "⊞",
                label = "全部应用",
                neonColor = CyberpunkColors.NeonGreen,
                onClick = onAppDrawerClick
            )
        }
    }
}

/**
 * 霓虹风格 Dock 图标项 - 与功能卡片一致的样式
 */
@Composable
private fun NeonDockItem(
    icon: String,
    label: String = "",
    neonColor: Color,
    onClick: () -> Unit,
    isHighlight: Boolean = false
) {
    Column(
        modifier = Modifier
            .width(Dimens.TouchTarget)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(Dimens.RadiusM),
                ambientColor = if (isHighlight) neonColor.copy(alpha = 0.8f) else neonColor.copy(alpha = 0.4f),
                spotColor = if (isHighlight) neonColor else Color.Transparent
            )
            .clip(RoundedCornerShape(Dimens.RadiusM))
            .background(Color(0x80000000))
            .border(
                width = if (isHighlight) 2.dp else 1.dp,
                color = if (isHighlight) neonColor.copy(alpha = 0.9f) else neonColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(Dimens.RadiusM)
            )
            .clickable(onClick = onClick)
            .padding(Dimens.SpaceXS),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 28.sp,
            color = if (isHighlight) neonColor else Color.White
        )
        
        if (label.isNotEmpty()) {
            Text(
                text = label,
                color = if (isHighlight) neonColor.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

/**
 * 渐变色分隔线
 */
@Composable
private fun GradientSeparator() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(0.5.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        CyberpunkColors.NeonCyan.copy(alpha = 0.6f),
                        CyberpunkColors.NeonPink.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                )
            )
    )
}

/**
 * 温度控制区域 - 霓虹风格
 */
@Composable
private fun NeonTemperatureControl() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXS),
        modifier = Modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(Dimens.RadiusL),
                ambientColor = CyberpunkColors.NeonBlue.copy(alpha = 0.4f),
                spotColor = CyberpunkColors.NeonBlue
            )
            .clip(RoundedCornerShape(Dimens.RadiusL))
            .background(Color(0x80000000))
            .border(
                width = 1.dp,
                color = CyberpunkColors.NeonBlue.copy(alpha = 0.6f),
                shape = RoundedCornerShape(Dimens.RadiusL)
            )
            .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS)
    ) {
        // 左箭头（降温）
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(CyberpunkColors.NeonBlue.copy(alpha = 0.2f))
                .clickable(onClick = { /* 降温 */ }),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<",
                color = CyberpunkColors.NeonBlue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 温度数值显示
        Text(
            text = "20°C",
            color = CyberpunkColors.NeonBlue,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        // 右箭头（升温）
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(CyberpunkColors.NeonBlue.copy(alpha = 0.2f))
                .clickable(onClick = { /* 升温 */ }),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ">",
                color = CyberpunkColors.NeonBlue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardesktop.ui.theme.CyberpunkColors
import com.cardesktop.ui.theme.ResponsiveDimensions

/**
 * 打开音乐应用
 */
fun openMusicApp(context: Context) {
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

/**
 * 赛博朋克风格底部 Dock 栏 - 自适应版本
 */
@Composable
fun FrostedGlassDockBar(
    dim: ResponsiveDimensions,
    onSettingsClick: () -> Unit = {},
    onAppDrawerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
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
            .padding(vertical = dim.spaceS)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dim.spaceM),
            horizontalArrangement = Arrangement.spacedBy(dim.spaceS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 车件设置按钮（霓虹橙色）
            NeonDockItem(
                icon = "⚙️",
                label = "车辆设置",
                neonColor = CyberpunkColors.NeonOrange,
                onClick = { 
                    context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                },
                dim = dim
            )

            GradientSeparator(dim)

            // 2. 主页按钮（霓虹青色高亮）
            NeonDockItem(
                icon = "🏠",
                label = "主页",
                neonColor = CyberpunkColors.NeonCyan,
                onClick = {},
                isHighlight = true,
                dim = dim
            )

            GradientSeparator(dim)

            // 3. 空调温度控制（霓虹蓝色）
            NeonTemperatureControl(dim)

            GradientSeparator(dim)

            // 4. 自定义app卡槽-音乐（霓虹粉色）
            NeonDockItem(
                icon = "🎵",
                label = "音乐",
                neonColor = CyberpunkColors.NeonPink,
                onClick = { openMusicApp(context) },
                dim = dim
            )

            GradientSeparator(dim)

            // 5. 空调按钮（霓虹紫色）
            NeonDockItem(
                icon = "❄️",
                label = "空调",
                neonColor = CyberpunkColors.NeonPurple,
                onClick = {},
                dim = dim
            )

            Spacer(modifier = Modifier.weight(1f))

            GradientSeparator(dim)

            // 6. 全应用按钮（霓虹绿色）
            NeonDockItem(
                icon = "⊞",
                label = "全部应用",
                neonColor = CyberpunkColors.NeonGreen,
                onClick = onAppDrawerClick,
                dim = dim
            )
        }
    }
}

@Composable
private fun NeonDockItem(
    icon: String,
    label: String = "",
    neonColor: Color,
    onClick: () -> Unit,
    isHighlight: Boolean = false,
    dim: ResponsiveDimensions
) {
    Column(
        modifier = Modifier
            .width(dim.touchTarget)
            .shadow(
                elevation = (6 * dim.scaleFactor).dp.coerceAtLeast(3.dp),
                shape = RoundedCornerShape(dim.radiusM),
                ambientColor = if (isHighlight) neonColor.copy(alpha = 0.8f) else neonColor.copy(alpha = 0.4f),
                spotColor = if (isHighlight) neonColor else Color.Transparent
            )
            .clip(RoundedCornerShape(dim.radiusM))
            .background(Color(0x80000000))
            .border(
                width = if (isHighlight) (2 * dim.scaleFactor).dp.coerceAtLeast(1.dp) else (1 * dim.scaleFactor).dp.coerceAtLeast(0.5.dp),
                color = if (isHighlight) neonColor.copy(alpha = 0.9f) else neonColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(dim.radiusM)
            )
            .clickable(onClick = onClick)
            .padding(dim.spaceXS),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = dim.dockItemIconSize.sp,
            color = if (isHighlight) neonColor else Color.White
        )
        
        if (label.isNotEmpty()) {
            Text(
                text = label,
                color = if (isHighlight) neonColor.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.7f),
                fontSize = dim.dockItemLabelSize.sp
            )
        }
    }
}

@Composable
private fun GradientSeparator(dim: ResponsiveDimensions) {
    Box(
        modifier = Modifier
            .width(dim.dockSeparatorWidth)
            .height(dim.dockSeparatorHeight)
            .clip(RoundedCornerShape(dim.dockSeparatorRadius))
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

@Composable
private fun NeonTemperatureControl(dim: ResponsiveDimensions) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dim.spaceXS),
        modifier = Modifier
            .shadow(
                elevation = (6 * dim.scaleFactor).dp.coerceAtLeast(3.dp),
                shape = RoundedCornerShape(dim.radiusL),
                ambientColor = CyberpunkColors.NeonBlue.copy(alpha = 0.4f),
                spotColor = CyberpunkColors.NeonBlue
            )
            .clip(RoundedCornerShape(dim.radiusL))
            .background(Color(0x80000000))
            .border(
                width = (1 * dim.scaleFactor).dp.coerceAtLeast(0.5.dp),
                color = CyberpunkColors.NeonBlue.copy(alpha = 0.6f),
                shape = RoundedCornerShape(dim.radiusL)
            )
            .padding(horizontal = dim.spaceM, vertical = dim.spaceS)
    ) {
        Box(
            modifier = Modifier
                .size(dim.dockTempButtonSize)
                .clip(CircleShape)
                .background(CyberpunkColors.NeonBlue.copy(alpha = 0.2f))
                .clickable(onClick = { /* 降温 */ }),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<",
                color = CyberpunkColors.NeonBlue,
                fontSize = (20 * dim.scaleFactor).coerceIn(14f, 30f).sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "20°C",
            color = CyberpunkColors.NeonBlue,
            fontSize = dim.dockTempTextSize.sp,
            fontWeight = FontWeight.Medium
        )

        Box(
            modifier = Modifier
                .size(dim.dockTempButtonSize)
                .clip(CircleShape)
                .background(CyberpunkColors.NeonBlue.copy(alpha = 0.2f))
                .clickable(onClick = { /* 升温 */ }),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ">",
                color = CyberpunkColors.NeonBlue,
                fontSize = (20 * dim.scaleFactor).coerceIn(14f, 30f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
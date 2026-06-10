package com.cardesktop.ui.widget

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardesktop.ui.theme.*

/**
 * 磨砂玻璃效果底部 Dock 栏 - 严格按照图片布局
 *
 * 从左到右依次是：
 * 1. 车辆设置按钮 (⚙️)
 * 2. 主页按钮 (🏠)
 * 3. 空调开关和温度增减 (< 20°C >)
 * 4. 自定义app卡槽 (🎵)
 * 5. 空调按钮 (❄️)
 * 6. 全应用按钮 (⊞)
 */
@Composable
fun FrostedGlassDockBar(
    onSettingsClick: () -> Unit = {},
    onAppDrawerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.15f) // 半透明白色背景（磨砂玻璃效果）
            )
            .padding(vertical = Dimens.SpaceS)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpaceM),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ========== 1. 车辆设置按钮 ==========
            DockIconItem(
                icon = "⚙️",
                label = "车辆设置",
                onClick = { 
                    context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }
            )

            // ========== 2. 主页按钮 ==========
            DockIconItem(
                icon = "🏠",
                label = "主页",
                onClick = {},
                isHighlight = true
            )

            // ========== 3. 空调开关和温度增减 ==========
            TemperatureControl()

            // ========== 4. 自定义app卡槽（音乐） ==========
            DockIconItem(
                icon = "🎵",
                label = "音乐",
                onClick = { openMusicApp(context) }
            )

            // ========== 5. 空调按钮 ==========
            DockIconItem(
                icon = "❄️",
                label = "空调",
                onClick = {}
            )

            // 弹性空间
            Spacer(modifier = Modifier.weight(1f))

            // ========== 6. 全应用按钮 ==========
            DockIconItem(
                icon = "⊞",
                label = "全部应用",
                onClick = onAppDrawerClick
            )
        }
    }
}

/**
 * 单个 Dock 图标项
 */
@Composable
private fun DockIconItem(
    icon: String,
    label: String = "",
    onClick: () -> Unit,
    isHighlight: Boolean = false
) {
    Column(
        modifier = Modifier
            .width(Dimens.TouchTarget)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 28.sp,
            color = if (isHighlight) Color(0xFF4FC3F7) else Color.White
        )
        if (label.isNotEmpty()) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }
    }
}

/**
 * 温度控制区域 - 带空调开关和左右调节按钮
 */
@Composable
private fun TemperatureControl() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
    ) {
        // 左箭头（降温）
        Text(
            text = "<",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = { /* 降温 */ })
        )

        // 温度数值显示
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "20°C",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 右箭头（升温）
        Text(
            text = ">",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = { /* 升温 */ })
        )
    }
}

/**
 * 打开音乐应用 - 复制自Widgets.kt以解决访问权限问题
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
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardesktop.ui.theme.*

/**
 * 磨砂玻璃效果底部 Dock 栏 - 完全按照参考图还原
 *
 * 特点：
 * - 半透明背景 (alpha = 0.6)
 * - 模糊效果 (blur)
 * - 包含所有图标：设置、主页、温度调节、音乐、导航等
 */
@Composable
fun FrostedGlassDockBar(
    onSettingsClick: () -> Unit = {},
    onAppDrawerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.15f) // 半透明白色背景
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
            // 1. 设置图标
            DockIconItem(icon = "⚙️", onClick = onSettingsClick)

            // 2. 主页图标
            DockIconItem(icon = "🏠", onClick = {})

            // 3. 温度显示区域（带 < > 调节）
            TemperatureDisplay()

            // 4. 音乐图标
            DockIconItem(icon = "🎵", onClick = {})

            // 5. 导航/消息图标
            DockIconItem(icon = "✈️", onClick = {})

            // 中间弹性空间
            Spacer(modifier = Modifier.weight(1f))

            // 6. 天气详情图标
            DockIconItem(icon = "🌤️", label = "关", onClick = {})

            // 7. 车辆状态图标
            DockIconItem(icon = "🚗", onClick = {})

            // 8. 应用网格图标
            DockIconItem(icon = "⊞", onClick = onAppDrawerClick)
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
 * 温度显示区域 - 带左右调节按钮
 */
@Composable
private fun TemperatureDisplay() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
    ) {
        // 左箭头（降温）
        Text(
            text = "<",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = { /* 降温 */ })
        )

        // 温度数值
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "20°C",
                color = Color.White,
                fontSize = 20.sp,
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
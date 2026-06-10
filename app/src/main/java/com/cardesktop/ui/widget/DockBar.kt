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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardesktop.data.model.AppInfo
import com.cardesktop.ui.theme.*

/**
 * 比亚迪风格底部 Dock 栏
 * 包含：主页 | 天气 | 音乐 | 导航 | 应用抽屉 | 设置
 */
@Composable
fun BYDDockBar(
    onHomeClick: () -> Unit = {},
    onWeatherClick: () -> Unit = {},
    onMusicClick: () -> Unit = {},
    onNavClick: () -> Unit = {},
    onAppDrawerClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark.copy(alpha = 0.95f))
            .padding(horizontal = Dimens.SpaceXL, vertical = Dimens.SpaceM),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 主页
        DockBarItem(icon = "🏠", label = "", onClick = onHomeClick, isHighlight = true)

        // 天气（带温度显示）
        DockBarItemWithTemp(icon = "🌡️", temperature = "26°C", onClick = onWeatherClick)

        // 音乐
        DockBarItem(icon = "🎵", label = "", onClick = onMusicClick)

        // 导航/消息
        DockBarItem(icon = "✈️", label = "", onClick = onNavClick)

        // 分隔区域 - 可滚动应用区
        Box(
            modifier = Modifier
                .weight(1f)
                .height(Dimens.TouchTarget)
                .clip(RoundedCornerShape(Dimens.RadiusM))
                .background(SurfaceLight.copy(alpha = 0.3f))
                .clickable(onClick = onAppDrawerClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "公众号 · 右右玩车机",
                color = TextHint,
                fontSize = Dimens.FontCaption
            )
        }

        // 更多应用/天气详情
        DockBarItem(icon = "🌤️", label = "关", onClick = {})

        // 车辆状态
        DockBarItem(icon = "🚗", label = "", onClick = {})

        // 应用网格
        DockBarItem(icon = "⊞", label = "", onClick = onAppDrawerClick)
    }
}

@Composable
private fun DockBarItem(
    icon: String,
    label: String,
    onClick: () -> Unit,
    isHighlight: Boolean = false
) {
    Column(
        modifier = Modifier
            .size(width = Dimens.TouchTarget, height = Dimens.TouchTarget)
            .clip(CircleShape)
            .background(if (isHighlight) Primary.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(Dimens.SpaceS),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 32.sp,
            color = if (isHighlight) Primary else TextPrimary
        )
        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = TextSecondary,
                fontSize = Dimens.FontSmall
            )
        }
    }
}

@Composable
private fun DockBarItemWithTemp(
    icon: String,
    temperature: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(Dimens.TouchTarget)
            .height(Dimens.TouchTarget)
            .clip(RoundedCornerShape(Dimens.RadiusM))
            .background(Color.Transparent)
            .clickable(onClick = onClick)
            .padding(Dimens.SpaceS),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = icon, fontSize = 24.sp)
            Text(
                text = temperature,
                color = TextPrimary,
                fontSize = Dimens.FontBody,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 温度调节指示器
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "<", color = TextSecondary, fontSize = Dimens.FontBody)
            Text(text = ">", color = TextSecondary, fontSize = Dimens.FontBody)
        }
    }
}
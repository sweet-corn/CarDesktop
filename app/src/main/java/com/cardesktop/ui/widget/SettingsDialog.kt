package com.cardesktop.ui.widget

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cardesktop.ui.theme.CyberpunkColors
import com.cardesktop.ui.theme.ResponsiveDimensions

/**
 * 赛博朋克风格设置弹窗
 */
@Composable
fun CyberpunkSettingsDialog(
    dim: ResponsiveDimensions,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.8f)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(dim.radiusXL),
                        ambientColor = CyberpunkColors.NeonCyan.copy(alpha = 0.3f),
                        spotColor = CyberpunkColors.NeonPink.copy(alpha = 0.3f)
                    )
                    .clip(RoundedCornerShape(dim.radiusXL))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0A1628),
                                Color(0xFF1A2332)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CyberpunkColors.NeonCyan.copy(alpha = 0.6f),
                                CyberpunkColors.NeonPink.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(dim.radiusXL)
                    )
                    .clickable(enabled = false) {}
                    .padding(dim.spaceXL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙️ 桌面设置",
                        color = CyberpunkColors.NeonCyan,
                        fontSize = (24 * dim.scaleFactor).coerceIn(18f, 36f).sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "✕",
                        color = CyberpunkColors.NeonRed,
                        fontSize = (28 * dim.scaleFactor).coerceIn(20f, 42f).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(dim.radiusS))
                            .clickable(onClick = onDismiss)
                            .padding(dim.spaceS)
                    )
                }
                
                Spacer(modifier = Modifier.height(dim.spaceL))
                
                // 设置内容（可滚动）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    SettingsGroupTitle(title = "🎨 外观", dim = dim)
                    Spacer(modifier = Modifier.height(dim.spaceM))
                    
                    CyberpunkSettingsItem(
                        icon = "🖼️",
                        title = "壁纸设置",
                        subtitle = "更换桌面壁纸",
                        dim = dim,
                        onClick = { /* TODO */ }
                    )
                    Spacer(modifier = Modifier.height(dim.spaceS))
                    
                    CyberpunkSettingsItem(
                        icon = "🎨",
                        title = "主题颜色",
                        subtitle = "当前：赛博朋克",
                        dim = dim,
                        onClick = { /* TODO */ }
                    )
                    Spacer(modifier = Modifier.height(dim.spaceS))
                    
                    CyberpunkSettingsItem(
                        icon = "🕐",
                        title = "时钟样式",
                        subtitle = "当前：数字时钟",
                        dim = dim,
                        onClick = { /* TODO */ }
                    )
                    
                    Spacer(modifier = Modifier.height(dim.spaceL))
                    
                    SettingsGroupTitle(title = "📱 显示", dim = dim)
                    Spacer(modifier = Modifier.height(dim.spaceM))
                    
                    CyberpunkSettingsItem(
                        icon = "",
                        title = "应用网格",
                        subtitle = "当前：6列 × 4行",
                        dim = dim,
                        onClick = { /* TODO */ }
                    )
                    Spacer(modifier = Modifier.height(dim.spaceS))
                    
                    CyberpunkSettingsItem(
                        icon = "⚓",
                        title = "Dock栏图标",
                        subtitle = "自定义快捷应用",
                        dim = dim,
                        onClick = { /* TODO */ }
                    )
                    
                    Spacer(modifier = Modifier.height(dim.spaceL))
                    
                    SettingsGroupTitle(title = "⚡ 功能", dim = dim)
                    Spacer(modifier = Modifier.height(dim.spaceM))
                    
                    CyberpunkSettingsItem(
                        icon = "",
                        title = "车辆数据显示",
                        subtitle = "胎压/油量/电量等",
                        dim = dim,
                        onClick = { /* TODO */ }
                    )
                    Spacer(modifier = Modifier.height(dim.spaceS))
                    
                    CyberpunkSettingsItem(
                        icon = "🔊",
                        title = "声音反馈",
                        subtitle = "点击音效：开启",
                        dim = dim,
                        onClick = { /* TODO */ }
                    )
                    
                    Spacer(modifier = Modifier.height(dim.spaceL))
                    
                    SettingsGroupTitle(title = "🔧 系统", dim = dim)
                    Spacer(modifier = Modifier.height(dim.spaceM))
                    
                    CyberpunkSettingsItem(
                        icon = "⚙️",
                        title = "系统设置",
                        subtitle = "打开Android系统设置",
                        dim = dim,
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        }
                    )
                    Spacer(modifier = Modifier.height(dim.spaceS))
                    
                    CyberpunkSettingsItem(
                        icon = "ℹ️",
                        title = "关于",
                        subtitle = "CarDesktop v1.0.0",
                        dim = dim,
                        onClick = { /* TODO */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(dim.spaceM))
                
                // 底部版本信息
                Text(
                    text = "© 2024 CarDesktop - 比亚迪车机桌面",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = (12 * dim.scaleFactor).coerceIn(9f, 16f).sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun SettingsGroupTitle(title: String, dim: ResponsiveDimensions) {
    Text(
        text = title,
        color = CyberpunkColors.NeonYellow.copy(alpha = 0.8f),
        fontSize = (14 * dim.scaleFactor).coerceIn(10f, 20f).sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun CyberpunkSettingsItem(
    icon: String,
    title: String,
    subtitle: String,
    dim: ResponsiveDimensions,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dim.radiusM))
            .background(Color(0x40FFFFFF))
            .border(
                width = 1.dp,
                color = CyberpunkColors.NeonCyan.copy(alpha = 0.3f),
                shape = RoundedCornerShape(dim.radiusM)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = dim.spaceL, vertical = dim.spaceM),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dim.spaceM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = (24 * dim.scaleFactor).coerceIn(18f, 36f).sp)
            
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = (16 * dim.scaleFactor).coerceIn(12f, 24f).sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height((2 * dim.scaleFactor).dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = (13 * dim.scaleFactor).coerceIn(10f, 20f).sp
                )
            }
        }
        
        Text(
            text = ">",
            color = CyberpunkColors.NeonCyan.copy(alpha = 0.6f),
            fontSize = (20 * dim.scaleFactor).coerceIn(14f, 30f).sp
        )
    }
}
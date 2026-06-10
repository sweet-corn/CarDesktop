package com.cardesktop.ui.screen

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardesktop.ui.theme.*

/**
 * 桌面设置页面 - 桌面APP的设置界面（不是系统设置）
 * 
 * 包含：
 * - 壁纸设置
 * - 显示设置
 * - 关于信息
 */
class SettingsActivity : ComponentActivity() {

    // 壁纸选择器
    private val wallpaperPicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // TODO: 保存壁纸并更新显示
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DesktopSettingsScreen(
                onBack = { finish() },
                onPickWallpaper = {
                    wallpaperPicker.launch("image/*")
                }
            )
        }
    }
}

@Composable
fun DesktopSettingsScreen(
    onBack: () -> Unit,
    onPickWallpaper: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A1628)) // 深色背景
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ========== 标题栏 ==========
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚙️ 桌面设置",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            // 返回按钮
            Text(
                text = "✕ 返回",
                color = Color(0xFF4FC3F7),
                fontSize = 16.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBack)
                    .background(Color(0xFF1a237e))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ========== 外观设置组 ==========
        SettingsGroupTitle(title = "🎨 外观")

        Spacer(modifier = Modifier.height(12.dp))

        // 壁纸设置
        SettingsItemCard(
            icon = "🖼️",
            title = "壁纸设置",
            subtitle = "更换桌面壁纸",
            onClick = onPickWallpaper
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 主题颜色
        SettingsItemCard(
            icon = "🎨",
            title = "主题颜色",
            subtitle = "当前：科技蓝",
            onClick = { /* TODO: 打开颜色选择器 */ }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 时钟样式
        SettingsItemCard(
            icon = "🕐",
            title = "时钟样式",
            subtitle = "当前：数字时钟",
            onClick = { /* TODO: 切换时钟样式 */ }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ========== 显示设置组 ==========
        SettingsGroupTitle(title = "📱 显示")

        Spacer(modifier = Modifier.height(12.dp))

        // 网格列数
        SettingsItemCard(
            icon = "🔲",
            title = "应用网格",
            subtitle = "当前：6列 × 4行",
            onClick = { /* TODO: 调整网格 */ }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Dock栏图标
        SettingsItemCard(
            icon = "⚓",
            title = "Dock栏图标",
            subtitle = "自定义快捷应用",
            onClick = { /* TODO: 编辑Dock */ }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ========== 功能设置组 ==========
        SettingsGroupTitle(title = "⚡ 功能")

        Spacer(modifier = Modifier.height(12.dp))

        // 车辆数据
        SettingsItemCard(
            icon = "🚗",
            title = "车辆数据显示",
            subtitle = "胎压/油量/电量等",
            onClick = { /* TODO: 配置数据源 */ }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 声音反馈
        SettingsItemCard(
            icon = "🔊",
            title = "声音反馈",
            subtitle = "点击音效：开启",
            onClick = { /* TODO: 切换声音 */ }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ========== 系统设置组 ==========
        SettingsGroupTitle(title = "🔧 系统")

        Spacer(modifier = Modifier.height(12.dp))

        // 打开系统设置
        SettingsItemCard(
            icon = "⚙️",
            title = "系统设置",
            subtitle = "打开Android系统设置",
            onClick = {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 关于
        SettingsItemCard(
            icon = "ℹ️",
            title = "关于",
            subtitle = "CarDesktop v1.0.0",
            onClick = { /* TODO: 显示关于对话框 */ }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 底部版本信息
        Text(
            text = "© 2024 CarDesktop - 比亚迪车机桌面",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

/**
 * 设置分组标题
 */
@Composable
private fun SettingsGroupTitle(title: String) {
    Text(
        text = title,
        color = Color.White.copy(alpha = 0.6f),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )
}

/**
 * 设置项卡片 - 半透明矩形包裹
 */
@Composable
private fun SettingsItemCard(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x80FFFFFF)) // 半透明白色背景
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 28.sp)

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
        }

        Text(
            text = ">",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 20.sp
        )
    }
}
package com.cardesktop.ui.screen

import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cardesktop.data.model.AppInfo
import com.cardesktop.ui.theme.*
import com.cardesktop.ui.widget.*

/**
 * 比亚迪车机桌面 - 完全按照参考图还原
 *
 * 布局结构：
 * ┌──────────────────────────────────────────────┐
 * │ 状态栏: [20:44]              [📍 🔇 📶 🔋] │
 * ├──────────────────────────────────────────────┤
 * │                                              │
 * │  20:44          ┌──────────────────┐        │
 * │  6月9日 星期二   │                  │        │
 * │                 │   壁纸图片区域    │        │
 * │                 │                  │        │
 * │                 └──────────────────┘        │
 │                                              │
 * ├──────────────────────────────────────────────┤
 * │[导航][媒体播放][车辆控制][胎压]      [设置]│
 * ├──────────────────────────────────────────────┤
 * │ ⚙️ 🏠 < 20°C > 🎵 ✈️ ... 🌤️ 🚗 ⊞ (毛玻璃)│
 * └──────────────────────────────────────────────┘
 */
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val time by viewModel.currentTime.collectAsState()
    val date by viewModel.currentDate.collectAsState()

    fun launchApp(app: AppInfo) {
        val intent = app.launchIntent?.let {
            Intent().apply {
                component = ComponentName(app.packageName, it)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } ?: context.packageManager.getLaunchIntentForPackage(app.packageName)
        intent?.let { context.startActivity(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ========== 1. 顶部状态栏 ==========
            TopStatusBar(time = time)

            // ========== 2. 主内容区域 ==========
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // 背景（深色渐变）
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1a237e),
                                    Color(0xFF0d47a1),
                                    Color(0xFF01579b)
                                )
                            )
                        )
                )

                // 内容层
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = Dimens.SpaceXXL),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
                ) {
                    // 左侧：大时钟
                    Column(
                        modifier = Modifier
                            .padding(start = Dimens.SpaceXL)
                            .align(Alignment.Top),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = time,
                            color = Color.White,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = date,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 18.sp
                        )
                    }

                    // 右侧：壁纸图片区域
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = Dimens.SpaceXL, top = Dimens.SpaceM, bottom = Dimens.SpaceL),
                        contentAlignment = Alignment.Center
                    ) {
                        WallpaperCard()
                    }
                }
            }

            // ========== 3. 功能卡片栏 ==========
            FunctionCardsRow()

            // ========== 4. 底部 Dock 栏（磨砂玻璃效果）=========
            FrostedGlassDockBar(
                onSettingsClick = {
                    context.startActivity(
                        Intent(context, SettingsActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
                onAppDrawerClick = {
                    context.startActivity(
                        Intent(context, AppDrawerActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            )
        }
    }
}
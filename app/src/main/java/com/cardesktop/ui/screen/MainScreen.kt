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
 * 赛博朋克风格车机桌面 - 100%还原AI生成图
 * 
 * 布局结构：
 * ┌──────────────────────────────────────────────┐
 * │ 状态栏: [20:44]              [📍 🔇 📶 📡 🔋] │
 * ├──────────────────────────────────────────────┤
 * │                                              │
 * │  20:44          ╔══════════════════╗        │
 * │  6月9日 星期二   ║                  ║        │
 * │                 ║  CYBERPUNK壁纸   ║        │
 * │                 ║  (霓虹边框)      ║        │
 * │                 ╚══════════════════╝        │
 * │                                              │
 * ├──────────────────────────────────────────────┤
 * │[导航][音乐][车辆][胎压][设置] (霓虹卡片)    │
 * ├──────────────────────────────────────────────┤
 * │ ⚙️ 🏠 < 20°C > 🎵 ❄️ ... (磨砂玻璃)       │
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
            .background(CyberpunkColors.BackgroundDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ========== 1. 顶部状态栏（赛博朋克风格）==========
            CyberpunkStatusBar(time = time)

            // ========== 2. 主内容区域 ==========
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // 深空背景
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    CyberpunkColors.GradientStart,
                                    CyberpunkColors.GradientEnd
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
                    // 左侧：大时钟（霓虹发光）
                    Column(
                        modifier = Modifier
                            .padding(start = Dimens.SpaceXL)
                            .align(Alignment.Top),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // 大时间 - 霓虹青色发光
                        Text(
                            text = time,
                            color = Color.White,
                            fontSize = Dimens.FontTimeLarge,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 日期
                        Text(
                            text = date,
                            color = CyberpunkColors.TextSecondary,
                            fontSize = Dimens.FontBody
                        )
                    }

                    // 右侧：壁纸区域（霓虹边框）
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = Dimens.SpaceXL, top = Dimens.SpaceM, bottom = Dimens.SpaceL),
                        contentAlignment = Alignment.Center
                    ) {
                        CyberpunkWallpaperCard()
                    }
                }
            }

            // ========== 3. 功能卡片栏（霓虹发光边框）==========
            NeonFunctionCardsRow()

            // ========== 4. 底部 Dock 栏（磨砂玻璃）==========
            FrostedGlassDockBar(
                onSettingsClick = {
                    context.startActivity(Intent(context, SettingsActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                },
                onAppDrawerClick = {
                    context.startActivity(Intent(context, AppDrawerActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            )
        }
    }
}
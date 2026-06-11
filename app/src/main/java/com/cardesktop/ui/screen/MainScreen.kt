package com.cardesktop.ui.screen

import android.content.ComponentName
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.cardesktop.ui.theme.CyberpunkColors
import com.cardesktop.ui.theme.responsiveDimensions
import com.cardesktop.ui.widget.*

/**
 * 赛博朋克风格车机桌面 - 自适应布局版本
 * 
 * 特点：
 * - 自动适配任意尺寸横屏设备（手机/平板/车机）
 * - 所有元素按比例缩放
 * - 保持视觉一致性
 * - 支持小屏(7寸)、中屏(7-10寸)、大屏(>10寸)
 * - Dock栏常驻最上层
 * - 设置和应用抽屉使用弹窗形式
 * 
 * 布局结构：
 * ┌──────────────────────────────────────────────┐
 * │ 状态栏: [时间]              [图标]            │
 * ├──────────────────────────────────────────────
 * │                                              │
 * │  [大时钟]        [壁纸区域]                  │
 * │                                              │
 * ├──────────────────────────────────────────────┤
 * │ [功能卡片栏 - 自适应宽度]                    │
 * ├──────────────────────────────────────────────┤
 * │ [Dock栏 - 常驻最上层]                        │
 * └──────────────────────────────────────────────┘
 */
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val time by viewModel.currentTime.collectAsState()
    val date by viewModel.currentDate.collectAsState()
    
    // 获取响应式尺寸
    val dim = responsiveDimensions()

    // 弹窗状态
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showAppDrawerDialog by rememberSaveable { mutableStateOf(false) }

    // 返回键处理
    BackHandler(enabled = showSettingsDialog || showAppDrawerDialog) {
        if (showAppDrawerDialog) {
            showAppDrawerDialog = false
        } else if (showSettingsDialog) {
            showSettingsDialog = false
        }
    }

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
            // ========== 1. 顶部状态栏（自适应）==========
            CyberpunkStatusBar(
                time = time,
                dim = dim
            )

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

                // 内容层 - 使用Row实现左右布局
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = dim.spaceXXL),
                    horizontalArrangement = Arrangement.spacedBy(dim.spaceL),
                    verticalAlignment = Alignment.Top
                ) {
                    // 左侧：大时钟（自适应宽度和字号）
                    Column(
                        modifier = Modifier
                            .padding(start = dim.spaceXL)
                            .width(dim.clockContainerWidth)
                            .align(Alignment.Top),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // 大时间 - 自适应字体大小
                        Text(
                            text = time,
                            color = Color.White,
                            fontSize = dim.fontTimeLarge.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (2 * dim.scaleFactor).sp
                        )
                        
                        Spacer(modifier = Modifier.height(dim.spaceS))
                        
                        // 日期
                        Text(
                            text = date,
                            color = CyberpunkColors.TextSecondary,
                            fontSize = dim.fontBody.sp
                        )
                    }

                    // 右侧：壁纸区域（自适应边框和圆角）
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = dim.spaceXL, top = dim.spaceM, bottom = dim.spaceL),
                        contentAlignment = Alignment.Center
                    ) {
                        CyberpunkWallpaperCard(dim = dim)
                    }
                }
            }

            // ========== 3. 功能卡片栏（自适应）==========
            NeonFunctionCardsRow(dim = dim)

            // ========== 4. 底部 Dock 栏（自适应，常驻最上层）==========
            FrostedGlassDockBar(
                dim = dim,
                onBackClick = {
                    // 返回逻辑：关闭弹窗或不做操作
                    if (showAppDrawerDialog) {
                        showAppDrawerDialog = false
                    } else if (showSettingsDialog) {
                        showSettingsDialog = false
                    }
                },
                onSettingsClick = {
                    showSettingsDialog = true
                },
                onAppDrawerClick = {
                    showAppDrawerDialog = true
                }
            )
        }

        // ========== 5. 弹窗层（覆盖在Dock栏之上）==========
        if (showSettingsDialog) {
            CyberpunkSettingsDialog(
                dim = dim,
                onDismiss = { showSettingsDialog = false }
            )
        }

        if (showAppDrawerDialog) {
            CyberpunkAppDrawerDialog(
                dim = dim,
                onDismiss = { showAppDrawerDialog = false },
                onAppClick = { app -> launchApp(app) }
            )
        }
    }
}
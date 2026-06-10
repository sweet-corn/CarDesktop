package com.cardesktop.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 赛博朋克配色方案
 * 
 * 配色方案：
 * - 主色：霓虹青色 (#00FFFF)
 * - 强调色：霓虹粉色 (#FF00FF)
 * - 警告色：霓虹黄色 (#FFFF00)
 * - 背景：深空黑 (#0A0E27)
 */
object CyberpunkColors {
    // ========== 霓虹主色调 ==========
    val NeonCyan = Color(0xFF00FFFF)      // 霓虹青色
    val NeonPink = Color(0xFFFF00FF)      // 霓虹粉色
    val NeonYellow = Color(0xFFFFFF00)    // 霓虹黄色
    val NeonPurple = Color(0xFF9D00FF)    // 霓虹紫色
    val NeonBlue = Color(0xFF0080FF)      // 霓虹蓝色
    val NeonGreen = Color(0xFF00FF41)     // 霓虹绿色
    val NeonOrange = Color(0xFFFF6600)    // 霓虹橙色
    val NeonRed = Color(0xFFFF003C)       // 霓虹红色
    
    // ========== 背景色系 ==========
    val BackgroundDark = Color(0xFF0A0E27)        // 深空黑
    val BackgroundCard = Color(0x80000000)         // 卡片背景（半透明黑）
    
    // ========== 文字颜色 ==========
    val TextPrimary = Color(0xFFFFFFFF)            // 纯白色
    val TextSecondary = Color(0xB3FFFFFF)          // 白色70%
    val TextHint = Color(0x66FFFFFF)               // 白色40%
    
    // ========== 功能色 ==========
    val StatusOnline = NeonGreen                   // 在线状态
    val StatusOffline = Color(0xFF666666)          // 离线状态
    val Warning = NeonYellow                       // 警告
    val Error = NeonRed                            // 错误
    
    // ========== 特殊效果色 ==========
    val GlowCyan = Color(0x4000FFFF)               // 青色发光
    val GlowPink = Color(0x40FF00FF)               // 粉色发光
    val GlowYellow = Color(0x40FFFF00)             // 黄色发光
    
    // ========== 渐变色 ==========
    val GradientStart = Color(0xFF1a1f3a)
    val GradientEnd = Color(0xFF0d1025)
}
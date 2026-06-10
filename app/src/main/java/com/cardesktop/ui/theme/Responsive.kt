package com.cardesktop.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * 屏幕尺寸分类
 */
enum class ScreenSize {
    SMALL,    // 小屏（< 7寸）- 手机横屏
    MEDIUM,   // 中屏（7-10寸）- 平板/小型车机
    LARGE     // 大屏（> 10寸）- 大型车机/中控
}

/**
 * 自适应尺寸系统 - 根据屏幕自动调整
 * 
 * 特点：
 * - 自动检测屏幕尺寸（小/中/大）
 * - 所有UI元素按比例缩放
 * - 保持视觉一致性
 * - 支持任意分辨率和DPI
 */
object ResponsiveDimens {
    
    // ========== 基准尺寸（以1080p为基准）==========
    private val BASE_WIDTH = 1920.dp  // 基准宽度（车机标准）
    private val BASE_HEIGHT = 720.dp  // 基准高度
    
    /**
     * 获取当前屏幕尺寸分类
     */
    @Composable
    fun getScreenSize(): ScreenSize {
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp.dp
        
        return remember(screenWidthDp) {
            when {
                screenWidthDp < 800.dp -> ScreenSize.SMALL   // < 8寸
                screenWidthDp < 1200.dp -> ScreenSize.MEDIUM  // 8-12寸
                else -> ScreenSize.LARGE                       // > 12寸
            }
        }
    }
    
    /**
     * 计算缩放比例
     */
    @Composable
    fun getScaleFactor(): Float {
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp.dp.toFloat()
        
        return remember(screenWidthDp) {
            (screenWidthDp / BASE_WIDTH.value).coerceIn(0.5f, 2.0f)
        }
    }
    
    /**
     * 自适应DP值
     */
    @Composable
    fun adaptiveDp(baseValue: Dp): Dp {
        val scale = getScaleFactor()
        return remember(baseValue, scale) {
            (baseValue.value * scale).dp
        }
    }
    
    /**
     * 自适应SP值（字体大小）
     */
    @Composable
    fun adaptiveSp(baseValue: Float): Float {
        val scale = getScaleFactor()
        return remember(baseValue, scale) {
            (baseValue * scale).coerceIn(8f, 72f)  // 限制范围：8sp - 72sp
        }
    }
}

/**
 * 响应式尺寸类 - 提供所有UI元素的动态尺寸
 */
@Composable
fun responsiveDimensions(): ResponsiveDimensions {
    val screenSize = ResponsiveDimens.getScreenSize()
    val scale = ResponsiveDimens.getScaleFactor()
    
    return remember(screenSize, scale) {
        ResponsiveDimensions(screenSize, scale)
    }
}

/**
 * 响应式尺寸数据类
 */
data class ResponsiveDimensions(
    val screenSize: ScreenSize,
    val scaleFactor: Float
) {
    // ========== 触摸目标 ==========
    val touchTarget: Dp = (72 * scaleFactor).dp.coerceAtLeast(48.dp)
    
    // ========== 间距系统 ==========
    val spaceXS: Dp = (4 * scaleFactor).dp.coerceAtLeast(2.dp)
    val spaceS: Dp = (8 * scaleFactor).dp.coerceAtLeast(4.dp)
    val spaceM: Dp = (12 * scaleFactor).dp.coerceAtLeast(6.dp)
    val spaceL: Dp = (16 * scaleFactor).dp.coerceAtLeast(8.dp)
    val spaceXL: Dp = (24 * scaleFactor).dp.coerceAtLeast(12.dp)
    val spaceXXL: Dp = (32 * scaleFactor).dp.coerceAtLeast(16.dp)
    
    // ========== 圆角系统 ==========
    val radiusS: Dp = (8 * scaleFactor).dp.coerceAtLeast(4.dp)
    val radiusM: Dp = (12 * scaleFactor).dp.coerceAtLeast(6.dp)
    val radiusL: Dp = (16 * scaleFactor).dp.coerceAtLeast(8.dp)
    val radiusXL: Dp = (24 * scaleFactor).dp.coerceAtLeast(12.dp)
    
    // ========== 字体大小系统 ==========
    val fontCaption: Float = (12 * scaleFactor).coerceIn(9f, 18f)
    val fontSmall: Float = (13 * scaleFactor).coerceIn(10f, 20f)
    val fontBody: Float = (14 * scaleFactor).coerceIn(11f, 22f)
    val fontSubtitle: Float = (16 * scaleFactor).coerceIn(13f, 26f)
    val fontTitle: Float = (18 * scaleFactor).coerceIn(15f, 30f)
    val fontTime: Float = (28 * scaleFactor).coerceIn(20f, 44f)
    val fontTimeLarge: Float = (64 * scaleFactor).coerceIn(36f, 96f)
    
    // ========== 状态栏 ==========
    val statusBarHeight: Dp = (40 * scaleFactor).dp.coerceAtLeast(28.dp)
    val statusBarIconSize: Float = (20 * scaleFactor).coerceIn(14f, 28f)
    
    // ========== 大时钟区域 ==========
    val clockContainerWidth: Dp = (280 * scaleFactor).dp.coerceAtLeast(180.dp)
    val clockTextSize: Float = fontTimeLarge
    
    // ========== 壁纸卡片 ==========
    val wallpaperBorderWidth: Dp = (3 * scaleFactor).dp.coerceAtLeast(2.dp)
    val wallpaperCornerRadius: Dp = radiusXL
    val wallpaperPadding: Dp = (3 * scaleFactor).dp.coerceAtLeast(2.dp)
    
    // ========== 功能卡片 ==========
    val cardHeight: Dp = (90 * scaleFactor).dp.coerceAtLeast(60.dp)
    val cardNavigationWidth: Dp = (180 * scaleFactor).dp.coerceAtLeast(120.dp)
    val cardVehicleWidth: Dp = (200 * scaleFactor).dp.coerceAtLeast(140.dp)
    val cardTireWidth: Dp = (200 * scaleFactor).dp.coerceAtLeast(140.dp)
    val cardSettingsSize: Dp = (90 * scaleFactor).dp.coerceAtLeast(60.dp)
    val cardBorderWidth: Dp = (2 * scaleFactor).dp.coerceAtLeast(1.dp)
    val cardShadowElevation: Dp = (8 * scaleFactor).dp.coerceAtLeast(4.dp)
    
    // ========== 音乐组件 ==========
    val musicIconSize: Dp = (56 * scaleFactor).dp.coerceAtLeast(40.dp)
    val musicControlButtonSmall: Dp = (44 * scaleFactor).dp.coerceAtLeast(32.dp)
    val musicControlButtonMain: Dp = (52 * scaleFactor).dp.coerceAtLeast(38.dp)
    val musicCardFixedHeight: Dp = (80 * scaleFactor).dp.coerceAtLeast(56.dp)
    val musicSongNameSize: Float = (16 * scaleFactor).coerceIn(12f, 24f)
    val musicStatusSize: Float = (12 * scaleFactor).coerceIn(9f, 18f)
    val musicIconEmoji: Float = (32 * scaleFactor).coerceIn(24f, 48f)
    val musicHeartIcon: Float = (18 * scaleFactor).coerceIn(14f, 28f)
    
    // ========== Dock栏 ==========
    val dockItemSize: Dp = touchTarget
    val dockItemIconSize: Float = (28 * scaleFactor).coerceIn(20f, 42f)
    val dockItemLabelSize: Float = (11 * scaleFactor).coerceIn(9f, 16f)
    val dockTempControlHeight: Dp = (40 * scaleFactor).dp.coerceAtLeast(28.dp)
    val dockTempButtonSize: Dp = (32 * scaleFactor).dp.coerceAtLeast(24.dp)
    val dockTempTextSize: Float = (18 * scaleFactor).coerceIn(14f, 28f)
    val dockSeparatorWidth: Dp = (1 * scaleFactor).dp.coerceAtLeast(0.5.dp)
    val dockSeparatorHeight: Dp = (40 * scaleFactor).dp.coerceAtLeast(28.dp)
    val dockSeparatorRadius: Dp = (0.5 * scaleFactor).dp
    
    // ========== 对话框 ==========
    val dialogWidth: Dp = (320 * scaleFactor).dp.coerceAtLeast(240.dp)
    val dialogLargeWidth: Dp = (400 * scaleFactor).dp.coerceAtLeast(300.dp)
    val dialogAppIconSize: Float = (36 * scaleFactor).coerceIn(28f, 54f)
    val dialogAppNameSize: Float = (16 * scaleFactor).coerceIn(13f, 24f)
    val dialogTitleSize: Float = (18 * scaleFactor).coerceIn(15f, 28f)
    val dialogCancelSize: Float = (14 * scaleFactor).coerceIn(11f, 21f)
}
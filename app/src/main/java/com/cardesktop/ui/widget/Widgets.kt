package com.cardesktop.ui.widget

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardesktop.ui.theme.*

/**
 * 赛博朋克风格顶部状态栏 - 霓虹青色时间 + 状态图标
 */
@Composable
fun CyberpunkStatusBar(
    time: String = "20:44",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = Dimens.SpaceXL, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：霓虹青色时间
        Text(
            text = time,
            color = CyberpunkColors.NeonCyan,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )

        // 右侧：系统状态图标（白色）
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonStatusIcon(icon = "📍", size = 18.dp)
                NeonStatusIcon(icon = "🔇", size = 18.dp)
                NeonStatusIcon(icon = "📶", size = 18.dp)
                NeonStatusIcon(icon = "📡", size = 18.dp)
                NeonStatusIcon(icon = "🔋", size = 18.dp)
            }
    }
}

@Composable
private fun NeonStatusIcon(icon: String, size: Dp) {
    Text(text = icon, fontSize = 18.sp, color = Color.White)
}

/**
 * 赛博朋克壁纸卡片 - 霓虹发光边框 + CYBERPUNK艺术字
 */
@Composable
fun CyberpunkWallpaperCard(modifier: Modifier = Modifier) {
    // 外层：霓虹发光边框容器
    Box(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(Dimens.RadiusXL),
                ambientColor = CyberpunkColors.NeonCyan,
                spotColor = CyberpunkColors.NeonPink
            )
            .border(
                width = 3.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        CyberpunkColors.NeonCyan,
                        CyberpunkColors.NeonPink,
                        CyberpunkColors.NeonCyan
                    )
                ),
                shape = RoundedCornerShape(Dimens.RadiusXL)
            )
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        // 内层：壁纸内容区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(Dimens.RadiusXL - 4.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // CYBERPUNK 艺术字（黄色霓虹）
                Text(
                    text = "CYBERPUNK",
                    color = CyberpunkColors.NeonYellow,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = CyberpunkColors.GlowYellow,
                            blurRadius = 20f,
                            offset = androidx.compose.ui.geometry.Offset(0f, 0f)
                        )
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "🌃 霓虹城市夜景壁纸",
                    color = CyberpunkColors.TextHint,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 霓虹功能卡片行 - 所有卡片带发光边框
 * 顺序：导航 | 音乐 | 车辆控制 | 胎压 | 设置
 */
@Composable
fun NeonFunctionCardsRow(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 导航卡片（青色霓虹边框）
        NeonCard(
            neonColor = CyberpunkColors.NeonCyan,
            onClick = { openNavigationApp(context) },
            content = { NavigationCardContent() },
            modifier = Modifier.width(180.dp).height(90.dp)
        )

        // 2. 音乐播放器卡片（粉色霓虹边框）
        NeonCard(
            neonColor = CyberpunkColors.NeonPink,
            onClick = { openMusicApp(context) },
            content = { MediaPlayerCardContent() },
            modifier = Modifier.weight(1f).height(90.dp)
        )

        // 3. 车辆控制卡片（紫色霓虹边框）
        NeonCard(
            neonColor = CyberpunkColors.NeonPurple,
            onClick = {},
            content = { VehicleControlCardContent() },
            modifier = Modifier.width(200.dp).height(90.dp)
        )

        // 4. 胎压信息卡片（蓝色霓虹边框）
        NeonCard(
            neonColor = CyberpunkColors.NeonBlue,
            onClick = {},
            content = { TirePressureCardContent() },
            modifier = Modifier.width(200.dp).height(90.dp)
        )

        // 5. 设置按钮（橙色霓虹边框）
        NeonCard(
            neonColor = CyberpunkColors.NeonOrange,
            onClick = { 
                context.startActivity(Intent(context, com.cardesktop.ui.screen.SettingsActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            },
            content = { SettingsButtonContent() },
            modifier = Modifier.size(90.dp)
        )
    }
}

/**
 * 霓虹发光卡片容器 - 核心组件
 */
@Composable
private fun NeonCard(
    neonColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(Dimens.RadiusL),
                ambientColor = neonColor.copy(alpha = 0.6f),
                spotColor = neonColor
            )
            .clip(RoundedCornerShape(Dimens.RadiusL))
            .background(CyberpunkColors.BackgroundCard)
            .border(
                width = 2.dp,
                color = neonColor.copy(alpha = 0.7f),
                shape = RoundedCornerShape(Dimens.RadiusL)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// ========== 卡片内容组件 ==========

/**
 * 导航卡片内容
 */
@Composable
private fun NavigationCardContent() {
    Row(
        modifier = Modifier.padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🗺️", fontSize = 28.sp)
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            NeonText(label = "回家")
            NeonText(label = "去公司")
        }

        Text(text = "🏢", fontSize = 28.sp)
    }
}

/**
 * 音乐播放器卡片内容 - 完整的桌面音乐控制
 * 
 * 功能：
 * - 左侧图标：点击选择音乐APP
 * - 中间：歌曲信息 + 歌词显示
 * - 右侧：上一曲/播放暂停/下一曲控制按钮
 */
@Composable
private fun MediaPlayerCardContent() {
    val context = LocalContext.current
    
    // 音乐状态
    var isPlaying by remember { mutableStateOf(false) }
    var currentSong by remember { mutableStateOf("等待播放") }
    var currentArtist by remember { mutableStateOf("") }
    var lyrics by remember { mutableStateOf("") }
    
    // 显示选择音乐APP对话框
    var showMusicAppSelector by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ========== 1. 左侧音乐图标（可点击选择APP）==========
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(CyberpunkColors.NeonPink.copy(alpha = 0.2f))
                .border(
                    width = 1.5.dp,
                    color = CyberpunkColors.NeonPink.copy(alpha = 0.6f),
                    shape = CircleShape
                )
                .clickable(onClick = { showMusicAppSelector = true }),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🎵", fontSize = 28.sp)
        }

        // ========== 2. 中间：歌曲信息和歌词 ==========
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            // 歌曲名称 + 收藏图标
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
            ) {
                Text(
                    text = currentSong,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                
                // 收藏/喜欢图标（心形）
                Text(
                    text = "❤️",
                    fontSize = 16.sp,
                    modifier = Modifier.clickable(onClick = { /* 切换收藏状态 */ })
                )
            }

            // 艺术家名称（如果有）
            if (currentArtist.isNotEmpty()) {
                Text(
                    text = currentArtist,
                    color = CyberpunkColors.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 歌词显示区域（单行滚动）
            if (lyrics.isNotEmpty() && isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberpunkColors.NeonPink.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = lyrics,
                        color = CyberpunkColors.NeonPink.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // ========== 3. 右侧：播放控制按钮 ==========
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 上一曲按钮
            NeonControlButton(
                icon = "⏮️",
                onClick = { sendMediaCommand(context, "previous") },
                size = 40.dp
            )

            // 播放/暂停按钮（主按钮）
            NeonControlButton(
                icon = if (isPlaying) "⏸️" else "▶️",
                onClick = { 
                    isPlaying = !isPlaying
                    sendMediaCommand(context, if (isPlaying) "pause" else "play")
                    
                    // 模拟歌词更新（实际应从媒体控制器获取）
                    if (isPlaying) {
                        currentSong = "正在播放..."
                        currentArtist = "未知艺术家"
                        lyrics = "~ 正在获取歌词 ~"
                    }
                },
                size = 48.dp,
                isMainButton = true
            )

            // 下一曲按钮
            NeonControlButton(
                icon = "⏭️",
                onClick = { sendMediaCommand(context, "next") },
                size = 40.dp
            )
        }
    }

    // 音乐APP选择器对话框
    if (showMusicAppSelector) {
        MusicAppSelectorDialog(
            onDismiss = { showMusicAppSelector = false },
            onAppSelected = { packageName ->
                openSpecificMusicApp(context, packageName)
                showMusicAppSelector = false
                currentSong = "连接中..."
                currentArtist = ""
                lyrics = ""
            }
        )
    }
}

/**
 * 霓虹风格控制按钮
 */
@Composable
private fun NeonControlButton(
    icon: String,
    onClick: () -> Unit,
    size: Dp,
    isMainButton: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (isMainButton) {
                    Modifier.background(CyberpunkColors.NeonPink.copy(alpha = 0.3f))
                        .border(
                            width = 2.dp,
                            color = CyberpunkColors.NeonPink.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                } else {
                    Modifier.background(Color.Transparent)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = if (isMainButton) 24.sp else 20.sp,
            color = if (isMainButton) CyberpunkColors.NeonPink else Color.White
        )
    }
}

/**
 * 发送媒体控制命令到当前播放的音乐应用
 */
private fun sendMediaCommand(context: Context, command: String) {
    try {
        val intent = when (command) {
            "play" -> Intent("com.android.music.musicservicecommand.togglepause")
            "pause" -> Intent("com.android.music.musicservicecommand.pause")
            "next" -> Intent("com.android.music.musicservicecommand.next")
            "previous" -> Intent("com.android.music.musicservicecommand.previous")
            else -> return
        }
        
        intent.putExtra("command", command)
        context.sendBroadcast(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 打开指定的音乐应用
 */
private fun openSpecificMusicApp(context: Context, packageName: String) {
    try {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 音乐APP选择器对话框
 */
@Composable
private fun MusicAppSelectorDialog(
    onDismiss: () -> Unit,
    onAppSelected: (String) -> Unit
) {
    val musicApps = listOf(
        Triple("QQ音乐", "com.tencent.qqmusic", "🎵"),
        Triple("酷狗音乐", "com.kugou.android", "🎶"),
        Triple("网易云音乐", "com.netease.cloudmusic", "🎼"),
        Triple("系统音乐", "", "🔊")
    )

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(400.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(Dimens.RadiusXL),
                    ambientColor = CyberpunkColors.NeonPink.copy(alpha = 0.5f),
                    spotColor = CyberpunkColors.NeonPink
                )
                .clip(RoundedCornerShape(Dimens.RadiusXL))
                .background(Color(0xCC000000))
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            CyberpunkColors.NeonCyan,
                            CyberpunkColors.NeonPink
                        )
                    ),
                    shape = RoundedCornerShape(Dimens.RadiusXL)
                )
                .padding(Dimens.SpaceXL)
        ) {
            Column {
                // 标题
                Text(
                    text = "🎵 选择音乐应用",
                    color = CyberpunkColors.NeonCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(Dimens.SpaceXL))

                // APP列表
                musicApps.forEach { (name, packageIcon, emoji) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimens.RadiusM))
                            .background(CyberpunkColors.BackgroundCard)
                            .clickable(onClick = { onAppSelected(packageIcon) })
                            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = packageIcon, fontSize = 32.sp)

                        Text(
                            text = name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = ">",
                            color = CyberpunkColors.NeonPink,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.SpaceS))
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceL))

                // 取消按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "取消",
                        color = CyberpunkColors.TextHint,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(Dimens.RadiusM))
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = Dimens.SpaceXL, vertical = Dimens.SpaceS)
                    )
                }
            }
        }
    }
}

/**
 * 车辆控制卡片内容
 */
@Composable
private fun VehicleControlCardContent() {
    Row(
        modifier = Modifier.padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🚙", fontSize = 48.sp)

        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VehicleControlItem(icon = "🔓", text = "已开门")
                VehicleControlItem(icon = "💡", text = "已开启")
            }
        }
    }
}

@Composable
private fun VehicleControlItem(icon: String, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 20.sp)
        NeonText(label = text)
    }
}

/**
 * 胎压信息卡片内容
 */
@Composable
private fun TirePressureCardContent() {
    Row(
        modifier = Modifier.padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            NeonTireValue(value = 222f)
            NeonTireValue(value = 232f)
        }

        Text(text = "🚗", fontSize = 40.sp)

        Column {
            NeonTireValue(value = 222f)
            NeonTireValue(value = 233f)
        }
    }
}

@Composable
private fun NeonTireValue(value: Float) {
    Text(
        text = "${value.toInt()} kPa",
        color = CyberpunkColors.NeonCyan,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
    )
}

/**
 * 设置按钮内容
 */
@Composable
private fun SettingsButtonContent() {
    Column(
        modifier = Modifier.padding(Dimens.SpaceS),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⚙️", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(4.dp))
        NeonText(label = "设置")
    }
}

/**
 * 霓虹风格文字标签
 */
@Composable
private fun NeonText(label: String, color: Color = CyberpunkColors.TextSecondary) {
    Text(
        text = label,
        color = color,
        fontSize = 13.sp
    )
}

/**
 * 打开导航应用 - 智能启动策略
 */
private fun openNavigationApp(context: Context) {
    try {
        val intent = context.packageManager.getLaunchIntentForPackage("com.autonavi.minimap")
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    try {
        val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.maps")
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("geo:0,0?q=")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 打开音乐应用
 */
private fun openMusicApp(context: Context) {
    try {
        val intent = context.packageManager.getLaunchIntentForPackage("com.tencent.qqmusic")
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    try {
        val intent = context.packageManager.getLaunchIntentForPackage("com.kugou.android")
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    try {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_MUSIC)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
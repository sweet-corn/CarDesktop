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
import com.cardesktop.ui.theme.CyberpunkColors
import com.cardesktop.ui.theme.ResponsiveDimensions

/**
 * 赛博朋克风格顶部状态栏 - 自适应版本
 */
@Composable
fun CyberpunkStatusBar(
    time: String = "20:44",
    dim: ResponsiveDimensions,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = dim.spaceXL, vertical = dim.spaceS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            color = CyberpunkColors.NeonCyan,
            fontSize = dim.statusBarIconSize.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (1 * dim.scaleFactor).sp
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(dim.spaceM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeonStatusIcon(icon = "📍", size = dim.statusBarIconSize)
            NeonStatusIcon(icon = "🔇", size = dim.statusBarIconSize)
            NeonStatusIcon(icon = "📶", size = dim.statusBarIconSize)
            NeonStatusIcon(icon = "📡", size = dim.statusBarIconSize)
            NeonStatusIcon(icon = "🔋", size = dim.statusBarIconSize)
        }
    }
}

@Composable
private fun NeonStatusIcon(icon: String, size: Float) {
    Text(text = icon, fontSize = size.sp, color = Color.White)
}

@Composable
fun CyberpunkWallpaperCard(
    dim: ResponsiveDimensions,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = dim.cardShadowElevation + 4.dp,
                shape = RoundedCornerShape(dim.wallpaperCornerRadius),
                ambientColor = CyberpunkColors.NeonCyan,
                spotColor = CyberpunkColors.NeonPink
            )
            .border(
                width = dim.wallpaperBorderWidth,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        CyberpunkColors.NeonCyan,
                        CyberpunkColors.NeonPink,
                        CyberpunkColors.NeonCyan
                    )
                ),
                shape = RoundedCornerShape(dim.wallpaperCornerRadius)
            )
            .padding(dim.wallpaperPadding),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(dim.wallpaperCornerRadius - dim.wallpaperPadding))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "CYBERPUNK",
                    color = CyberpunkColors.NeonYellow,
                    fontSize = (48 * dim.scaleFactor).sp.coerceIn(28f, 72f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (8 * dim.scaleFactor).sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = CyberpunkColors.GlowYellow,
                            blurRadius = 20f,
                            offset = androidx.compose.ui.geometry.Offset(0f, 0f)
                        )
                    )
                )
                
                Spacer(modifier = Modifier.height((16 * dim.scaleFactor).dp))
                
                Text(
                    text = "🌃 霓虹城市夜景壁纸",
                    color = CyberpunkColors.TextHint,
                    fontSize = (14 * dim.scaleFactor).sp.coerceIn(10f, 20f)
                )
            }
        }
    }
}

@Composable
fun NeonFunctionCardsRow(
    dim: ResponsiveDimensions,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = dim.spaceL, vertical = dim.spaceS),
        horizontalArrangement = Arrangement.spacedBy(dim.spaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NeonCard(
            neonColor = CyberpunkColors.NeonCyan,
            onClick = { openNavigationApp(context) },
            content = { NavigationCardContent(dim = dim) },
            modifier = Modifier.width(dim.cardNavigationWidth).height(dim.cardHeight)
        )

        NeonCard(
            neonColor = CyberpunkColors.NeonPink,
            onClick = { openMusicApp(context) },
            content = { MediaPlayerCardContent(dim = dim) },
            modifier = Modifier.weight(1f).height(dim.cardHeight)
        )

        NeonCard(
            neonColor = CyberpunkColors.NeonPurple,
            onClick = {},
            content = { VehicleControlCardContent(dim = dim) },
            modifier = Modifier.width(dim.cardVehicleWidth).height(dim.cardHeight)
        )

        NeonCard(
            neonColor = CyberpunkColors.NeonBlue,
            onClick = {},
            content = { TirePressureCardContent(dim = dim) },
            modifier = Modifier.width(dim.cardTireWidth).height(dim.cardHeight)
        )

        NeonCard(
            neonColor = CyberpunkColors.NeonOrange,
            onClick = { 
                context.startActivity(Intent(context, com.cardesktop.ui.screen.SettingsActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            },
            content = { SettingsButtonContent(dim = dim) },
            modifier = Modifier.size(dim.cardSettingsSize)
        )
    }
}

@Composable
private fun NeonCard(
    neonColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dim: ResponsiveDimensions? = null
) {
    val effectiveDim = dim ?: responsiveDimensions()
    
    Box(
        modifier = modifier
            .shadow(
                elevation = effectiveDim.cardShadowElevation,
                shape = RoundedCornerShape(effectiveDim.radiusL),
                ambientColor = neonColor.copy(alpha = 0.6f),
                spotColor = neonColor
            )
            .clip(RoundedCornerShape(effectiveDim.radiusL))
            .background(CyberpunkColors.BackgroundCard)
            .border(
                width = effectiveDim.cardBorderWidth,
                color = neonColor.copy(alpha = 0.7f),
                shape = RoundedCornerShape(effectiveDim.radiusL)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// ========== 卡片内容组件（自适应）==========

@Composable
private fun NavigationCardContent(dim: ResponsiveDimensions) {
    Row(
        modifier = Modifier.padding(horizontal = dim.spaceM, vertical = dim.spaceS),
        horizontalArrangement = Arrangement.spacedBy(dim.spaceL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🗺️", fontSize = (28 * dim.scaleFactor).sp.coerceIn(20f, 42f))
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            NeonText(label = "回家", fontSize = (13 * dim.scaleFactor).sp.coerceIn(10f, 20f))
            NeonText(label = "去公司", fontSize = (13 * dim.scaleFactor).sp.coerceIn(10f, 20f))
        }

        Text(text = "🏢", fontSize = (28 * dim.scaleFactor).sp.coerceIn(20f, 42f))
    }
}

@Composable
private fun MediaPlayerCardContent(dim: ResponsiveDimensions) {
    val context = LocalContext.current
    
    var isPlaying by remember { mutableStateOf(false) }
    var currentSong by remember { mutableStateOf("等待播放") }
    var showMusicAppSelector by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dim.spaceS),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dim.musicCardFixedHeight),
            horizontalArrangement = Arrangement.spacedBy(dim.spaceM, Alignment.CenterVertically),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(dim.musicIconSize)
                    .clip(RoundedCornerShape(dim.radiusM))
                    .background(CyberpunkColors.NeonPink.copy(alpha = 0.2f))
                    .border(
                        width = (2 * dim.scaleFactor).dp,
                        color = CyberpunkColors.NeonPink.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(dim.radiusM)
                    )
                    .clickable(onClick = { showMusicAppSelector = true }),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎵", fontSize = dim.musicIconEmoji.sp)
            }
            
            Spacer(modifier = Modifier.width((8 * dim.scaleFactor).dp))

            Column(
                modifier = Modifier.weight(1f).height((56 * dim.scaleFactor).dp.coerceAtLeast(40.dp)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = currentSong,
                    color = Color.White,
                    fontSize = dim.musicSongNameSize.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height((4 * dim.scaleFactor).dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "❤️", fontSize = dim.musicHeartIcon.sp)
                    
                    Spacer(modifier = Modifier.width((8 * dim.scaleFactor).dp))
                    
                    if (isPlaying) {
                        Text(
                            text = "~ 正在播放 ~",
                            color = CyberpunkColors.NeonPink.copy(alpha = 0.8f),
                            fontSize = dim.musicStatusSize.sp
                        )
                    } else {
                        Text(
                            text = "点击▶播放",
                            color = CyberpunkColors.TextHint,
                            fontSize = dim.musicStatusSize.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width((8 * dim.scaleFactor).dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(dim.spaceS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonControlButton(
                    icon = "⏮️",
                    onClick = { sendMediaCommand(context, "previous") },
                    size = dim.musicControlButtonSmall
                )

                NeonControlButton(
                    icon = if (isPlaying) "⏸️" else "▶️",
                    onClick = { 
                        isPlaying = !isPlaying
                        currentSong = if (isPlaying) "正在播放..." else "已暂停"
                        sendMediaCommand(context, if (isPlaying) "play" else "pause")
                    },
                    size = dim.musicControlButtonMain,
                    isMainButton = true
                )

                NeonControlButton(
                    icon = "⏭️",
                    onClick = { sendMediaCommand(context, "next") },
                    size = dim.musicControlButtonSmall
                )
            }
        }
    }

    if (showMusicAppSelector) {
        SimpleMusicAppDialog(
            dim = dim,
            onDismiss = { showMusicAppSelector = false },
            onAppSelected = { packageName ->
                openSpecificMusicApp(context, packageName)
                showMusicAppSelector = false
            }
        )
    }
}

@Composable
private fun VehicleControlCardContent(dim: ResponsiveDimensions) {
    Row(
        modifier = Modifier.padding(horizontal = dim.spaceM, vertical = dim.spaceS),
        horizontalArrangement = Arrangement.spacedBy(dim.spaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🚙", fontSize = (48 * dim.scaleFactor).sp.coerceIn(32f, 72f))

        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dim.spaceS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VehicleControlItem(icon = "🔓", text = "已开门", dim = dim)
                VehicleControlItem(icon = "💡", text = "已开启", dim = dim)
            }
        }
    }
}

@Composable
private fun VehicleControlItem(icon: String, text: String, dim: ResponsiveDimensions) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = (20 * dim.scaleFactor).sp.coerceIn(14f, 30f))
        NeonText(label = text, fontSize = (13 * dim.scaleFactor).sp.coerceIn(10f, 20f))
    }
}

@Composable
private fun TirePressureCardContent(dim: ResponsiveDimensions) {
    Row(
        modifier = Modifier.padding(horizontal = dim.spaceM, vertical = dim.spaceS),
        horizontalArrangement = Arrangement.spacedBy(dim.spaceS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            NeonTireValue(value = 222f, dim = dim)
            NeonTireValue(value = 232f, dim = dim)
        }

        Text(text = "🚗", fontSize = (40 * dim.scaleFactor).sp.coerceIn(28f, 60f))

        Column {
            NeonTireValue(value = 222f, dim = dim)
            NeonTireValue(value = 233f, dim = dim)
        }
    }
}

@Composable
private fun NeonTireValue(value: Float, dim: ResponsiveDimensions) {
    Text(
        text = "${value.toInt()} kPa",
        color = CyberpunkColors.NeonCyan,
        fontSize = (13 * dim.scaleFactor).sp.coerceIn(9f, 20f),
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun SettingsButtonContent(dim: ResponsiveDimensions) {
    Column(
        modifier = Modifier.padding(dim.spaceS),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⚙️", fontSize = (32 * dim.scaleFactor).sp.coerceIn(24f, 48f))
        Spacer(modifier = Modifier.height((4 * dim.scaleFactor).dp))
        NeonText(label = "设置", fontSize = (13 * dim.scaleFactor).sp.coerceIn(10f, 20f))
    }
}

@Composable
private fun NeonText(label: String, color: Color = CyberpunkColors.TextSecondary, fontSize: Float = 13f) {
    Text(
        text = label,
        color = color,
        fontSize = fontSize.sp
    )
}

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
                            width = (2.5 * (size.value / 52)).dp.coerceAtLeast(1.5.dp),
                            color = CyberpunkColors.NeonPink,
                            shape = CircleShape
                        )
                } else {
                    Modifier.background(CyberpunkColors.BackgroundCard)
                        .border(
                            width = (1.5 * (size.value / 44)).dp.coerceAtLeast(1.dp),
                            color = CyberpunkColors.TextSecondary.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = ((if (isMainButton) 26 else 22) * (size.value / 44)).sp.coerceIn(16f, 32f),
            color = if (isMainButton) CyberpunkColors.NeonPink else Color.White
        )
    }
}

@Composable
private fun SimpleMusicAppDialog(
    dim: ResponsiveDimensions,
    onDismiss: () -> Unit,
    onAppSelected: (String) -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .width(dim.dialogWidth)
                .shadow(
                    elevation = (10 * dim.scaleFactor).dp.coerceAtLeast(6.dp),
                    shape = RoundedCornerShape(dim.radiusL),
                    ambientColor = CyberpunkColors.NeonPink.copy(alpha = 0.5f),
                    spotColor = CyberpunkColors.NeonPink
                )
                .clip(RoundedCornerShape(dim.radiusL))
                .background(Color(0xE6000000))
                .border(
                    width = (2 * dim.scaleFactor).dp.coerceAtLeast(1.dp),
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            CyberpunkColors.NeonCyan,
                            CyberpunkColors.NeonPink
                        )
                    ),
                    shape = RoundedCornerShape(dim.radiusL)
                )
                .padding(dim.spaceXL)
        ) {
            Text(
                text = "🎵 选择音乐应用",
                color = CyberpunkColors.NeonCyan,
                fontSize = dim.dialogTitleSize.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(dim.spaceXL))

            MusicAppOptionItem(
                name = "QQ音乐",
                packageName = "com.tencent.qqmusic",
                emoji = "🎵",
                dim = dim,
                onSelected = onAppSelected
            )

            Spacer(modifier = Modifier.height(dim.spaceM))

            MusicAppOptionItem(
                name = "网易云音乐",
                packageName = "com.netease.cloudmusic",
                emoji = "🎼",
                dim = dim,
                onSelected = onAppSelected
            )

            Spacer(modifier = Modifier.height(dim.spaceXL))

            Text(
                text = "取消",
                color = CyberpunkColors.TextHint,
                fontSize = dim.dialogCancelSize.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = dim.spaceXL, vertical = dim.spaceS)
            )
        }
    }
}

@Composable
private fun MusicAppOptionItem(
    name: String,
    packageName: String,
    emoji: String,
    dim: ResponsiveDimensions,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dim.radiusM))
            .background(CyberpunkColors.BackgroundCard)
            .clickable(onClick = { onSelected(packageName) })
            .padding(horizontal = dim.spaceL, vertical = dim.spaceM),
        horizontalArrangement = Arrangement.spacedBy(dim.spaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = dim.dialogAppIconSize.sp)

        Text(
            text = name,
            color = Color.White,
            fontSize = dim.dialogAppNameSize.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "▶",
            color = CyberpunkColors.NeonPink,
            fontSize = (20 * dim.scaleFactor).sp.coerceIn(14f, 30f)
        )
    }
}

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
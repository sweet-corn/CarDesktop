package com.cardesktop.ui.widget

import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.cardesktop.data.model.AppInfo
import com.cardesktop.data.repository.AppRepository
import com.cardesktop.ui.theme.CyberpunkColors
import com.cardesktop.ui.theme.ResponsiveDimensions

/**
 * 赛博朋克风格全部应用弹窗
 */
@Composable
fun CyberpunkAppDrawerDialog(
    dim: ResponsiveDimensions,
    onDismiss: () -> Unit,
    onAppClick: (AppInfo) -> Unit
) {
    val context = LocalContext.current
    var allApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var currentPage by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val appsPerPage = 24

    LaunchedEffect(Unit) {
        allApps = AppRepository.getInstalledApps()
        isLoading = false
    }

    val totalPages = (allApps.size + appsPerPage - 1) / appsPerPage

    val currentApps = remember(allApps, currentPage) {
        val start = currentPage * appsPerPage
        val end = minOf(start + appsPerPage, allApps.size)
        if (start < allApps.size) allApps.subList(start, end) else emptyList()
    }

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
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(dim.radiusXL),
                        ambientColor = CyberpunkColors.NeonGreen.copy(alpha = 0.3f),
                        spotColor = CyberpunkColors.NeonCyan.copy(alpha = 0.3f)
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
                                CyberpunkColors.NeonGreen.copy(alpha = 0.6f),
                                CyberpunkColors.NeonCyan.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(dim.radiusXL)
                    )
                    .clickable(enabled = false) {}
                    .padding(dim.spaceXL)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📱 全部应用",
                        color = CyberpunkColors.NeonGreen,
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
                
                // 应用网格区域
                if (isLoading) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "加载中...",
                            color = CyberpunkColors.NeonCyan,
                            fontSize = (18 * dim.scaleFactor).coerceIn(14f, 28f).sp
                        )
                    }
                } else if (currentApps.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无应用",
                            color = CyberpunkColors.TextSecondary,
                            fontSize = (18 * dim.scaleFactor).coerceIn(14f, 28f).sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        horizontalArrangement = Arrangement.spacedBy(dim.spaceM),
                        verticalArrangement = Arrangement.spacedBy(dim.spaceS),
                        contentPadding = PaddingValues(dim.spaceS),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        items(currentApps, key = { it.packageName }) { app ->
                            CyberpunkAppIconItem(
                                app = app,
                                dim = dim,
                                onClick = {
                                    onAppClick(app)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
                
                // 分页指示器
                if (totalPages > 1 && !isLoading) {
                    Spacer(modifier = Modifier.height(dim.spaceM))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dim.spaceS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "<",
                            color = if (currentPage > 0) CyberpunkColors.NeonCyan else Color.Gray,
                            fontSize = (20 * dim.scaleFactor).coerceIn(14f, 30f).sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(enabled = currentPage > 0) {
                                if (currentPage > 0) currentPage--
                            }
                        )

                        repeat(totalPages) { page ->
                            Box(
                                modifier = Modifier
                                    .size(
                                        if (page == currentPage)
                                            (10 * dim.scaleFactor).dp.coerceAtLeast(6.dp)
                                        else
                                            (8 * dim.scaleFactor).dp.coerceAtLeast(5.dp)
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        if (page == currentPage) CyberpunkColors.NeonGreen else Color.White.copy(alpha = 0.4f)
                                    )
                                    .clickable { currentPage = page }
                            )
                        }

                        Text(
                            text = ">",
                            color = if (currentPage < totalPages - 1) CyberpunkColors.NeonCyan else Color.Gray,
                            fontSize = (20 * dim.scaleFactor).coerceIn(14f, 30f).sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(enabled = currentPage < totalPages - 1) {
                                if (currentPage < totalPages - 1) currentPage++
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CyberpunkAppIconItem(
    app: AppInfo,
    dim: ResponsiveDimensions,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(((90 * dim.scaleFactor).dp).coerceIn(60.dp, 120.dp))
            .height(((100 * dim.scaleFactor).dp).coerceIn(70.dp, 130.dp))
            .clip(RoundedCornerShape(dim.radiusM))
            .background(Color(0x30FFFFFF))
            .border(
                width = 1.dp,
                color = CyberpunkColors.NeonCyan.copy(alpha = 0.2f),
                shape = RoundedCornerShape(dim.radiusM)
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(((56 * dim.scaleFactor).dp).coerceIn(40.dp, 72.dp))
                .clip(RoundedCornerShape(((14 * dim.scaleFactor).dp).coerceIn(10.dp, 18.dp)))
                .background(Color(0xFF2C3E50))
                .padding((4 * dim.scaleFactor).dp),
            contentAlignment = Alignment.Center
        ) {
            if (app.icon != null) {
                Image(
                    painter = drawablePainter(app.icon),
                    contentDescription = app.label,
                    modifier = Modifier.size(((48 * dim.scaleFactor).dp).coerceIn(34.dp, 62.dp))
                )
            } else {
                Text(
                    text = app.label.firstOrNull()?.toString() ?: "?",
                    color = CyberpunkColors.NeonCyan,
                    fontSize = (28 * dim.scaleFactor).coerceIn(20f, 42f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height((4 * dim.scaleFactor).dp))

        Text(
            text = app.label,
            color = Color.White,
            fontSize = (11 * dim.scaleFactor).coerceIn(8f, 16f).sp,
            maxLines = 1,
            modifier = Modifier.widthIn(max = ((80 * dim.scaleFactor).dp).coerceIn(56.dp, 104.dp))
        )
    }
}
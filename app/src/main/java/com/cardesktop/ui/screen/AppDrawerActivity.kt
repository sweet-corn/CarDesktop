package com.cardesktop.ui.screen

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cardesktop.data.model.AppInfo
import com.cardesktop.data.repository.AppRepository
import com.cardesktop.ui.theme.*

/**
 * 应用抽屉 - 显示所有已安装应用（参照图2样式）
 * 
 * 特点：
 * - 6列网格布局
 * - 显示应用图标+名称
 * - 底部分页指示器
 * - 深色半透明背景
 */
class AppDrawerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var allApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
            var currentPage by remember { mutableStateOf(0) }
            var isLoading by remember { mutableStateOf(true) }

            // 每页显示的应用数量
            val appsPerPage = 24

            LaunchedEffect(Unit) {
                allApps = AppRepository.getInstalledApps()
                isLoading = false
            }

            // 计算总页数
            val totalPages = (allApps.size + appsPerPage - 1) / appsPerPage

            // 当前页的应用列表
            val currentApps = remember(allApps, currentPage) {
                val start = currentPage * appsPerPage
                val end = minOf(start + appsPerPage, allApps.size)
                if (start < allApps.size) allApps.subList(start, end) else emptyList()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000)) // 深色半透明背景
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 应用网格区域
                    if (isLoading) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "加载中...", color = Color.White, fontSize = 18.sp)
                        }
                    } else if (currentApps.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "暂无应用", color = Color.White, fontSize = 18.sp)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(6), // 6列布局，参照图2
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        ) {
                            items(currentApps, key = { it.packageName }) { app ->
                                AppIconItem(
                                    app = app,
                                    onClick = {
                                        launchApp(app)
                                        finish()
                                    }
                                )
                            }
                        }
                    }

                    // 分页指示器（底部圆点）
                    if (totalPages > 1 && !isLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 上一页按钮
                            Text(
                                text = "<",
                                color = if (currentPage > 0) Color.White else Color.Gray,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable(enabled = currentPage > 0) {
                                    if (currentPage > 0) currentPage--
                                }
                            )

                            // 页码圆点
                            repeat(totalPages) { page ->
                                Box(
                                    modifier = Modifier
                                        .size(if (page == currentPage) 10.dp else 8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (page == currentPage) Color.White else Color.White.copy(alpha = 0.4f)
                                        )
                                        .clickable { currentPage = page }
                                )
                            }

                            // 下一页按钮
                            Text(
                                text = ">",
                                color = if (currentPage < totalPages - 1) Color.White else Color.Gray,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable(enabled = currentPage < totalPages - 1) {
                                    if (currentPage < totalPages - 1) currentPage++
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 关闭提示文字
                    Text(
                        text = "点击任意位置或按返回键关闭",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    /**
     * 启动应用
     */
    private fun launchApp(app: AppInfo) {
        try {
            val intent = app.launchIntent?.let {
                Intent().apply {
                    component = ComponentName(app.packageName, it)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } ?: packageManager.getLaunchIntentForPackage(app.packageName)
            
            intent?.let { startActivity(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * 应用图标项 - 参照图2样式
 */
@Composable
private fun AppIconItem(
    app: AppInfo,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(90.dp)
            .height(100.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 应用图标
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                .background(Color(0xFF2C3E50))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // 如果有图标则显示图标，否则显示首字母
            if (app.icon != null) {
                androidx.compose.foundation.Image(
                    painter = app.icon!!,
                    contentDescription = app.label,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Text(
                    text = app.label.firstOrNull()?.toString() ?: "?",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 应用名称
        Text(
            text = app.label,
            color = Color.White,
            fontSize = 11.sp,
            maxLines = 1,
            modifier = Modifier.widthIn(max = 80.dp)
        )
    }
}
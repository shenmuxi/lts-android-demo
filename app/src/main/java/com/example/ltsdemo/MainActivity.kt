package com.example.ltsdemo

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.ltsdemo.ui.LogTestScreen
import com.example.ltsdemo.ui.SettingsScreen
import com.example.ltsdemo.ui.ConcurrentTestScreen
import com.example.ltsdemo.ui.MultiInstanceTestScreen
import com.example.ltsdemo.ui.theme.LTSDemoTheme
import com.cloud.lts.database.DatabaseUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // Auto-initialize LTS SDK if saved config exists
        val savedConfig = ConfigManager.loadSavedConfig(this)
        if (savedConfig != null) {
            LtsManager.initialize(
                this.application,
                savedConfig.host,
                savedConfig.region,
                savedConfig.projectId,
                savedConfig.instances.firstOrNull()?.groupId ?: "",
                savedConfig.instances.firstOrNull()?.streamId ?: "",
                savedConfig.ak,
                savedConfig.sk,
                savedConfig.cacheThreshold,
                savedConfig.timeInterval
            )
        }

        setContent {
            LTSDemoTheme {
                MainContainer()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("单条上报", "批量上报", "多实例上报", "配置详情")
    var showInitToast by remember { mutableStateOf(!LtsManager.isInitialized()) }

    Scaffold(
        // Removed TopAppBar as per request
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(64.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        label = { 
                            Text(
                                title,
                                style = if (isSelected) 
                                    MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
                                else 
                                    MaterialTheme.typography.labelMedium
                            )
                        },
                        icon = { /* No icon */ },
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    TabContent(0, selectedTab) { LogTestScreen() }
                    TabContent(1, selectedTab) { ConcurrentTestScreen() }
                    TabContent(2, selectedTab) { MultiInstanceTestScreen() }
                    TabContent(3, selectedTab) { SettingsScreen() }
                }
            }
            FloatingLogCount()

            if (showInitToast) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        shape = MaterialTheme.shapes.medium,
                        shadowElevation = 8.dp
                    ) {
                        Text(
                            text = "前往配置页面\n进行SDK初始化",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                LaunchedEffect(Unit) {
                    delay(3000)
                    showInitToast = false
                }
            }
        }
    }
}

@Composable
fun TabContent(index: Int, selectedIndex: Int, content: @Composable () -> Unit) {
    val active = index == selectedIndex
    // Preserve the state once the tab has been visited
    var hasBeenVisited by remember { mutableStateOf(false) }
    if (active) hasBeenVisited = true
    
    if (hasBeenVisited) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = if (active) 1f else 0f
                    // If not active, move far away to prevent interaction and layout overlap issues
                    translationX = if (active) 0f else 10000f
                }
        ) {
            content()
        }
    }
}

@Composable
fun FloatingLogCount() {
    var logCount by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                if (DatabaseUtil.isInitDatabaseComplete) {
                    val count = withContext(Dispatchers.IO) {
                        DatabaseUtil.dataDao.getAllCount()
                    }
                    logCount = count
                }
            } catch (e: Exception) {
                // Ignore errors
                delay(1)
            }
            delay(2000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 4.dp
        ) {
            Text(
                text = "DB: $logCount",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

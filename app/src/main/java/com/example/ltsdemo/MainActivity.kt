package com.example.ltsdemo

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
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
                when (selectedTab) {
                    0 -> LogTestScreen()
                    1 -> ConcurrentTestScreen()
                    2 -> MultiInstanceTestScreen()
                    3 -> SettingsScreen()
                }
            }
            FloatingLogCount()
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
                text = "DB Logs: $logCount",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

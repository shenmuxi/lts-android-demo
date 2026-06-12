package com.example.ltsdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ltsdemo.ui.LogTestScreen
import com.example.ltsdemo.ui.SettingsScreen
import com.example.ltsdemo.ui.ConcurrentTestScreen
import com.example.ltsdemo.ui.theme.LTSDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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

        enableEdgeToEdge()
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
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("单条上报", "批量上报", "配置详情")

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
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> LogTestScreen()
                1 -> ConcurrentTestScreen()
                2 -> SettingsScreen()
            }
        }
    }
}

package com.example.ltsdemo.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ltsdemo.*

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val predefinedConfigs = remember { ConfigManager.loadPredefinedConfigs(context) }
    
    // Initial Load
    var currentConfig by remember { 
        mutableStateOf(ConfigManager.loadSavedConfig(context) ?: predefinedConfigs.firstOrNull() ?: LtsFullConfig()) 
    }

    // Editable state derived from currentConfig
    var region by remember(currentConfig) { mutableStateOf(currentConfig.region) }
    var projectId by remember(currentConfig) { mutableStateOf(currentConfig.projectId) }
    var ak by remember(currentConfig) { mutableStateOf(currentConfig.ak) }
    var sk by remember(currentConfig) { mutableStateOf(currentConfig.sk) }
    var cacheThreshold by remember(currentConfig) { mutableStateOf(currentConfig.cacheThreshold.toString()) }
    var timeInterval by remember(currentConfig) { mutableStateOf(currentConfig.timeInterval.toString()) }
    
    // We only handle the first instance for simplicity in this UI
    var groupId by remember(currentConfig) { mutableStateOf(currentConfig.instances.firstOrNull()?.groupId ?: "") }
    var streamId by remember(currentConfig) { mutableStateOf(currentConfig.instances.firstOrNull()?.streamId ?: "") }

    var showConfigList by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("LTS SDK 配置", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { showConfigList = true }) {
                Text("选择预设配置")
            }
        }
        
        if (showConfigList) {
            AlertDialog(
                onDismissRequest = { showConfigList = false },
                title = { Text("选择配置") },
                text = {
                    Column {
                        predefinedConfigs.forEach { config ->
                            TextButton(
                                onClick = {
                                    currentConfig = config
                                    showConfigList = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(config.title)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showConfigList = false }) { Text("关闭") }
                }
            )
        }

        OutlinedTextField(value = region, onValueChange = { region = it }, label = { Text("区域 (Region)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = projectId, onValueChange = { projectId = it }, label = { Text("项目 ID (Project ID)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = groupId, onValueChange = { groupId = it }, label = { Text("日志组 ID (Log Group ID)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = streamId, onValueChange = { streamId = it }, label = { Text("日志流 ID (Log Stream ID)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = ak, onValueChange = { ak = it }, label = { Text("Access Key (AK)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = sk, onValueChange = { sk = it }, label = { Text("Secret Key (SK)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = cacheThreshold, onValueChange = { cacheThreshold = it }, label = { Text("缓存阈值 (条数)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = timeInterval, onValueChange = { timeInterval = it }, label = { Text("上报间隔 (秒)") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                val newConfig = LtsFullConfig(
                    title = "Modified",
                    region = region,
                    projectId = projectId,
                    ak = ak,
                    sk = sk,
                    cacheThreshold = cacheThreshold.toIntOrNull() ?: 200,
                    timeInterval = timeInterval.toIntOrNull() ?: 3,
                    instances = listOf(LtsInstance(groupId, streamId))
                )
                // Persistence
                ConfigManager.saveConfig(context, newConfig)
                
                // SDK Initialization
                try {
                    LtsManager.initialize(
                        context.applicationContext as android.app.Application,
                        newConfig.region,
                        newConfig.projectId,
                        newConfig.instances.firstOrNull()?.groupId ?: "",
                        newConfig.instances.firstOrNull()?.streamId ?: "",
                        newConfig.ak,
                        newConfig.sk,
                        newConfig.cacheThreshold,
                        newConfig.timeInterval
                    )
                    val toast = Toast.makeText(context, "配置已保存，SDK 初始化成功", Toast.LENGTH_SHORT)
                    toast.setGravity(android.view.Gravity.CENTER, 0, 0)
                    toast.show()
                } catch (e: Exception) {
                    val toast = Toast.makeText(context, "SDK 初始化失败: ${e.message}", Toast.LENGTH_LONG)
                    toast.setGravity(android.view.Gravity.CENTER, 0, 0)
                    toast.show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存并初始化 SDK")
        }
    }
    
    // Auto-init on startup handled in LtsManager or elsewhere if needed, 
    // but keep LaunchedEffect if we want to ensure it runs when this screen first appears
    LaunchedEffect(Unit) {
        val saved = ConfigManager.loadSavedConfig(context)
        if (saved != null && !LtsManager.isInitialized()) {
            LtsManager.initialize(
                context.applicationContext as android.app.Application,
                saved.region,
                saved.projectId,
                saved.instances.firstOrNull()?.groupId ?: "",
                saved.instances.firstOrNull()?.streamId ?: "",
                saved.ak,
                saved.sk,
                saved.cacheThreshold,
                saved.timeInterval
            )
        }
    }
}

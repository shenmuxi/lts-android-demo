package com.example.ltsdemo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ltsdemo.*
import com.example.ltsdemo.ui.components.CenterSnackbarHost
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var predefinedConfigs by remember { mutableStateOf<List<LtsFullConfig>>(emptyList()) }
    
    // Initial Load
    var currentConfig by remember { 
        mutableStateOf(ConfigManager.loadSavedConfig(context) ?: LtsFullConfig()) 
    }

    // Editable state
    var host by remember(currentConfig) { mutableStateOf(currentConfig.host) }
    var region by remember(currentConfig) { mutableStateOf(currentConfig.region) }
    var projectId by remember(currentConfig) { mutableStateOf(currentConfig.projectId) }
    var ak by remember(currentConfig) { mutableStateOf(currentConfig.ak) }
    var sk by remember(currentConfig) { mutableStateOf(currentConfig.sk) }
    var cacheThreshold by remember(currentConfig) { mutableStateOf(currentConfig.cacheThreshold.toString()) }
    var timeInterval by remember(currentConfig) { mutableStateOf(currentConfig.timeInterval.toString()) }
    var groupId by remember(currentConfig) { mutableStateOf(currentConfig.instances.firstOrNull()?.groupId ?: "") }
    var streamId by remember(currentConfig) { mutableStateOf(currentConfig.instances.firstOrNull()?.streamId ?: "") }

    var showConfigList by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.ime)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("LTS SDK 配置", style = MaterialTheme.typography.titleLarge)
            
            // Password Dialog
            if (showPasswordDialog) {
                AlertDialog(
                    onDismissRequest = { showPasswordDialog = false },
                    title = { Text("解密密码") },
                    text = {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("输入密码") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            try {
                                predefinedConfigs = ConfigManager.loadDecryptedConfigs(context, password)
                                showPasswordDialog = false
                                showConfigList = true
                            } catch (e: Exception) {
                                scope.launch { snackbarHostState.showSnackbar("解密失败: 请检查密码") }
                            }
                        }) { Text("解密") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPasswordDialog = false }) { Text("取消") }
                    }
                )
            }
            
            // Config Selection Dialog
            if (showConfigList) {
                AlertDialog(
                    onDismissRequest = { showConfigList = false },
                    title = { Text("选择预设配置") },
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

            OutlinedTextField(value = host, onValueChange = { host = it }, label = { Text("Host") }, modifier = Modifier.fillMaxWidth().height(64.dp))
            OutlinedTextField(value = region, onValueChange = { region = it }, label = { Text("区域 (Region)") }, modifier = Modifier.fillMaxWidth().height(64.dp))
            OutlinedTextField(value = projectId, onValueChange = { projectId = it }, label = { Text("项目 ID (Project ID)") }, modifier = Modifier.fillMaxWidth().height(64.dp))
            OutlinedTextField(value = groupId, onValueChange = { groupId = it }, label = { Text("日志组 ID (Log Group ID)") }, modifier = Modifier.fillMaxWidth().height(64.dp))
            OutlinedTextField(value = streamId, onValueChange = { streamId = it }, label = { Text("日志流 ID (Log Stream ID)") }, modifier = Modifier.fillMaxWidth().height(64.dp))
            OutlinedTextField(value = ak, onValueChange = { ak = it }, label = { Text("Access Key (AK)") }, modifier = Modifier.fillMaxWidth().height(64.dp))
            OutlinedTextField(value = sk, onValueChange = { sk = it }, label = { Text("Secret Key (SK)") }, modifier = Modifier.fillMaxWidth().height(64.dp))
            OutlinedTextField(value = cacheThreshold, onValueChange = { cacheThreshold = it }, label = { Text("缓存阈值 (条数)") }, modifier = Modifier.fillMaxWidth().height(64.dp))
            OutlinedTextField(value = timeInterval, onValueChange = { timeInterval = it }, label = { Text("上报间隔 (秒)") }, modifier = Modifier.fillMaxWidth().height(64.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val newConfig = LtsFullConfig(
                            title = currentConfig.title,
                            host = host,
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
                                newConfig.host,
                                newConfig.region,
                                newConfig.projectId,
                                newConfig.instances.firstOrNull()?.groupId ?: "",
                                newConfig.instances.firstOrNull()?.streamId ?: "",
                                newConfig.ak,
                                newConfig.sk,
                                newConfig.cacheThreshold,
                                newConfig.timeInterval
                            )
                            scope.launch { snackbarHostState.showSnackbar("配置已保存，SDK 初始化成功") }
                        } catch (e: Exception) {
                            scope.launch { snackbarHostState.showSnackbar("SDK 初始化失败: ${e.message}") }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存&初始化", maxLines = 1)
                }

                OutlinedButton(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("加载预配置", maxLines = 1)
                }
            }
        }
        
        CenterSnackbarHost(snackbarHostState)
    }
    
    // Auto-init on startup
    LaunchedEffect(Unit) {
        val saved = ConfigManager.loadSavedConfig(context)
        if (saved != null && !LtsManager.isInitialized()) {
            LtsManager.initialize(
                context.applicationContext as android.app.Application,
                saved.host,
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

package com.example.ltsdemo.ui

import androidx.compose.foundation.border
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
import kotlin.random.Random

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@Composable
fun MultiInstanceTestScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var predefinedConfigs by remember { mutableStateOf<List<LtsFullConfig>>(emptyList()) }
    var selectedConfig by remember { mutableStateOf<LtsFullConfig?>(null) }
    
    var showConfigList by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    // Test Parameters
    var reportCount by remember { mutableStateOf("10") }
    var threadCount by remember { mutableStateOf("2") }
    var isReporting by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.ime),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("多实例并发上报", style = MaterialTheme.typography.titleLarge)

            // Configuration Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("当前配置: ${selectedConfig?.title ?: "未选择"}", style = MaterialTheme.typography.titleMedium)
                    
                    Button(
                        onClick = { showPasswordDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("选择预配置")
                    }
                }
            }

            // Instance List
            if (selectedConfig != null) {
                Text("实例列表 (${selectedConfig?.instances?.size ?: 0})", style = MaterialTheme.typography.titleSmall)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
                    shape = MaterialTheme.shapes.small
                ) {
                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                        itemsIndexed(selectedConfig?.instances ?: emptyList()) { index, instance ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("实例 #$index", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text("Group: ${instance.groupId}", style = MaterialTheme.typography.bodySmall)
                                Text("Stream: ${instance.streamId}", style = MaterialTheme.typography.bodySmall)
                                Divider()
                            }
                        }
                    }
                }
            }

            // Parameters
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = reportCount,
                    onValueChange = { reportCount = it },
                    label = { Text("每实例发送量") },
                    modifier = Modifier.weight(1f).height(64.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = threadCount,
                    onValueChange = { threadCount = it },
                    label = { Text("线程数") },
                    modifier = Modifier.weight(1f).height(64.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            if (isReporting) {
                LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val config = selectedConfig ?: return@Button
                        val totalToReport = reportCount.toIntOrNull() ?: 0
                        val threads = threadCount.toIntOrNull() ?: 1
                        
                        if (totalToReport <= 0) return@Button
                        
                        isReporting = true
                        progress = 0f
                        
                        scope.launch(Dispatchers.Default) {
                            val executor = Executors.newFixedThreadPool(threads)
                            val completedCount = AtomicInteger(0)
                            
                            repeat(totalToReport) { idx ->
                                executor.execute {
                                    val labels = mapOf("batch_idx" to idx)
                                    val content = mapOf(
                                        "msg" to "Multi-instance concurrent report",
                                        "rand" to generateRandomString(10),
                                        "batch" to idx
                                    )
                                    LtsManager.reportMulti(labels, content)
                                    
                                    val completed = completedCount.incrementAndGet()
                                    progress = completed.toFloat() / totalToReport
                                }
                            }
                            
                            executor.shutdown()
                            while (!executor.isTerminated) { 
                                kotlinx.coroutines.delay(100) 
                            }
                            
                            withContext(Dispatchers.Main) {
                                isReporting = false
                                scope.launch { snackbarHostState.showSnackbar("并发上报完成") }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isReporting && selectedConfig != null
                ) {
                    Text(if (isReporting) "正在上报..." else "并发上报")
                }

                Button(
                    onClick = {
                        val config = selectedConfig ?: return@Button
                        val totalToReport = reportCount.toIntOrNull() ?: 0
                        val threads = threadCount.toIntOrNull() ?: 1
                        
                        if (totalToReport <= 0) return@Button
                        
                        isReporting = true
                        progress = 0f
                        
                        scope.launch(Dispatchers.Default) {
                            val executor = Executors.newFixedThreadPool(threads)
                            val completedCount = AtomicInteger(0)
                            
                            repeat(totalToReport) { idx ->
                                executor.execute {
                                    val labels = mapOf("batch_idx" to idx)
                                    val content = mapOf(
                                        "msg" to "Multi-instance concurrent immediate",
                                        "rand" to generateRandomString(10),
                                        "batch" to idx
                                    )
                                    LtsManager.reportMultiImmediately(labels, content)
                                    
                                    val completed = completedCount.incrementAndGet()
                                    progress = completed.toFloat() / totalToReport
                                }
                            }
                            
                            executor.shutdown()
                            while (!executor.isTerminated) { 
                                kotlinx.coroutines.delay(100) 
                            }
                            
                            withContext(Dispatchers.Main) {
                                isReporting = false
                                scope.launch { snackbarHostState.showSnackbar("并发立即上报完成") }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isReporting && selectedConfig != null
                ) {
                    Text(if (isReporting) "正在上报..." else "并发立即上报")
                }
            }

            // Password Dialog (keep existing)
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
                                scope.launch { snackbarHostState.showSnackbar("解密失败") }
                            }
                        }) { Text("解密") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPasswordDialog = false }) { Text("取消") }
                    }
                )
            }
            
            // Config Selection Dialog (keep existing)
            if (showConfigList) {
                AlertDialog(
                    onDismissRequest = { showConfigList = false },
                    title = { Text("选择配置") },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            predefinedConfigs.forEach { config ->
                                TextButton(
                                    onClick = {
                                        selectedConfig = config
                                        showConfigList = false
                                        LtsManager.initializeMulti(context.applicationContext as android.app.Application, config)
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
        }
        
        CenterSnackbarHost(snackbarHostState)
    }
}

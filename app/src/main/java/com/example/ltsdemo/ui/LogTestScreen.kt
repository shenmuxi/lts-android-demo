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
import com.example.ltsdemo.LtsManager
import com.example.ltsdemo.ui.components.CenterSnackbarHost
import com.cloud.lts.database.LogDatabase
import java.io.FileOutputStream
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import kotlin.random.Random

@Composable
fun LogTestScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var lockJob by remember { mutableStateOf<Job?>(null) }
    val isDbLocked = lockJob != null

    // 专门用于锁定数据库的单线程调度器，确保开始和结束事务在同一线程
    val lockDispatcher = remember { Executors.newSingleThreadExecutor().asCoroutineDispatcher() }
    DisposableEffect(Unit) {
        onDispose {
            (lockDispatcher.executor as ExecutorService).shutdown()
        }
    }

    // Input Key/Value
    var inputKey by remember { mutableStateOf("") }
    var inputValue by remember { mutableStateOf("") }
    
    // Configured Labels and Content Maps
    val labelsMap = remember { mutableStateMapOf<String, Any>() }
    val contentMap = remember { mutableStateMapOf<String, Any>() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.ime)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("编辑标签与内容", style = MaterialTheme.typography.titleLarge)

            // 1. Labels and Content Display Windows
            Row(modifier = Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Labels Window
                Surface(
                    modifier = Modifier.weight(1f).fillMaxHeight().border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Labels", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            if (labelsMap.isNotEmpty()) {
                                TextButton(
                                    onClick = { labelsMap.clear() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.heightIn(max = 32.dp)
                                ) {
                                    Text("清空", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            labelsMap.forEach { (k, v) ->
                                Text("$k: $v", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Content Window
                Surface(
                    modifier = Modifier.weight(1f).fillMaxHeight().border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Content", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                            if (contentMap.isNotEmpty()) {
                                TextButton(
                                    onClick = { contentMap.clear() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.heightIn(max = 32.dp)
                                ) {
                                    Text("清空", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            contentMap.forEach { (k, v) ->
                                Text("$k: $v", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // 2. Key/Value Input Section
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { if (it.length <= 100) inputKey = it },
                    label = { Text("键 (Key)") },
                    modifier = Modifier.weight(1f).height(90.dp),
                    supportingText = { Text("${inputKey.length}/100") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { if (it.length <= 100) inputValue = it },
                    label = { Text("值 (Value)") },
                    modifier = Modifier.weight(1f).height(90.dp),
                    supportingText = { Text("${inputValue.length}/100") },
                    singleLine = true
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        inputKey = generateRandomString(Random.nextInt(1, 11))
                        inputValue = generateRandomString(Random.nextInt(1, 91))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("随机生成")
                }
                Button(
                    onClick = {
                        inputKey = ""
                        inputValue = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("清空输入")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (inputKey.isNotBlank()) {
                            labelsMap[inputKey] = inputValue
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("添加到 Labels")
                }
                Button(
                    onClick = {
                        if (inputKey.isNotBlank()) {
                            contentMap[inputKey] = inputValue
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("添加到 Content")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (!LtsManager.isInitialized()) {
                        scope.launch { snackbarHostState.showSnackbar("SDK未初始化，请先配置") }
                        return@Button
                    }
                    if (contentMap.isEmpty()) {
                        scope.launch { snackbarHostState.showSnackbar("Content 不能为空") }
                        return@Button
                    }
                    LtsManager.report(labelsMap.toMap(), contentMap.toMap())
                }, modifier = Modifier.weight(1f)) {
                    Text("普通上报")
                }

                Button(onClick = {
                    if (!LtsManager.isInitialized()) {
                        scope.launch { snackbarHostState.showSnackbar("SDK未初始化，请先配置") }
                        return@Button
                    }
                    if (contentMap.isEmpty()) {
                        scope.launch { snackbarHostState.showSnackbar("Content 不能为空") }
                        return@Button
                    }
                    LtsManager.reportImmediately(labelsMap.toMap(), contentMap.toMap())
                }, modifier = Modifier.weight(1f)) {
                    Text("立即上报")
                }
            }

            Button(
                onClick = {
                    if (!LtsManager.isInitialized()) {
                        scope.launch { snackbarHostState.showSnackbar("SDK未初始化，请先配置") }
                        return@Button
                    }
                    if (!isDbLocked) {
                        lockJob = scope.launch(lockDispatcher) {
                            val db = LogDatabase.getInstance(context)
                            val sdb = db.openHelper.writableDatabase
                            try {
                                sdb.beginTransaction()
                                awaitCancellation()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                withContext(NonCancellable) {
                                    try {
                                        if (sdb.inTransaction()) {
                                            sdb.endTransaction()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    } else {
                        lockJob?.cancel()
                        lockJob = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDbLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isDbLocked) "解锁数据库" else "锁定数据库 - 触发SQLite Busy异常")
            }

            Button(
                onClick = {
                    if (!LtsManager.isInitialized()) {
                        scope.launch { snackbarHostState.showSnackbar("SDK未初始化，请先配置") }
                        return@Button
                    }
                    scope.launch(Dispatchers.IO) {
                        try {
                            val db = LogDatabase.getInstance(context)
                            val dbPath = db.openHelper.writableDatabase.path
                            db.close()
                            
                            val dbFile = java.io.File(dbPath)
                            if (dbFile.exists()) {
                                FileOutputStream(dbFile).use { fos ->
                                    val junk = "CORRUPTED_DATABASE_DATA_".repeat(1000).toByteArray()
                                    fos.write(junk)
                                    fos.flush()
                                    fos.fd.sync()
                                }
                                java.io.File("$dbPath-wal").delete()
                                java.io.File("$dbPath-shm").delete()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        delay(500)
                        withContext(Dispatchers.Main) {
                            android.os.Process.killProcess(android.os.Process.myPid())
                            java.lang.System.exit(0)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("毁坏数据库 - 写入脏数据")
            }

            Button(
                onClick = {
                    if (!LtsManager.isInitialized()) {
                        scope.launch { snackbarHostState.showSnackbar("SDK未初始化，请先配置") }
                        return@Button
                    }
                    scope.launch(Dispatchers.IO) {
                        try {
                            val db = LogDatabase.getInstance(context)
                            val dbPath = db.openHelper.writableDatabase.path
                            db.close()

                            val dbFile = java.io.File(dbPath)
                            if (dbFile.exists()) {
                                context.assets.open("configs_encrypted.json").use { inputStream ->
                                    FileOutputStream(dbFile).use { outputStream ->
                                        inputStream.copyTo(outputStream)
                                        outputStream.flush()
                                        outputStream.fd.sync()
                                    }
                                }
                                java.io.File("$dbPath-wal").delete()
                                java.io.File("$dbPath-shm").delete()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        delay(500)
                        withContext(Dispatchers.Main) {
                            android.os.Process.killProcess(android.os.Process.myPid())
                            java.lang.System.exit(0)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("毁坏数据库 - 非DB文件")
            }
        }
        
        CenterSnackbarHost(snackbarHostState)
    }
}

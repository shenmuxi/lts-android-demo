package com.example.ltsdemo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ltsdemo.LtsManager
import kotlinx.coroutines.*
import kotlin.random.Random

@Composable
fun ConcurrentTestScreen() {
    // Reporting Mode State
    var useReportImmediately by remember { mutableStateOf(false) }

    // Concurrent Test State
    var threadCount by remember { mutableStateOf("5") }
    var logCountPerThread by remember { mutableStateOf("10") }
    var isRunning by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0) }
    
    // Timed Test State
    var intervalSeconds by remember { mutableStateOf("5") }
    var isTimedRunning by remember { mutableStateOf(false) }
    var timedProgress by remember { mutableStateOf(0) }
    
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.ime)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section 1: Batch/Concurrent Reporting
        Text("批量并发上报", style = MaterialTheme.typography.titleLarge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = !useReportImmediately,
                    onClick = { useReportImmediately = false }
                )
                Text("缓存上报", modifier = Modifier.padding(start = 4.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = useReportImmediately,
                    onClick = { useReportImmediately = true }
                )
                Text("立即上报", modifier = Modifier.padding(start = 4.dp))
            }
        }

        OutlinedTextField(
            value = threadCount,
            onValueChange = { threadCount = it },
            label = { Text("并发线程数") },
            modifier = Modifier.fillMaxWidth().height(64.dp)
        )

        OutlinedTextField(
            value = logCountPerThread,
            onValueChange = { logCountPerThread = it },
            label = { Text("每线程发送日志数") },
            modifier = Modifier.fillMaxWidth().height(64.dp)
        )



        Button(
            onClick = {
                val threads = threadCount.toIntOrNull() ?: 5
                val logs = logCountPerThread.toIntOrNull() ?: 10
                isRunning = true
                progress = 0
                scope.launch(Dispatchers.Default) {
                    val jobs = List(threads) { tId ->
                        launch {
                            repeat(logs) { lId ->
                                val randomContent = generateRandomString(100)
                                val labels = mapOf("thread_id" to "tid_$tId")
                                val content = "并发测试随机内容($lId): $randomContent"
                                
                                if (useReportImmediately) {
                                    LtsManager.reportImmediately(labels, content)
                                } else {
                                    LtsManager.report(labels, content)
                                }

                                withContext(Dispatchers.Main) {
                                    progress++
                                }
                                delay(Random.nextLong(10, 50))
                            }
                        }
                    }
                    jobs.joinAll()
                    withContext(Dispatchers.Main) {
                        isRunning = false
                    }
                }
            },
            enabled = !isRunning,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRunning) "正在上报 ($progress/${(threadCount.toIntOrNull() ?: 0) * (logCountPerThread.toIntOrNull() ?: 0)})..." else "开始并发上报")
        }

        if (isRunning) {
            LinearProgressIndicator(
                progress = progress.toFloat() / ((threadCount.toIntOrNull() ?: 1) * (logCountPerThread.toIntOrNull() ?: 1)),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // Section 2: Timed Reporting
        Text("定时上报", style = MaterialTheme.typography.titleLarge)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = intervalSeconds,
                onValueChange = { intervalSeconds = it },
                label = { Text("定时间隔 (秒)") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    isTimedRunning = !isTimedRunning
                    if (isTimedRunning) {
                        timedProgress = 0
                        scope.launch {
                            val interval = (intervalSeconds.toLongOrNull() ?: 5) * 1000
                            while (isTimedRunning) {
                                val randomContent = generateRandomString(50)
                                val labels = mapOf("type" to "timed_report")
                                val content = "定时上报随机内容: $randomContent"
                                
                                if (useReportImmediately) {
                                    LtsManager.reportImmediately(labels, content)
                                } else {
                                    LtsManager.report(labels, content)
                                }
                                
                                timedProgress++
                                delay(interval)
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text(if (isTimedRunning) "停止 ($timedProgress)" else "开始定时上报")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // Section 3: Large Log Reporting
        Text("上报单条大日志", style = MaterialTheme.typography.titleLarge)
        
        Button(onClick = {
            val labels = mapOf("size_test" to "large")
            val content = "A".repeat(30 * 1000) // ~30KB
            if (useReportImmediately) {
                LtsManager.reportImmediately(labels, content)
            } else {
                LtsManager.report(labels, content)
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("上报 (约30*1024长度)")
        }
    }
}

fun generateRandomString(length: Int): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { Random.nextInt(0, charPool.size).let { charPool[it] } }
        .joinToString("")
}

package com.example.ltsdemo.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ltsdemo.LtsManager
import kotlin.random.Random

@Composable
fun LogTestScreen() {
    // Input Key/Value
    var inputKey by remember { mutableStateOf("") }
    var inputValue by remember { mutableStateOf("") }
    
    // Configured Labels and Content Maps
    val labelsMap = remember { mutableStateMapOf<String, Any>() }
    val contentMap = remember { mutableStateMapOf<String, Any>() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
                        verticalAlignment = Alignment.CenterVertically // Vertical alignment
                    ) {
                        Text("Labels", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        if (labelsMap.isNotEmpty()) {
                            TextButton(
                                onClick = { labelsMap.clear() }, 
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.heightIn(max = 32.dp) // Ensure button doesn't stretch
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
                        verticalAlignment = Alignment.CenterVertically // Vertical alignment
                    ) {
                        Text("Content", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                        if (contentMap.isNotEmpty()) {
                            TextButton(
                                onClick = { contentMap.clear() }, 
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.heightIn(max = 32.dp) // Ensure button doesn't stretch
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

        // 2. Key/Value Input Section with fixed height and char count
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = inputKey, 
                onValueChange = { if (it.length <= 100) inputKey = it }, 
                label = { Text("键 (Key)") }, 
                modifier = Modifier.weight(1f).height(90.dp), // Fixed height
                supportingText = { Text("${inputKey.length}/100") }, // Char count
                singleLine = false,
                maxLines = 2
            )
            OutlinedTextField(
                value = inputValue, 
                onValueChange = { if (it.length <= 100) inputValue = it }, 
                label = { Text("值 (Value)") }, 
                modifier = Modifier.weight(1f).height(90.dp), // Fixed height
                supportingText = { Text("${inputValue.length}/100") }, // Char count
                singleLine = false,
                maxLines = 2
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

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                LtsManager.report(labelsMap.toMap(), contentMap.toMap())
            }, modifier = Modifier.weight(1f)) {
                Text("普通上报")
            }

            Button(onClick = {
                LtsManager.reportImmediately(labelsMap.toMap(), contentMap.toMap())
            }, modifier = Modifier.weight(1f)) {
                Text("立即上报")
            }
        }
    }
}

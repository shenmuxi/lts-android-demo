package com.example.ltsdemo

import android.app.Application
import com.cloud.lts.LTSSDK
import com.cloud.lts.UserConfig
import com.cloud.lts.util.log.LogLevel
import java.util.HashMap

object LtsManager {
    private var ltsSdk: LTSSDK? = null

    fun initialize(
        application: Application,
        host: String,
        region: String,
        projectId: String,
        groupId: String,
        streamId: String,
        ak: String,
        sk: String,
        cacheThreshold: Int = 200,
        timeInterval: Int = 3
    ) {
        val builder = UserConfig.Builder()
            .setRegion(region)
            .setProjectId(projectId)
            .setGroupId(groupId)
            .setStreamId(streamId)
            .setAccessKey(ak)
            .setSecretKey(sk)
            .setCacheThreshold(cacheThreshold.toLong())
            .setTimeInterval(timeInterval.toLong())
            .setIsReportBackground(true)
        
        if (host.isNotBlank()) {
            builder.setUrlHost(host)
        }
        
        val userConfig = builder.build()

        LTSSDK.setLogLevel(LogLevel.DEBUG)
        ltsSdk = LTSSDK(application, userConfig)
    }

    fun report(labels: Map<String, Any>, contents: Any) {
        val labelMap = HashMap<String, Any>()
        labelMap.putAll(labels)
        
        when (contents) {
            is String -> ltsSdk?.report(contents, labelMap)
            is Map<*, *> -> {
                val contentMap = HashMap<String, Any?>()
                contents.forEach { (k, v) -> contentMap[k.toString()] = v }
                ltsSdk?.report(contentMap, labelMap)
            }
            else -> ltsSdk?.report(contents.toString(), labelMap)
        }
    }

    fun reportImmediately(labels: Map<String, Any>, contents: Any) {
        val labelMap = HashMap<String, Any>()
        labelMap.putAll(labels)

        when (contents) {
            is String -> ltsSdk?.reportImmediately(contents, labelMap)
            is Map<*, *> -> {
                val contentMap = HashMap<String, Any?>()
                contents.forEach { (k, v) -> contentMap[k.toString()] = v }
                ltsSdk?.reportImmediately(contentMap, labelMap)
            }
            else -> ltsSdk?.reportImmediately(contents.toString(), labelMap)
        }
    }
    
    fun isInitialized(): Boolean = ltsSdk != null
}

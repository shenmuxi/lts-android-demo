package com.example.ltsdemo

import android.app.Application
import com.cloud.lts.LTSSDK
import com.cloud.lts.UserConfig
import java.util.HashMap

object LtsManager {
    private var ltsSdk: LTSSDK? = null

    fun initialize(
        application: Application,
        region: String,
        projectId: String,
        groupId: String,
        streamId: String,
        ak: String,
        sk: String,
        cacheThreshold: Int = 200,
        timeInterval: Int = 3
    ) {
        val userConfig = UserConfig.Builder()
            .setRegion(region)
            .setProjectId(projectId)
            .setGroupId(groupId)
            .setStreamId(streamId)
            .setAccessKey(ak)
            .setSecretKey(sk)
            .setCacheThreshold(cacheThreshold.toLong())
            .setTimeInterval(timeInterval.toLong())
            .setIsReportBackground(true)
            .build()

        ltsSdk = LTSSDK(application, userConfig)
        // Attempting to set log level using the most common package/class structure for 1.0.28
        try {
            val logLevelClass = Class.forName("com.cloud.lts.utils.LTSLogLevel")
            val debugField = logLevelClass.getField("DEBUG")
            val setLogLevelMethod = LTSSDK::class.java.getMethod("setLogLevel", logLevelClass)
            setLogLevelMethod.invoke(ltsSdk, debugField.get(null))
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

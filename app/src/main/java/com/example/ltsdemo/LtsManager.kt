package com.example.ltsdemo

import android.app.Application
import com.cloud.lts.LTSSDK
import com.cloud.lts.UserConfig
import com.cloud.lts.util.log.LogLevel
import java.util.HashMap

object LtsManager {
    private var ltsSdk: LTSSDK? = null
    private val multiSdkInstances = mutableListOf<LTSSDK>()

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
        
        // Release existing default instance if any
        ltsSdk?.safeRelease()

        ltsSdk = LTSSDK(application, userConfig)
        
        // Also add to multi-instances for consistency
        multiSdkInstances.clear()
        ltsSdk?.let { multiSdkInstances.add(it) }
    }

    fun initializeMulti(application: Application, config: LtsFullConfig) {
        LTSSDK.setLogLevel(LogLevel.DEBUG)
        
        // Release existing instances before re-initializing
        releaseAll()
        multiSdkInstances.clear()
        
        config.instances.forEach { instance ->
            val builder = UserConfig.Builder()
                .setRegion(config.region)
                .setProjectId(config.projectId)
                .setGroupId(instance.groupId)
                .setStreamId(instance.streamId)
                .setAccessKey(config.ak)
                .setSecretKey(config.sk)
                .setCacheThreshold(config.cacheThreshold.toLong())
                .setTimeInterval(config.timeInterval.toLong())
                .setIsReportBackground(config.isReportBackground)
            
            if (config.host.isNotBlank()) {
                builder.setUrlHost(config.host)
            }
            
            val userConfig = builder.build()
            multiSdkInstances.add(LTSSDK(application, userConfig))
        }
        
        // Update default ltsSdk to the first one if available
        ltsSdk = multiSdkInstances.firstOrNull()
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

    fun reportMulti(labels: Map<String, Any>, contents: Any) {
        val labelMap = HashMap<String, Any>()
        labelMap.putAll(labels)

        multiSdkInstances.forEach { sdk ->
            when (contents) {
                is String -> sdk.report(contents, labelMap)
                is Map<*, *> -> {
                    val contentMap = HashMap<String, Any?>()
                    contents.forEach { (k, v) -> contentMap[k.toString()] = v }
                    sdk.report(contentMap, labelMap)
                }
                else -> sdk.report(contents.toString(), labelMap)
            }
        }
    }

    fun reportMultiImmediately(labels: Map<String, Any>, contents: Any) {
        val labelMap = HashMap<String, Any>()
        labelMap.putAll(labels)

        multiSdkInstances.forEach { sdk ->
            when (contents) {
                is String -> sdk.reportImmediately(contents, labelMap)
                is Map<*, *> -> {
                    val contentMap = HashMap<String, Any?>()
                    contents.forEach { (k, v) -> contentMap[k.toString()] = v }
                    sdk.reportImmediately(contentMap, labelMap)
                }
                else -> sdk.reportImmediately(contents.toString(), labelMap)
            }
        }
    }
    
    fun isInitialized(): Boolean = ltsSdk != null && ltsSdk!!.isInitialized
    
    fun getMultiInstanceCount(): Int = multiSdkInstances.size

    /**
     * Safely releases an LTSSDK instance by checking for the 'release' method via reflection.
     * This ensures compatibility with older SDK versions that don't have this method.
     */
    private fun LTSSDK.safeRelease() {
        try {
            val releaseMethod = this.javaClass.getMethod("release")
            releaseMethod.invoke(this)
        } catch (e: NoSuchMethodException) {
            // Method doesn't exist in this version of the SDK, ignore
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Releases all active SDK instances.
     */
    fun releaseAll() {
        ltsSdk?.safeRelease()
        ltsSdk = null
        
        multiSdkInstances.forEach { it.safeRelease() }
        multiSdkInstances.clear()
    }
}

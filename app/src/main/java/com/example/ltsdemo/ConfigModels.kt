package com.example.ltsdemo

import com.google.gson.annotations.SerializedName

data class LtsInstance(
    @SerializedName("group-id") val groupId: String = "",
    @SerializedName("stream-id") val streamId: String = ""
)

data class LtsFullConfig(
    @SerializedName("title") val title: String = "Custom",
    @SerializedName("region") val region: String = "cn-north-4",
    @SerializedName("project-id") val projectId: String = "",
    @SerializedName("ak") val ak: String = "",
    @SerializedName("sk") val sk: String = "",
    @SerializedName("cache-threshold") val cacheThreshold: Int = 200,
    @SerializedName("time-interval") val timeInterval: Int = 3,
    @SerializedName("IsReportBackground") val isReportBackground: Boolean = true,
    @SerializedName("IsReportLaunch") val isReportLaunch: Boolean = false,
    @SerializedName("instances") val instances: List<LtsInstance> = listOf(LtsInstance())
)

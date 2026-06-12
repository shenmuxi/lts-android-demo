package com.example.ltsdemo

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object ConfigManager {
    private const val PREFS_NAME = "lts_prefs"
    private const val CONFIG_KEY = "current_config"
    private const val ASSET_FILE = "configs.json"
    private const val PERSIST_FILE = "user_config.json"
    private val gson = Gson()

    fun loadPredefinedConfigs(context: Context): List<LtsFullConfig> {
        return try {
            val json = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<LtsFullConfig>>() {}.type
            gson.fromJson(json, listType)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveConfig(context: Context, config: LtsFullConfig) {
        try {
            val json = gson.toJson(config)
            val file = File(context.filesDir, PERSIST_FILE)
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadSavedConfig(context: Context): LtsFullConfig? {
        return try {
            val file = File(context.filesDir, PERSIST_FILE)
            if (file.exists()) {
                val json = file.readText()
                gson.fromJson(json, LtsFullConfig::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

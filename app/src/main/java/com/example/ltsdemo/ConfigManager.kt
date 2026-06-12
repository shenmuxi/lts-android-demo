package com.example.ltsdemo

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object ConfigManager {
    private const val ENCRYPTED_ASSET_FILE = "configs_encrypted.json"
    private const val PERSIST_FILE = "user_config.json"
    private val gson = Gson()

    fun loadDecryptedConfigs(context: Context, password: String): List<LtsFullConfig> {
        return try {
            val encryptedBase64 = context.assets.open(ENCRYPTED_ASSET_FILE).bufferedReader().use { it.readText() }
            val decryptedJson = CryptoUtils.decrypt(encryptedBase64, password)
            val listType = object : TypeToken<List<LtsFullConfig>>() {}.type
            gson.fromJson(decryptedJson, listType)
        } catch (e: Exception) {
            throw e
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

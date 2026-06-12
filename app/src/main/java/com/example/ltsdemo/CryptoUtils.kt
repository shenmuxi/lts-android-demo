package com.example.ltsdemo

import android.util.Base64
import android.util.Log
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private const val TAG = "CryptoUtils"
    private const val ITERATION_COUNT = 1000
    private const val KEY_LENGTH = 256
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 16

    fun decrypt(encryptedBase64: String, password: String): String {
        try {
            // Trim any potential whitespace from the input
            val trimmedInput = encryptedBase64.trim()
            val combined = Base64.decode(trimmedInput, Base64.DEFAULT)
            
            if (combined.size < SALT_SIZE + IV_SIZE) {
                throw SecurityException("Invalid encrypted data format (too short)")
            }

            // Extract components
            val salt = combined.copyOfRange(0, SALT_SIZE)
            val iv = combined.copyOfRange(SALT_SIZE, SALT_SIZE + IV_SIZE)
            val ciphertext = combined.copyOfRange(SALT_SIZE + IV_SIZE, combined.size)

            Log.d(TAG, "Salt size: ${salt.size}, IV size: ${iv.size}, Ciphertext size: ${ciphertext.size}")

            // Derive key using PBKDF2WithHmacSHA1
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
            val tmp = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.encoded, "AES")

            // Decrypt using AES/CBC/PKCS5Padding
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            val decryptedBytes = cipher.doFinal(ciphertext)
            
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption error: ${e.message}", e)
            throw SecurityException("解密失败：密码错误或数据损坏", e)
        }
    }
}

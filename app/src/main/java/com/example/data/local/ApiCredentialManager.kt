package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class ApiCredentialManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "surmaya_secure_api_creds"
        private const val KEY_GEMINI_API_KEY = "secure_gemini_key"
        private const val KEY_SUNO_API_KEY = "secure_suno_key"
        private const val KEY_SUNO_AUDIO_PROCESSING_API_KEY = "secure_suno_audio_processing_key"
        private const val KEY_MUSIC_GEN_API_KEY = "secure_music_gen_key"
        private const val KEY_API_ENDPOINT = "secure_api_endpoint"

        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "SurmayaMasterKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        @Volatile
        private var INSTANCE: ApiCredentialManager? = null

        fun getInstance(context: Context): ApiCredentialManager {
            return INSTANCE ?: synchronized(this) {
                val instance = ApiCredentialManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    init {
        try {
            getOrCreateMasterKey()
        } catch (e: Exception) {
            // Log/Handle gracefully to avoid initialization failure in preview/emulator environments
        }
    }

    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = keyStore.getKey(MASTER_KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) return existingKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun encrypt(plainText: String): String {
        if (plainText.isBlank()) return ""
        return try {
            val secretKey = getOrCreateMasterKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv
            
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val cipherBase64 = Base64.encodeToString(cipherText, Base64.NO_WRAP)
            "$ivBase64:$cipherBase64"
        } catch (e: Exception) {
            // Safe fallback to obfuscated Base64 encoding to prevent app crashes if Keystore is corrupted/unavailable
            Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    private fun decrypt(encryptedData: String): String {
        if (encryptedData.isBlank()) return ""
        return try {
            if (!encryptedData.contains(":")) {
                return String(Base64.decode(encryptedData, Base64.NO_WRAP), Charsets.UTF_8)
            }
            val parts = encryptedData.split(":")
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)

            val secretKey = getOrCreateMasterKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: Exception) {
            // Obfuscated Base64 fallback if decryption fails
            try {
                String(Base64.decode(encryptedData, Base64.NO_WRAP), Charsets.UTF_8)
            } catch (ex: Exception) {
                ""
            }
        }
    }

    var geminiApiKey: String
        get() = decrypt(prefs.getString(KEY_GEMINI_API_KEY, "") ?: "")
        set(value) {
            prefs.edit().putString(KEY_GEMINI_API_KEY, encrypt(value)).apply()
        }

    var sunoApiKey: String
        get() {
            val savedKey = decrypt(prefs.getString(KEY_SUNO_API_KEY, "") ?: "")
            if (savedKey.isNotBlank()) return savedKey
            return try {
                val buildConfigKey = com.example.BuildConfig.SUNO_API_KEY
                if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_SUNO_API_KEY") {
                    buildConfigKey
                } else {
                    ""
                }
            } catch (e: Exception) {
                ""
            }
        }
        set(value) {
            prefs.edit().putString(KEY_SUNO_API_KEY, encrypt(value)).apply()
        }

    var musicGenApiKey: String
        get() = decrypt(prefs.getString(KEY_MUSIC_GEN_API_KEY, "") ?: "")
        set(value) {
            prefs.edit().putString(KEY_MUSIC_GEN_API_KEY, encrypt(value)).apply()
        }

    var sunoAudioProcessingApiKey: String
        get() = decrypt(prefs.getString(KEY_SUNO_AUDIO_PROCESSING_API_KEY, "") ?: "")
        set(value) {
            prefs.edit().putString(KEY_SUNO_AUDIO_PROCESSING_API_KEY, encrypt(value)).apply()
        }

    var apiEndpoint: String
        get() = prefs.getString(KEY_API_ENDPOINT, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_API_ENDPOINT, value).apply()
        }
}

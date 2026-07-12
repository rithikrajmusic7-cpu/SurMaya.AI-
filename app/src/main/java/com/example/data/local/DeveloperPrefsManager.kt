package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeveloperPrefsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "surmaya_dev_prefs"
        private const val KEY_DEV_MODE_ENABLED = "dev_mode_enabled"
        private const val KEY_CUSTOM_GEMINI_KEY = "custom_gemini_key"
        private const val KEY_CUSTOM_SUNO_KEY = "custom_suno_key"
        private const val KEY_CUSTOM_MUSIC_GEN_KEY = "custom_music_gen_key"
        private const val KEY_CUSTOM_API_ENDPOINT = "custom_api_endpoint"
        private const val KEY_GENERATION_MODEL = "generation_model"
        private const val KEY_GENERATION_TEMP = "generation_temp"
        private const val KEY_GENERATION_DURATION = "generation_duration"
        private const val KEY_FORCE_REAL_API = "force_real_api"
        private const val KEY_SYSTEM_INSTRUCTION = "system_instruction"

        @Volatile
        private var INSTANCE: DeveloperPrefsManager? = null

        fun getInstance(context: Context): DeveloperPrefsManager {
            return INSTANCE ?: synchronized(this) {
                val instance = DeveloperPrefsManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    // Hot flow triggers to emit changes directly to screens
    private val _isDeveloperModeEnabledFlow = MutableStateFlow(isDeveloperModeEnabled)
    val isDeveloperModeEnabledFlow: StateFlow<Boolean> = _isDeveloperModeEnabledFlow.asStateFlow()

    var isDeveloperModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DEV_MODE_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_DEV_MODE_ENABLED, value).apply()
            _isDeveloperModeEnabledFlow.value = value
        }

    var customGeminiApiKey: String
        get() = prefs.getString(KEY_CUSTOM_GEMINI_KEY, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_CUSTOM_GEMINI_KEY, value).apply()
        }

    var customSunoApiKey: String
        get() = prefs.getString(KEY_CUSTOM_SUNO_KEY, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_CUSTOM_SUNO_KEY, value).apply()
        }

    var customMusicGenApiKey: String
        get() = prefs.getString(KEY_CUSTOM_MUSIC_GEN_KEY, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_CUSTOM_MUSIC_GEN_KEY, value).apply()
        }

    var customApiEndpoint: String
        get() = prefs.getString(KEY_CUSTOM_API_ENDPOINT, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_CUSTOM_API_ENDPOINT, value).apply()
        }

    var generationModel: String
        get() = prefs.getString(KEY_GENERATION_MODEL, "gemini-2.5-flash-preview-tts") ?: "gemini-2.5-flash-preview-tts"
        set(value) {
            prefs.edit().putString(KEY_GENERATION_MODEL, value).apply()
        }

    var generationTemperature: Float
        get() = prefs.getFloat(KEY_GENERATION_TEMP, 1.0f)
        set(value) {
            prefs.edit().putFloat(KEY_GENERATION_TEMP, value).apply()
        }

    var generationDuration: Int
        get() = prefs.getInt(KEY_GENERATION_DURATION, 15)
        set(value) {
            prefs.edit().putInt(KEY_GENERATION_DURATION, value).apply()
        }

    var forceRealApiRequests: Boolean
        get() = prefs.getBoolean(KEY_FORCE_REAL_API, false)
        set(value) {
            prefs.edit().putBoolean(KEY_FORCE_REAL_API, value).apply()
        }

    var systemInstructionOverride: String
        get() = prefs.getString(
            KEY_SYSTEM_INSTRUCTION,
            "Generate a high fidelity musical piece or song. Standard Indian music theme, clean production."
        ) ?: "Generate a high fidelity musical piece or song. Standard Indian music theme, clean production."
        set(value) {
            prefs.edit().putString(KEY_SYSTEM_INSTRUCTION, value).apply()
        }
}

package com.example.data.remote.gateway

import android.content.Context
import android.util.Log

// ==========================================
// AI Provider Manager
// ==========================================

class ProviderManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: ProviderManager? = null

        fun getInstance(context: Context): ProviderManager {
            return INSTANCE ?: synchronized(this) {
                val instance = ProviderManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    // Dynamic configuration storage representing selected providers
    private val activeProviders = mutableMapOf<Capability, ProviderConfig>()

    init {
        // Load default primary/fallback setups
        resetToDefaults()
    }

    enum class Capability {
        LYRICS,
        MUSIC_GENERATION,
        VOICE_SYNTHESIS,
        VOICE_CLONE,
        INSTRUMENT_GENERATION,
        MOOD_ANALYSIS,
        COMPOSITION,
        MIXING,
        MASTERING
    }

    data class ProviderConfig(
        val primaryProviderId: String,
        val fallbackProviderId: String
    )

    fun resetToDefaults() {
        activeProviders[Capability.LYRICS] = ProviderConfig("gemini_pro", "local_offline")
        activeProviders[Capability.MUSIC_GENERATION] = ProviderConfig("suno_v3", "local_sine_synth")
        activeProviders[Capability.VOICE_SYNTHESIS] = ProviderConfig("elevenlabs_pro", "local_harmonic_hum")
        activeProviders[Capability.VOICE_CLONE] = ProviderConfig("elevenlabs_cloner", "mock_dummy")
        activeProviders[Capability.INSTRUMENT_GENERATION] = ProviderConfig("musicgen_v2", "local_sine_synth")
        activeProviders[Capability.MOOD_ANALYSIS] = ProviderConfig("gemini_flash", "local_dictionary")
        activeProviders[Capability.COMPOSITION] = ProviderConfig("gemini_composer", "local_chords")
        activeProviders[Capability.MIXING] = ProviderConfig("surmaya_mixer", "mock_bypass")
        activeProviders[Capability.MASTERING] = ProviderConfig("surmaya_masterer", "mock_bypass")
        Log.i("ProviderManager", "AI Provider Manager initialized with default primary/fallback structures.")
    }

    fun updateProviderConfig(capability: Capability, primary: String, fallback: String) {
        activeProviders[capability] = ProviderConfig(primary, fallback)
        Log.d("ProviderManager", "Config updated for $capability: Primary=$primary, Fallback=$fallback")
    }

    fun getProviderConfig(capability: Capability): ProviderConfig {
        return activeProviders[capability] ?: ProviderConfig("default", "local")
    }
}

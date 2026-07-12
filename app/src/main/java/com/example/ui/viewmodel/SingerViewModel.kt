package com.example.ui.viewmodel

import android.app.Application
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import kotlin.math.max
import kotlin.math.sin
import java.util.Random

data class VoiceCategory(
    val id: String,
    val name: String,
    val category: String, // "Male", "Female", "Kids", "Duet", "Choir", "Classical", "Special"
    val description: String,
    val version: String,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val status: String = "Available", // "Available", "Offline Ready", "Downloading", "Cloud AI Required"
    val previewBaseFreq: Double = 220.0
)

class SingerViewModel(application: Application) : AndroidViewModel(application) {

    // --- State Streams ---
    private val _voices = MutableStateFlow<List<VoiceCategory>>(emptyList())
    val voices: StateFlow<List<VoiceCategory>> = _voices

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites

    private val _selectedVoice = MutableStateFlow<VoiceCategory?>(null)
    val selectedVoice: StateFlow<VoiceCategory?> = _selectedVoice

    // --- Voice parameters ---
    val paramPitch = MutableStateFlow("Medium") // Low, Medium, High
    val paramTone = MutableStateFlow("Warm") // Warm, Bright, Dark, Deep, Natural
    val paramEmotion = MutableStateFlow("Romantic") // Happy, Romantic, Sad, Angry, Energetic, Peaceful, Devotional, Patriotic, Emotional
    val paramExpression = MutableStateFlow(80f) // 0-100%
    val paramVibrato = MutableStateFlow(40f) // 0-100%
    val paramBreathiness = MutableStateFlow(15f) // 0-100%
    val paramPower = MutableStateFlow(75f) // 0-100%
    val paramSoftness = MutableStateFlow(30f) // 0-100%
    val paramPronunciation = MutableStateFlow("Standard Hindi")

    // --- Duet Mode ---
    val duetType = MutableStateFlow("Male + Female") // Male + Female, Male + Male, Female + Female, Adult + Kid, Lead + Harmony
    val duetSinger1Pitch = MutableStateFlow("Medium")
    val duetSinger1Emotion = MutableStateFlow("Romantic")
    val duetSinger1Tone = MutableStateFlow("Warm")
    val duetSinger1Volume = MutableStateFlow(80f)
    val duetSinger2Pitch = MutableStateFlow("Medium")
    val duetSinger2Emotion = MutableStateFlow("Happy")
    val duetSinger2Tone = MutableStateFlow("Bright")
    val duetSinger2Volume = MutableStateFlow(85f)

    // --- Choir Mode ---
    val choirSingersCount = MutableStateFlow(16) // 4 to 64
    val choirHarmonyDepth = MutableStateFlow(70f)
    val choirStereoWidth = MutableStateFlow(80f)
    val choirStyle = MutableStateFlow("Indian Devotional") // Indian Devotional, Cinematic Choir, Children Choir, Mixed Choir

    // --- Indian Classical Mode ---
    val classicalSubtype = MutableStateFlow("Male Classical") // Male Classical, Female Classical, Semi-Classical, Bhajan, Alaap, Sargam, Khayal-inspired, Light Classical
    val classicalMeend = MutableStateFlow(60f)
    val classicalGamak = MutableStateFlow(45f)
    val classicalMurki = MutableStateFlow(50f)

    // --- Rap Mode ---
    val rapFlowSpeed = MutableStateFlow(75f)
    val rapAggression = MutableStateFlow(60f)
    val rapRhythmTightness = MutableStateFlow(90f)
    val rapStyle = MutableStateFlow("Commercial Style") // Street Style, Commercial Style, Trap Style, Old School Style

    // --- Singing Controls ---
    val controlTempo = MutableStateFlow(105f) // 60 - 200 BPM
    val controlKey = MutableStateFlow("C")
    val controlScale = MutableStateFlow("Major") // Major, Minor, Bhairav, Yaman, Kalyani
    val controlOctave = MutableStateFlow(0) // -2 to +2
    val controlReverb = MutableStateFlow(40f)
    val controlDelay = MutableStateFlow(20f)
    val controlStereoWidth = MutableStateFlow(60f)
    val controlAutoTune = MutableStateFlow(80f)
    val controlBreathControl = MutableStateFlow(50f)

    // --- Active Playback & Synthesis State ---
    val isPlayingPreview = MutableStateFlow(false)
    val previewingVoiceId = MutableStateFlow<String?>(null)
    val previewWaves = MutableStateFlow(List(16) { 0.1f })

    val isSynthesizing = MutableStateFlow(false)
    val synthesisProgress = MutableStateFlow(0f)
    val synthesizedLyricsOutput = MutableStateFlow<String?>(null)
    val synthesizedAudioDuration = MutableStateFlow("0:00")
    val selectedOutputFormat = MutableStateFlow("MP3") // MP3, WAV, FLAC, Vocal Stem, Harmony Stem

    private var synthJob: Job? = null
    private var progressJob: Job? = null
    private var audioTrack: AudioTrack? = null
    private var isAudioActive = false

    init {
        loadDefaultVoices()
    }

    private fun loadDefaultVoices() {
        val defaultList = listOf(
            VoiceCategory(
                id = "ajit",
                name = "Ajit",
                category = "Male",
                description = "Expressive & silky male voice, perfect for heartfelt romantic ballads and ghazals.",
                version = "v1.8-neural-m",
                isFavorite = true,
                isDownloaded = true,
                status = "Offline Ready",
                previewBaseFreq = 220.0
            ),
            VoiceCategory(
                id = "shrija",
                name = "Shrija",
                category = "Female",
                description = "Bright, sweet, and classical female voice, ideal for romantic and devotional raga pop.",
                version = "v2.0-neural-f",
                isFavorite = true,
                isDownloaded = true,
                status = "Offline Ready",
                previewBaseFreq = 349.23
            ),
            VoiceCategory(
                id = "chota_singer",
                name = "Chota Singer",
                category = "Kids",
                description = "Playful, energetic, and pure children voice for nursery rhymes, jingles, and innocence.",
                version = "v1.1-kids",
                isFavorite = false,
                isDownloaded = false,
                status = "Available",
                previewBaseFreq = 440.0
            ),
            VoiceCategory(
                id = "harmonic_couple",
                name = "Harmonic Couple",
                category = "Duet",
                description = "Dual male and female voice generation engine with customizable interaction presets.",
                version = "v1.5-duet",
                isFavorite = false,
                isDownloaded = false,
                status = "Cloud AI Required",
                previewBaseFreq = 261.63
            ),
            VoiceCategory(
                id = "vocal_ensemble",
                name = "Vocal Ensemble",
                category = "Choir",
                description = "Configurable choir that generates beautiful rich layers of unison and background harmonies.",
                version = "v2.3-choir",
                isFavorite = false,
                isDownloaded = false,
                status = "Cloud AI Required",
                previewBaseFreq = 261.63
            ),
            VoiceCategory(
                id = "pandit_g",
                name = "Pandit G",
                category = "Classical",
                description = "Veteran classical maestro capable of high-fidelity alaaps, taans, and heavy ragas.",
                version = "v1.6-classical",
                isFavorite = true,
                isDownloaded = true,
                status = "Offline Ready",
                previewBaseFreq = 196.00
            ),
            VoiceCategory(
                id = "kishore_style",
                name = "Kishore Style",
                category = "Male",
                description = "Retro warm medium-pitch baritone with natural acoustics and rich vibrato.",
                version = "v1.2-romantic",
                isFavorite = false,
                isDownloaded = false,
                status = "Available",
                previewBaseFreq = 164.81
            ),
            VoiceCategory(
                id = "sukhwinder_style",
                name = "Sukhwinder Style",
                category = "Special",
                description = "Powerhouse high-octane energetic voice, great for folk, patriotic, and dance tracks.",
                version = "v1.9-power",
                isFavorite = false,
                isDownloaded = false,
                status = "Available",
                previewBaseFreq = 293.66
            ),
            VoiceCategory(
                id = "sad_ghazal",
                name = "Sad Ghazal",
                category = "Female",
                description = "Muffled, emotional, slightly breathy tone curated for tragic love poetry and ghazals.",
                version = "v1.4-emotional",
                isFavorite = false,
                isDownloaded = false,
                status = "Available",
                previewBaseFreq = 311.13
            ),
            VoiceCategory(
                id = "anup_style",
                name = "Anup Style",
                category = "Classical",
                description = "Serene, devotional male voice calibrated specifically for stotras and bhajan sangeet.",
                version = "v2.1-devotional",
                isFavorite = false,
                isDownloaded = false,
                status = "Available",
                previewBaseFreq = 174.61
            ),
            VoiceCategory(
                id = "mc_shanti",
                name = "Shanti",
                category = "Special",
                description = "Fast flows, sharp syllables, and aggressive rhythmic patterns for modern Indian rap.",
                version = "v2.5-rap",
                isFavorite = false,
                isDownloaded = true,
                status = "Offline Ready",
                previewBaseFreq = 130.81
            )
        )
        _voices.value = defaultList
        _selectedVoice.value = defaultList.first()
    }

    // --- Search, Filters & Bookmarks ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun toggleShowOnlyFavorites() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    fun selectVoice(voice: VoiceCategory) {
        _selectedVoice.value = voice
        // Adapt parameters based on voice characteristics
        when (voice.category) {
            "Classical" -> {
                paramPitch.value = "Medium"
                paramSoftness.value = 40f
                paramPower.value = 80f
            }
            "Kids" -> {
                paramPitch.value = "High"
                paramSoftness.value = 65f
            }
            "Special" -> {
                if (voice.id == "mc_shanti") {
                    paramPitch.value = "Low"
                    paramPower.value = 90f
                }
            }
        }
    }

    fun toggleFavorite(voiceId: String) {
        _voices.value = _voices.value.map {
            if (it.id == voiceId) it.copy(isFavorite = !it.isFavorite) else it
        }
        if (_selectedVoice.value?.id == voiceId) {
            _selectedVoice.value = _selectedVoice.value?.copy(isFavorite = !_selectedVoice.value!!.isFavorite)
        }
    }

    fun startDownload(voiceId: String) {
        viewModelScope.launch {
            _voices.value = _voices.value.map {
                if (it.id == voiceId) it.copy(status = "Downloading") else it
            }
            if (_selectedVoice.value?.id == voiceId) {
                _selectedVoice.value = _selectedVoice.value?.copy(status = "Downloading")
            }

            delay(2000) // Simulate download

            _voices.value = _voices.value.map {
                if (it.id == voiceId) it.copy(status = "Offline Ready", isDownloaded = true) else it
            }
            if (_selectedVoice.value?.id == voiceId) {
                _selectedVoice.value = _selectedVoice.value?.copy(status = "Offline Ready", isDownloaded = true)
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null

    // --- Real Audio Synthesis Preview Engine ---
    fun togglePreview(voice: VoiceCategory) {
        if (isPlayingPreview.value && previewingVoiceId.value == voice.id) {
            stopAudio()
        } else {
            stopAudio()
            startAudioPreview(voice)
        }
    }

    private fun startAudioPreview(voice: VoiceCategory) {
        isPlayingPreview.value = true
        previewingVoiceId.value = voice.id
        isAudioActive = true

        val cacheFile = File(getApplication<Application>().cacheDir, "voice_preview_${voice.id}.wav")
        viewModelScope.launch {
            if (!cacheFile.exists()) {
                try {
                    val repo = ServiceLocator.getMusicGenerationRepository(getApplication())
                    val geminiVoiceName = when (voice.name.lowercase()) {
                        "shrija", "kavita" -> "Kore"
                        "ajit", "sonu" -> "Puck"
                        else -> "Charon"
                    }
                    val prompt = "Namaste! Main SurMaya AI ki ${voice.name} aawaaz hoon. Aapka sangeet saathi."
                    val genImpl = repo as com.example.data.repository.MusicGenerationRepositoryImpl
                    genImpl.generateVoiceSample(prompt, geminiVoiceName).getOrNull()?.renameTo(cacheFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (cacheFile.exists()) {
                viewModelScope.launch(Dispatchers.Main) {
                    try {
                        mediaPlayer?.release()
                        mediaPlayer = null
                        val player = MediaPlayer().apply {
                            setDataSource(cacheFile.absolutePath)
                            prepare()
                            start()
                        }
                        mediaPlayer = player

                        player.setOnCompletionListener {
                            stopAudio()
                        }

                        // Progress and waves loop
                        while (isAudioActive && player.isPlaying) {
                            previewWaves.value = List(16) { 0.1f + (Math.random().toFloat() * 0.8f) }
                            delay(250)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        startLegacyAudioPreview(voice)
                    }
                }
            } else {
                startLegacyAudioPreview(voice)
            }
        }
    }

    private fun startLegacyAudioPreview(voice: VoiceCategory) {
        // Base frequency modified by Pitch selection
        val pitchMultiplier = when (paramPitch.value) {
            "High" -> 1.25
            "Low" -> 0.75
            else -> 1.0
        }
        val baseFreq = voice.previewBaseFreq * pitchMultiplier

        synthJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 22050
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val track = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize.coerceAtLeast(sampleRate * 2),
                    AudioTrack.MODE_STREAM
                )
                audioTrack = track
                track.play()

                var phase = 0.0
                val vibratoAmt = paramVibrato.value / 100f
                val breathAmt = paramBreathiness.value / 100f
                val random = Random()

                // Generate vocal hum with a vibrato LFO and slight breath noise
                while (isAudioActive) {
                    val bufferSize = 1024
                    val buffer = ShortArray(bufferSize)

                    for (i in 0 until bufferSize) {
                        val t = phase / sampleRate
                        
                        // Vibrato LFO at 6Hz
                        val lfo = sin(2.0 * Math.PI * 6.0 * t) * 6.0 * vibratoAmt
                        val currentFreq = baseFreq + lfo
                        
                        // Human tone synthesis: Sine base with subtle odd and even harmonics
                        var vocalWave = sin(2.0 * Math.PI * currentFreq * t) * 0.7 +
                                        sin(4.0 * Math.PI * currentFreq * t) * 0.2 +
                                        sin(6.0 * Math.PI * currentFreq * t) * 0.1

                        // Add breathiness (white noise)
                        if (breathAmt > 0) {
                            val noise = (random.nextDouble() * 2.0 - 1.0) * breathAmt * 0.35
                            vocalWave = vocalWave * (1.0 - breathAmt * 0.2) + noise
                        }

                        // Expression shaping
                        val envelope = 0.85

                        buffer[i] = (vocalWave * 8500.0 * envelope).toInt().toShort()
                        phase += 1.0
                    }

                    if (isAudioActive) {
                        track.write(buffer, 0, buffer.size)
                    }

                    // Dynamic wave generator for the visualizer UI
                    val visualWaves = List(16) { 0.1f + (random.nextFloat() * 0.8f * (1f - breathAmt * 0.3f)) }
                    previewWaves.value = visualWaves

                    delay(15)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopAudio() {
        isAudioActive = false
        isPlayingPreview.value = false
        previewingVoiceId.value = null
        previewWaves.value = List(16) { 0.1f }

        synthJob?.cancel()
        synthJob = null

        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // safe ignore
        }
        audioTrack = null

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // safe ignore
        }
        mediaPlayer = null
        audioTrack = null
    }

    // --- Vocal Synthesis Processing Flow ---
    fun startVocalSynthesis(lyrics: String) {
        if (lyrics.isBlank()) return
        stopAudio()
        isSynthesizing.value = true
        synthesisProgress.value = 0f
        synthesizedLyricsOutput.value = null

        progressJob = viewModelScope.launch(Dispatchers.Main) {
            var currentProg = 0f
            while (currentProg < 1.0f) {
                delay(120)
                currentProg += 0.04f
                synthesisProgress.value = currentProg.coerceAtMost(1f)
            }

            // Synthesis complete
            isSynthesizing.value = false
            val voiceName = _selectedVoice.value?.name ?: "Vocalist"
            val formatStr = selectedOutputFormat.value
            synthesizedLyricsOutput.value = """
                [SurMaya AI Singer Studio - Vocal Synthesis Report]
                Vocalist Name: $voiceName (Model: ${_selectedVoice.value?.version})
                Multilingual Pronunciation: ${paramPronunciation.value}
                Mode/Category: ${_selectedVoice.value?.category}
                Pitch Tuning: ${paramPitch.value}, Tone: ${paramTone.value}, Emotion: ${paramEmotion.value}
                Parameters used: Vibrato ${paramVibrato.value.toInt()}%, Power ${paramPower.value.toInt()}%, Breathiness ${paramBreathiness.value.toInt()}%
                
                [Generated Audio Output]
                Primary Vocal Stem: File_Vocal_$voiceName.${formatStr.lowercase()}
                Harmonized Backing Stem: File_Harm_$voiceName.${formatStr.lowercase()}
                Format Quality: High Definition (24-bit PCM / 48kHz Stereo)
                
                [Synthesized Singing Lyrics Track]
                ${lyrics.trim().lines().joinToString("\n  ")}
            """.trimIndent()
            synthesizedAudioDuration.value = "1:48"
        }
    }

    fun stopVocalSynthesis() {
        progressJob?.cancel()
        progressJob = null
        isSynthesizing.value = false
        synthesisProgress.value = 0f
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
        stopVocalSynthesis()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SingerViewModel::class.java)) {
                return SingerViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

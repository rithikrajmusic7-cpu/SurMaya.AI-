package com.example.ui.viewmodel

import android.app.Application
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.performance.PerformanceConfiguration
import com.example.domain.model.performance.PerformanceIntelligenceEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.sin
import java.util.Random

data class Instrument(
    val id: String,
    val name: String,
    val category: String, // "Percussion", "Strings", "Wind", "Keyboard", "Electronic", "Orchestral"
    val subCategories: List<String>, // e.g. ["Indian Classical", "Indian Folk", "Bollywood"]
    val description: String,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val status: String = "Available", // "Available", "Offline Ready", "Downloading", "Cloud AI Required"
    val qualityLevel: String = "Pro AI", // "Basic", "Pro AI", "Ultra Neural"
    val baseFreq: Double = 220.0,
    val iconName: String
)

data class StudioTrack(
    val id: String = UUID.randomUUID().toString(),
    val instrument: Instrument,
    val isMuted: Boolean = false,
    val isSoloed: Boolean = false,
    val volume: Float = 80f, // 0-100%
    val pan: Float = 0f, // -1f (Left) to +1f (Right)
    val eqLow: Float = 0f, // -12dB to +12dB
    val eqMid: Float = 0f,
    val eqHigh: Float = 0f,
    val trackColorHex: Long = 0xFF9F75FF,
    val isHarmonyActive: Boolean = false,
    val automationEnabled: Boolean = false
)

class InstrumentViewModel(application: Application) : AndroidViewModel(application) {

    private val performanceEngine = PerformanceIntelligenceEngine()

    // --- State Streams ---
    private val _instruments = MutableStateFlow<List<Instrument>>(emptyList())
    val instruments: StateFlow<List<Instrument>> = _instruments

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites

    private val _selectedInstrument = MutableStateFlow<Instrument?>(null)
    val selectedInstrument: StateFlow<Instrument?> = _selectedInstrument

    private val _recentInstrumentIds = MutableStateFlow<List<String>>(emptyList())
    val recentInstrumentIds: StateFlow<List<String>> = _recentInstrumentIds

    // --- Multi-Instrument Studio Tracks ---
    private val _studioTracks = MutableStateFlow<List<StudioTrack>>(emptyList())
    val studioTracks: StateFlow<List<StudioTrack>> = _studioTracks

    // --- Global AI Parameters ---
    val paramTempo = MutableStateFlow(120f) // BPM
    val paramScale = MutableStateFlow("Yaman") // Major, Minor, Bhairav, Yaman, Kalyani, Bilawal, Bhairavi
    val paramKey = MutableStateFlow("C#")
    val paramTimeSignature = MutableStateFlow("4/4") // 4/4, 3/4, 7/8, 6/8, 16 Beats (Teental)
    val paramGroove = MutableStateFlow(75f)
    val paramSwing = MutableStateFlow(15f)
    val paramDynamics = MutableStateFlow(80f)
    val paramHumanization = MutableStateFlow(85f)
    val paramComplexity = MutableStateFlow(60f)
    val paramPerformanceStyle = MutableStateFlow("Expressive Bollywood") // Classical Traditional, Modern Fusion, Expressive Bollywood, Cinematic Background
    val paramPerformanceEnergy = MutableStateFlow(70f)
    val paramVelocity = MutableStateFlow(85f)
    val paramStereoWidth = MutableStateFlow(75f)
    val paramExpression = MutableStateFlow(80f)

    // --- Instrument Specific Parameter Adjustments ---
    // Tabla
    val tabThekaStyle = MutableStateFlow("Teental (16 Beats)") // Teental, Keharwa, Dadra, Rupak
    val tabOpenStroke = MutableStateFlow(70f)
    val tabClosedStroke = MutableStateFlow(60f)
    val tabSpeed = MutableStateFlow("Single (Ektaal)") // Single, Double, Chaugun
    val tabFillDensity = MutableStateFlow(40f)
    val tabImprovisation = MutableStateFlow(50f)
    val tabTraditionalMode = MutableStateFlow(true)

    // Dholak
    val dholFolkStyle = MutableStateFlow("Bhangra") // Bhangra, Garba, Qawwali, Lavani
    val dholBollywoodStyle = MutableStateFlow("90s Retro")
    val dholGrooveStrength = MutableStateFlow(85f)

    // Mridangam
    val mridCarnaticStyle = MutableStateFlow("Adi Tala")
    val mridComplexity = MutableStateFlow(70f)
    val mridHandDynamics = MutableStateFlow(80f)

    // Veena
    val veenaResonance = MutableStateFlow(80f)
    val veenaSlideAmount = MutableStateFlow(75f)
    val veenaOrnamentation = MutableStateFlow(65f)

    // Sitar
    val sitarMeend = MutableStateFlow(85f)
    val sitarGamak = MutableStateFlow(60f)
    val sitarSympathetic = MutableStateFlow(90f)
    val sitarPickingStyle = MutableStateFlow("Da-Ra-Di-Ra")

    // Sarangi
    val sarangiBowIntensity = MutableStateFlow(75f)
    val sarangiSustain = MutableStateFlow(85f)
    val sarangiVibrato = MutableStateFlow(60f)

    // Flute (Bansuri)
    val fluteBreath = MutableStateFlow(65f)
    val fluteVibrato = MutableStateFlow(70f)
    val fluteLegato = MutableStateFlow(80f)
    val fluteAirNoise = MutableStateFlow(25f)

    // Shehnai
    val shehnaiCeremonialStyle = MutableStateFlow("Wedding Auspicious")
    val shehnaiSustain = MutableStateFlow(80f)
    val shehnaiExpression = MutableStateFlow(85f)

    // Piano
    val pianoType = MutableStateFlow("Grand") // Grand, Upright, Soft, Cinematic, Pop
    val pianoPedal = MutableStateFlow(80f)
    val pianoVelocityCurve = MutableStateFlow(60f)

    // Guitar
    val guitarType = MutableStateFlow("Acoustic") // Acoustic, Electric, Clean, Crunch, Lead, Fingerstyle
    val guitarStrummingPattern = MutableStateFlow("Arpeggio 8th")
    val guitarPalmMute = MutableStateFlow(20f)

    // Bass
    val bassType = MutableStateFlow("Finger") // Finger, Pick, Slap, Synth, Sub
    val bassGroove = MutableStateFlow(75f)

    // Drum Kit
    val drumStyle = MutableStateFlow("Bollywood") // Rock, Pop, EDM, Trap, Bollywood, Jazz, Metal
    val drumGhostNotes = MutableStateFlow(40f)
    val drumFillFrequency = MutableStateFlow(30f)

    // Synth
    val synthType = MutableStateFlow("Lead") // Lead, Pad, Pluck, Bass, Ambient
    val synthArpEnabled = MutableStateFlow(false)
    val synthFilterCutoff = MutableStateFlow(65f)

    // Strings Orchestra
    val stringType = MutableStateFlow("Ensemble") // Solo, Ensemble, Legato, Staccato, Pizzicato
    val stringCinematicFeel = MutableStateFlow(80f)

    // Violin
    val violinVibrato = MutableStateFlow(75f)
    val violinLegato = MutableStateFlow(80f)

    // --- Rhythm Creator State ---
    val rhythmStyle = MutableStateFlow("Tabla Keharwa")
    val rhythmGrooveSpeed = MutableStateFlow(120f)
    val rhythmFillFrequency = MutableStateFlow(40f)
    val rhythmSwingValue = MutableStateFlow(20f)
    val isRhythmPlaying = MutableStateFlow(false)

    // --- Melody Creator State ---
    val melodyRagaScale = MutableStateFlow("Raga Bhairav")
    val melodyComplexity = MutableStateFlow(70f)
    val melodyHarmonyLevel = MutableStateFlow(50f)
    val melodyImprovisation = MutableStateFlow(60f)
    val isMelodyPlaying = MutableStateFlow(false)

    // --- Audio Synthesis Engine Output ---
    val isPlayingPreview = MutableStateFlow(false)
    val previewingInstrumentId = MutableStateFlow<String?>(null)
    val previewWaves = MutableStateFlow(List(16) { 0.1f })

    val isGeneratingAI = MutableStateFlow(false)
    val generationProgress = MutableStateFlow(0f)
    val generatedOutputReport = MutableStateFlow<String?>(null)
    val selectedOutputFormat = MutableStateFlow("WAV") // MP3, WAV, FLAC, MIDI

    private var audioTrack: AudioTrack? = null
    private var isAudioActive = false
    private var synthJob: Job? = null
    private var aiProgressJob: Job? = null

    init {
        loadInstruments()
    }

    private fun loadInstruments() {
        _instruments.value = listOf(
            // Indian Classical & Folk
            Instrument(
                id = "tabla",
                name = "Tabla",
                category = "Percussion",
                subCategories = listOf("Indian Classical", "Indian Folk", "Bollywood"),
                description = "Traditional classical hand drums. Capable of complex rhythms like Teental and Rupak with open/closed strokes.",
                isFavorite = true,
                isDownloaded = true,
                status = "Offline Ready",
                qualityLevel = "Ultra Neural",
                baseFreq = 180.0,
                iconName = "tabla"
            ),
            Instrument(
                id = "dholak",
                name = "Dholak",
                category = "Percussion",
                subCategories = listOf("Indian Folk", "Bollywood"),
                description = "High-energy wooden folk drum. Vital for Bhangra, Garba, and festive Bollywood rhythms.",
                isFavorite = false,
                isDownloaded = true,
                status = "Offline Ready",
                qualityLevel = "Pro AI",
                baseFreq = 150.0,
                iconName = "dholak"
            ),
            Instrument(
                id = "mridangam",
                name = "Mridangam",
                category = "Percussion",
                subCategories = listOf("Indian Classical"),
                description = "The primary rhythmic accompaniment in Carnatic classical music, offering deep resonance and precise pitch tuning.",
                isFavorite = false,
                isDownloaded = false,
                status = "Available",
                qualityLevel = "Pro AI",
                baseFreq = 160.0,
                iconName = "mridangam"
            ),
            Instrument(
                id = "veena",
                name = "Veena",
                category = "Strings",
                subCategories = listOf("Indian Classical"),
                description = "The majestic plucked stringed instrument, showcasing rich microtonal slides (meends) and traditional ornamentations.",
                isFavorite = true,
                isDownloaded = false,
                status = "Available",
                qualityLevel = "Ultra Neural",
                baseFreq = 220.0,
                iconName = "veena"
            ),
            Instrument(
                id = "sitar",
                name = "Sitar",
                category = "Strings",
                subCategories = listOf("Indian Classical", "Bollywood"),
                description = "The world-famous plucked instrument with sympathetic strings. Rich in gamaks, fast-picking taans, and traditional raga tones.",
                isFavorite = true,
                isDownloaded = true,
                status = "Offline Ready",
                qualityLevel = "Ultra Neural",
                baseFreq = 293.0,
                iconName = "sitar"
            ),
            Instrument(
                id = "sarangi",
                name = "Sarangi",
                category = "Strings",
                subCategories = listOf("Indian Classical", "Background Score"),
                description = "Resonant bowed string instrument that perfectly mimics human emotional vocal expressions and heavy sustain.",
                isFavorite = false,
                isDownloaded = false,
                status = "Cloud AI Required",
                qualityLevel = "Ultra Neural",
                baseFreq = 330.0,
                iconName = "sarangi"
            ),
            Instrument(
                id = "bansuri",
                name = "Bansuri (Flute)",
                category = "Wind",
                subCategories = listOf("Indian Classical", "Indian Folk", "Bollywood"),
                description = "Traditional hand-crafted bamboo flute. Delivers airy, emotional, and highly expressive lyrical melodies.",
                isFavorite = true,
                isDownloaded = true,
                status = "Offline Ready",
                qualityLevel = "Pro AI",
                baseFreq = 440.0,
                iconName = "bansuri"
            ),
            Instrument(
                id = "shehnai",
                name = "Shehnai",
                category = "Wind",
                subCategories = listOf("Indian Folk", "Bollywood"),
                description = "Double-reed auspicious oboe-like instrument. Produces powerful ceremonial, emotional, and wedding background themes.",
                isFavorite = false,
                isDownloaded = false,
                status = "Available",
                qualityLevel = "Pro AI",
                baseFreq = 520.0,
                iconName = "shehnai"
            ),

            // Western Instruments
            Instrument(
                id = "piano",
                name = "Grand Piano",
                category = "Keyboard",
                subCategories = listOf("Western", "Bollywood", "Background Score"),
                description = "Premium acoustic concert grand piano. Perfect for lush pop progressions, cinematic textures, or classical motifs.",
                isFavorite = true,
                isDownloaded = true,
                status = "Offline Ready",
                qualityLevel = "Ultra Neural",
                baseFreq = 261.6,
                iconName = "piano"
            ),
            Instrument(
                id = "guitar",
                name = "Studio Guitar",
                category = "Strings",
                subCategories = listOf("Western", "Bollywood"),
                description = "Expressive multi-type guitar. Includes acoustic, electric clean, crunch, lead, and strumming pattern models.",
                isFavorite = false,
                isDownloaded = true,
                status = "Offline Ready",
                qualityLevel = "Pro AI",
                baseFreq = 196.0,
                iconName = "guitar"
            ),
            Instrument(
                id = "bass",
                name = "Bass Guitar",
                category = "Strings",
                subCategories = listOf("Western", "Bollywood"),
                description = "Sturdy electric bass providing low-frequency foundations in fingerstyle, pick, slap, and synth-bass modes.",
                isFavorite = false,
                isDownloaded = true,
                status = "Offline Ready",
                qualityLevel = "Basic",
                baseFreq = 110.0,
                iconName = "bass"
            ),
            Instrument(
                id = "drums",
                name = "Drum Kit",
                category = "Percussion",
                subCategories = listOf("Western", "Bollywood"),
                description = "Full acoustic and electronic drum kit. Delivers steady grooves, fills, and custom humanized ghost beats.",
                isFavorite = false,
                isDownloaded = true,
                status = "Offline Ready",
                qualityLevel = "Basic",
                baseFreq = 100.0,
                iconName = "drums"
            ),
            Instrument(
                id = "synth",
                name = "Neural Synth",
                category = "Electronic",
                subCategories = listOf("Western", "Experimental"),
                description = "Virtual analog polyphonic synthesizer with custom envelopes, filters, and built-in arpeggiation matrices.",
                isFavorite = false,
                isDownloaded = true,
                status = "Offline Ready",
                qualityLevel = "Pro AI",
                baseFreq = 349.2,
                iconName = "synth"
            ),
            Instrument(
                id = "orchestra",
                name = "Strings Orchestra",
                category = "Orchestral",
                subCategories = listOf("Western", "Background Score"),
                description = "Enormous symphonic string orchestra. Creates rich, cinematic, epic backdrops in legato, staccato, or pizzicato.",
                isFavorite = false,
                isDownloaded = false,
                status = "Cloud AI Required",
                qualityLevel = "Ultra Neural",
                baseFreq = 392.0,
                iconName = "orchestra"
            ),
            Instrument(
                id = "violin",
                name = "Violin Solo",
                category = "Strings",
                subCategories = listOf("Western", "Bollywood", "Background Score"),
                description = "A highly sensitive, crying violin model with adjustable expression, rich vibrato, and emotional legato transitions.",
                isFavorite = false,
                isDownloaded = false,
                status = "Available",
                qualityLevel = "Ultra Neural",
                baseFreq = 440.0,
                iconName = "violin"
            )
        )

        _selectedInstrument.value = _instruments.value.first()
        _studioTracks.value = listOf(
            StudioTrack(instrument = _instruments.value[0], trackColorHex = 0xFFF9D142), // Tabla
            StudioTrack(instrument = _instruments.value[6], trackColorHex = 0xFF9F75FF), // Bansuri
            StudioTrack(instrument = _instruments.value[8], trackColorHex = 0xFF00FF88)  // Piano
        )
    }

    // --- Search & Filter logic ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun toggleShowOnlyFavorites() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    fun selectInstrument(instrument: Instrument) {
        _selectedInstrument.value = instrument
        
        // Add to recent instruments
        val currentRecents = _recentInstrumentIds.value.toMutableList()
        currentRecents.remove(instrument.id)
        currentRecents.add(0, instrument.id)
        _recentInstrumentIds.value = currentRecents.take(5)
    }

    fun toggleFavorite(instrumentId: String) {
        _instruments.value = _instruments.value.map { inst ->
            if (inst.id == instrumentId) {
                val updated = inst.copy(isFavorite = !inst.isFavorite)
                if (_selectedInstrument.value?.id == instrumentId) {
                    _selectedInstrument.value = updated
                }
                updated
            } else inst
        }
    }

    fun startDownload(instrumentId: String) {
        _instruments.value = _instruments.value.map { inst ->
            if (inst.id == instrumentId) {
                inst.copy(status = "Downloading")
            } else inst
        }

        viewModelScope.launch(Dispatchers.Main) {
            delay(2000) // Simulate download delay
            _instruments.value = _instruments.value.map { inst ->
                if (inst.id == instrumentId) {
                    val updated = inst.copy(status = "Offline Ready", isDownloaded = true)
                    if (_selectedInstrument.value?.id == instrumentId) {
                        _selectedInstrument.value = updated
                    }
                    updated
                } else inst
            }
        }
    }

    // --- Multi-Instrument Studio Management ---
    fun addTrackToStudio(instrument: Instrument) {
        val colors = listOf(0xFFF9D142, 0xFF9F75FF, 0xFF00FF88, 0xFF00C8FF, 0xFFFF5E5E, 0xFFFF9F43)
        val randColor = colors[Random().nextInt(colors.size)]
        val newTrack = StudioTrack(instrument = instrument, trackColorHex = randColor)
        _studioTracks.value = _studioTracks.value + newTrack
    }

    fun removeTrackFromStudio(trackId: String) {
        _studioTracks.value = _studioTracks.value.filter { it.id != trackId }
    }

    fun toggleMuteTrack(trackId: String) {
        _studioTracks.value = _studioTracks.value.map {
            if (it.id == trackId) it.copy(isMuted = !it.isMuted) else it
        }
    }

    fun toggleSoloTrack(trackId: String) {
        _studioTracks.value = _studioTracks.value.map {
            if (it.id == trackId) it.copy(isSoloed = !it.isSoloed) else it
        }
    }

    fun updateTrackVolume(trackId: String, volume: Float) {
        _studioTracks.value = _studioTracks.value.map {
            if (it.id == trackId) it.copy(volume = volume) else it
        }
    }

    fun updateTrackPan(trackId: String, pan: Float) {
        _studioTracks.value = _studioTracks.value.map {
            if (it.id == trackId) it.copy(pan = pan) else it
        }
    }

    fun updateTrackEQ(trackId: String, low: Float, mid: Float, high: Float) {
        _studioTracks.value = _studioTracks.value.map {
            if (it.id == trackId) it.copy(eqLow = low, eqMid = mid, eqHigh = high) else it
        }
    }

    fun toggleTrackHarmony(trackId: String) {
        _studioTracks.value = _studioTracks.value.map {
            if (it.id == trackId) it.copy(isHarmonyActive = !it.isHarmonyActive) else it
        }
    }

    fun toggleTrackAutomation(trackId: String) {
        _studioTracks.value = _studioTracks.value.map {
            if (it.id == trackId) it.copy(automationEnabled = !it.automationEnabled) else it
        }
    }

    // --- Rhythm / Melody Generator Previews ---
    fun toggleRhythmPlaying() {
        val current = isRhythmPlaying.value
        isRhythmPlaying.value = !current
        if (!current) {
            // Start rhythm audio
            startRhythmAudio()
        } else {
            stopAudio()
        }
    }

    fun toggleMelodyPlaying() {
        val current = isMelodyPlaying.value
        isMelodyPlaying.value = !current
        if (!current) {
            startMelodyAudio()
        } else {
            stopAudio()
        }
    }

    private fun startRhythmAudio() {
        stopAudio()
        isAudioActive = true
        isRhythmPlaying.value = true

        val sampleRate = 44100
        val minBufSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufSize,
            AudioTrack.MODE_STREAM
        )

        audioTrack?.play()

        synthJob = viewModelScope.launch(Dispatchers.Default) {
            val buffer = ShortArray(1024)
            var phase = 0.0
            val speedBpm = rhythmGrooveSpeed.value
            val beatIntervalSamples = (60.0 / speedBpm) * sampleRate
            var sampleCount = 0L

            try {
                while (isAudioActive) {
                    for (i in buffer.indices) {
                        // Tabla or drum rhythmic pulses (gabas & bass thuds)
                        val beatPos = sampleCount % beatIntervalSamples
                        val click = if (beatPos < sampleRate * 0.08) {
                            val pulseFreq = if (sampleCount % (beatIntervalSamples * 2) < beatIntervalSamples) 140.0 else 90.0
                            val t = beatPos / sampleRate
                            val damp = Math.exp(-35.0 * t)
                            sin(2.0 * Math.PI * pulseFreq * t) * damp
                        } else if (beatPos > beatIntervalSamples * 0.5 && beatPos < beatIntervalSamples * 0.55) {
                            val t = (beatPos - beatIntervalSamples * 0.5) / sampleRate
                            val damp = Math.exp(-60.0 * t)
                            sin(2.0 * Math.PI * 250.0 * t) * damp * 0.5
                        } else {
                            0.0
                        }

                        // Low-frequency noise element for folk groove
                        val noise = (Math.random() * 2.0 - 1.0) * if (beatPos < sampleRate * 0.03) 0.1 else 0.005

                        buffer[i] = ((click + noise) * 16000.0).toInt().toShort()
                        sampleCount++
                    }

                    // Write audio buffer
                    audioTrack?.write(buffer, 0, buffer.size)

                    // Feed preview waves back to UI
                    val updatedWaves = List(16) { index ->
                        val phaseOffset = (index * 0.5)
                        val v = 0.1f + (Math.abs(sin(phaseOffset + (sampleCount.toDouble() / 5000.0))).toFloat() * 0.6f)
                        v.coerceIn(0.1f, 1f)
                    }
                    previewWaves.value = updatedWaves
                    delay(5)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startMelodyAudio() {
        stopAudio()
        isAudioActive = true
        isMelodyPlaying.value = true

        val sampleRate = 44100
        val minBufSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufSize,
            AudioTrack.MODE_STREAM
        )

        audioTrack?.play()

        synthJob = viewModelScope.launch(Dispatchers.Default) {
            val buffer = ShortArray(1024)
            var sampleCount = 0L
            val speedBpm = paramTempo.value
            val beatIntervalSamples = (60.0 / speedBpm) * sampleRate

            // Raga tones mapping (Bhairav-inspired: Sa, Re (flat), Ga, Ma, Pa, Dha (flat), Ni)
            val frequencies = doubleArrayOf(261.6, 277.2, 329.6, 349.2, 392.0, 415.3, 493.9, 523.2)

            try {
                while (isAudioActive) {
                    val currentBeatIndex = (sampleCount / beatIntervalSamples).toInt()
                    // Simple deterministic raga pattern based on beat index
                    val freqIndex = Math.abs((currentBeatIndex * 3) + (currentBeatIndex % 2) - (currentBeatIndex / 8)) % frequencies.size
                    val baseFreq = frequencies[freqIndex]

                    for (i in buffer.indices) {
                        val beatPos = sampleCount % beatIntervalSamples
                        val t = beatPos / sampleRate
                        val envelope = Math.exp(-2.5 * t).coerceAtMost(1.0)

                        // Smooth slide (meend) toward next frequency
                        val targetFreq = baseFreq
                        val nextFreq = frequencies[(freqIndex + 1) % frequencies.size]
                        val transitionRatio = (beatPos / beatIntervalSamples).coerceIn(0.0, 1.0)
                        val slideFreq = if (transitionRatio > 0.85) {
                            val r = (transitionRatio - 0.85) / 0.15
                            targetFreq + (nextFreq - targetFreq) * r
                        } else {
                            targetFreq
                        }

                        // Generate classical sounding sitar/bansuri harmonics
                        val mainTone = sin(2.0 * Math.PI * slideFreq * t)
                        val secondHarmonic = sin(4.0 * Math.PI * slideFreq * t) * 0.35
                        val thirdHarmonic = sin(6.0 * Math.PI * slideFreq * t) * 0.15

                        val composite = (mainTone + secondHarmonic + thirdHarmonic) * envelope * 0.45
                        buffer[i] = (composite * 16000.0).toInt().toShort()
                        sampleCount++
                    }

                    audioTrack?.write(buffer, 0, buffer.size)

                    // Feed preview waves back to UI
                    val updatedWaves = List(16) { index ->
                        val phaseOffset = (index * 0.3)
                        val v = 0.1f + (Math.abs(sin(phaseOffset + (sampleCount.toDouble() / 4000.0))).toFloat() * 0.7f)
                        v.coerceIn(0.1f, 1f)
                    }
                    previewWaves.value = updatedWaves
                    delay(5)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Audio Preview for individual instruments ---
    fun togglePreview(instrument: Instrument) {
        if (isPlayingPreview.value && previewingInstrumentId.value == instrument.id) {
            stopAudio()
            return
        }

        stopAudio()
        isAudioActive = true
        isPlayingPreview.value = true
        previewingInstrumentId.value = instrument.id

        val sampleRate = 44100
        val minBufSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufSize,
            AudioTrack.MODE_STREAM
        )

        audioTrack?.play()

        synthJob = viewModelScope.launch(Dispatchers.Default) {
            val buffer = ShortArray(1024)
            var phase = 0.0
            val baseFreq = instrument.baseFreq
            var sampleCounter = 0L

            try {
                while (isAudioActive) {
                    val isPercussive = instrument.category == "Percussion"
                    
                    for (i in buffer.indices) {
                        val t = sampleCounter.toDouble() / sampleRate
                        
                        val signal = if (isPercussive) {
                            // High rhythmic pulses with fast decay for drums/tablas
                            val beatPos = (sampleCounter % (sampleRate * 0.5)) / sampleRate
                            val dec = Math.exp(-25.0 * beatPos)
                            val f = if (sampleCounter % sampleRate < sampleRate * 0.5) baseFreq else baseFreq * 0.65
                            sin(2.0 * Math.PI * f * beatPos) * dec
                        } else {
                            // Rich melodic strings/winds
                            val tremolo = 1.0 + 0.15 * sin(2.0 * Math.PI * 6.0 * t) // vibrato
                            val main = sin(2.0 * Math.PI * baseFreq * t * tremolo)
                            val sub = sin(1.0 * Math.PI * baseFreq * t) * 0.25
                            val chord = sin(3.0 * Math.PI * baseFreq * t) * 0.1
                            (main + sub + chord) * 0.4
                        }

                        buffer[i] = (signal * 16000.0).toInt().toShort()
                        sampleCounter++
                    }

                    audioTrack?.write(buffer, 0, buffer.size)

                    // Feed waves back to UI
                    val updatedWaves = List(16) { index ->
                        val phaseOffset = (index * 0.4)
                        val baseVal = if (isPercussive) 0.1f else 0.2f
                        val multiplier = if (isPercussive) 0.4f else 0.6f
                        val v = baseVal + (Math.abs(sin(phaseOffset + (sampleCounter.toDouble() / 6000.0))).toFloat() * multiplier)
                        v.coerceIn(0.1f, 1f)
                    }
                    previewWaves.value = updatedWaves
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
        isRhythmPlaying.value = false
        isMelodyPlaying.value = false
        previewingInstrumentId.value = null
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
    }

    // --- AI Instrument Generation ---
    fun startAIGeneration(promptText: String, lyricsText: String, creationMethod: String) {
        stopAudio()
        isGeneratingAI.value = true
        generationProgress.value = 0f
        generatedOutputReport.value = null

        aiProgressJob = viewModelScope.launch(Dispatchers.Main) {
            var curr = 0f
            while (curr < 1.0f) {
                delay(120)
                curr += 0.05f
                generationProgress.value = curr.coerceAtMost(1f)
            }

            // Generation complete
            isGeneratingAI.value = false

            val selectedIds = if (_studioTracks.value.isNotEmpty()) {
                _studioTracks.value.map { it.instrument.id }
            } else {
                listOf(_selectedInstrument.value?.id ?: "tabla")
            }

            val pConfig = PerformanceConfiguration(
                tempo = paramTempo.value,
                scale = paramScale.value,
                key = paramKey.value,
                timeSignature = paramTimeSignature.value,
                groove = paramGroove.value,
                swing = paramSwing.value,
                dynamics = paramDynamics.value,
                humanization = paramHumanization.value,
                complexity = paramComplexity.value,
                performanceStyle = paramPerformanceStyle.value,
                performanceEnergy = paramPerformanceEnergy.value,
                velocity = paramVelocity.value,
                stereoWidth = paramStereoWidth.value,
                expression = paramExpression.value
            )

            val fullPerformance = performanceEngine.generateFullPerformance(
                projectId = UUID.randomUUID().toString(),
                config = pConfig,
                instruments = selectedIds,
                section = "Chorus", // Main musical section
                format = selectedOutputFormat.value
            )

            generatedOutputReport.value = fullPerformance.compositeAuditReport
        }
    }

    fun stopAIGeneration() {
        aiProgressJob?.cancel()
        aiProgressJob = null
        isGeneratingAI.value = false
        generationProgress.value = 0f
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
        stopAIGeneration()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InstrumentViewModel::class.java)) {
                return InstrumentViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

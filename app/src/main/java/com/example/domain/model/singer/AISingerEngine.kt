package com.example.domain.model.singer

import java.util.UUID
import kotlin.math.sin
import kotlin.random.Random

// 1. Lyrics-to-Phoneme Pipeline
interface IPhonemeMapper {
    fun convertLyricsToPhonemes(
        lyrics: String,
        language: String,
        tempo: Float,
        baseMidi: Int
    ): List<PhonemeSegment>
}

interface LyricsToPhonemePipeline : IPhonemeMapper

class DefaultLyricsToPhonemePipeline : LyricsToPhonemePipeline {
    private val ipaDictionary = mapOf(
        "namaste" to "nə.məs.teː",
        "surmaya" to "suːr.maː.jaː",
        "sangeet" to "səŋ.ɡiːt",
        "dil" to "d̪ɪl",
        "tumi" to "t̪u.mi",
        "sundori" to "ʃun.d̪o.ri",
        "aawaaz" to "aː.waːz",
        "pyar" to "pjaːr",
        "mann" to "mən",
        "sa" to "saː",
        "re" to "reː",
        "ga" to "ɡaː",
        "ma" to "maː",
        "pa" to "paː",
        "dha" to "d̪ʱaː",
        "ni" to "niː"
    )

    override fun convertLyricsToPhonemes(
        lyrics: String,
        language: String,
        tempo: Float,
        baseMidi: Int
    ): List<PhonemeSegment> {
        val words = lyrics.split(Regex("[\\s,;.?!]+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()

        val phonemes = mutableListOf<PhonemeSegment>()
        var currentTimeMs = 150L
        val beatDurationMs = (60000 / tempo).toLong()

        val r = Random(lyrics.hashCode().toLong())

        for ((index, word) in words.withIndex()) {
            val cleanWord = word.lowercase()
            val ipa = ipaDictionary[cleanWord] ?: "wər.d_${cleanWord}"
            
            // Derive duration based on syllable length or random variation
            val durationMultiplier = when {
                cleanWord.length <= 3 -> 0.5f
                cleanWord.length >= 7 -> 1.5f
                else -> 1.0f
            }
            val durationMs = (beatDurationMs * durationMultiplier * r.nextDouble(0.8, 1.2)).toLong()

            // Map pitch following a simple scalar pattern (Sa Re Ga Ma Pa Dha Ni scale)
            val pitchOffset = when (index % 8) {
                0 -> 0 // Sa
                1 -> 2 // Re
                2 -> 4 // Ga
                3 -> 5 // Ma
                4 -> 7 // Pa
                5 -> 9 // Dha
                6 -> 11 // Ni
                else -> 12 // Sa'
            }

            phonemes.add(
                PhonemeSegment(
                    text = word,
                    ipaPhoneme = ipa,
                    startTimeMs = currentTimeMs,
                    durationMs = durationMs,
                    pitch = baseMidi + pitchOffset,
                    vowelSustain = 0.6f + r.nextFloat() * 0.25f
                )
            )
            currentTimeMs += durationMs + 50L // 50ms pause between words
        }

        return phonemes
    }
}

// 2. Indian Classical Ornamentation Handler
interface IOrnamentationHandler {
    fun injectOrnamentations(
        phonemes: List<PhonemeSegment>,
        style: String,
        meendIntensity: Float,
        gamakIntensity: Float,
        murkiIntensity: Float
    ): List<VocalOrnamentation>
}

interface VocalOrnamentationHandler : IOrnamentationHandler

class DefaultVocalOrnamentationHandler : VocalOrnamentationHandler {
    override fun injectOrnamentations(
        phonemes: List<PhonemeSegment>,
        style: String,
        meendIntensity: Float,
        gamakIntensity: Float,
        murkiIntensity: Float
    ): List<VocalOrnamentation> {
        val ornamentations = mutableListOf<VocalOrnamentation>()
        if (phonemes.isEmpty()) return ornamentations

        val r = Random(style.hashCode())
        val isClassical = style.contains("Classical", ignoreCase = true) || style.contains("Traditional", ignoreCase = true)

        for (phoneme in phonemes) {
            val start = phoneme.startTimeMs
            val duration = phoneme.durationMs

            // Classical singers pull expressive ornaments
            val roll = r.nextFloat() * 100f
            
            when {
                // 1. Meend (Slide between distant pitches)
                roll < meendIntensity && duration >= 600L -> {
                    ornamentations.add(
                        VocalOrnamentation(
                            timeMs = start,
                            type = "Meend",
                            intensity = meendIntensity / 100f,
                            pitchRange = 2.0f,
                            durationMs = (duration * 0.4f).toInt()
                        )
                    )
                }
                // 2. Gamak (Rapid heavy oscillations)
                roll < meendIntensity + gamakIntensity && duration >= 800L && isClassical -> {
                    ornamentations.add(
                        VocalOrnamentation(
                            timeMs = start + (duration * 0.2).toLong(),
                            type = "Gamak",
                            intensity = gamakIntensity / 100f,
                            pitchRange = 1.5f,
                            durationMs = (duration * 0.6f).toInt()
                        )
                    )
                }
                // 3. Murki (Delicate grace notes)
                roll < meendIntensity + gamakIntensity + murkiIntensity && duration >= 400L -> {
                    ornamentations.add(
                        VocalOrnamentation(
                            timeMs = start + (duration * 0.1).toLong(),
                            type = "Murki",
                            intensity = murkiIntensity / 100f,
                            pitchRange = 0.5f,
                            durationMs = 150
                        )
                    )
                }
                // 4. KanSwar (Quick touch notes)
                roll < 30f && isClassical -> {
                    ornamentations.add(
                        VocalOrnamentation(
                            timeMs = start,
                            type = "KanSwar",
                            intensity = 0.8f,
                            pitchRange = 1.0f,
                            durationMs = 80
                        )
                    )
                }
            }
        }

        return ornamentations
    }
}

// 3. Breath Planning Engine
interface BreathPlanningEngine {
    fun planBreaths(
        phonemes: List<PhonemeSegment>,
        breathControl: Float
    ): List<BreathMarker>
}

class DefaultBreathPlanningEngine : BreathPlanningEngine {
    override fun planBreaths(
        phonemes: List<PhonemeSegment>,
        breathControl: Float
    ): List<BreathMarker> {
        val markers = mutableListOf<BreathMarker>()
        if (phonemes.isEmpty()) return markers

        // Higher breathControl means the singer holds oxygen longer (efficient lung usage)
        val maxSingingSecsBeforeBreath = 3.5f + (breathControl / 100f) * 4.5f // 3.5s to 8s
        var consecutiveSingingMs = 0L
        var lungCapacity = 1.0f

        for (i in 0 until phonemes.size - 1) {
            val current = phonemes[i]
            val next = phonemes[i + 1]
            consecutiveSingingMs += current.durationMs

            val restGapMs = next.startTimeMs - (current.startTimeMs + current.durationMs)

            // Dynamic oxygen decay based on duration
            lungCapacity -= (current.durationMs / 1000f) * (0.12f - (breathControl / 1000f))
            lungCapacity = lungCapacity.coerceAtLeast(0.05f)

            if (restGapMs >= 300L || consecutiveSingingMs >= (maxSingingSecsBeforeBreath * 1000).toLong()) {
                // Schedule breath marker in the silence, or forcefully insert
                val breathTime = current.startTimeMs + current.durationMs + (restGapMs / 4)
                val breathDuration = if (restGapMs >= 300L) restGapMs.coerceAtMost(600L).toInt() else 350

                markers.add(
                    BreathMarker(
                        timeMs = breathTime,
                        durationMs = breathDuration,
                        breathIntensity = 0.4f + (1f - lungCapacity) * 0.5f,
                        lungVolumeLeft = lungCapacity
                    )
                )

                // Respiratory recovery
                consecutiveSingingMs = 0L
                lungCapacity = 1.0f
            }
        }

        return markers
    }
}

// 4. Vocal Emotion Synthesizer
interface VocalEmotionSynthesizer {
    fun modulateVocalAttributes(
        emotion: String,
        baseVibrato: Float,
        baseBreathiness: Float,
        basePower: Float
    ): Pair<Float, Float> // returns modulated (Vibrato, Breathiness)
}

class DefaultVocalEmotionSynthesizer : VocalEmotionSynthesizer {
    override fun modulateVocalAttributes(
        emotion: String,
        baseVibrato: Float,
        baseBreathiness: Float,
        basePower: Float
    ): Pair<Float, Float> {
        return when (emotion.lowercase()) {
            "romantic" -> {
                val modVibrato = (baseVibrato * 1.15f).coerceIn(0f, 100f)
                val modBreath = (baseBreathiness * 1.4f).coerceIn(0f, 100f)
                Pair(modVibrato, modBreath)
            }
            "sad" -> {
                val modVibrato = (baseVibrato * 1.3f).coerceIn(0f, 100f)
                val modBreath = (baseBreathiness * 1.6f).coerceIn(0f, 100f)
                Pair(modVibrato, modBreath)
            }
            "devotional" -> {
                val modVibrato = (baseVibrato * 0.9f).coerceIn(0f, 100f)
                val modBreath = (baseBreathiness * 0.7f).coerceIn(0f, 100f)
                Pair(modVibrato, modBreath)
            }
            "energetic" -> {
                val modVibrato = (baseVibrato * 0.75f).coerceIn(0f, 100f)
                val modBreath = (baseBreathiness * 0.4f).coerceIn(0f, 100f)
                Pair(modVibrato, modBreath)
            }
            else -> Pair(baseVibrato, baseBreathiness)
        }
    }
}

// 5. Vocal Validation & Explainability Engine
interface VocalValidationEngine {
    fun validateVocalPlayability(
        voice: VoiceIdentity,
        phonemes: List<PhonemeSegment>,
        breaths: List<BreathMarker>
    ): VocalSynthesisValidation
}

class DefaultVocalValidationEngine : VocalValidationEngine {
    override fun validateVocalPlayability(
        voice: VoiceIdentity,
        phonemes: List<PhonemeSegment>,
        breaths: List<BreathMarker>
    ): VocalSynthesisValidation {
        val warnings = mutableListOf<String>()
        val limitingFactors = mutableListOf<String>()
        var isValid = true
        var suggestedFix: String? = null

        if (phonemes.isEmpty()) {
            return VocalSynthesisValidation(true, emptyList(), emptyList(), null)
        }

        // Check 1: Register range limits
        val minPitch = phonemes.minOf { it.pitch }
        val maxPitch = phonemes.maxOf { it.pitch }

        if (minPitch < voice.minPitch) {
            isValid = false
            limitingFactors.add("Sub-bass floor range violation")
            warnings.add("Melody falls below the physical register limits for ${voice.name} (${voice.minPitch} MIDI).")
            suggestedFix = "Transpose the master composition key up by ${voice.minPitch - minPitch} semitones."
        }

        if (maxPitch > voice.maxPitch) {
            isValid = false
            limitingFactors.add("Soprano/Tenor ceiling range violation")
            warnings.add("Melody exceeds the physical voice register ceiling for ${voice.name} (${voice.maxPitch} MIDI).")
            if (suggestedFix == null) {
                suggestedFix = "Transpose the master composition key down by ${maxPitch - voice.maxPitch} semitones."
            }
        }

        // Check 2: Oxygen starvation limits
        if (breaths.isEmpty() && phonemes.size > 5) {
            isValid = false
            limitingFactors.add("Oxygen starvation")
            warnings.add("No breathing points planned in the current syllable block, leading to vocal tension.")
            if (suggestedFix == null) {
                suggestedFix = "Reduce tempo or insert commas/punctuation marks in the lyrics to trigger respiratory pauses."
            }
        } else {
            // Check for very short breathes
            val criticalBreaths = breaths.filter { it.durationMs < 200 }
            if (criticalBreaths.isNotEmpty()) {
                warnings.add("Detected ${criticalBreaths.size} shallow inhalations under 200ms which can lead to vocal fatigue.")
            }
        }

        return VocalSynthesisValidation(isValid, limitingFactors, warnings, suggestedFix)
    }
}

// 6. Master AI Singer Engine Orchestrator
interface ISingerEngine {
    fun getVoiceIdentity(voiceId: String): VoiceIdentity
    fun synthesizeVocals(
        projectId: String,
        lyrics: String,
        config: SingerConfiguration,
        tempo: Float,
        keyMidi: Int
    ): VocalSynthesisResult
}

class AISingerEngine(
    private val phonemePipeline: LyricsToPhonemePipeline = DefaultLyricsToPhonemePipeline(),
    private val ornamentationHandler: VocalOrnamentationHandler = DefaultVocalOrnamentationHandler(),
    private val breathPlanner: BreathPlanningEngine = DefaultBreathPlanningEngine(),
    private val emotionSynthesizer: VocalEmotionSynthesizer = DefaultVocalEmotionSynthesizer(),
    private val validationEngine: VocalValidationEngine = DefaultVocalValidationEngine()
) : ISingerEngine {
    private val voicesDatabase = mapOf(
        "ajit" to VoiceIdentity(
            voiceId = "ajit", name = "Ajit", gender = "Male",
            description = "Expressive & silky male voice, perfect for heartfelt romantic ballads and ghazals.",
            nativeLanguage = "Hindi", minPitch = 48, maxPitch = 76,
            supportedEmotions = listOf("Romantic", "Sad", "Devotional")
        ),
        "shrija" to VoiceIdentity(
            voiceId = "shrija", name = "Shrija", gender = "Female",
            description = "Bright, sweet, and classical female voice, ideal for romantic raga pop.",
            nativeLanguage = "Hindi", minPitch = 57, maxPitch = 88,
            supportedEmotions = listOf("Romantic", "Happy", "Devotional")
        ),
        "pandit_g" to VoiceIdentity(
            voiceId = "pandit_g", name = "Pandit G", gender = "Male",
            description = "Veteran classical maestro capable of high-fidelity alaaps and taans.",
            nativeLanguage = "Sanskrit", minPitch = 40, maxPitch = 69,
            supportedEmotions = listOf("Devotional", "Sad", "Peaceful")
        ),
        "mc_shanti" to VoiceIdentity(
            voiceId = "mc_shanti", name = "Shanti", gender = "Female",
            description = "Fast flows, sharp syllables for modern Indian rap.",
            nativeLanguage = "Odia", minPitch = 50, maxPitch = 80,
            supportedEmotions = listOf("Energetic", "Angry")
        )
    )

    override fun getVoiceIdentity(voiceId: String): VoiceIdentity {
        return voicesDatabase[voiceId] ?: VoiceIdentity(
            voiceId = voiceId, name = "Vocalist", gender = "Custom",
            description = "Standard neural singer model.",
            nativeLanguage = "Hindi", minPitch = 40, maxPitch = 90
        )
    }

    override fun synthesizeVocals(
        projectId: String,
        lyrics: String,
        config: SingerConfiguration,
        tempo: Float,
        keyMidi: Int
    ): VocalSynthesisResult {
        val voice = getVoiceIdentity(config.voiceId)

        // 1. Convert text lyrics to phoneme sequences
        val phonemes = phonemePipeline.convertLyricsToPhonemes(lyrics, config.pronunciation, tempo, keyMidi)

        // 2. Modulate features via the emotional engine
        val (modVibrato, modBreath) = emotionSynthesizer.modulateVocalAttributes(
            config.emotion, config.vibratoDepth, config.breathiness, config.power
        )
        val modulatedConfig = config.copy(vibratoDepth = modVibrato, breathiness = modBreath)

        // 3. Render classical ornamentation (Meend, Gamak, Murki, KanSwar)
        val isClassical = config.style.contains("Classical", ignoreCase = true)
        val meendInt = if (isClassical) modulatedConfig.vibratoDepth * 1.2f else modulatedConfig.vibratoDepth * 0.8f
        val gamakInt = if (isClassical) modulatedConfig.power * 0.9f else 15f
        val murkiInt = if (isClassical) modulatedConfig.softness * 1.1f else 30f

        val ornaments = ornamentationHandler.injectOrnamentations(
            phonemes, config.style, meendInt, gamakInt, murkiInt
        )

        // 4. Plan breathing and rests
        val breaths = breathPlanner.planBreaths(phonemes, modulatedConfig.softness + 30f)

        // 5. Structure phrases
        val phrases = if (phonemes.isNotEmpty()) {
            listOf(
                VocalPhrase(
                    phraseId = UUID.randomUUID().toString(),
                    text = lyrics,
                    startTimeMs = phonemes.first().startTimeMs,
                    durationMs = phonemes.last().startTimeMs + phonemes.last().durationMs - phonemes.first().startTimeMs,
                    phonemes = phonemes,
                    ornaments = ornaments,
                    breaths = breaths,
                    vocalRegister = if (keyMidi > 60) "Head" else "Chest"
                )
            )
        } else {
            emptyList()
        }

        // 6. Validate breath boundaries and range physical parameters
        val validation = validationEngine.validateVocalPlayability(voice, phonemes, breaths)

        // Compile logs for the summary audit report
        val report = StringBuilder().apply {
            append("=================================================================\n")
            append("           SURMAYA AI - AI SINGER EMOTION ENGINE v1.0            \n")
            append("=================================================================\n")
            append("Project ID: $projectId | Vocalist: ${voice.name} (${voice.gender})\n")
            append("Emotion Layer: ${modulatedConfig.emotion} (Vibrato: ${modulatedConfig.vibratoDepth.toInt()}%, Breathiness: ${modulatedConfig.breathiness.toInt()}%)\n")
            append("Pronunciation Engine: ${modulatedConfig.pronunciation} | Vocal Style: ${modulatedConfig.style}\n")
            append("-----------------------------------------------------------------\n\n")
            append("🎶 PHONEME GENERATION:\n")
            append("  - Syllable/Phoneme segments synthesized: ${phonemes.size}\n")
            append("  - Phonetic transcriptions: ${phonemes.take(6).map { "${it.text} [${it.ipaPhoneme}]" }.joinToString(", ")}...\n")
            append("\n")
            append("🎙️ ORNAMENTATION METRIC REPORT:\n")
            append("  - Slide/Meend count: ${ornaments.count { it.type == "Meend" }}\n")
            append("  - Oscillating/Gamak count: ${ornaments.count { it.type == "Gamak" }}\n")
            append("  - Grace notes/Murki count: ${ornaments.count { it.type == "Murki" }}\n")
            append("  - Touch notes/KanSwar count: ${ornaments.count { it.type == "KanSwar" }}\n")
            append("\n")
            append("🫁 RESPIRATORY ANALYSIS:\n")
            append("  - Breath markers planned: ${breaths.size}\n")
            append("  - Mean inhale intensity: ${String.format("%.2f", breaths.map { it.breathIntensity }.average().takeIf { !it.isNaN() } ?: 0.0)}\n")
            append("\n")
            append("🔍 PLAYABILITY AUDIT CHECK: ${if (validation.isValid) "SUCCESS ✅" else "VIOLATION ⚠️"}\n")
            if (validation.warnings.isNotEmpty()) {
                validation.warnings.forEach { append("  ⚠️ Warning: $it\n") }
                append("  💡 Resolution Strategy: ${validation.suggestedFix}\n")
            } else {
                append("  ✅ Physical boundaries are safe. Voice is inside natural vocal registers.\n")
            }
            append("\n=================================================================\n")
        }.toString()

        return VocalSynthesisResult(
            projectId = projectId,
            voiceIdentity = voice,
            config = modulatedConfig,
            phrases = phrases,
            validation = validation,
            offlineFallbackStatus = if (config.voiceId == "pandit_g" || config.voiceId == "ajit") "Offline Ready" else "API Key Required",
            summaryAuditReport = report
        )
    }
}

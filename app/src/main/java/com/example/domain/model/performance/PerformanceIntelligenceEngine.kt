package com.example.domain.model.performance

import java.util.UUID
import kotlin.math.sin
import kotlin.random.Random

class PerformanceIntelligenceEngine {

    // 1. Regional Instrument Knowledge Base
    private val instrumentDatabase = mapOf(
        // Indian Classical & Folk
        "tabla" to InstrumentCapability(
            instrumentId = "tabla", name = "Tabla", range = "N/A (Percussive)",
            timbre = "Resonant, deep bass (bayan) paired with sharp metallic treble (dayan)",
            sustain = "Short to Medium", dynamics = "Extremely Wide (Soft snaps to loud slaps)",
            supportedArticulations = listOf("Ta", "Tin", "Ghe", "Dha", "Dhage", "Keharwa Theka", "Teental Bols")
        ),
        "dholak" to InstrumentCapability(
            instrumentId = "dholak", name = "Dholak", range = "N/A (Percussive)",
            timbre = "Punchy, warm, wood-folk hollow tone",
            sustain = "Short", dynamics = "High Energy",
            supportedArticulations = listOf("Thap", "Giss", "Double Stroke", "Rim Shot", "Bhangra Beat", "Garba Pick")
        ),
        "mridangam" to InstrumentCapability(
            instrumentId = "mridangam", name = "Mridangam", range = "N/A (Percussive)",
            timbre = "Deep metallic ring with precise harmonic frequencies",
            sustain = "Short to Medium", dynamics = "Wide (Fine acoustic touch)",
            supportedArticulations = listOf("Tha", "Dhin", "Num", "Ki", "Te", "Adi Tala patterns")
        ),
        "veena" to InstrumentCapability(
            instrumentId = "veena", name = "Veena", range = "C3 - C6",
            timbre = "Majestic, woody, rich sub-harmonics",
            sustain = "Medium to Long", dynamics = "Expressive",
            supportedArticulations = listOf("Pluck", "Pull (Meend)", "Slide", "Gamak", "Double Pluck", "Sympathetic Resonance")
        ),
        "sitar" to InstrumentCapability(
            instrumentId = "sitar", name = "Sitar", range = "C3 - F6",
            timbre = "Bright, buzzing, shimmering sympathetic harmonics",
            sustain = "Medium to Long", dynamics = "Dynamic Plucking",
            supportedArticulations = listOf("Da-Ra plucking", "Meend (Pitch Pull)", "Gamak (Oscillation)", "Krintan (Hammer-on)", "Chikari ring")
        ),
        "sarangi" to InstrumentCapability(
            instrumentId = "sarangi", name = "Sarangi", range = "G3 - B5",
            timbre = "Emotional, crying, matching human singing voice perfectly",
            sustain = "Infinite (Bowed)", dynamics = "Very Wide",
            supportedArticulations = listOf("Legato bow", "Portamento slide", "Vibrato (heavy)", "Taan", "Gamak")
        ),
        "bansuri" to InstrumentCapability(
            instrumentId = "bansuri", name = "Bansuri (Flute)", range = "E4 - A6",
            timbre = "Airy, breathy, warm, pure organic woodwind",
            sustain = "Breath-limited", dynamics = "Subtle to Breath-intense",
            supportedArticulations = listOf("Slur (Legato)", "Meend (Fingertip slide)", "Murki (Grace notes)", "Double tongue", "Breath flutter")
        ),
        "shehnai" to InstrumentCapability(
            instrumentId = "shehnai", name = "Shehnai", range = "A4 - G6",
            timbre = "Auspicious, double-reed, piercingly nasal, highly emotional",
            sustain = "Breath-limited", dynamics = "Very Loud & Piercing",
            supportedArticulations = listOf("Ceremonial vibrato", "Legato slide", "Staccato alert", "Murki ornament")
        ),
        // Western
        "piano" to InstrumentCapability(
            instrumentId = "piano", name = "Grand Piano", range = "A0 - C8",
            timbre = "Clean, rich, full-frequency, crystalline",
            sustain = "Pedal-extended", dynamics = "Full Range (Pianissimo to Fortissimo)",
            supportedArticulations = listOf("Sustain pedal", "Staccato", "Sostenuto", "Glissando")
        ),
        "guitar" to InstrumentCapability(
            instrumentId = "guitar", name = "Studio Guitar", range = "E2 - E6",
            timbre = "Crisp, intimate acoustic strum or electric lead bite",
            sustain = "Medium", dynamics = "Expressive plucking",
            supportedArticulations = listOf("Strummed chords", "Arpeggiated pluck", "Legato slides", "Hammer-on", "Pull-off", "Palm mute")
        ),
        "bass" to InstrumentCapability(
            instrumentId = "bass", name = "Bass Guitar", range = "E1 - G4",
            timbre = "Thick, warm, sub-bass foundation",
            sustain = "Medium", dynamics = "Steady",
            supportedArticulations = listOf("Fingerstyle pluck", "Slap & Pop", "Muted thud", "Slide down")
        ),
        "violin" to InstrumentCapability(
            instrumentId = "violin", name = "Violin Solo", range = "G3 - A7",
            timbre = "Sweet, expressive, singing orchestral lead",
            sustain = "Infinite (Bowed)", dynamics = "Wide crescendo/decrescendo",
            supportedArticulations = listOf("Legato bow", "Staccato (detached)", "Pizzicato (plucked)", "Tremolo (fast shake)", "Heavy vibrato")
        ),
        // Electronic
        "synth" to InstrumentCapability(
            instrumentId = "synth", name = "Neural Synth", range = "C0 - C9",
            timbre = "Synthetic, buzz-saw, lush retro pads, electric modulation",
            sustain = "ADSR Controlled", dynamics = "Mod-wheel driven",
            supportedArticulations = listOf("Filter sweep", "Arpeggiation", "Portamento slide", "LFO modulation")
        )
    )

    // 2. Instrument Selection Engine
    fun chooseInstruments(style: String, complexity: Float): List<InstrumentCapability> {
        val selected = mutableListOf<InstrumentCapability>()
        
        when {
            style.contains("Bollywood", ignoreCase = true) -> {
                // Classic Bollywood arrangement setup
                selected.add(instrumentDatabase["tabla"] ?: error(""))
                selected.add(instrumentDatabase["bansuri"] ?: error(""))
                selected.add(instrumentDatabase["guitar"] ?: error(""))
                selected.add(instrumentDatabase["piano"] ?: error(""))
                if (complexity > 50f) {
                    selected.add(instrumentDatabase["violin"] ?: error(""))
                }
            }
            style.contains("Classical", ignoreCase = true) || style.contains("Traditional", ignoreCase = true) -> {
                // Sitar & Tanpura classical mood
                selected.add(instrumentDatabase["tabla"] ?: error(""))
                selected.add(instrumentDatabase["sitar"] ?: error(""))
                selected.add(instrumentDatabase["veena"] ?: error(""))
                if (complexity > 60f) {
                    selected.add(instrumentDatabase["bansuri"] ?: error(""))
                }
            }
            style.contains("Fusion", ignoreCase = true) || style.contains("Modern", ignoreCase = true) -> {
                // Fusion: drums, sitar, synth, bass
                selected.add(instrumentDatabase["tabla"] ?: error(""))
                selected.add(instrumentDatabase["sitar"] ?: error(""))
                selected.add(instrumentDatabase["synth"] ?: error(""))
                selected.add(instrumentDatabase["bass"] ?: error(""))
            }
            else -> {
                // Fallback setup
                selected.add(instrumentDatabase["piano"] ?: error(""))
                selected.add(instrumentDatabase["guitar"] ?: error(""))
                selected.add(instrumentDatabase["bass"] ?: error(""))
            }
        }
        return selected
    }

    // 3. Instrument Capability Engine
    fun getCapability(instrumentId: String): InstrumentCapability {
        return instrumentDatabase[instrumentId] ?: InstrumentCapability(
            instrumentId = instrumentId,
            name = instrumentId.replaceFirstChar { it.uppercase() },
            range = "Full Range",
            timbre = "Standard tone",
            sustain = "Medium",
            dynamics = "Standard",
            supportedArticulations = listOf("Sustain")
        )
    }

    // 4. Performance Note Engine
    fun generatePerformanceNotes(
        instrumentId: String,
        config: PerformanceConfiguration,
        section: String
    ): List<PerformanceNote> {
        val notes = mutableListOf<PerformanceNote>()
        val baseMidi = when (instrumentId) {
            "bass" -> 36 // E1/C2 range
            "sitar", "veena" -> 48 // C3 range
            "sarangi" -> 55 // G3 range
            "bansuri" -> 64 // E4 range
            "shehnai" -> 72 // C5 range
            "piano" -> 60 // C4
            "guitar" -> 52 // E3
            "violin" -> 67 // G4
            else -> 60
        }

        // Define a simple key/scale pitch step list
        // Aman/Yaman scale: Sa, Re, Ga, Ma(sharp), Pa, Dha, Ni -> semitones 0, 2, 4, 6, 7, 9, 11, 12
        val scaleIntervals = when (config.scale) {
            "Yaman" -> listOf(0, 2, 4, 6, 7, 9, 11, 12)
            "Bhairav" -> listOf(0, 1, 4, 5, 7, 8, 11, 12)
            "Bhairavi" -> listOf(0, 1, 3, 5, 7, 8, 10, 12)
            else -> listOf(0, 2, 4, 5, 7, 9, 11, 12) // Major
        }

        val r = Random(seed = instrumentId.hashCode().toLong() + section.hashCode())
        val notesCount = when (section.lowercase()) {
            "intro" -> 8
            "verse" -> 16
            "chorus" -> 24
            "ending" -> 6
            else -> 12
        }

        var currentTimeMs = 100L
        val stepDurationMs = (60000 / config.tempo).toLong() // Beat duration

        for (i in 0 until notesCount) {
            val pitchIndex = r.nextInt(scaleIntervals.size)
            val pitchOffset = scaleIntervals[pitchIndex]
            val pitch = baseMidi + pitchOffset

            // Articulation mapping
            val noteArt = when {
                instrumentId == "bansuri" && r.nextFloat() < 0.35 -> "Murki"
                (instrumentId == "sitar" || instrumentId == "veena") && r.nextFloat() < 0.4 -> "Meend"
                (instrumentId == "sarangi" || instrumentId == "violin") && r.nextFloat() < 0.3 -> "Legato"
                instrumentId == "tabla" -> if (r.nextBoolean()) "Ghe" else "Ta"
                else -> "Sustain"
            }

            // Humanized velocities
            val baseVel = config.velocity.toInt()
            val humanizedVelocity = (baseVel + r.nextInt(-10, 11)).coerceIn(1, 127)

            notes.add(
                PerformanceNote(
                    pitch = pitch,
                    startTimeMs = currentTimeMs,
                    durationMs = (stepDurationMs * r.nextDouble(0.7, 1.2)).toLong(),
                    velocity = humanizedVelocity,
                    articulation = noteArt
                )
            )
            currentTimeMs += stepDurationMs
        }

        return notes
    }

    // 5. Articulation Engine
    fun generateArticulations(
        instrumentId: String,
        notes: List<PerformanceNote>
    ): List<ArticulationEvent> {
        val events = mutableListOf<ArticulationEvent>()
        for (note in notes) {
            if (note.articulation != "Sustain") {
                events.add(
                    ArticulationEvent(
                        timeMs = note.startTimeMs,
                        type = note.articulation,
                        intensity = 0.7f + Random.nextFloat() * 0.25f,
                        durationMs = (note.durationMs * 0.5f).toInt()
                    )
                )
            }
        }
        return events
    }

    // 6. Expression Engine (Generates dynamic CC curves)
    fun generateExpressionCurves(
        instrumentId: String,
        notes: List<PerformanceNote>
    ): ExpressionEnvelope {
        val cc1 = mutableListOf<EnvelopePoint>()
        val cc11 = mutableListOf<EnvelopePoint>()
        val vibrato = mutableListOf<EnvelopePoint>()
        val pitchBend = mutableListOf<EnvelopePoint>()

        for (note in notes) {
            val start = note.startTimeMs
            val dur = note.durationMs
            
            // CC1 & CC11 Expression points across the duration of each note
            cc1.add(EnvelopePoint(start, 0.5f + Random.nextFloat() * 0.3f))
            cc1.add(EnvelopePoint(start + dur / 2, 0.7f + Random.nextFloat() * 0.25f))
            cc1.add(EnvelopePoint(start + dur, 0.4f + Random.nextFloat() * 0.2f))

            cc11.add(EnvelopePoint(start, 0.6f + Random.nextFloat() * 0.2f))
            cc11.add(EnvelopePoint(start + dur, 0.8f + Random.nextFloat() * 0.15f))

            if (instrumentId == "sarangi" || instrumentId == "bansuri" || instrumentId == "violin") {
                // Sinuous vibrato curve
                for (step in 0..4) {
                    val progress = step / 4f
                    val time = start + (dur * progress).toLong()
                    val value = 0.3f + 0.4f * sin(progress * Math.PI * 2.0).toFloat()
                    vibrato.add(EnvelopePoint(time, value))
                }
            }

            if (note.articulation == "Meend" || note.articulation == "Slide") {
                // Slide pitch bend envelope
                pitchBend.add(EnvelopePoint(start, 0f))
                pitchBend.add(EnvelopePoint(start + (dur * 0.2).toLong(), 0.1f))
                pitchBend.add(EnvelopePoint(start + (dur * 0.7).toLong(), 0.9f))
                pitchBend.add(EnvelopePoint(start + dur, 1.0f))
            }
        }

        return ExpressionEnvelope(cc1, cc11, vibrato, pitchBend)
    }

    // 7. Humanization Engine
    fun generateHumanization(config: PerformanceConfiguration): HumanizationOffset {
        val r = Random.Default
        val human = config.humanization / 100f // Scale 0f to 1f
        
        // Offset values based on humanization amount
        val timingOffset = (r.nextInt(-15, 16) * human).toInt()
        val velocityOffset = (r.nextInt(-12, 13) * human).toInt()
        val microPitch = (r.nextFloat() * 12f - 6f) * human
        val jitter = 0.05f * human

        return HumanizationOffset(timingOffset, velocityOffset, microPitch, jitter)
    }

    // 8. Performance Validation Engine (Check boundaries and playability limits)
    fun validatePlayability(instrumentId: String, notes: List<PerformanceNote>): PlayabilityCheck {
        if (notes.isEmpty()) return PlayabilityCheck(true, null)

        val cap = getCapability(instrumentId)
        val warnings = mutableListOf<String>()
        var isValid = true
        var limitingFactor: String? = null
        var suggestedFix: String? = null

        // Check 1: Range limits
        if (cap.range != "N/A (Percussive)") {
            val minMidi = when (instrumentId) {
                "bass" -> 28
                "sitar", "veena" -> 45
                "bansuri" -> 58
                "shehnai" -> 64
                "piano" -> 21
                "violin" -> 55
                else -> 36
            }
            val maxMidi = when (instrumentId) {
                "bass" -> 64
                "sitar", "veena" -> 84
                "bansuri" -> 90
                "shehnai" -> 92
                "piano" -> 108
                "violin" -> 100
                else -> 96
            }

            val outOfBoundsNotes = notes.filter { it.pitch < minMidi || it.pitch > maxMidi }
            if (outOfBoundsNotes.isNotEmpty()) {
                isValid = false
                limitingFactor = "Instrument physical octave range limit exceeded"
                suggestedFix = "Transpose notes inside $instrumentId's range (${cap.range})"
                warnings.add("Found ${outOfBoundsNotes.size} notes exceeding acoustic hardware pitch limit of $instrumentId.")
            }
        }

        // Check 2: Breath capacity limits for Wind instruments (Bansuri, Shehnai)
        if (instrumentId == "bansuri" || instrumentId == "shehnai") {
            var consecutiveSustainedMs = 0L
            for (note in notes) {
                consecutiveSustainedMs += note.durationMs
            }
            if (consecutiveSustainedMs > 8000L) { // Over 8 seconds continuous blowing
                isValid = false
                warnings.add("Continuous blowing duration is ${consecutiveSustainedMs / 1000f}s, exceeding maximum breath capacity.")
                if (limitingFactor == null) {
                    limitingFactor = "Human lung capacity limit (wind physics)"
                    suggestedFix = "Insert rests (silence) or break notes for respiratory recovery."
                }
            }
        }

        // Check 3: Hand stretch limits for stringed instruments (Veena, Sitar)
        if (instrumentId == "veena" || instrumentId == "sitar" || instrumentId == "guitar") {
            // Check for large intervals in overlapping or fast notes
            var maxJump = 0
            for (i in 0 until notes.size - 1) {
                val jump = Math.abs(notes[i].pitch - notes[i+1].pitch)
                if (jump > maxJump) maxJump = jump
            }
            if (maxJump > 12) { // Over an octave jump
                isValid = false
                warnings.add("Large fretboard jump of $maxJump semitones may compromise playability.")
                if (limitingFactor == null) {
                    limitingFactor = "Acoustic fretboard finger stretch constraint"
                    suggestedFix = "Distribute wide pitch jumps across separate polyphonic tracks or simplify melody."
                }
            }
        }

        return PlayabilityCheck(isValid, limitingFactor, warnings, suggestedFix)
    }

    // 9. Sample Routing Engine
    fun getSampleMapping(instrumentId: String, format: String): SampleMapping {
        val cleanFormat = format.uppercase()
        val path = when (instrumentId) {
            "tabla" -> "/presets/percussion/Indian_Tabla_Pro.$cleanFormat"
            "dholak" -> "/presets/percussion/Indian_Dholak_Folk.$cleanFormat"
            "bansuri" -> "/presets/winds/Bansuri_Breath_Expressive.$cleanFormat"
            "sitar" -> "/presets/strings/Sitar_Yaman_Sympathetic.$cleanFormat"
            "veena" -> "/presets/strings/Saraswati_Veena_Acoustic.$cleanFormat"
            "sarangi" -> "/presets/strings/Sarangi_Bowed_Emotional.$cleanFormat"
            "piano" -> "/presets/keys/Grand_Concert_Stereo.$cleanFormat"
            "guitar" -> "/presets/strings/Studio_Steel_Guitar.$cleanFormat"
            "violin" -> "/presets/orchestra/Violin_Legato_Solo.$cleanFormat"
            else -> "/presets/synths/Neural_Generator_Default.$cleanFormat"
        }
        val target = when (instrumentId) {
            "tabla", "dholak", "mridangam" -> "Percussion Bus"
            "sitar", "veena", "sarangi", "violin", "guitar" -> "Strings Bus"
            "bansuri", "shehnai" -> "Wind Bus"
            else -> "Melodic Master Bus"
        }

        return SampleMapping(format = cleanFormat, presetPath = path, routingTarget = target)
    }

    // Combine all sub-engines to process a complete performance
    fun generateFullPerformance(
        projectId: String,
        config: PerformanceConfiguration,
        instruments: List<String>,
        section: String,
        format: String
    ): PerformanceIntelligenceResult {
        val tracks = mutableListOf<PerformanceTrack>()
        val summaryBuilder = java.lang.StringBuilder()

        summaryBuilder.append("=================================================================\n")
        summaryBuilder.append("         SURMAYA AI - PERFORMANCE INTELLIGENCE ENGINE v1.0       \n")
        summaryBuilder.append("=================================================================\n")
        summaryBuilder.append("Project ID: $projectId | Section: $section\n")
        summaryBuilder.append("Scale: ${config.scale} in ${config.key} | Tempo: ${config.tempo.toInt()} BPM\n")
        summaryBuilder.append("Performance Preset: ${config.performanceStyle} | Complexity: ${config.complexity.toInt()}%\n")
        summaryBuilder.append("-----------------------------------------------------------------\n\n")

        for (id in instruments) {
            val capability = getCapability(id)
            val notes = generatePerformanceNotes(id, config, section)
            val articulations = generateArticulations(id, notes)
            val expression = generateExpressionCurves(id, notes)
            val humanization = generateHumanization(config)
            val playability = validatePlayability(id, notes)
            val routing = getSampleMapping(id, format)

            tracks.add(
                PerformanceTrack(
                    trackId = UUID.randomUUID().toString(),
                    instrumentId = id,
                    capability = capability,
                    notes = notes,
                    articulations = articulations,
                    expressionEnvelope = expression,
                    humanizationOffset = humanization,
                    playability = playability,
                    routing = routing
                )
            )

            // Compile logs for compositeAuditReport
            summaryBuilder.append("🎻 TRACK: ${capability.name} (${capability.timbre})\n")
            summaryBuilder.append("  - Range: ${capability.range} | Sustain Mode: ${capability.sustain}\n")
            summaryBuilder.append("  - Target Output Format: ${routing.format} | Routing: ${routing.routingTarget}\n")
            summaryBuilder.append("  - Notes Synthesized: ${notes.size} notes | Ornamentations: ${articulations.size} (${articulations.map { it.type }.distinct().joinToString()})\n")
            summaryBuilder.append("  - Humanization Offsets: Shift: ${humanization.timingOffsetMs}ms | Tuning: ${String.format("%.2f", humanization.microPitchOffsetCents)} cents\n")
            summaryBuilder.append("  - Playability Check: ${if (playability.isValid) "PASS ✅" else "WARNING ⚠️ (${playability.limitingFactor})"}\n")
            if (playability.warnings.isNotEmpty()) {
                playability.warnings.forEach { summaryBuilder.append("    ⚠️ Warning: $it\n") }
                summaryBuilder.append("    💡 Suggested Fix: ${playability.suggestedFix}\n")
            }
            summaryBuilder.append("\n")
        }

        summaryBuilder.append("=================================================================\n")
        summaryBuilder.append("    Engine status: STABLE. Ready for AI Singer & Mixing stages.  \n")
        summaryBuilder.append("=================================================================\n")

        return PerformanceIntelligenceResult(
            projectId = projectId,
            config = config,
            tracks = tracks,
            compositeAuditReport = summaryBuilder.toString()
        )
    }
}

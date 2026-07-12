package com.example

import com.example.domain.model.singer.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AISingerEngineTest {

    private val engine = AISingerEngine()

    @Test
    fun testLyricsToPhonemePipeline() {
        val lyrics = "Namaste SurMaya sangeet Sa Re Ga Ma Pa"
        val phonemes = DefaultLyricsToPhonemePipeline().convertLyricsToPhonemes(
            lyrics = lyrics,
            language = "Standard Hindi",
            tempo = 120f,
            baseMidi = 60 // Middle C
        )

        // Verifications
        assertTrue(phonemes.isNotEmpty())
        assertEquals(8, phonemes.size) // 8 distinct words
        
        // Check IPA pronunciation transcription lookup
        val namastePhoneme = phonemes.first { it.text.equals("namaste", ignoreCase = true) }
        assertEquals("nə.məs.teː", namastePhoneme.ipaPhoneme)

        val surmayaPhoneme = phonemes.first { it.text.equals("surmaya", ignoreCase = true) }
        assertEquals("suːr.maː.jaː", surmayaPhoneme.ipaPhoneme)

        // Pitch offsets verification (Sa Re Ga Ma Pa scale mappings)
        assertEquals(60, phonemes[0].pitch) // Sa
        assertEquals(62, phonemes[1].pitch) // Re
        assertEquals(64, phonemes[2].pitch) // Ga
        assertEquals(65, phonemes[3].pitch) // Ma
        assertEquals(67, phonemes[4].pitch) // Pa
    }

    @Test
    fun testVocalOrnamentationHandler() {
        val phonemes = listOf(
            PhonemeSegment("sa", "saː", 0L, 800L, 60, 0.8f),
            PhonemeSegment("re", "reː", 800L, 500L, 62, 0.8f)
        )

        val handler = DefaultVocalOrnamentationHandler()
        val classicalOrnaments = handler.injectOrnamentations(
            phonemes = phonemes,
            style = "Classical Traditional",
            meendIntensity = 100f,
            gamakIntensity = 100f,
            murkiIntensity = 100f
        )

        // Verify that ornaments are successfully generated
        assertTrue(classicalOrnaments.isNotEmpty())
        assertTrue(classicalOrnaments.any { it.type in listOf("Meend", "Gamak", "Murki", "KanSwar") })
    }

    @Test
    fun testBreathPlanningEngine() {
        // Continuous vocal blocks to trigger breath inhalation
        val longVocalPhonemes = List(12) { index ->
            PhonemeSegment("word$index", "ipa", index * 1000L, 1000L, 60, 0.7f)
        }

        val planner = DefaultBreathPlanningEngine()
        val breaths = planner.planBreaths(longVocalPhonemes, breathControl = 50f)

        // Verify that the respiratory limits successfully plan breath breaks
        assertTrue(breaths.isNotEmpty())
        breaths.forEach { breath ->
            assertTrue(breath.durationMs >= 200)
            assertTrue(breath.breathIntensity in 0f..1.1f)
            assertTrue(breath.lungVolumeLeft in 0f..1.1f)
        }
    }

    @Test
    fun testVocalEmotionSynthesizer() {
        val synthesizer = DefaultVocalEmotionSynthesizer()

        // Romantic increases vibrato and breathiness
        val (romVibrato, romBreath) = synthesizer.modulateVocalAttributes("Romantic", 50f, 20f, 75f)
        assertTrue(romVibrato > 50f)
        assertTrue(romBreath > 20f)

        // Sad increases vibrato even more and maximizes breathiness
        val (sadVibrato, sadBreath) = synthesizer.modulateVocalAttributes("Sad", 50f, 20f, 60f)
        assertTrue(sadVibrato > romVibrato)
        assertTrue(sadBreath > romBreath)

        // Devotional / Energetic decreases breathiness (clear strong vocals)
        val (_, energeticBreath) = synthesizer.modulateVocalAttributes("Energetic", 50f, 20f, 90f)
        assertTrue(energeticBreath < 20f)
    }

    @Test
    fun testVocalValidationEngine() {
        val voiceIdentity = VoiceIdentity(
            voiceId = "test_singer",
            name = "Test Vocalist",
            gender = "Male",
            description = "Baritone",
            nativeLanguage = "Hindi",
            minPitch = 48, // C3
            maxPitch = 72  // C5
        )

        val validationEngine = DefaultVocalValidationEngine()

        // 1. Verify safe range passes validation
        val safePhonemes = listOf(
            PhonemeSegment("sa", "saː", 0L, 500L, 60, 0.8f) // 60 is C4, inside [48, 72]
        )
        val safeBreaths = listOf(BreathMarker(500L, 300, 0.5f, 0.8f))
        val safeValidation = validationEngine.validateVocalPlayability(voiceIdentity, safePhonemes, safeBreaths)
        assertTrue(safeValidation.isValid)

        // 2. Verify sub-bass range violation triggers warning
        val lowPhonemes = listOf(
            PhonemeSegment("sa", "saː", 0L, 500L, 40, 0.8f) // 40 is below 48
        )
        val lowValidation = validationEngine.validateVocalPlayability(voiceIdentity, lowPhonemes, safeBreaths)
        assertFalse(lowValidation.isValid)
        assertTrue(lowValidation.limitingFactors.contains("Sub-bass floor range violation"))

        // 3. Verify ceiling range violation triggers warning
        val highPhonemes = listOf(
            PhonemeSegment("sa", "saː", 0L, 500L, 80, 0.8f) // 80 is above 72
        )
        val highValidation = validationEngine.validateVocalPlayability(voiceIdentity, highPhonemes, safeBreaths)
        assertFalse(highValidation.isValid)
        assertTrue(highValidation.limitingFactors.contains("Soprano/Tenor ceiling range violation"))

        // 4. Verify oxygen starvation warning when no breaths are planned
        val longUnbreathingPhonemes = List(10) { index ->
            PhonemeSegment("word", "ipa", index * 1000L, 1000L, 60, 0.7f)
        }
        val unbreathingValidation = validationEngine.validateVocalPlayability(voiceIdentity, longUnbreathingPhonemes, emptyList())
        assertFalse(unbreathingValidation.isValid)
        assertTrue(unbreathingValidation.limitingFactors.contains("Oxygen starvation"))
    }

    @Test
    fun testOrchestratedVocalSynthesis() {
        val config = SingerConfiguration(
            voiceId = "ajit",
            style = "Classical Traditional",
            emotion = "Sad"
        )

        val result = engine.synthesizeVocals(
            projectId = "surmaya-test-vocal-synthesis-99",
            lyrics = "namaste surmaya sangeet dil pyar sa re ga ma pa namaste surmaya sangeet dil pyar sa re ga ma pa namaste surmaya sangeet dil pyar sa re ga ma pa",
            config = config,
            tempo = 100f,
            keyMidi = 60
        )

        // Verify top-level result mappings
        assertEquals("surmaya-test-vocal-synthesis-99", result.projectId)
        assertEquals("Ajit", result.voiceIdentity.name)
        assertEquals("Sad", result.config.emotion)
        assertEquals("Classical Traditional", result.config.style)

        // Verify compiled structural phrases
        assertTrue(result.phrases.isNotEmpty())
        val primaryPhrase = result.phrases.first()
        assertTrue(primaryPhrase.phonemes.isNotEmpty())
        assertTrue(primaryPhrase.ornaments.isNotEmpty())
        assertTrue(primaryPhrase.breaths.isNotEmpty())

        // Verify summary audits contains metadata and correct emojis
        assertTrue(result.summaryAuditReport.contains("SURMAYA AI - AI SINGER EMOTION ENGINE"))
        assertTrue(result.summaryAuditReport.contains("Vocalist: Ajit"))
        assertTrue(result.summaryAuditReport.contains("ORNAMENTATION METRIC REPORT"))
    }
}

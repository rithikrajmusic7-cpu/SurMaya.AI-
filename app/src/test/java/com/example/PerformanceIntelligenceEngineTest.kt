package com.example

import com.example.domain.model.performance.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PerformanceIntelligenceEngineTest {

    private val engine = PerformanceIntelligenceEngine()

    @Test
    fun testInstrumentSelectionEngine() {
        // Bollywood style selects Tabla, Bansuri, Guitar, Piano
        val bollywoodInsts = engine.chooseInstruments("Expressive Bollywood", 40f)
        assertTrue(bollywoodInsts.any { it.instrumentId == "tabla" })
        assertTrue(bollywoodInsts.any { it.instrumentId == "bansuri" })
        assertTrue(bollywoodInsts.any { it.instrumentId == "guitar" })

        // Sitar and Veena should be selected in Classical Traditional
        val classicalInsts = engine.chooseInstruments("Classical Traditional", 80f)
        assertTrue(classicalInsts.any { it.instrumentId == "sitar" })
        assertTrue(classicalInsts.any { it.instrumentId == "veena" })
    }

    @Test
    fun testInstrumentCapabilityEngine() {
        val bansuriCap = engine.getCapability("bansuri")
        assertEquals("Bansuri (Flute)", bansuriCap.name)
        assertEquals("E4 - A6", bansuriCap.range)
        assertTrue(bansuriCap.supportedArticulations.contains("Meend (Fingertip slide)"))
        assertTrue(bansuriCap.supportedArticulations.contains("Murki (Grace notes)"))

        val sitarCap = engine.getCapability("sitar")
        assertEquals("Sitar", sitarCap.name)
        assertTrue(sitarCap.supportedArticulations.contains("Meend (Pitch Pull)"))
    }

    @Test
    fun testPerformanceEngineNotesGeneration() {
        val config = PerformanceConfiguration(
            tempo = 100f,
            scale = "Bhairav",
            key = "D"
        )
        val notes = engine.generatePerformanceNotes("bansuri", config, "Intro")
        
        // Should generate standard intro length notes
        assertTrue(notes.isNotEmpty())
        assertEquals(8, notes.size)
        
        // Frequencies mapped correctly to octave ranges
        notes.forEach { note ->
            assertTrue(note.pitch in 55..95)
            assertTrue(note.velocity in 1..127)
        }
    }

    @Test
    fun testArticulationEngine() {
        val notes = listOf(
            PerformanceNote(60, 0L, 500L, 80, "Murki"),
            PerformanceNote(62, 500L, 500L, 85, "Sustain")
        )
        val events = engine.generateArticulations("bansuri", notes)
        assertEquals(1, events.size)
        assertEquals("Murki", events[0].type)
        assertEquals(0L, events[0].timeMs)
    }

    @Test
    fun testExpressionEngine() {
        val notes = listOf(
            PerformanceNote(60, 0L, 1000L, 80, "Meend")
        )
        val envelope = engine.generateExpressionCurves("sitar", notes)
        
        // Verify CC1 & CC11 points generated
        assertTrue(envelope.cc1Modulation.isNotEmpty())
        assertTrue(envelope.cc11Expression.isNotEmpty())
        // Sitar Meend triggers a pitch bend slide
        assertTrue(envelope.pitchBend.isNotEmpty())
    }

    @Test
    fun testHumanizationEngine() {
        val config = PerformanceConfiguration(humanization = 100f)
        val offset = engine.generateHumanization(config)
        
        // Dynamic offsets generated
        assertTrue(offset.timingOffsetMs in -15..15)
        assertTrue(offset.velocityOffset in -12..12)
        assertTrue(offset.microPitchOffsetCents in -6f..6f)
    }

    @Test
    fun testPlayabilityValidationEngine() {
        // Test wind physical lung breath warnings
        val longNotes = List(15) { index ->
            PerformanceNote(72, index * 1000L, 1000L, 80, "Sustain")
        }
        val bansuriCheck = engine.validatePlayability("bansuri", longNotes)
        assertFalse(bansuriCheck.isValid)
        assertEquals("Human lung capacity limit (wind physics)", bansuriCheck.limitingFactor)
        assertTrue(bansuriCheck.warnings.any { it.contains("breath capacity") })

        // Test pitch jump fretboard warnings
        val jumpNotes = listOf(
            PerformanceNote(48, 0L, 500L, 80, "Sustain"),
            PerformanceNote(80, 500L, 500L, 80, "Sustain") // Over 2 octaves jump
        )
        val sitarCheck = engine.validatePlayability("sitar", jumpNotes)
        assertFalse(sitarCheck.isValid)
        assertEquals("Acoustic fretboard finger stretch constraint", sitarCheck.limitingFactor)
    }

    @Test
    fun testSampleRoutingEngine() {
        val routing = engine.getSampleMapping("tabla", "wav")
        assertEquals("WAV", routing.format)
        assertEquals("/presets/percussion/Indian_Tabla_Pro.WAV", routing.presetPath)
        assertEquals("Percussion Bus", routing.routingTarget)
    }

    @Test
    fun testCompositePerformanceIntelligenceExecution() {
        val config = PerformanceConfiguration(
            tempo = 110f,
            scale = "Yaman",
            key = "C#"
        )
        val result = engine.generateFullPerformance(
            projectId = "test-project-123",
            config = config,
            instruments = listOf("tabla", "bansuri", "sitar"),
            section = "Verse",
            format = "sfz"
        )

        assertEquals("test-project-123", result.projectId)
        assertEquals(3, result.tracks.size)
        assertTrue(result.compositeAuditReport.contains("SURMAYA AI - PERFORMANCE INTELLIGENCE ENGINE"))
        assertTrue(result.compositeAuditReport.contains("TRACK: Tabla"))
        assertTrue(result.compositeAuditReport.contains("TRACK: Sitar"))
    }
}

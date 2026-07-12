package com.example

import com.example.domain.model.mixing.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AIMixingEngineTest {

    private val engine = AIMixingEngine()

    @Test
    fun testTrackAnalyzerSubEngine() {
        val analyzer = DefaultTrackAnalyzer()
        val analysis = analyzer.analyzeTrack(
            trackId = "vocal_1",
            trackName = "Lead Vocal",
            trackType = "Vocal",
            rawSampleSeed = 42
        )

        assertEquals("vocal_1", analysis.trackId)
        assertEquals("Lead Vocal", analysis.trackName)
        assertEquals("Vocal", analysis.trackType)
        assertTrue(analysis.rmsLoudnessDb < 0.0f)
        assertTrue(analysis.peakLoudnessDb < 0.0f)
        assertTrue(analysis.peakLoudnessDb >= analysis.rmsLoudnessDb)
        assertTrue(analysis.stereoWidth in 0.0f..1.0f)
        assertTrue(analysis.midFreqEnergy > 0f)
    }

    @Test
    fun testGainStagingSubEngine() {
        val analysis = TrackAnalysis(
            trackId = "bass_1",
            trackName = "Synth Bass",
            trackType = "Bass",
            rmsLoudnessDb = -12.0f,
            peakLoudnessDb = -1.5f,
            dynamicRange = 10.5f,
            stereoWidth = 0.01f,
            lowFreqEnergy = 90f,
            midFreqEnergy = 8f,
            highFreqEnergy = 2f,
            transientCrispness = 30f
        )

        val stager = DefaultGainStager()
        val gs = stager.stageGain(analysis, headroomTargetDb = 6.0f)

        assertEquals("bass_1", gs.trackId)
        // Trim correction: -18 - (-12) = -6.0f
        assertEquals(-6.0f, gs.recommendedTrimDb, 0.01f)
        assertEquals(-3.0f, gs.targetFaderLevelDb, 0.01f)
        assertTrue(gs.compressionRatio > 1.0f)
        assertTrue(gs.compressionThresholdDb < 0.0f)
    }

    @Test
    fun testEQIntelligenceSubEngine() {
        val analysis = TrackAnalysis(
            trackId = "vocal_1",
            trackName = "Lead Vocal",
            trackType = "Vocal",
            rmsLoudnessDb = -16.0f,
            peakLoudnessDb = -4.0f,
            dynamicRange = 12.0f,
            stereoWidth = 0.12f,
            lowFreqEnergy = 12f,
            midFreqEnergy = 68f,
            highFreqEnergy = 20f,
            transientCrispness = 40f
        )

        val eqIntel = DefaultEQIntelligence()
        val eq = eqIntel.designEQ(analysis, "Bollywood")

        assertEquals("vocal_1", eq.trackId)
        assertEquals(80f, eq.lowCutHz, 0.01f)
        assertTrue(eq.bands.isNotEmpty())
        
        // Should contain lowShelf and highMid bands for vocal
        val hasLowShelf = eq.bands.any { it.bandName == "LowShelf" }
        val hasHighMid = eq.bands.any { it.bandName == "HighMid" }
        assertTrue(hasLowShelf)
        assertTrue(hasHighMid)
    }

    @Test
    fun testSpatialMixerSubEngine() {
        val analysis = TrackAnalysis(
            trackId = "melody_1",
            trackName = "Flute Melody",
            trackType = "Melody",
            rmsLoudnessDb = -18.0f,
            peakLoudnessDb = -6.0f,
            dynamicRange = 12.0f,
            stereoWidth = 0.5f,
            lowFreqEnergy = 5f,
            midFreqEnergy = 65f,
            highFreqEnergy = 30f,
            transientCrispness = 35f
        )

        val spatialMixer = DefaultSpatialMixer()
        val sm = spatialMixer.routeSpatial(analysis, "Classical")

        assertEquals("melody_1", sm.trackId)
        // Melody pan default
        assertEquals(0.25f, sm.pan, 0.01f)
        assertEquals(50f, sm.stereoSpread, 0.01f)
        assertTrue(sm.reverbSendDb < 0.0f)
        assertTrue(sm.delaySendDb < 0.0f)
    }

    @Test
    fun testOrchestratedMixingEngine() {
        val tracks = listOf(
            Pair("Lead Vocal", "Vocal"),
            Pair("Bansuri Flute", "Melody"),
            Pair("Tanpura Chords", "Chord"),
            Pair("Acoustic Bass", "Bass"),
            Pair("Tabla Percussion", "Drum")
        )

        val result = engine.analyzeAndSynthesizeMix(
            projectId = "surmaya-test-mix-77",
            tracks = tracks,
            genreStyle = "Ghazal",
            targetLoudnessLufs = -14.0f
        )

        // Verify top-level mappings
        assertEquals("surmaya-test-mix-77", result.projectId)
        assertEquals("Ghazal", result.blueprint.genreStyle)
        assertEquals(-14.0f, result.blueprint.targetLoudnessLufs, 0.01f)

        // Verify sub-engine integrations are complete
        assertEquals(5, result.trackAnalyses.size)
        assertEquals(5, result.blueprint.gainStagingMap.size)
        assertEquals(5, result.blueprint.eqIntelligenceMap.size)
        assertEquals(5, result.blueprint.spatialMixingMap.size)

        // Verify reference comparison output
        assertEquals("Ghazal Master Standard", result.referenceComparison.referenceName)
        assertTrue(result.referenceComparison.spectralMatchPercentage in 0f..100f)

        // Verify summary mix reports are formatted
        assertTrue(result.explainableMixReport.contains("SURMAYA AI - AI MIXING INTELLIGENCE ENGINE"))
        assertTrue(result.explainableMixReport.contains("📊 MULTI-TRACK WAVEFORM ANALYSIS:"))
        assertTrue(result.explainableMixReport.contains("🎸 INTELLIGENT EQ SLOT DESIGN:"))
    }
}

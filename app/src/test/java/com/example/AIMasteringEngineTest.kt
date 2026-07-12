package com.example

import com.example.domain.model.mastering.*
import com.example.domain.mastering.MasteringResult
import com.example.data.mastering.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AIMasteringEngineTest {

    private val loudnessAnalyzer = DefaultLoudnessAnalyzer()
    private val multibandProcessor = DefaultMultibandProcessor()
    private val stereoEnhancer = DefaultStereoEnhancer()
    private val harmonicExciter = DefaultHarmonicExciter()
    private val truePeakLimiter = DefaultTruePeakLimiter()
    private val ditherEngine = DefaultDitherEngine()
    private val streamingOptimizer = DefaultStreamingOptimizer()
    private val referenceMatcher = DefaultReferenceMatcher()

    private val masteringEngine = AIMasteringEngine(
        loudnessAnalyzer = loudnessAnalyzer,
        multibandProcessor = multibandProcessor,
        stereoEnhancer = stereoEnhancer,
        harmonicExciter = harmonicExciter,
        truePeakLimiter = truePeakLimiter,
        ditherEngine = ditherEngine,
        streamingOptimizer = streamingOptimizer,
        referenceMatcher = referenceMatcher
    )

    @Test
    fun testLoudnessAnalyzer() {
        val dummyData = FloatArray(1024) { 0.05f }
        val lufs = loudnessAnalyzer.analyzeLoudness(dummyData, 44100)
        assertTrue(lufs.integratedLufs < 0f)
        assertTrue(lufs.shortTermLufs < 0f)
        assertTrue(lufs.momentaryLufs < 0f)
        assertTrue(lufs.truePeakDb <= 0f)
    }

    @Test
    fun testTruePeakLimiter() {
        val processed = truePeakLimiter.processLimiting(-1.0f, -14.0f)
        assertTrue(processed.ceilingDb == -1.0f)
        assertTrue(processed.releaseMs > 0f)
    }

    @Test
    fun testStereoWidthAndMonoCompatibility() {
        val metrics = stereoEnhancer.enhanceStereo("Bollywood")
        assertTrue(metrics.correlation in -1.0f..1.0f)
        assertTrue(metrics.stereoWidth >= 0.5f)
        assertTrue(metrics.monoCompatibility in 0f..100f)
    }

    @Test
    fun testMultibandProcessorAndDynamicRange() {
        val bands = multibandProcessor.processMultibandDynamics("Bollywood", -14.0f)
        assertTrue(bands.isNotEmpty())
        assertTrue(bands.any { it.bandName == "Low" })
        assertTrue(bands.all { it.thresholdDb < 0f })
    }

    @Test
    fun testStreamingProfiles() {
        val targetSpotify = streamingOptimizer.optimizeForPlatform("Spotify")
        val targetApple = streamingOptimizer.optimizeForPlatform("Apple Music")
        
        assertEquals("Spotify", targetSpotify.name)
        assertEquals(-14.0f, targetSpotify.targetLufs, 0.01f)
        assertEquals("Apple Music", targetApple.name)
        assertEquals(-16.0f, targetApple.targetLufs, 0.01f)
    }

    @Test
    fun testReferenceMatching() {
        val report = referenceMatcher.matchReference("Bollywood", "Polished & Dynamic", "Multi-lingual")
        assertTrue(report.spectralMatchPercentage in 0f..100f)
        assertTrue(report.dynamicsMatchPercentage in 0f..100f)
        assertTrue(report.recommendations.isNotEmpty())
    }

    @Test
    fun testDitherEngine() {
        val ditherResult16 = ditherEngine.processDither("16 Bit")
        val ditherResult24 = ditherEngine.processDither("24 Bit")
        val ditherResult32 = ditherEngine.processDither("32 Float")

        assertEquals("16 Bit", ditherResult16.bitDepth)
        assertEquals("24 Bit", ditherResult24.bitDepth)
        assertEquals("32 Float", ditherResult32.bitDepth)
        assertTrue(ditherResult16.noiseShaping.isNotEmpty())
    }

    @Test
    fun testOrchestrationMasteringResult() {
        val result = masteringEngine.masterTrack(
            projectId = "project_test",
            genreStyle = "Bollywood",
            targetLoudnessLufs = -14.0f,
            selectedPlatforms = listOf("Spotify", "Apple Music", "YouTube Music")
        )

        assertEquals("project_test", result.projectId)
        assertNotNull(result.blueprint)
        assertNotNull(result.loudnessMetrics)
        assertNotNull(result.referenceMatching)
        assertNotNull(result.explainableReport)
    }
}

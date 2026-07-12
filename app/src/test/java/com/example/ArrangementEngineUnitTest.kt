package com.example

import com.example.data.repository.ArrangementEngineImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArrangementEngineUnitTest {

    @Test
    fun testOrchestrateBasicAndPresets() {
        val engine = ArrangementEngineImpl()
        
        // 1. Test cinematic epic preset
        val blueprintEpic = engine.orchestrate(
            title = "Epic Cinematic Journey",
            songStructureType = "Epic Cinematic",
            chordProgression = null,
            melodyPlan = null,
            prompt = "An epic cinematic trailer scoring with massive orchestra"
        )
        
        assertNotNull(blueprintEpic)
        assertEquals("Epic Cinematic Journey", blueprintEpic.title)
        assertTrue(blueprintEpic.tracks.isNotEmpty())
        assertTrue(blueprintEpic.sections.isNotEmpty())
        
        // Check if cinematic epic instruments are loaded
        val trackNames = blueprintEpic.tracks.map { it.instrumentName }
        assertTrue(trackNames.contains("Epic Strings Ensemble"))
        assertTrue(trackNames.contains("French Horns & Low Brass"))
        
        // Check if transition engine planned transitions
        assertEquals(blueprintEpic.sections.size - 1, blueprintEpic.transitions.size)
        
        // Check if automation points are populated
        assertTrue(blueprintEpic.masterAutomation.isNotEmpty())
        
        // 2. Test 90s Bollywood preset
        val blueprint90s = engine.orchestrate(
            title = "Retro Melodies",
            songStructureType = "Bollywood Pop",
            chordProgression = null,
            melodyPlan = null,
            prompt = "90s Bollywood romance with mandolin and bansuri"
        )
        
        val trackNames90s = blueprint90s.tracks.map { it.instrumentName }
        assertTrue(trackNames90s.contains("Retro Dholak") || trackNames90s.contains("Sitar (Lead)"))
        
        // 3. Check evaluation results
        assertNotNull(blueprintEpic.evaluation)
        assertTrue(blueprintEpic.evaluation.overallQualityScore > 80)
    }
}

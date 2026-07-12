package com.example.domain.model.arrangement

import com.example.domain.model.chord.GeneratedChordProgression
import com.example.domain.model.melody.GeneratedMelodyPlan

/**
 * ArrangementBlueprint represents the core data model of structured section timelines,
 * instrument layers, automation targets, and structural transitions constructed
 * by orchestrating Chord and Melody metadata.
 */
data class ArrangementBlueprint(
    val title: String,
    val genre: String,
    val mood: String,
    val emotion: String,
    val bpm: Int,
    val key: String,
    val scale: String,
    val raga: String,
    val songDurationSeconds: Int,
    val sections: List<ArrangementSection>,
    val tracks: List<InstrumentTrack>,
    val transitions: List<ArrangementTransition>,
    val masterAutomation: List<AutomationLane>,
    val counterMelodies: List<CounterMelody>,
    val evaluation: ArrangementEvaluation,
    val compilationLog: List<String> = emptyList()
)

/**
 * ArrangementEngine orchestrates the translation of Chord/Melody metadata plans
 * into a highly cohesive, production-ready ArrangementBlueprint structured timeline.
 */
interface ArrangementEngine {
    
    /**
     * Translates and orchestrates the input Chord and Melody plans into an ArrangementBlueprint.
     * 
     * @param title The title of the composition.
     * @param songStructureType The structured layout pattern (e.g. "Bollywood Verse-Chorus", "EDM Build-Drop", etc.).
     * @param chordProgression The generated chord progression plan (metadata).
     * @param melodyPlan The generated melody plan (metadata).
     * @param prompt User-defined dynamic prompts/directives to shape the translation.
     * @return The orchestrated ArrangementBlueprint.
     */
    fun orchestrate(
        title: String,
        songStructureType: String,
        chordProgression: GeneratedChordProgression?,
        melodyPlan: GeneratedMelodyPlan?,
        prompt: String = ""
    ): ArrangementBlueprint
}

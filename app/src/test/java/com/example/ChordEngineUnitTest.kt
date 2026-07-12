package com.example

import com.example.data.local.entity.ChordProjectEntity
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.chord.ChordProject
import com.example.domain.model.chord.ChordSegment
import com.example.domain.model.chord.GeneratedChordProgression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChordEngineUnitTest {

    @Test
    fun testChordProjectMapping() {
        // Arrange
        val domainProject = ChordProject(
            id = "proj_test_123",
            title = "Test Harmony",
            createdTimestamp = 1625097600000L,
            updatedTimestamp = 1625097600000L,
            melodyProjectId = "mel_456",
            lyrics = "SurMaya is the best",
            prompt = "Extended minor jazz feel",
            genre = "Classical",
            emotion = "Happy",
            mood = "Excited",
            scale = "C Major",
            raga = "Bhairav",
            bpm = 100,
            chordComplexity = "High",
            currentProgressionJson = "{}"
        )

        // Act - Map to Entity
        val entity = domainProject.toEntity()

        // Assert Entity Fields
        assertEquals("proj_test_123", entity.id)
        assertEquals("Test Harmony", entity.title)
        assertEquals("mel_456", entity.melodyProjectId)
        assertEquals("SurMaya is the best", entity.lyrics)
        assertEquals("Extended minor jazz feel", entity.prompt)
        assertEquals("Classical", entity.genre)
        assertEquals("Happy", entity.emotion)
        assertEquals("Excited", entity.mood)
        assertEquals("C Major", entity.scale)
        assertEquals("Bhairav", entity.raga)
        assertEquals(100, entity.bpm)
        assertEquals("High", entity.chordComplexity)
        assertEquals(1625097600000L, entity.createdTimestamp)
        assertEquals("{}", entity.currentProgressionJson)

        // Act - Map back to Domain
        val domainBack = entity.toDomain()

        // Assert Domain Fields
        assertEquals(domainProject.id, domainBack.id)
        assertEquals(domainProject.title, domainBack.title)
        assertEquals(domainProject.scale, domainBack.scale)
        assertEquals(domainProject.raga, domainBack.raga)
        assertEquals(domainProject.bpm, domainBack.bpm)
        assertEquals(domainProject.chordComplexity, domainBack.chordComplexity)
    }

    @Test
    fun testMidiNoteAndPitchCalculations() {
        // C4 is MIDI 60, should be 261.63 Hz
        val c4Midi = 60
        val c4Pitch = (440.0 * Math.pow(2.0, (c4Midi - 69) / 12.0)).toFloat()
        assertTrue(c4Pitch > 261.0f && c4Pitch < 262.0f)

        // A4 is MIDI 69, should be 440 Hz
        val a4Midi = 69
        val a4Pitch = (440.0 * Math.pow(2.0, (a4Midi - 69) / 12.0)).toFloat()
        assertEquals(440.0f, a4Pitch)
    }

    @Test
    fun testGuitarFretboardRepresentation() {
        // Custom check for chord segments and fingering charts
        val segment = ChordSegment(
            id = "seg_1",
            chordName = "C",
            romanNumeral = "I",
            startTimeBeats = 0.0f,
            durationBeats = 4.0f,
            midiNotes = listOf(60, 64, 67),
            pitchHz = listOf(261.63f, 329.63f, 392.00f),
            noteNames = listOf("C4", "E4", "G4"),
            guitarFingering = "x32010"
        )

        assertEquals("C", segment.chordName)
        assertEquals("x32010", segment.guitarFingering)
        assertEquals(3, segment.midiNotes.size)
    }
}

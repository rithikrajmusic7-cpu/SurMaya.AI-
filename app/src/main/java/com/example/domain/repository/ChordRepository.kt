package com.example.domain.repository

import com.example.domain.model.chord.ChordProject
import com.example.domain.model.chord.GeneratedChordProgression
import com.example.domain.model.chord.ChordHistory
import kotlinx.coroutines.flow.Flow

interface ChordRepository {
    fun getAllProjects(): Flow<List<ChordProject>>
    
    fun getProjectById(id: String): Flow<ChordProject?>
    
    suspend fun createProject(
        title: String,
        melodyProjectId: String?,
        lyrics: String,
        prompt: String,
        genre: String,
        emotion: String,
        mood: String,
        scale: String,
        raga: String,
        bpm: Int,
        chordComplexity: String
    ): ChordProject
    
    suspend fun updateProject(project: ChordProject)
    
    suspend fun deleteProject(id: String)
    
    fun getHistoryForProject(projectId: String): Flow<List<ChordHistory>>
    
    suspend fun saveHistory(projectId: String, description: String, progression: GeneratedChordProgression)
    
    suspend fun generateChordProgression(project: ChordProject): GeneratedChordProgression
    
    suspend fun exportProgression(progression: GeneratedChordProgression, format: String): String
}

package com.example.domain.repository

import com.example.domain.model.melody.MelodyProject
import com.example.domain.model.melody.GeneratedMelodyPlan
import kotlinx.coroutines.flow.Flow

interface MelodyRepository {
    fun getAllProjects(): Flow<List<MelodyProject>>
    
    fun getProjectById(id: String): Flow<MelodyProject?>
    
    suspend fun createProject(
        title: String,
        lyrics: String,
        chords: String,
        prompt: String,
        emotion: String,
        genre: String,
        mood: String,
        scale: String,
        raga: String,
        tempo: Int,
        vocalStyle: String,
        sectionType: String
    ): MelodyProject
    
    suspend fun updateProject(project: MelodyProject)
    
    suspend fun deleteProject(id: String)
    
    suspend fun generateMelody(project: MelodyProject): GeneratedMelodyPlan
    
    suspend fun exportMelody(plan: GeneratedMelodyPlan, format: String): String
}

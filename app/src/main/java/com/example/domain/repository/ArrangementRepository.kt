package com.example.domain.repository

import com.example.domain.model.arrangement.*
import kotlinx.coroutines.flow.Flow

interface ArrangementRepository {
    fun getAllProjects(): Flow<List<ArrangementProject>>
    
    fun getProjectById(id: String): Flow<ArrangementProject?>
    
    suspend fun getProjectByIdSync(id: String): ArrangementProject?
    
    suspend fun createProject(
        title: String,
        lyricsProjectId: String?,
        melodyProjectId: String?,
        chordProjectId: String?,
        lyrics: String,
        prompt: String,
        genre: String,
        mood: String,
        emotion: String,
        bpm: Int,
        key: String,
        scale: String,
        raga: String,
        songDurationSeconds: Int,
        singerType: String,
        language: String,
        targetAudience: String,
        songStructureType: String
    ): ArrangementProject
    
    suspend fun updateProject(project: ArrangementProject)
    
    suspend fun deleteProject(id: String)
    
    fun getSectionsForProject(projectId: String): Flow<List<ArrangementSection>>
    
    suspend fun saveSections(sections: List<ArrangementSection>)
    
    fun getTracksForProject(projectId: String): Flow<List<InstrumentTrack>>
    
    suspend fun saveTracks(tracks: List<InstrumentTrack>)
    
    fun getAutomationLanes(projectId: String): Flow<List<AutomationLane>>
    
    suspend fun saveAutomationLane(lane: AutomationLane)
    
    fun getTransitions(projectId: String): Flow<List<ArrangementTransition>>
    
    suspend fun saveTransition(transition: ArrangementTransition)
    
    fun getCounterMelodies(projectId: String): Flow<List<CounterMelody>>
    
    suspend fun saveCounterMelody(melody: CounterMelody)
    
    fun getHistoryForProject(projectId: String): Flow<List<ArrangementHistory>>
    
    suspend fun saveHistory(projectId: String, description: String, stateJson: String)
    
    fun getAllTemplates(): Flow<List<ArrangementTemplate>>
    
    suspend fun saveTemplate(template: ArrangementTemplate)
    
    fun getEvaluationForProject(projectId: String): Flow<ArrangementEvaluation?>
    
    suspend fun saveEvaluation(evaluation: ArrangementEvaluation)
    
    suspend fun generateArrangement(
        project: ArrangementProject,
        useOfflineAI: Boolean
    ): ArrangementProject
    
    suspend fun exportArrangement(
        project: ArrangementProject,
        format: String
    ): String
}

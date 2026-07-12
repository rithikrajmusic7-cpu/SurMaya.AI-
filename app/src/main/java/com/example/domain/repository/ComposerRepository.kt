package com.example.domain.repository

import com.example.domain.model.composer.ComposerProject
import com.example.domain.model.composer.CompositionVersion
import com.example.domain.model.composer.MasterCompositionPlan
import kotlinx.coroutines.flow.Flow

interface ComposerRepository {
    fun getAllProjects(): Flow<List<ComposerProject>>
    suspend fun getProjectById(id: String): ComposerProject?
    fun getProjectByIdFlow(id: String): Flow<ComposerProject?>
    
    suspend fun createProject(
        title: String,
        lyrics: String,
        language: String,
        genre: String,
        mood: String,
        filmSituation: String,
        era: String,
        productionScale: String,
        emotionalJourney: String,
        instrumentPreferences: String,
        userNotes: String
    ): ComposerProject

    suspend fun updateProject(project: ComposerProject)
    suspend fun deleteProject(id: String)

    suspend fun compileMasterCompositionPlan(project: ComposerProject): Result<MasterCompositionPlan>
    
    fun getVersionsForProject(projectId: String): Flow<List<CompositionVersion>>
    suspend fun getVersionById(id: String): CompositionVersion?
    suspend fun saveVersion(projectId: String, plan: MasterCompositionPlan, lyrics: String, label: String, editSummary: String): CompositionVersion
    suspend fun deleteVersion(id: String)
    suspend fun restoreVersion(projectId: String, version: CompositionVersion): ComposerProject
    suspend fun branchProject(projectId: String, branchName: String, version: CompositionVersion): ComposerProject
    suspend fun duplicateProject(project: ComposerProject): ComposerProject
}

package com.example.domain.repository

import com.example.domain.model.mixing.MixingSynthesisResult
import com.example.domain.model.mixing.MixingBlueprint
import com.example.data.local.entity.MixProjectEntity
import kotlinx.coroutines.flow.Flow

interface MixingRepository {
    fun getAllProjectsFlow(): Flow<List<MixProjectEntity>>
    fun getProjectByIdFlow(id: String): Flow<MixProjectEntity?>
    suspend fun getProjectById(id: String): MixProjectEntity?
    suspend fun createOrUpdateProject(project: MixProjectEntity)
    suspend fun deleteProject(id: String)

    suspend fun getMixingResult(projectId: String): Result<MixingSynthesisResult?>
    
    suspend fun synthesizeMix(
        projectId: String,
        tracks: List<Pair<String, String>>, // list of Pair(TrackName, TrackType)
        genreStyle: String, // "Bollywood", "Classical", "Pop", "EDM", "Ghazal"
        targetLoudnessLufs: Float
    ): Result<MixingSynthesisResult>
    
    suspend fun saveMixingBlueprint(
        projectId: String,
        blueprint: MixingBlueprint
    ): Result<Unit>
}


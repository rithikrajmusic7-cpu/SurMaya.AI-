package com.example.domain.repository

import com.example.data.local.entity.MasteringProjectEntity
import com.example.domain.mastering.MasteringResult
import com.example.domain.model.mastering.MasteringBlueprint
import com.example.domain.model.mastering.ReleaseBlueprint
import kotlinx.coroutines.flow.Flow

interface MasteringRepository {
    fun getAllProjectsFlow(): Flow<List<MasteringProjectEntity>>
    fun getProjectByIdFlow(id: String): Flow<MasteringProjectEntity?>
    suspend fun getProjectById(id: String): MasteringProjectEntity?
    suspend fun createOrUpdateProject(project: MasteringProjectEntity)
    suspend fun deleteProject(id: String)

    suspend fun synthesizeMaster(
        projectId: String,
        genreStyle: String,
        targetLoudnessLufs: Float,
        selectedPlatforms: List<String>
    ): Result<MasteringResult>

    suspend fun createReleasePackage(
        projectId: String,
        title: String,
        artist: String,
        isrc: String,
        upcEan: String,
        masteringBlueprint: MasteringBlueprint,
        masteredLoudnessLufs: Float,
        masteredTruePeakDb: Float
    ): Result<ReleaseBlueprint>
}

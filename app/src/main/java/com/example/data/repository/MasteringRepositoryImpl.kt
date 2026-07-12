package com.example.data.repository

import com.example.data.local.dao.MasteringDao
import com.example.data.local.entity.MasteringProjectEntity
import com.example.domain.mastering.IMasteringEngine
import com.example.domain.mastering.MasteringResult
import com.example.domain.model.mastering.MasteringBlueprint
import com.example.domain.model.mastering.ReleaseBlueprint
import com.example.domain.repository.MasteringRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MasteringRepositoryImpl(
    private val masteringDao: MasteringDao,
    private val masteringEngine: IMasteringEngine = com.example.data.mastering.AIMasteringEngine()
) : MasteringRepository {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val resultAdapter = moshi.adapter(MasteringResult::class.java)
    private val platformsAdapter = moshi.adapter<List<String>>(
        com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
    )

    private val cachedMasters = mutableMapOf<String, MasteringResult>()

    override fun getAllProjectsFlow(): Flow<List<MasteringProjectEntity>> {
        return masteringDao.getAllProjects()
    }

    override fun getProjectByIdFlow(id: String): Flow<MasteringProjectEntity?> {
        return masteringDao.getProjectByIdFlow(id)
    }

    override suspend fun getProjectById(id: String): MasteringProjectEntity? = withContext(Dispatchers.IO) {
        masteringDao.getProjectById(id)
    }

    override suspend fun createOrUpdateProject(project: MasteringProjectEntity) = withContext(Dispatchers.IO) {
        masteringDao.insertProject(project)
    }

    override suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        masteringDao.deleteProjectById(id)
    }

    override suspend fun synthesizeMaster(
        projectId: String,
        genreStyle: String,
        targetLoudnessLufs: Float,
        selectedPlatforms: List<String>
    ): Result<MasteringResult> {
        return withContext(Dispatchers.IO) {
            try {
                val result = masteringEngine.masterTrack(
                    projectId = projectId,
                    genreStyle = genreStyle,
                    targetLoudnessLufs = targetLoudnessLufs,
                    selectedPlatforms = selectedPlatforms
                )
                cachedMasters[projectId] = result

                val existingEntity = masteringDao.getProjectById(projectId)
                val platformsJson = platformsAdapter.toJson(selectedPlatforms) ?: "[]"
                val resultJson = resultAdapter.toJson(result)

                val entity = MasteringProjectEntity(
                    id = projectId,
                    title = existingEntity?.title ?: "Mastering Project",
                    genreStyle = genreStyle,
                    targetLoudnessLufs = targetLoudnessLufs,
                    createdTimestamp = existingEntity?.createdTimestamp ?: System.currentTimeMillis(),
                    updatedTimestamp = System.currentTimeMillis(),
                    selectedPlatformsJson = platformsJson,
                    lastSynthesisResultJson = resultJson
                )
                masteringDao.insertProject(entity)

                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun createReleasePackage(
        projectId: String,
        title: String,
        artist: String,
        isrc: String,
        upcEan: String,
        masteringBlueprint: MasteringBlueprint,
        masteredLoudnessLufs: Float,
        masteredTruePeakDb: Float
    ): Result<ReleaseBlueprint> {
        return withContext(Dispatchers.IO) {
            try {
                val releaseBlueprint = masteringEngine.generateReleasePackage(
                    projectId = projectId,
                    title = title,
                    artist = artist,
                    isrc = isrc,
                    upcEan = upcEan,
                    masteringBlueprint = masteringBlueprint,
                    masteredLoudnessLufs = masteredLoudnessLufs,
                    masteredTruePeakDb = masteredTruePeakDb
                )
                Result.success(releaseBlueprint)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

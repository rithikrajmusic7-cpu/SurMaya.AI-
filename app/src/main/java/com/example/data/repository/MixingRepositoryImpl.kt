package com.example.data.repository

import com.example.data.local.dao.MixingDao
import com.example.data.local.entity.MixProjectEntity
import com.example.domain.model.mixing.AIMixingEngine
import com.example.domain.model.mixing.MixingBlueprint
import com.example.domain.model.mixing.MixingSynthesisResult
import com.example.domain.repository.MixingRepository
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class MoshiTrackPair(val first: String, val second: String)

class MixingRepositoryImpl(
    private val mixingDao: MixingDao,
    private val mixingEngine: AIMixingEngine = AIMixingEngine()
) : MixingRepository {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
        
    private val resultAdapter: com.squareup.moshi.JsonAdapter<MixingSynthesisResult> = moshi.adapter(MixingSynthesisResult::class.java)
    private val tracksAdapter: com.squareup.moshi.JsonAdapter<List<MoshiTrackPair>> = moshi.adapter(com.squareup.moshi.Types.newParameterizedType(List::class.java, MoshiTrackPair::class.java))

    private val cachedMixes = mutableMapOf<String, MixingSynthesisResult>()

    override fun getAllProjectsFlow(): Flow<List<MixProjectEntity>> {
        return mixingDao.getAllProjects()
    }

    override fun getProjectByIdFlow(id: String): Flow<MixProjectEntity?> {
        return mixingDao.getProjectByIdFlow(id)
    }

    override suspend fun getProjectById(id: String): MixProjectEntity? = withContext(Dispatchers.IO) {
        mixingDao.getProjectById(id)
    }

    override suspend fun createOrUpdateProject(project: MixProjectEntity) = withContext(Dispatchers.IO) {
        mixingDao.insertProject(project)
    }

    override suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        mixingDao.deleteProjectById(id)
    }

    override suspend fun getMixingResult(projectId: String): Result<MixingSynthesisResult?> {
        return withContext(Dispatchers.IO) {
            try {
                val cached = cachedMixes[projectId]
                if (cached != null) {
                    Result.success(cached)
                } else {
                    val entity = mixingDao.getProjectById(projectId)
                    val result = entity?.lastSynthesisResultJson?.let {
                        try {
                            resultAdapter.fromJson(it)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (result != null) {
                        cachedMixes[projectId] = result
                    }
                    Result.success(result)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun synthesizeMix(
        projectId: String,
        tracks: List<Pair<String, String>>,
        genreStyle: String,
        targetLoudnessLufs: Float
    ): Result<MixingSynthesisResult> {
        return withContext(Dispatchers.IO) {
            try {
                // Generate synthesis using our AMIE Engine
                val result = mixingEngine.analyzeAndSynthesizeMix(
                    projectId = projectId,
                    tracks = tracks,
                    genreStyle = genreStyle,
                    targetLoudnessLufs = targetLoudnessLufs
                )
                cachedMixes[projectId] = result

                // Persist the updated state to the DB as well
                val existingEntity = mixingDao.getProjectById(projectId)
                val moshiTracks = tracks.map { MoshiTrackPair(it.first, it.second) }
                val tracksJson = tracksAdapter.toJson(moshiTracks) ?: "[]"
                val resultJson = resultAdapter.toJson(result)

                val entity = MixProjectEntity(
                    id = projectId,
                    title = existingEntity?.title ?: "Mix Project",
                    genreStyle = genreStyle,
                    targetLoudnessLufs = targetLoudnessLufs,
                    createdTimestamp = existingEntity?.createdTimestamp ?: System.currentTimeMillis(),
                    updatedTimestamp = System.currentTimeMillis(),
                    trackNamesAndTypesJson = tracksJson,
                    lastSynthesisResultJson = resultJson
                )
                mixingDao.insertProject(entity)

                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun saveMixingBlueprint(
        projectId: String,
        blueprint: MixingBlueprint
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val existing = cachedMixes[projectId]
                val existingEntity = mixingDao.getProjectById(projectId)

                if (existing != null) {
                    val updatedResult = existing.copy(blueprint = blueprint)
                    cachedMixes[projectId] = updatedResult

                    if (existingEntity != null) {
                        val resultJson = resultAdapter.toJson(updatedResult)
                        val updatedEntity = existingEntity.copy(
                            genreStyle = blueprint.genreStyle,
                            targetLoudnessLufs = blueprint.targetLoudnessLufs,
                            updatedTimestamp = System.currentTimeMillis(),
                            lastSynthesisResultJson = resultJson
                        )
                        mixingDao.insertProject(updatedEntity)
                    }
                } else if (existingEntity != null) {
                    // Re-synthesize or try to parse
                    val parsedResult = existingEntity.lastSynthesisResultJson?.let {
                        try { resultAdapter.fromJson(it) } catch (e: Exception) { null }
                    }
                    if (parsedResult != null) {
                        val updatedResult = parsedResult.copy(blueprint = blueprint)
                        cachedMixes[projectId] = updatedResult
                        val resultJson = resultAdapter.toJson(updatedResult)
                        val updatedEntity = existingEntity.copy(
                            genreStyle = blueprint.genreStyle,
                            targetLoudnessLufs = blueprint.targetLoudnessLufs,
                            updatedTimestamp = System.currentTimeMillis(),
                            lastSynthesisResultJson = resultJson
                        )
                        mixingDao.insertProject(updatedEntity)
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

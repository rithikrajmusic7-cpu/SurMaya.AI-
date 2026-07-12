package com.example.data.remote.gateway.voice

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class VoiceModelManagerImpl(private val context: Context) : VoiceModelManager {

    private val metaFile = File(context.filesDir, "custom_vocal_models.json")
    private val modelsCache = mutableMapOf<String, VoiceModel>()

    init {
        loadModelsFromDisk()
    }

    private fun loadModelsFromDisk() {
        if (!metaFile.exists()) {
            return
        }
        try {
            val jsonStr = metaFile.readText()
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val model = VoiceModel(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    description = obj.getString("description"),
                    providerId = obj.getString("providerId"),
                    isFavorite = obj.optBoolean("isFavorite", false),
                    creationDate = obj.optLong("creationDate", System.currentTimeMillis()),
                    language = obj.optString("language", "Hindi-English"),
                    accent = obj.optString("accent", "Indian Standard"),
                    gender = obj.optString("gender", "Neutral"),
                    modelUrl = obj.optString("modelUrl", null),
                    isTrained = obj.optBoolean("isTrained", false),
                    fileSizeBytes = obj.optLong("fileSizeBytes", 0)
                )
                modelsCache[model.id] = model
            }
            Log.d("VoiceModelManager", "Loaded ${modelsCache.size} vocal models from persistence.")
        } catch (e: Exception) {
            Log.e("VoiceModelManager", "Failed loading voice configurations", e)
        }
    }

    private fun saveModelsToDisk() = synchronized(this) {
        try {
            val array = JSONArray()
            for (model in modelsCache.values) {
                val obj = JSONObject().apply {
                    put("id", model.id)
                    put("name", model.name)
                    put("description", model.description)
                    put("providerId", model.providerId)
                    put("isFavorite", model.isFavorite)
                    put("creationDate", model.creationDate)
                    put("language", model.language)
                    put("accent", model.accent)
                    put("gender", model.gender)
                    put("modelUrl", model.modelUrl)
                    put("isTrained", model.isTrained)
                    put("fileSizeBytes", model.fileSizeBytes)
                }
                array.put(obj)
            }
            metaFile.writeText(array.toString(2))
        } catch (e: Exception) {
            Log.e("VoiceModelManager", "Error saving voice settings to file", e)
        }
    }

    override suspend fun saveModel(model: VoiceModel): Result<Unit> = withContext(Dispatchers.IO) {
        modelsCache[model.id] = model
        saveModelsToDisk()
        Result.success(Unit)
    }

    override suspend fun deleteModel(modelId: String): Result<Unit> = withContext(Dispatchers.IO) {
        modelsCache.remove(modelId)
        saveModelsToDisk()
        Result.success(Unit)
    }

    override suspend fun renameModel(modelId: String, newName: String): Result<Unit> = withContext(Dispatchers.IO) {
        val model = modelsCache[modelId] ?: return@withContext Result.failure(VoiceException.ModelNotFound(modelId))
        modelsCache[modelId] = model.copy(name = newName)
        saveModelsToDisk()
        Result.success(Unit)
    }

    override suspend fun getModel(modelId: String): Result<VoiceModel?> {
        return Result.success(modelsCache[modelId])
    }

    override suspend fun getAllModels(): List<VoiceModel> {
        return modelsCache.values.toList()
    }

    override suspend fun toggleFavorite(modelId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val model = modelsCache[modelId] ?: return@withContext Result.failure(VoiceException.ModelNotFound(modelId))
        modelsCache[modelId] = model.copy(isFavorite = !model.isFavorite)
        saveModelsToDisk()
        Result.success(Unit)
    }
}

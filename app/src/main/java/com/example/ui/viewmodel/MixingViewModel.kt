package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.audio.export.*
import com.example.di.ServiceLocator
import com.example.data.local.entity.MixProjectEntity
import com.example.domain.model.mixing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MixingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ServiceLocator.getMixingRepository(application)
    private val aiGateway = com.example.data.remote.gateway.AIGateway.getInstance(application)

    private fun mapTrackIdToChannel(trackId: String): Int {
        return when (trackId) {
            "track_1" -> 3 // Vocal
            "track_2" -> 0 // Melody (melody_synth)
            "track_3" -> 2 // Chords/Sampler
            "track_5" -> 1 // Tabla/Percussion
            else -> -1
        }
    }

    // --- State Streams ---
    private val _projects = MutableStateFlow<List<MixProjectEntity>>(emptyList())
    val projects: StateFlow<List<MixProjectEntity>> = _projects

    private val _currentProject = MutableStateFlow<MixProjectEntity?>(null)
    val currentProject: StateFlow<MixProjectEntity?> = _currentProject

    private val _synthesisResult = MutableStateFlow<MixingSynthesisResult?>(null)
    val synthesisResult: StateFlow<MixingSynthesisResult?> = _synthesisResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // --- Selected state variables for creation ---
    val newProjectTitle = MutableStateFlow("Naya Sangeet Mix")
    val selectedGenre = MutableStateFlow("Bollywood") // "Bollywood", "Classical", "Pop", "EDM", "Ghazal"
    val targetLoudness = MutableStateFlow(-14.0f) // -24 to -6 dB LUFS
    val masterFaderLevelDb = MutableStateFlow(0.0f)

    init {
        // Observe all mixing projects from Room database
        viewModelScope.launch {
            repository.getAllProjectsFlow().collect { list ->
                _projects.value = list
                // If there are projects and we haven't selected one, select the first one automatically
                if (list.isNotEmpty() && _currentProject.value == null) {
                    selectProject(list.first().id)
                }
            }
        }
    }

    fun selectProject(projectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val project = repository.getProjectById(projectId)
                _currentProject.value = project
                
                if (project != null) {
                    val resultRes = repository.getMixingResult(projectId)
                    _synthesisResult.value = resultRes.getOrNull()
                } else {
                    _synthesisResult.value = null
                }
            } catch (e: Exception) {
                _error.value = "Failed to load project: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createProject(title: String, tracks: List<Pair<String, String>>, genre: String, loudness: Float) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val projectId = UUID.randomUUID().toString()
                
                // Helper MoshiTrackPair mapping is done automatically in MixingRepositoryImpl
                val entity = MixProjectEntity(
                    id = projectId,
                    title = title.ifBlank { "Untitled Mix" },
                    genreStyle = genre,
                    targetLoudnessLufs = loudness,
                    createdTimestamp = System.currentTimeMillis(),
                    updatedTimestamp = System.currentTimeMillis(),
                    trackNamesAndTypesJson = "[]",
                    lastSynthesisResultJson = null
                )
                repository.createOrUpdateProject(entity)
                
                // Synthesize the mix immediately to populate recommendations
                val synthResult = repository.synthesizeMix(
                    projectId = projectId,
                    tracks = tracks,
                    genreStyle = genre,
                    targetLoudnessLufs = loudness
                )
                
                if (synthResult.isSuccess) {
                    _synthesisResult.value = synthResult.getOrNull()
                    _currentProject.value = repository.getProjectById(projectId)
                } else {
                    _error.value = "Synthesis failed: ${synthResult.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to create project: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun synthesizeCurrentMix(genreStyle: String, targetLoudnessLufs: Float) {
        val current = _currentProject.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Parse existing tracks if any, or default to standard multitrack
                val tracks = listOf(
                    Pair("Lead Vocal", "Vocal"),
                    Pair("Bansuri Flute", "Melody"),
                    Pair("Tanpura Chords", "Chord"),
                    Pair("Acoustic Bass", "Bass"),
                    Pair("Tabla Percussion", "Drum")
                )

                val synthResult = repository.synthesizeMix(
                    projectId = current.id,
                    tracks = tracks,
                    genreStyle = genreStyle,
                    targetLoudnessLufs = targetLoudnessLufs
                )

                if (synthResult.isSuccess) {
                    _synthesisResult.value = synthResult.getOrNull()
                    _currentProject.value = repository.getProjectById(current.id)
                } else {
                    _error.value = "Re-synthesis failed: ${synthResult.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Synthesis error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFaderLevel(trackId: String, newFaderDb: Float) {
        val currentResult = _synthesisResult.value ?: return
        val blueprint = currentResult.blueprint
        val currentGainMap = blueprint.gainStagingMap.toMutableMap()
        val currentGain = currentGainMap[trackId] ?: return

        currentGainMap[trackId] = currentGain.copy(targetFaderLevelDb = newFaderDb)
        val updatedBlueprint = blueprint.copy(gainStagingMap = currentGainMap)

        saveUpdatedBlueprint(updatedBlueprint)

        // Trigger real-time JNI mixer volume update
        val channelIndex = mapTrackIdToChannel(trackId)
        if (channelIndex >= 0) {
            aiGateway.setChannelVolume(channelIndex, newFaderDb)
        }
    }

    fun updatePan(trackId: String, newPan: Float) {
        val currentResult = _synthesisResult.value ?: return
        val blueprint = currentResult.blueprint
        val currentSpatialMap = blueprint.spatialMixingMap.toMutableMap()
        val currentSpatial = currentSpatialMap[trackId] ?: return

        currentSpatialMap[trackId] = currentSpatial.copy(pan = newPan)
        val updatedBlueprint = blueprint.copy(spatialMixingMap = currentSpatialMap)

        saveUpdatedBlueprint(updatedBlueprint)

        // Trigger real-time JNI mixer panning update
        val channelIndex = mapTrackIdToChannel(trackId)
        if (channelIndex >= 0) {
            aiGateway.setChannelPan(channelIndex, newPan)
        }
    }

    fun updateStereoWidth(trackId: String, newWidth: Float) {
        val currentResult = _synthesisResult.value ?: return
        val blueprint = currentResult.blueprint
        val currentSpatialMap = blueprint.spatialMixingMap.toMutableMap()
        val currentSpatial = currentSpatialMap[trackId] ?: return

        currentSpatialMap[trackId] = currentSpatial.copy(stereoSpread = newWidth)
        val updatedBlueprint = blueprint.copy(spatialMixingMap = currentSpatialMap)

        saveUpdatedBlueprint(updatedBlueprint)
    }

    fun updateEQBandGain(trackId: String, bandName: String, newGainDb: Float) {
        val currentResult = _synthesisResult.value ?: return
        val blueprint = currentResult.blueprint
        val currentEQMap = blueprint.eqIntelligenceMap.toMutableMap()
        val currentEQ = currentEQMap[trackId] ?: return

        val updatedBands = currentEQ.bands.map { band ->
            if (band.bandName == bandName) band.copy(gainDb = newGainDb) else band
        }
        currentEQMap[trackId] = currentEQ.copy(bands = updatedBands)
        val updatedBlueprint = blueprint.copy(eqIntelligenceMap = currentEQMap)

        saveUpdatedBlueprint(updatedBlueprint)

        // Trigger real-time JNI mixer EQ gain update
        val channelIndex = mapTrackIdToChannel(trackId)
        if (channelIndex >= 0) {
            aiGateway.setChannelEQ(channelIndex, newGainDb)
        }
    }

    fun updateAuxSends(trackId: String, reverbSendDb: Float, delaySendDb: Float) {
        val currentResult = _synthesisResult.value ?: return
        val blueprint = currentResult.blueprint
        val currentSpatialMap = blueprint.spatialMixingMap.toMutableMap()
        val currentSpatial = currentSpatialMap[trackId] ?: return

        currentSpatialMap[trackId] = currentSpatial.copy(reverbSendDb = reverbSendDb, delaySendDb = delaySendDb)
        val updatedBlueprint = blueprint.copy(spatialMixingMap = currentSpatialMap)

        saveUpdatedBlueprint(updatedBlueprint)

        // Trigger real-time JNI mixer aux sends update
        val channelIndex = mapTrackIdToChannel(trackId)
        if (channelIndex >= 0) {
            aiGateway.setChannelAuxSends(channelIndex, reverbSendDb, delaySendDb)
        }
    }

    fun updateMasterFader(masterDb: Float) {
        masterFaderLevelDb.value = masterDb
        // Trigger real-time JNI master fader update
        aiGateway.setMasterFader(masterDb)
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_currentProject.value?.id == projectId) {
                _currentProject.value = null
                _synthesisResult.value = null
            }
        }
    }

    // --- Export Engine Integration ---
    val exportEngine = com.example.core.audio.export.AudioExportEngine.getInstance(application)
    val exportStatus: StateFlow<ExportStatus> = exportEngine.exportStatus

    fun startExport(format: com.example.core.audio.export.ExportFormat) {
        val currentResult = _synthesisResult.value ?: return
        val blueprint = currentResult.blueprint
        
        val volumes = blueprint.gainStagingMap.mapValues { it.value.targetFaderLevelDb }
        val pans = blueprint.spatialMixingMap.mapValues { it.value.pan }
        val eqGains = blueprint.eqIntelligenceMap.mapValues { entry ->
            entry.value.bands.firstOrNull()?.gainDb ?: 0.0f
        }
        val reverbSends = blueprint.spatialMixingMap.mapValues { it.value.reverbSendDb }
        val delaySends = blueprint.spatialMixingMap.mapValues { it.value.delaySendDb }

        val metadata = ExportMetadata(
            title = _currentProject.value?.title ?: "SurMaya Mix",
            artist = "SurMaya Maestro",
            album = "SurMaya Session Mix",
            genre = blueprint.genreStyle
        )

        exportEngine.startExport(
            format = format,
            metadata = metadata,
            durationSec = 20, // 20 seconds multi-track bounce down
            channelVolumes = volumes,
            channelPans = pans,
            channelEQGains = eqGains,
            channelReverbSends = reverbSends,
            channelDelaySends = delaySends,
            masterFaderDb = masterFaderLevelDb.value
        )
    }

    fun pauseExport() {
        exportEngine.pauseExport()
    }

    fun resumeExport() {
        exportEngine.resumeExport()
    }

    fun cancelExport() {
        exportEngine.cancelExport()
    }

    fun clearError() {
        _error.value = null
    }

    private fun saveUpdatedBlueprint(blueprint: MixingBlueprint) {
        viewModelScope.launch {
            repository.saveMixingBlueprint(blueprint.projectId, blueprint)
            // Reload the local state from cached repository result
            val resultRes = repository.getMixingResult(blueprint.projectId)
            _synthesisResult.value = resultRes.getOrNull()
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MixingViewModel::class.java)) {
                return MixingViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

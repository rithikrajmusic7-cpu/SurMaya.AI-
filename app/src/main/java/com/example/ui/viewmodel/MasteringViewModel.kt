package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.audio.export.*
import com.example.data.local.entity.MasteringProjectEntity
import com.example.di.ServiceLocator
import com.example.domain.mastering.MasteringResult
import com.example.domain.model.mastering.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MasteringViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ServiceLocator.getMasteringRepository(application)

    // --- State Streams ---
    private val _projects = MutableStateFlow<List<MasteringProjectEntity>>(emptyList())
    val projects: StateFlow<List<MasteringProjectEntity>> = _projects

    private val _currentProject = MutableStateFlow<MasteringProjectEntity?>(null)
    val currentProject: StateFlow<MasteringProjectEntity?> = _currentProject

    private val _masteringResult = MutableStateFlow<MasteringResult?>(null)
    val masteringResult: StateFlow<MasteringResult?> = _masteringResult

    private val _releaseBlueprint = MutableStateFlow<ReleaseBlueprint?>(null)
    val releaseBlueprint: StateFlow<ReleaseBlueprint?> = _releaseBlueprint

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // --- Adjustable Real-Time Mastering Params ---
    val targetLoudness = MutableStateFlow(-14.0f)
    val selectedPlatforms = MutableStateFlow(listOf("Spotify", "Apple Music", "YouTube Music"))
    val exciterIntensity = MutableStateFlow(0.5f)
    val limiterCeiling = MutableStateFlow(-1.0f)
    val stereoWidthMultiplier = MutableStateFlow(1.0f)
    val ditherBitDepth = MutableStateFlow("24 Bit")

    init {
        // Observe all projects
        viewModelScope.launch {
            repository.getAllProjectsFlow().collect { list ->
                _projects.value = list
                if (list.isNotEmpty() && _currentProject.value == null) {
                    selectProject(list.first().id)
                }
            }
        }
    }

    fun selectProject(projectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val project = repository.getProjectById(projectId)
                _currentProject.value = project
                _releaseBlueprint.value = null // reset release state
                
                if (project != null) {
                    targetLoudness.value = project.targetLoudnessLufs
                    
                    // Parse selected platforms
                    val platforms = try {
                        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                        val adapter = moshi.adapter<List<String>>(
                            com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
                        )
                        adapter.fromJson(project.selectedPlatformsJson) ?: listOf("Spotify", "Apple Music", "YouTube Music")
                    } catch (e: Exception) {
                        listOf("Spotify", "Apple Music", "YouTube Music")
                    }
                    selectedPlatforms.value = platforms

                    // Load last master result if available
                    val result = project.lastSynthesisResultJson?.let {
                        try {
                            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                            val adapter = moshi.adapter(MasteringResult::class.java)
                            adapter.fromJson(it)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _masteringResult.value = result

                    // Initialize sliders from existing blueprint if loaded
                    result?.blueprint?.let { bp ->
                        exciterIntensity.value = bp.exciterSettings.saturationAmount / 100.0f
                        limiterCeiling.value = bp.limiterSettings.ceilingDb
                        stereoWidthMultiplier.value = bp.stereoSettings.stereoWidth / 1.25f
                        ditherBitDepth.value = bp.ditherSettings.bitDepth
                    }
                } else {
                    _masteringResult.value = null
                }
            } catch (e: Exception) {
                _error.value = "Failed to load project: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createProject(title: String, genre: String, targetLufs: Float, platforms: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _releaseBlueprint.value = null
            try {
                val projectId = UUID.randomUUID().toString()
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                val adapter = moshi.adapter<List<String>>(
                    com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
                )
                val platformsJson = adapter.toJson(platforms) ?: "[]"

                val entity = MasteringProjectEntity(
                    id = projectId,
                    title = title.ifBlank { "Untitled Master" },
                    genreStyle = genre,
                    targetLoudnessLufs = targetLufs,
                    createdTimestamp = System.currentTimeMillis(),
                    updatedTimestamp = System.currentTimeMillis(),
                    selectedPlatformsJson = platformsJson,
                    lastSynthesisResultJson = null
                )
                repository.createOrUpdateProject(entity)

                // Synthesize initial master
                val synthResult = repository.synthesizeMaster(
                    projectId = projectId,
                    genreStyle = genre,
                    targetLoudnessLufs = targetLufs,
                    selectedPlatforms = platforms
                )

                if (synthResult.isSuccess) {
                    _masteringResult.value = synthResult.getOrNull()
                    _currentProject.value = repository.getProjectById(projectId)
                } else {
                    _error.value = "Mastering failed: ${synthResult.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to create mastering project: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun remasterCurrentTrack() {
        val current = _currentProject.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val synthResult = repository.synthesizeMaster(
                    projectId = current.id,
                    genreStyle = current.genreStyle,
                    targetLoudnessLufs = targetLoudness.value,
                    selectedPlatforms = selectedPlatforms.value
                )

                if (synthResult.isSuccess) {
                    // Inject user real-time customized adjustments to make UI interactive
                    val baseResult = synthResult.getOrNull()!!
                    val adjustedResult = baseResult.copy(
                        blueprint = baseResult.blueprint.copy(
                            exciterSettings = baseResult.blueprint.exciterSettings.copy(
                                saturationAmount = exciterIntensity.value * 100.0f
                            ),
                            limiterSettings = baseResult.blueprint.limiterSettings.copy(
                                ceilingDb = limiterCeiling.value
                            ),
                            stereoSettings = baseResult.blueprint.stereoSettings.copy(
                                stereoWidth = stereoWidthMultiplier.value * 1.25f,
                                monoCompatibility = if (stereoWidthMultiplier.value > 1.3f) 68.0f else 88.0f
                            ),
                            ditherSettings = baseResult.blueprint.ditherSettings.copy(
                                bitDepth = ditherBitDepth.value
                            )
                        )
                    )
                    
                    _masteringResult.value = adjustedResult
                    
                    // Save back modified version
                    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                    val adapter = moshi.adapter(MasteringResult::class.java)
                    val resultJson = adapter.toJson(adjustedResult)
                    
                    val updatedEntity = current.copy(
                        targetLoudnessLufs = targetLoudness.value,
                        updatedTimestamp = System.currentTimeMillis(),
                        lastSynthesisResultJson = resultJson
                    )
                    repository.createOrUpdateProject(updatedEntity)
                    _currentProject.value = updatedEntity
                } else {
                    _error.value = "Remastering failed: ${synthResult.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Error during remastering: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateRelease(title: String, artist: String, isrc: String, upc: String) {
        val currentResult = _masteringResult.value ?: return
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val releaseRes = repository.createReleasePackage(
                    projectId = currentResult.projectId,
                    title = title,
                    artist = artist,
                    isrc = isrc,
                    upcEan = upc,
                    masteringBlueprint = currentResult.blueprint,
                    masteredLoudnessLufs = currentResult.loudnessMetrics.integratedLufs,
                    masteredTruePeakDb = currentResult.blueprint.limiterSettings.ceilingDb
                )
                if (releaseRes.isSuccess) {
                    _releaseBlueprint.value = releaseRes.getOrNull()
                } else {
                    _error.value = "Release Package builder failed: ${releaseRes.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to build release: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun togglePlatform(platform: String) {
        val currentList = selectedPlatforms.value.toMutableList()
        if (currentList.contains(platform)) {
            currentList.remove(platform)
        } else {
            currentList.add(platform)
        }
        selectedPlatforms.value = currentList
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_currentProject.value?.id == projectId) {
                _currentProject.value = null
                _masteringResult.value = null
                _releaseBlueprint.value = null
            }
        }
    }

    // --- Export Engine Integration ---
    private val exportEngine = com.example.core.audio.export.AudioExportEngine.getInstance(application)
    val exportStatus: StateFlow<ExportStatus> = exportEngine.exportStatus

    fun startExport(format: com.example.core.audio.export.ExportFormat, title: String, artist: String, genre: String, isrc: String, upc: String) {
        val metadata = ExportMetadata(
            title = title,
            artist = artist,
            genre = genre,
            isrc = isrc,
            upc = upc
        )
        // Mastering suite runs an offline bounce with limiting applied
        exportEngine.startExport(
            format = format,
            metadata = metadata,
            durationSec = 20, // High fidelity 20 sec master deliverable package preview
            masterFaderDb = 0.0f
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

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MasteringViewModel::class.java)) {
                return MasteringViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

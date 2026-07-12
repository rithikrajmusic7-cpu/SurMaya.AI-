package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.ServiceLocator
import com.example.domain.model.arrangement.*
import com.example.domain.repository.ArrangementRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class ArrangementGenerationState {
    IDLE, GENERATING, SUCCESS, ERROR
}

class ArrangementViewModel(
    application: Application,
    private val repository: ArrangementRepository
) : AndroidViewModel(application) {

    private val _generationState = MutableStateFlow(ArrangementGenerationState.IDLE)
    val generationState: StateFlow<ArrangementGenerationState> = _generationState.asStateFlow()

    private val _currentProject = MutableStateFlow<ArrangementProject?>(null)
    val currentProject: StateFlow<ArrangementProject?> = _currentProject.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0.0f) // 0.0 to 1.0
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _playbackTimeSeconds = MutableStateFlow(0f)
    val playbackTimeSeconds: StateFlow<Float> = _playbackTimeSeconds.asStateFlow()

    private val _activeSectionIndex = MutableStateFlow(-1)
    val activeSectionIndex: StateFlow<Int> = _activeSectionIndex.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _exportedText = MutableStateFlow("")
    val exportedText: StateFlow<String> = _exportedText.asStateFlow()

    // Integrations
    private val _lyricsProjects = MutableStateFlow<List<com.example.domain.model.Lyrics>>(emptyList())
    val lyricsProjects: StateFlow<List<com.example.domain.model.Lyrics>> = _lyricsProjects.asStateFlow()

    private val _melodyProjects = MutableStateFlow<List<com.example.domain.model.melody.MelodyProject>>(emptyList())
    val melodyProjects: StateFlow<List<com.example.domain.model.melody.MelodyProject>> = _melodyProjects.asStateFlow()

    private val _chordProjects = MutableStateFlow<List<com.example.domain.model.chord.ChordProject>>(emptyList())
    val chordProjects: StateFlow<List<com.example.domain.model.chord.ChordProject>> = _chordProjects.asStateFlow()

    // Composer Memory
    private val _favArrStyle = MutableStateFlow("Bollywood Pop")
    val favArrStyle: StateFlow<String> = _favArrStyle.asStateFlow()

    private val _favInsts = MutableStateFlow("Sitar, Bansuri, Tabla")
    val favInsts: StateFlow<String> = _favInsts.asStateFlow()

    private val _favTrackCount = MutableStateFlow("8 Tracks")
    val favTrackCount: StateFlow<String> = _favTrackCount.asStateFlow()

    val allProjects: StateFlow<List<ArrangementProject>> = repository.getAllProjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var playbackJob: Job? = null

    init {
        // Load upstream details
        viewModelScope.launch {
            try {
                // Lyrics
                val musicRepo = ServiceLocator.getMusicRepository(application)
                musicRepo.getAllSavedLyricsFlow().collect { lyrics ->
                    _lyricsProjects.value = lyrics
                }
            } catch (e: Exception) {
                Log.e("ArrangementVM", "Failed to load lyrics projects: ${e.message}")
            }
        }

        viewModelScope.launch {
            try {
                // Melody
                val melodyRepo = ServiceLocator.getMelodyRepository(application)
                melodyRepo.getAllProjects().collect { mProjs ->
                    _melodyProjects.value = mProjs
                }
            } catch (e: Exception) {
                Log.e("ArrangementVM", "Failed to load melody projects: ${e.message}")
            }
        }

        viewModelScope.launch {
            try {
                // Chords
                val chordRepo = ServiceLocator.getChordRepository(application)
                chordRepo.getAllProjects().collect { cProjs ->
                    _chordProjects.value = cProjs
                }
            } catch (e: Exception) {
                Log.e("ArrangementVM", "Failed to load chord projects: ${e.message}")
            }
        }

        // Load Composer Memory from SharedPreferences
        val prefs = application.getSharedPreferences("arrangement_memory", Context.MODE_PRIVATE)
        _favArrStyle.value = prefs.getString("fav_arr_style", "Bollywood Pop") ?: "Bollywood Pop"
        _favInsts.value = prefs.getString("fav_insts", "Sitar, Bansuri, Tabla") ?: "Sitar, Bansuri, Tabla"
        _favTrackCount.value = prefs.getString("fav_track_count", "8 Tracks") ?: "8 Tracks"
    }

    fun selectProject(project: ArrangementProject) {
        _currentProject.value = project
        _error.value = null
        stopPlayback()
    }

    fun createNewProject(
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
    ) {
        viewModelScope.launch {
            _generationState.value = ArrangementGenerationState.GENERATING
            try {
                val proj = repository.createProject(
                    title = title,
                    lyricsProjectId = lyricsProjectId,
                    melodyProjectId = melodyProjectId,
                    chordProjectId = chordProjectId,
                    lyrics = lyrics,
                    prompt = prompt,
                    genre = genre,
                    mood = mood,
                    emotion = emotion,
                    bpm = bpm,
                    key = key,
                    scale = scale,
                    raga = raga,
                    songDurationSeconds = songDurationSeconds,
                    singerType = singerType,
                    language = language,
                    targetAudience = targetAudience,
                    songStructureType = songStructureType
                )
                
                // Immediately generate initial procedural blueprint
                val hydrated = repository.generateArrangement(proj, useOfflineAI = true)
                _currentProject.value = hydrated
                _generationState.value = ArrangementGenerationState.SUCCESS
            } catch (e: Exception) {
                _generationState.value = ArrangementGenerationState.ERROR
                _error.value = "Failed to create project: ${e.message}"
            }
        }
    }

    fun runAIGeneration(useOfflineAI: Boolean) {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            _generationState.value = ArrangementGenerationState.GENERATING
            _error.value = null
            stopPlayback()
            try {
                val result = repository.generateArrangement(proj, useOfflineAI)
                _currentProject.value = result
                _generationState.value = ArrangementGenerationState.SUCCESS
            } catch (e: Exception) {
                _generationState.value = ArrangementGenerationState.ERROR
                _error.value = "AI Arrangement Generation Failed: ${e.message}"
            }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            try {
                repository.deleteProject(projectId)
                if (_currentProject.value?.id == projectId) {
                    _currentProject.value = null
                    stopPlayback()
                }
            } catch (e: Exception) {
                _error.value = "Delete project failed: ${e.message}"
            }
        }
    }

    fun exportSessionPlan(format: String) {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            try {
                val exported = repository.exportArrangement(proj, format)
                _exportedText.value = exported
            } catch (e: Exception) {
                _error.value = "Export failed: ${e.message}"
            }
        }
    }

    fun clearExportedText() {
        _exportedText.value = ""
    }

    // Interactive Track mutations
    fun toggleTrackMute(trackId: String) {
        val proj = _currentProject.value ?: return
        val updatedTracks = proj.tracks.map {
            if (it.id == trackId) it.copy(isMuted = !it.isMuted) else it
        }
        val updatedProj = proj.copy(tracks = updatedTracks)
        _currentProject.value = updatedProj
        viewModelScope.launch {
            repository.updateProject(updatedProj)
        }
    }

    fun toggleTrackSolo(trackId: String) {
        val proj = _currentProject.value ?: return
        val updatedTracks = proj.tracks.map {
            if (it.id == trackId) it.copy(isSoloed = !it.isSoloed) else it
        }
        val updatedProj = proj.copy(tracks = updatedTracks)
        _currentProject.value = updatedProj
        viewModelScope.launch {
            repository.updateProject(updatedProj)
        }
    }

    fun toggleTrackLock(trackId: String) {
        val proj = _currentProject.value ?: return
        val updatedTracks = proj.tracks.map {
            if (it.id == trackId) it.copy(isLocked = !it.isLocked) else it
        }
        val updatedProj = proj.copy(tracks = updatedTracks)
        _currentProject.value = updatedProj
        viewModelScope.launch {
            repository.updateProject(updatedProj)
        }
    }

    fun addCustomTrack(instrumentName: String, colorHex: String) {
        val proj = _currentProject.value ?: return
        val newTrack = InstrumentTrack(
            id = "track_custom_${UUID.randomUUID().toString().take(6)}",
            projectId = proj.id,
            instrumentName = instrumentName,
            trackColorHex = colorHex
        )
        val updatedTracks = proj.tracks + newTrack
        val updatedProj = proj.copy(tracks = updatedTracks)
        _currentProject.value = updatedProj
        viewModelScope.launch {
            repository.updateProject(updatedProj)
        }
    }

    fun updateProjectTitle(title: String) {
        val proj = _currentProject.value ?: return
        val updated = proj.copy(title = title)
        _currentProject.value = updated
        viewModelScope.launch {
            repository.updateProject(updated)
        }
    }

    fun updateComposerMemory(style: String, insts: String, trackCount: String) {
        _favArrStyle.value = style
        _favInsts.value = insts
        _favTrackCount.value = trackCount

        val application = getApplication<Application>()
        val prefs = application.getSharedPreferences("arrangement_memory", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("fav_arr_style", style)
            .putString("fav_insts", insts)
            .putString("fav_track_count", trackCount)
            .apply()
    }

    // Playback controls
    fun startPlayback() {
        val proj = _currentProject.value ?: return
        if (_isPlaying.value) return
        _isPlaying.value = true

        val totalDuration = proj.songDurationSeconds.toFloat().coerceAtLeast(10f)

        playbackJob = viewModelScope.launch(Dispatchers.Default) {
            val stepMs = 100L
            while (_isPlaying.value) {
                delay(stepMs)
                val currentSec = _playbackTimeSeconds.value + (stepMs / 1000f)
                if (currentSec >= totalDuration) {
                    _playbackTimeSeconds.value = 0f
                    _playbackProgress.value = 0.0f
                    _activeSectionIndex.value = -1
                    _isPlaying.value = false
                    break
                }
                _playbackTimeSeconds.value = currentSec
                _playbackProgress.value = currentSec / totalDuration

                // Identify Active Section
                var cumulative = 0f
                var foundIndex = -1
                proj.sections.forEachIndexed { index, section ->
                    val secEnd = cumulative + section.durationSeconds
                    if (currentSec >= cumulative && currentSec < secEnd) {
                        foundIndex = index
                    }
                    cumulative += section.durationSeconds
                }
                _activeSectionIndex.value = foundIndex
            }
        }
    }

    fun stopPlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
    }

    fun seekPlayback(progress: Float) {
        val proj = _currentProject.value ?: return
        val totalDuration = proj.songDurationSeconds.toFloat()
        val targetSec = (progress * totalDuration).coerceIn(0f, totalDuration)
        _playbackProgress.value = progress
        _playbackTimeSeconds.value = targetSec

        var cumulative = 0f
        var foundIndex = -1
        proj.sections.forEachIndexed { index, section ->
            val secEnd = cumulative + section.durationSeconds
            if (targetSec >= cumulative && targetSec < secEnd) {
                foundIndex = index
            }
            cumulative += section.durationSeconds
        }
        _activeSectionIndex.value = foundIndex
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }

    // Factory Class
    class Factory(
        private val application: Application,
        private val repository: ArrangementRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ArrangementViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ArrangementViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

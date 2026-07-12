package com.example.domain.service

import com.example.domain.model.Project
import kotlinx.coroutines.flow.StateFlow
import java.io.File

data class StudioClipState(
    val clipId: String,
    val name: String,
    val startOffsetSec: Double,
    val durationSec: Double,
    val sourceFile: String? = null,
    val colorHex: String = "#9F75FF"
)

data class StudioTrackState(
    val trackId: String,
    val name: String,
    val type: String, // "Melody", "Percussion", "Vocal", "Drone"
    val volumeDb: Float = 0.0f,
    val pan: Float = 0.0f, // -1f (Left) to +1f (Right)
    val isMuted: Boolean = false,
    val isSoloed: Boolean = false,
    val clips: List<StudioClipState> = emptyList(),
    val folderGroup: String? = null // Supporting folders and grouping
)

data class StudioProjectState(
    val projectId: String,
    val name: String,
    val description: String,
    val bpm: Float = 120f,
    val timeSignature: String = "4/4",
    val loopEnabled: Boolean = false,
    val playheadPositionSec: Double = 0.0,
    val tracks: List<StudioTrackState> = emptyList(),
    val isDirty: Boolean = false
)

interface StudioService {
    // --- Project Lifecycle ---
    val currentProjectState: StateFlow<StudioProjectState?>
    val recentProjects: StateFlow<List<Project>>
    val undoStackSize: StateFlow<Int>
    val redoStackSize: StateFlow<Int>

    suspend fun createNewProject(name: String, description: String): StudioProjectState
    suspend fun loadProject(projectFile: File): Result<StudioProjectState>
    suspend fun loadProjectById(projectId: String): Result<StudioProjectState>
    suspend fun saveCurrentProject(autosave: Boolean = false): Result<File>
    suspend fun closeCurrentProject()
    suspend fun deleteProject(projectId: String)

    // --- Transport Controls ---
    val isPlaying: StateFlow<Boolean>
    val bpm: StateFlow<Float>
    val timeSignature: StateFlow<String>
    val loopEnabled: StateFlow<Boolean>
    val playheadPositionSec: StateFlow<Double>

    fun play()
    fun pause()
    fun stop()
    fun seekTo(seconds: Double)
    fun setBpm(bpm: Float)
    fun setTimeSignature(signature: String)
    fun toggleLoop()

    // --- Undo / Redo Actions ---
    fun updateProjectState(newState: StudioProjectState, actionDescription: String)
    fun undo()
    fun redo()
    fun clearHistory()

    // --- Asset Browser / Files ---
    fun getProjectAssets(): List<File>
    fun addAssetToProject(file: File): Result<File>
}

package com.example.data.service

import android.content.Context
import android.util.Log
import com.example.core.audio.SurmayaProjectPackager
import com.example.domain.model.Project
import com.example.domain.repository.MusicRepository
import com.example.domain.service.StudioProjectState
import com.example.domain.service.StudioService
import com.example.domain.service.StudioTrackState
import com.example.domain.service.StudioClipState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.io.File
import java.util.UUID

class StudioServiceImpl(
    private val context: Context,
    private val musicRepository: MusicRepository
) : StudioService {

    private val tag = "StudioServiceImpl"
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Packager for packaging .surmaya zip files
    private val packager = SurmayaProjectPackager(context)

    // Current project state stream
    private val _currentProjectState = MutableStateFlow<StudioProjectState?>(null)
    override val currentProjectState: StateFlow<StudioProjectState?> = _currentProjectState

    // Recent projects stream directly from Room
    override val recentProjects: StateFlow<List<Project>> = musicRepository.getAllProjectsFlow()
        .stateIn(serviceScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transport states
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _bpm = MutableStateFlow(120f)
    override val bpm: StateFlow<Float> = _bpm

    private val _timeSignature = MutableStateFlow("4/4")
    override val timeSignature: StateFlow<String> = _timeSignature

    private val _loopEnabled = MutableStateFlow(false)
    override val loopEnabled: StateFlow<Boolean> = _loopEnabled

    private val _playheadPositionSec = MutableStateFlow(0.0)
    override val playheadPositionSec: StateFlow<Double> = _playheadPositionSec

    // Undo / Redo history stacks
    private val undoStack = mutableListOf<StudioProjectState>()
    private val redoStack = mutableListOf<StudioProjectState>()

    private val _undoStackSize = MutableStateFlow(0)
    override val undoStackSize: StateFlow<Int> = _undoStackSize

    private val _redoStackSize = MutableStateFlow(0)
    override val redoStackSize: StateFlow<Int> = _redoStackSize

    // Transport dynamic progression job
    private var playheadJob: Job? = null

    init {
        // Automatically set up defaults from loaded project
        serviceScope.launch {
            currentProjectState.collect { state ->
                if (state != null) {
                    _bpm.value = state.bpm
                    _timeSignature.value = state.timeSignature
                    _loopEnabled.value = state.loopEnabled
                    _playheadPositionSec.value = state.playheadPositionSec
                }
            }
        }
    }

    override suspend fun createNewProject(name: String, description: String): StudioProjectState {
        closeCurrentProject()
        
        val projectId = UUID.randomUUID().toString()
        val defaultTracks = listOf(
            StudioTrackState(
                trackId = UUID.randomUUID().toString(),
                name = "AI Vocals (Melody)",
                type = "Vocal",
                volumeDb = 0f,
                pan = 0f,
                isMuted = false,
                isSoloed = false,
                clips = listOf(
                    StudioClipState(UUID.randomUUID().toString(), "Vocal Alap Intro", 0.0, 25.0, "vocal_alap.wav", "#9F75FF"),
                    StudioClipState(UUID.randomUUID().toString(), "Sargam Fast Taan", 35.0, 30.0, "vocal_taan.wav", "#FF759F"),
                    StudioClipState(UUID.randomUUID().toString(), "Main Bandish Theme", 75.0, 40.0, "vocal_bandish.wav", "#9F75FF")
                ),
                folderGroup = "Melody Stems"
            ),
            StudioTrackState(
                trackId = UUID.randomUUID().toString(),
                name = "Sitar / Veena Lead",
                type = "Melody",
                volumeDb = -2f,
                pan = -0.2f,
                isMuted = false,
                isSoloed = false,
                clips = listOf(
                    StudioClipState(UUID.randomUUID().toString(), "Sitar Alaap-Gat", 15.0, 30.0, "sitar_gat.wav", "#2FD6AA"),
                    StudioClipState(UUID.randomUUID().toString(), "Lead Jugalbandi Duet", 60.0, 45.0, "sitar_jugal.wav", "#2FD6AA")
                ),
                folderGroup = "Melody Stems"
            ),
            StudioTrackState(
                trackId = UUID.randomUUID().toString(),
                name = "Tabla Percussions",
                type = "Percussion",
                volumeDb = -3f,
                pan = 0.1f,
                isMuted = false,
                isSoloed = false,
                clips = listOf(
                    StudioClipState(UUID.randomUUID().toString(), "Teental Vilambit (Slow)", 0.0, 55.0, "tabla_slow.wav", "#F9D142"),
                    StudioClipState(UUID.randomUUID().toString(), "Teental Drut (Fast)", 55.0, 65.0, "tabla_fast.wav", "#FFB300")
                ),
                folderGroup = "Rhythm Sect"
            ),
            StudioTrackState(
                trackId = UUID.randomUUID().toString(),
                name = "Drone Tanpura",
                type = "Drone",
                volumeDb = -6f,
                pan = 0f,
                isMuted = false,
                isSoloed = false,
                clips = listOf(
                    StudioClipState(UUID.randomUUID().toString(), "Drone Continuous G", 0.0, 120.0, "tanpura_drone.wav", "#4E5C6E")
                ),
                folderGroup = "Ambient Drone"
            )
        )

        val projectState = StudioProjectState(
            projectId = projectId,
            name = name,
            description = description,
            bpm = 120f,
            timeSignature = "4/4",
            loopEnabled = false,
            playheadPositionSec = 0.0,
            tracks = defaultTracks,
            isDirty = true
        )

        // Save project metadata to database
        musicRepository.createProject(name, description)

        // Set as active project
        _currentProjectState.value = projectState
        clearHistory()
        saveCurrentProject(autosave = false)
        
        Log.i(tag, "Created new professional studio project: $name (ID: $projectId)")
        return projectState
    }

    override suspend fun loadProject(projectFile: File): Result<StudioProjectState> {
        return try {
            if (!projectFile.exists()) {
                return Result.failure(Exception("Project file does not exist: ${projectFile.absolutePath}"))
            }

            val targetUnpackDir = File(context.cacheDir, "unpacked_${projectFile.nameWithoutExtension}")
            if (targetUnpackDir.exists()) {
                targetUnpackDir.deleteRecursively()
            }
            targetUnpackDir.mkdirs()

            val unpackResult = packager.unpackProject(projectFile, targetUnpackDir)
            if (unpackResult.isFailure) {
                return Result.failure(unpackResult.exceptionOrNull() ?: Exception("Failed to unpack project"))
            }

            val unpacked = unpackResult.getOrThrow()
            val state = deserializeJsonToState(unpacked.projectJson)
                ?: return Result.failure(Exception("Failed to parse project.json metadata inside package"))

            // Copy unpacked assets back into project local documents folder
            val projectAssetsDir = getProjectAssetsDirectory(state.projectId)
            if (!projectAssetsDir.exists()) {
                projectAssetsDir.mkdirs()
            }
            unpacked.extractedFiles.forEach { file ->
                if (file.name != "project.json") {
                    val dest = File(projectAssetsDir, file.name)
                    file.copyTo(dest, overwrite = true)
                }
            }

            _currentProjectState.value = state.copy(isDirty = false)
            clearHistory()
            
            // Sync transport states
            _bpm.value = state.bpm
            _timeSignature.value = state.timeSignature
            _loopEnabled.value = state.loopEnabled
            _playheadPositionSec.value = state.playheadPositionSec

            Log.i(tag, "Loaded project: ${state.name} from ${projectFile.name}")
            Result.success(state)
        } catch (e: Exception) {
            Log.e(tag, "Error loading project from file", e)
            Result.failure(e)
        }
    }

    override suspend fun loadProjectById(projectId: String): Result<StudioProjectState> {
        return try {
            // Find in recent project files directory
            val projectDir = getProjectAssetsDirectory(projectId)
            val packageFile = File(projectDir, "session_archive.surmaya")
            if (packageFile.exists()) {
                loadProject(packageFile)
            } else {
                // Return a reconstructed state if package file doesn't exist yet but DB metadata exists
                val list = recentProjects.value
                val match = list.find { it.id == projectId }
                if (match != null) {
                    val defaultState = StudioProjectState(
                        projectId = projectId,
                        name = match.name,
                        description = match.description,
                        bpm = 105f,
                        timeSignature = "Teental (16 Beats)"
                    )
                    _currentProjectState.value = defaultState
                    clearHistory()
                    Result.success(defaultState)
                } else {
                    Result.failure(Exception("Project not found in database or files"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveCurrentProject(autosave: Boolean): Result<File> {
        val state = _currentProjectState.value ?: return Result.failure(Exception("No project currently active to save"))
        return try {
            val projectDir = getProjectAssetsDirectory(state.projectId)
            if (!projectDir.exists()) {
                projectDir.mkdirs()
            }

            // Sync with active transient transport values
            val stateToSave = state.copy(
                bpm = _bpm.value,
                timeSignature = _timeSignature.value,
                loopEnabled = _loopEnabled.value,
                playheadPositionSec = _playheadPositionSec.value,
                isDirty = false
            )

            val jsonString = serializeStateToJson(stateToSave)
            
            val archiveFile = if (autosave) {
                File(projectDir, "autosave_temp.json")
            } else {
                File(projectDir, "session_archive.surmaya")
            }

            if (autosave) {
                archiveFile.writeText(jsonString)
                Result.success(archiveFile)
            } else {
                // Gather project assets
                val assets = projectDir.listFiles { file -> file.isFile && file.name != "session_archive.surmaya" && file.name != "autosave_temp.json" }?.toList() ?: emptyList()
                
                val packResult = packager.packProject(stateToSave.projectId, jsonString, assets, archiveFile)
                if (packResult.isFailure) {
                    return Result.failure(packResult.exceptionOrNull() ?: Exception("Failed to package surmaya project"))
                }
                
                _currentProjectState.value = stateToSave
                Log.i(tag, "Saved project archive successfully at: ${archiveFile.absolutePath}")
                Result.success(archiveFile)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to save project", e)
            Result.failure(e)
        }
    }

    override suspend fun closeCurrentProject() {
        stop()
        if (_currentProjectState.value != null && _currentProjectState.value?.isDirty == true) {
            saveCurrentProject(autosave = false)
        }
        _currentProjectState.value = null
        clearHistory()
    }

    override suspend fun deleteProject(projectId: String) {
        if (_currentProjectState.value?.projectId == projectId) {
            closeCurrentProject()
        }
        val projectDir = getProjectAssetsDirectory(projectId)
        if (projectDir.exists()) {
            projectDir.deleteRecursively()
        }
        musicRepository.deleteProject(projectId)
        Log.i(tag, "Deleted project ID: $projectId from disk and DB")
    }

    // --- Transport controls ---

    override fun play() {
        if (_isPlaying.value) return
        _isPlaying.value = true
        
        playheadJob = serviceScope.launch {
            val updateIntervalMs = 50L
            val maxDurationSec = 120.0 // Playback length simulation boundary
            
            while (isActive && _isPlaying.value) {
                val bpmVal = _bpm.value
                val beatsPerSec = bpmVal / 60.0
                val secondsPerInterval = (updateIntervalMs / 1000.0) * beatsPerSec
                
                var nextPos = _playheadPositionSec.value + secondsPerInterval
                if (nextPos >= maxDurationSec) {
                    if (_loopEnabled.value) {
                        nextPos = 0.0
                    } else {
                        nextPos = maxDurationSec
                        _isPlaying.value = false
                        break
                    }
                }
                _playheadPositionSec.value = nextPos
                delay(updateIntervalMs)
            }
        }
        Log.d(tag, "Transport: PLAY started")
    }

    override fun pause() {
        _isPlaying.value = false
        playheadJob?.cancel()
        playheadJob = null
        Log.d(tag, "Transport: PAUSE")
    }

    override fun stop() {
        _isPlaying.value = false
        playheadJob?.cancel()
        playheadJob = null
        _playheadPositionSec.value = 0.0
        Log.d(tag, "Transport: STOP & Reset playhead")
    }

    override fun seekTo(seconds: Double) {
        _playheadPositionSec.value = seconds.coerceAtLeast(0.0)
    }

    override fun setBpm(bpm: Float) {
        _bpm.value = bpm.coerceIn(40f, 240f)
        _currentProjectState.value?.let { state ->
            updateProjectState(state.copy(bpm = _bpm.value), "Change Tempo BPM to ${bpm.toInt()}")
        }
    }

    override fun setTimeSignature(signature: String) {
        _timeSignature.value = signature
        _currentProjectState.value?.let { state ->
            updateProjectState(state.copy(timeSignature = signature), "Change Time Signature to $signature")
        }
    }

    override fun toggleLoop() {
        _loopEnabled.value = !_loopEnabled.value
    }

    // --- History / Undo-Redo management ---

    override fun updateProjectState(newState: StudioProjectState, actionDescription: String) {
        val currentState = _currentProjectState.value ?: return
        
        // Push current state to undo history
        undoStack.add(currentState)
        redoStack.clear() // Clear redo on new action
        
        _undoStackSize.value = undoStack.size
        _redoStackSize.value = redoStack.size

        // Set new state with dirty flag
        _currentProjectState.value = newState.copy(isDirty = true)
        Log.i(tag, "Action performed: $actionDescription. History Stack size: ${undoStack.size}")

        // Auto-save state updates in background
        serviceScope.launch {
            saveCurrentProject(autosave = true)
        }
    }

    override fun undo() {
        if (undoStack.isEmpty()) return
        val previousState = undoStack.removeAt(undoStack.size - 1)
        val currentState = _currentProjectState.value ?: return
        
        redoStack.add(currentState)
        
        _undoStackSize.value = undoStack.size
        _redoStackSize.value = redoStack.size
        
        _currentProjectState.value = previousState
        Log.i(tag, "Undo performed. Active project restored to: ${previousState.name}")
    }

    override fun redo() {
        if (redoStack.isEmpty()) return
        val nextState = redoStack.removeAt(redoStack.size - 1)
        val currentState = _currentProjectState.value ?: return
        
        undoStack.add(currentState)
        
        _undoStackSize.value = undoStack.size
        _redoStackSize.value = redoStack.size
        
        _currentProjectState.value = nextState
        Log.i(tag, "Redo performed. Active project restored to: ${nextState.name}")
    }

    override fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
        _undoStackSize.value = 0
        _redoStackSize.value = 0
    }

    // --- Asset Browsing ---

    override fun getProjectAssets(): List<File> {
        val state = _currentProjectState.value ?: return emptyList()
        val dir = getProjectAssetsDirectory(state.projectId)
        return dir.listFiles { file -> file.isFile && file.name != "session_archive.surmaya" && file.name != "autosave_temp.json" }?.toList() ?: emptyList()
    }

    override fun addAssetToProject(file: File): Result<File> {
        val state = _currentProjectState.value ?: return Result.failure(Exception("No project currently active to add asset to"))
        return try {
            if (!file.exists()) {
                return Result.failure(Exception("Source file does not exist"))
            }
            val dir = getProjectAssetsDirectory(state.projectId)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val dest = File(dir, file.name)
            file.copyTo(dest, overwrite = true)
            Log.i(tag, "Asset added to project folder: ${dest.absolutePath}")
            Result.success(dest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper folder resolution
    private fun getProjectAssetsDirectory(projectId: String): File {
        val baseDir = File(context.getExternalFilesDir(null), "Projects")
        return File(baseDir, projectId)
    }

    // Manual brace-matching helper to parsing nested objects without errors
    private fun findMatchingEndBrace(json: String, startIdx: Int): Int {
        var braceCount = 0
        var insideQuotes = false
        var escaped = false
        for (i in startIdx until json.length) {
            val char = json[i]
            if (escaped) {
                escaped = false
                continue
            }
            if (char == '\\') {
                escaped = true
                continue
            }
            if (char == '"') {
                insideQuotes = !insideQuotes
                continue
            }
            if (!insideQuotes) {
                if (char == '{') {
                    braceCount++
                } else if (char == '}') {
                    braceCount--
                    if (braceCount == 0) {
                        return i
                    }
                }
            }
        }
        return -1
    }

    // Manual super lightweight, 100% stable JSON serializer/deserializer to avoid any complex library class-matching build overhead
    private fun serializeStateToJson(state: StudioProjectState): String {
        val sb = java.lang.StringBuilder()
        sb.append("{\n")
        sb.append("  \"projectId\": \"${state.projectId}\",\n")
        sb.append("  \"name\": \"${state.name.replace("\"", "\\\"")}\",\n")
        sb.append("  \"description\": \"${state.description.replace("\"", "\\\"")}\",\n")
        sb.append("  \"bpm\": ${state.bpm},\n")
        sb.append("  \"timeSignature\": \"${state.timeSignature}\",\n")
        sb.append("  \"loopEnabled\": ${state.loopEnabled},\n")
        sb.append("  \"playheadPositionSec\": ${state.playheadPositionSec},\n")
        sb.append("  \"tracks\": [\n")
        
        state.tracks.forEachIndexed { index, track ->
            sb.append("    {\n")
            sb.append("      \"trackId\": \"${track.trackId}\",\n")
            sb.append("      \"name\": \"${track.name.replace("\"", "\\\"")}\",\n")
            sb.append("      \"type\": \"${track.type}\",\n")
            sb.append("      \"volumeDb\": ${track.volumeDb},\n")
            sb.append("      \"pan\": ${track.pan},\n")
            sb.append("      \"isMuted\": ${track.isMuted},\n")
            sb.append("      \"isSoloed\": ${track.isSoloed},\n")
            if (track.folderGroup != null) {
                sb.append("      \"folderGroup\": \"${track.folderGroup}\",\n")
            } else {
                sb.append("      \"folderGroup\": null,\n")
            }
            sb.append("      \"clips\": [\n")
            track.clips.forEachIndexed { clipIndex, clip ->
                sb.append("        {\n")
                sb.append("          \"clipId\": \"${clip.clipId}\",\n")
                sb.append("          \"name\": \"${clip.name.replace("\"", "\\\"")}\",\n")
                sb.append("          \"startOffsetSec\": ${clip.startOffsetSec},\n")
                sb.append("          \"durationSec\": ${clip.durationSec},\n")
                sb.append("          \"sourceFile\": ${if (clip.sourceFile != null) "\"${clip.sourceFile}\"" else "null"},\n")
                sb.append("          \"colorHex\": \"${clip.colorHex}\"\n")
                sb.append("        }")
                if (clipIndex < track.clips.size - 1) {
                    sb.append(",")
                }
                sb.append("\n")
            }
            sb.append("      ]\n")
            sb.append("    }")
            if (index < state.tracks.size - 1) {
                sb.append(",")
            }
            sb.append("\n")
        }
        sb.append("  ]\n")
        sb.append("}")
        return sb.toString()
    }

    private fun deserializeJsonToState(json: String): StudioProjectState? {
        return try {
            val projectId = extractJsonStringValue(json, "projectId") ?: return null
            val name = extractJsonStringValue(json, "name") ?: "Untitled Studio Session"
            val description = extractJsonStringValue(json, "description") ?: ""
            val bpm = extractJsonNumericValue(json, "bpm")?.toFloat() ?: 120f
            val timeSignature = extractJsonStringValue(json, "timeSignature") ?: "4/4"
            val loopEnabled = extractJsonBooleanValue(json, "loopEnabled") ?: false
            val playheadPositionSec = extractJsonNumericValue(json, "playheadPositionSec") ?: 0.0

            val tracks = mutableListOf<StudioTrackState>()
            // Find tracks list segment
            val tracksSegmentStart = json.indexOf("\"tracks\":")
            if (tracksSegmentStart != -1) {
                var currentSearchIdx = tracksSegmentStart
                while (true) {
                    val trackStart = json.indexOf("{", currentSearchIdx)
                    if (trackStart == -1) break
                    
                    val trackEnd = findMatchingEndBrace(json, trackStart)
                    if (trackEnd == -1) break
                    
                    val trackJson = json.substring(trackStart, trackEnd + 1)
                    val trackId = extractJsonStringValue(trackJson, "trackId")
                    if (trackId != null) {
                        val trackName = extractJsonStringValue(trackJson, "name") ?: "Track"
                        val trackType = extractJsonStringValue(trackJson, "type") ?: "Melody"
                        val volumeDb = extractJsonNumericValue(trackJson, "volumeDb")?.toFloat() ?: 0f
                        val pan = extractJsonNumericValue(trackJson, "pan")?.toFloat() ?: 0f
                        val isMuted = extractJsonBooleanValue(trackJson, "isMuted") ?: false
                        val isSoloed = extractJsonBooleanValue(trackJson, "isSoloed") ?: false
                        val folderGroup = extractJsonStringValue(trackJson, "folderGroup")

                        // Parse nested clips
                        val clips = mutableListOf<StudioClipState>()
                        val clipsStart = trackJson.indexOf("\"clips\":")
                        if (clipsStart != -1) {
                            var clipSearchIdx = clipsStart
                            while (true) {
                                val clipStart = trackJson.indexOf("{", clipSearchIdx)
                                if (clipStart == -1) break
                                
                                val clipEnd = findMatchingEndBrace(trackJson, clipStart)
                                if (clipEnd == -1) break
                                
                                val clipJson = trackJson.substring(clipStart, clipEnd + 1)
                                val clipId = extractJsonStringValue(clipJson, "clipId")
                                if (clipId != null) {
                                    val clipName = extractJsonStringValue(clipJson, "name") ?: "Clip"
                                    val startOffsetSec = extractJsonNumericValue(clipJson, "startOffsetSec") ?: 0.0
                                    val durationSec = extractJsonNumericValue(clipJson, "durationSec") ?: 10.0
                                    val sourceFile = extractJsonStringValue(clipJson, "sourceFile")
                                    val colorHex = extractJsonStringValue(clipJson, "colorHex") ?: "#9F75FF"
                                    
                                    clips.add(
                                        StudioClipState(
                                            clipId = clipId,
                                            name = clipName,
                                            startOffsetSec = startOffsetSec,
                                            durationSec = durationSec,
                                            sourceFile = sourceFile,
                                            colorHex = colorHex
                                        )
                                    )
                                }
                                clipSearchIdx = clipEnd + 1
                            }
                        }
                        
                        tracks.add(
                            StudioTrackState(
                                trackId = trackId,
                                name = trackName,
                                type = trackType,
                                volumeDb = volumeDb,
                                pan = pan,
                                isMuted = isMuted,
                                isSoloed = isSoloed,
                                clips = clips,
                                folderGroup = if (folderGroup == "null") null else folderGroup
                            )
                        )
                    }
                    currentSearchIdx = trackEnd + 1
                }
            }

            StudioProjectState(
                projectId = projectId,
                name = name,
                description = description,
                bpm = bpm,
                timeSignature = timeSignature,
                loopEnabled = loopEnabled,
                playheadPositionSec = playheadPositionSec,
                tracks = tracks,
                isDirty = false
            )
        } catch (e: Exception) {
            Log.e(tag, "JSON parsing error", e)
            null
        }
    }

    private fun extractJsonStringValue(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        return pattern.find(json)?.groupValues?.get(1)
    }

    private fun extractJsonNumericValue(json: String, key: String): Double? {
        val pattern = "\"$key\"\\s*:\\s*(-?[0-9\\.]+)".toRegex()
        return pattern.find(json)?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun extractJsonBooleanValue(json: String, key: String): Boolean? {
        val pattern = "\"$key\"\\s*:\\s*(true|false)".toRegex()
        return pattern.find(json)?.groupValues?.get(1)?.toBoolean()
    }
}

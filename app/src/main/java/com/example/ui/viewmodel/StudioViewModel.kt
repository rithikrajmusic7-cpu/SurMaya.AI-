package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.audio.export.AudioExportEngine
import com.example.core.audio.export.ExportFormat
import com.example.core.audio.export.ExportMetadata
import com.example.core.audio.export.ExportStatus
import com.example.di.ServiceLocator
import com.example.domain.model.Project
import com.example.domain.service.StudioProjectState
import com.example.domain.service.StudioService
import com.example.domain.service.StudioTrackState
import com.example.domain.service.StudioClipState
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaRecorder
import android.util.Log

class StudioViewModel(
    application: Application,
    private val studioService: StudioService
) : AndroidViewModel(application) {

    // Export Engine
    private val exportEngine = AudioExportEngine.getInstance(application)

    // --- State Streams ---
    val currentProjectState: StateFlow<StudioProjectState?> = studioService.currentProjectState
    val recentProjects: StateFlow<List<Project>> = studioService.recentProjects
    val undoStackSize: StateFlow<Int> = studioService.undoStackSize
    val redoStackSize: StateFlow<Int> = studioService.redoStackSize

    // Transport state streams
    val isPlaying: StateFlow<Boolean> = studioService.isPlaying
    val bpm: StateFlow<Float> = studioService.bpm
    val timeSignature: StateFlow<String> = studioService.timeSignature
    val loopEnabled: StateFlow<Boolean> = studioService.loopEnabled
    val playheadPositionSec: StateFlow<Double> = studioService.playheadPositionSec

    // Export rendering status
    val exportStatus: StateFlow<ExportStatus> = exportEngine.exportStatus

    // Simple view-specific UI flags
    val activeStudioTab = MutableStateFlow("overview") // "overview", "mixer", "arranger", "assets", "export"
    val showCreateProjectDialog = MutableStateFlow(false)
    val showExportDialog = MutableStateFlow(false)
    val operationLogs = MutableStateFlow<List<String>>(listOf("Studio Workspace initialized successfully"))

    // --- Direct Multi-track Recording State (Milestone 3B.3) ---
    val isRecordingDirect = MutableStateFlow(false)
    val recordingTrackId = MutableStateFlow<String?>(null)
    val recordingStartSec = MutableStateFlow(0.0)
    val selectedInputDeviceName = MutableStateFlow("Default Microphone")
    val connectedInputDevices = MutableStateFlow<List<String>>(listOf("Default Microphone"))
    val inputLevelDb = MutableStateFlow(-120f)

    // Professional Recording Configurations
    val selectedInputSource = MutableStateFlow("Built-in Microphone") // "Built-in Microphone", "USB Audio Interface"
    val selectedChannelConfig = MutableStateFlow("Mono") // "Mono", "Stereo"
    val selectedSampleRate = MutableStateFlow(44100) // 44100, 48000
    val selectedBitDepth = MutableStateFlow(16) // 16, 24
    val isLiveMonitoringEnabled = MutableStateFlow(false)
    val recordingDurationSec = MutableStateFlow(0)

    private var activeRecordingFile: File? = null
    private var recordingJob: kotlinx.coroutines.Job? = null

    private val audioDeviceCallback = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        object : android.media.AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out android.media.AudioDeviceInfo>?) {
                scanAudioInputDevices()
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out android.media.AudioDeviceInfo>?) {
                val currentSelected = selectedInputDeviceName.value
                scanAudioInputDevices()
                val stillConnected = connectedInputDevices.value.contains(currentSelected)
                if (!stillConnected) {
                    if (isRecordingDirect.value) {
                        stopDirectRecording()
                        logOperation("⚠️ USB Audio Interface disconnected during recording! Take saved gracefully.")
                    } else {
                        selectedInputDeviceName.value = "Default Microphone"
                        selectedInputSource.value = "Built-in Microphone"
                        logOperation("⚠️ Selected Audio Device disconnected. Switched to Default Microphone.")
                    }
                }
            }
        }
    } else {
        null
    }

    init {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && audioDeviceCallback != null) {
            val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
        }
        scanAudioInputDevices()
    }

    override fun onCleared() {
        super.onCleared()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && audioDeviceCallback != null) {
            val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        }
    }

    fun scanAudioInputDevices() {
        val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
        val list = mutableListOf("Default Microphone")
        var usbFound = false
        for (device in devices) {
            val name = device.productName?.toString() ?: "Audio Device"
            val type = device.type
            if (type == AudioDeviceInfo.TYPE_USB_DEVICE || type == AudioDeviceInfo.TYPE_USB_HEADSET) {
                list.add("⚡ USB: $name")
                usbFound = true
            } else if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                list.add("🎧 Headset: $name")
            }
        }
        connectedInputDevices.value = list
        if (usbFound && selectedInputDeviceName.value == "Default Microphone") {
            val firstUsb = list.firstOrNull { it.startsWith("⚡ USB:") }
            if (firstUsb != null) {
                selectedInputDeviceName.value = firstUsb
                selectedInputSource.value = "USB Audio Interface"
                logOperation("Auto-detected USB Audio Interface: $firstUsb")
            }
        }
    }

    private fun getSelectedAudioDeviceInfo(): AudioDeviceInfo? {
        val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
        val selectedName = selectedInputDeviceName.value
        if (selectedName.startsWith("⚡ USB:") || selectedName.startsWith("🎧 Headset:")) {
            val cleanName = selectedName.substringAfter(": ").trim()
            return devices.find { (it.productName?.toString() ?: "").contains(cleanName, ignoreCase = true) }
        }
        return null
    }

    fun startDirectRecording(trackId: String) {
        if (isRecordingDirect.value) return
        val currentPlayhead = playheadPositionSec.value

        try {
            val outputDir = getApplication<Application>().cacheDir
            val file = File(outputDir, "take_${UUID.randomUUID().toString().take(6)}.wav")
            activeRecordingFile = file
            recordingTrackId.value = trackId
            recordingStartSec.value = currentPlayhead
            isRecordingDirect.value = true
            recordingDurationSec.value = 0

            logOperation("Recording armed. Track: '${trackId.take(6)}...' at ${String.format("%.2f", currentPlayhead)}s using ${selectedInputDeviceName.value}")

            recordingJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                var record: android.media.AudioRecord? = null
                var audioTrack: android.media.AudioTrack? = null
                var raf: java.io.RandomAccessFile? = null
                var timerJob: kotlinx.coroutines.Job? = null

                try {
                    val sampleRate = selectedSampleRate.value
                    val channelConfig = if (selectedChannelConfig.value == "Stereo") {
                        android.media.AudioFormat.CHANNEL_IN_STEREO
                    } else {
                        android.media.AudioFormat.CHANNEL_IN_MONO
                    }
                    val numChannels = if (selectedChannelConfig.value == "Stereo") 2 else 1
                    val audioFormat = android.media.AudioFormat.ENCODING_PCM_16BIT
                    val minBufferSize = android.media.AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                    val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

                    record = android.media.AudioRecord(
                        android.media.MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        channelConfig,
                        audioFormat,
                        bufferSize
                    )

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        getSelectedAudioDeviceInfo()?.let { devInfo ->
                            record.preferredDevice = devInfo
                        }
                    }

                    if (record.state != android.media.AudioRecord.STATE_INITIALIZED) {
                        throw IllegalStateException("AudioRecord failed to initialize. Try changing sample rate.")
                    }

                    if (isLiveMonitoringEnabled.value) {
                        val outChannel = if (numChannels == 2) {
                            android.media.AudioFormat.CHANNEL_OUT_STEREO
                        } else {
                            android.media.AudioFormat.CHANNEL_OUT_MONO
                        }
                        val trackBufferSize = android.media.AudioTrack.getMinBufferSize(sampleRate, outChannel, audioFormat)
                        audioTrack = android.media.AudioTrack(
                            android.media.AudioManager.STREAM_MUSIC,
                            sampleRate,
                            outChannel,
                            audioFormat,
                            trackBufferSize.coerceAtLeast(4096),
                            android.media.AudioTrack.MODE_STREAM
                        )
                        audioTrack.play()
                    }

                    record.startRecording()

                    raf = java.io.RandomAccessFile(file, "rw")
                    raf.setLength(0)
                    raf.write(ByteArray(44)) // Reserve WAV header space

                    val buffer = ByteArray(4096)
                    var totalAudioLen = 0L

                    timerJob = launch(kotlinx.coroutines.Dispatchers.Main) {
                        val startTime = System.currentTimeMillis()
                        while (isRecordingDirect.value) {
                            recordingDurationSec.value = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                            kotlinx.coroutines.delay(200)
                        }
                    }

                    while (isRecordingDirect.value) {
                        val readSize = record.read(buffer, 0, buffer.size)
                        if (readSize > 0) {
                            raf.write(buffer, 0, readSize)
                            totalAudioLen += readSize

                            var maxAmp = 0
                            for (i in 0 until readSize step 2) {
                                if (i + 1 < readSize) {
                                    val value = (buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xff)
                                    val absVal = kotlin.math.abs(value)
                                    if (absVal > maxAmp) {
                                        maxAmp = absVal
                                    }
                                }
                            }
                            val db = if (maxAmp > 0) {
                                20 * kotlin.math.log10(maxAmp.toDouble() / 32768.0)
                            } else {
                                -120.0
                            }
                            inputLevelDb.value = db.toFloat().coerceIn(-120f, 0f)

                            if (isLiveMonitoringEnabled.value && audioTrack != null) {
                                audioTrack.write(buffer, 0, readSize)
                            }
                        } else if (readSize < 0) {
                            Log.e("StudioViewModel", "AudioRecord read error: $readSize")
                            break
                        }
                    }

                    val totalDataLen = totalAudioLen + 36
                    val byteRate = (sampleRate * numChannels * 2).toLong()
                    raf.seek(0)
                    val headerStream = java.io.ByteArrayOutputStream()
                    writeWavHeader(headerStream, totalAudioLen, totalDataLen, sampleRate.toLong(), numChannels, byteRate)
                    raf.write(headerStream.toByteArray())

                    logOperation("Saved Take successfully: WAV Format (${numChannels}ch, ${sampleRate}Hz)")

                } catch (e: Exception) {
                    Log.e("StudioViewModel", "Error in AudioRecord recording pipeline", e)
                    launch(kotlinx.coroutines.Dispatchers.Main) {
                        logOperation("⚠️ Recording Error: ${e.localizedMessage}")
                        isRecordingDirect.value = false
                    }
                } finally {
                    timerJob?.cancel()
                    try {
                        record?.stop()
                        record?.release()
                    } catch (e: Exception) { Log.e("StudioViewModel", "Error releasing AudioRecord", e) }

                    try {
                        audioTrack?.stop()
                        audioTrack?.release()
                    } catch (e: Exception) { Log.e("StudioViewModel", "Error releasing AudioTrack", e) }

                    try {
                        raf?.close()
                    } catch (e: Exception) { Log.e("StudioViewModel", "Error closing RandomAccessFile", e) }
                }
            }

        } catch (e: Exception) {
            Log.e("StudioViewModel", "Failed to start direct recording", e)
            logOperation("Direct recording failed to initialize: ${e.localizedMessage}")
            isRecordingDirect.value = false
            recordingTrackId.value = null
        }
    }

    private fun writeWavHeader(out: java.io.OutputStream, totalAudioLen: Long, totalDataLen: Long, longSampleRate: Long, channels: Int, byteRate: Long) {
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte() // RIFF
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte() // WAVE
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // fmt
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1 (PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = ((longSampleRate shr 8) and 0xff).toByte()
        header[26] = ((longSampleRate shr 16) and 0xff).toByte()
        header[27] = ((longSampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * 2).toByte() // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte() // data
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()
        out.write(header, 0, 44)
    }

    fun stopDirectRecording() {
        if (!isRecordingDirect.value) return
        val file = activeRecordingFile
        val trackId = recordingTrackId.value ?: return
        val startSec = recordingStartSec.value

        isRecordingDirect.value = false
        recordingTrackId.value = null
        recordingJob?.cancel()
        recordingJob = null
        inputLevelDb.value = -120f

        viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            if (file != null && file.exists()) {
                val durationSec = try {
                    val retriever = android.media.MediaMetadataRetriever()
                    retriever.setDataSource(file.absolutePath)
                    val durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                    retriever.release()
                    (durationStr?.toDouble() ?: 0.0) / 1000.0
                } catch (e: Exception) {
                    recordingDurationSec.value.toDouble().coerceAtLeast(0.5)
                }

                val project = currentProjectState.value
                if (project != null) {
                    val clipId = UUID.randomUUID().toString().take(8)
                    val namePrefix = if (trackId.contains("vocal", ignoreCase = true)) "Vocal Take" else "Take"
                    val newClip = StudioClipState(
                        clipId = clipId,
                        name = "$namePrefix (${file.nameWithoutExtension})",
                        startOffsetSec = startSec,
                        durationSec = durationSec,
                        sourceFile = file.absolutePath,
                        colorHex = "#9F75FF"
                    )

                    val updatedTracks = project.tracks.map { track ->
                        if (track.trackId == trackId) {
                            track.copy(clips = track.clips + newClip)
                        } else {
                            track
                        }
                    }

                    studioService.updateProjectState(
                        project.copy(tracks = updatedTracks),
                        "Recorded Audio Clip on track ${trackId.take(5)}"
                    )

                    logOperation("Saved Take successfully: ${String.format("%.1f", durationSec)} seconds at offset ${String.format("%.1f", startSec)}s")
                }
            }
        }
    }

    fun logOperation(message: String) {
        val current = operationLogs.value.toMutableList()
        current.add(0, "[${System.currentTimeMillis() % 100000}] $message")
        operationLogs.value = current.take(30) // Keep last 30 logs
    }

    // --- Actions ---

    fun createNewProject(name: String, description: String) {
        viewModelScope.launch {
            try {
                studioService.createNewProject(name, description)
                logOperation("Created project: $name")
            } catch (e: Exception) {
                logOperation("Error creating project: ${e.message}")
            }
        }
    }

    fun loadProject(file: File) {
        viewModelScope.launch {
            val result = studioService.loadProject(file)
            result.fold(
                onSuccess = { state -> logOperation("Successfully loaded project: ${state.name}") },
                onFailure = { err -> logOperation("Load failed: ${err.message}") }
            )
        }
    }

    fun loadProjectById(projectId: String) {
        viewModelScope.launch {
            val result = studioService.loadProjectById(projectId)
            result.fold(
                onSuccess = { state -> logOperation("Opened session: ${state.name}") },
                onFailure = { err -> logOperation("Failed to open session: ${err.message}") }
            )
        }
    }

    fun saveCurrentProject() {
        viewModelScope.launch {
            val result = studioService.saveCurrentProject(autosave = false)
            result.fold(
                onSuccess = { file -> logOperation("Project saved successfully as surmaya archive: ${file.name}") },
                onFailure = { err -> logOperation("Failed to save project: ${err.message}") }
            )
        }
    }

    fun closeCurrentProject() {
        viewModelScope.launch {
            studioService.closeCurrentProject()
            logOperation("Active project closed")
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            studioService.deleteProject(projectId)
            logOperation("Deleted project: $projectId")
        }
    }

    // --- Transport Commands ---

    fun play() {
        studioService.play()
        logOperation("Transport: PLAY")
    }

    fun pause() {
        studioService.pause()
        logOperation("Transport: PAUSE")
    }

    fun stop() {
        studioService.stop()
        logOperation("Transport: STOP")
    }

    fun seekTo(seconds: Double) {
        studioService.seekTo(seconds)
    }

    fun setBpm(newBpm: Float) {
        studioService.setBpm(newBpm)
        logOperation("Tempo adjusted to ${newBpm.toInt()} BPM")
    }

    fun setTimeSignature(sig: String) {
        studioService.setTimeSignature(sig)
        logOperation("Time signature updated to $sig")
    }

    fun toggleLoop() {
        studioService.toggleLoop()
        logOperation("Loop state toggled to ${if (loopEnabled.value) "OFF" else "ON"}")
    }

    // --- Undo/Redo commands ---

    fun undo() {
        studioService.undo()
        logOperation("Undo operation executed")
    }

    fun redo() {
        studioService.redo()
        logOperation("Redo operation executed")
    }

    // --- Channel Strip Adjustments ---

    fun updateTrackVolume(trackId: String, volumeDb: Float) {
        val current = currentProjectState.value ?: return
        val updatedTracks = current.tracks.map { track ->
            if (track.trackId == trackId) track.copy(volumeDb = volumeDb) else track
        }
        val targetTrackName = current.tracks.find { it.trackId == trackId }?.name ?: "Track"
        
        studioService.updateProjectState(
            current.copy(tracks = updatedTracks),
            "Adjust volume of '$targetTrackName' to ${String.format("%.1f", volumeDb)} dB"
        )
    }

    fun updateTrackPan(trackId: String, pan: Float) {
        val current = currentProjectState.value ?: return
        val updatedTracks = current.tracks.map { track ->
            if (track.trackId == trackId) track.copy(pan = pan) else track
        }
        val targetTrackName = current.tracks.find { it.trackId == trackId }?.name ?: "Track"
        
        studioService.updateProjectState(
            current.copy(tracks = updatedTracks),
            "Pan '$targetTrackName' to ${if (pan < 0) "Left" else if (pan > 0) "Right" else "Center"}"
        )
    }

    fun toggleTrackMute(trackId: String) {
        val current = currentProjectState.value ?: return
        val updatedTracks = current.tracks.map { track ->
            if (track.trackId == trackId) track.copy(isMuted = !track.isMuted) else track
        }
        val targetTrack = current.tracks.find { it.trackId == trackId }
        val action = if (targetTrack?.isMuted == true) "Unmute" else "Mute"
        
        studioService.updateProjectState(
            current.copy(tracks = updatedTracks),
            "$action track '${targetTrack?.name}'"
        )
    }

    fun toggleTrackSolo(trackId: String) {
        val current = currentProjectState.value ?: return
        val updatedTracks = current.tracks.map { track ->
            if (track.trackId == trackId) track.copy(isSoloed = !track.isSoloed) else track
        }
        val targetTrack = current.tracks.find { it.trackId == trackId }
        val action = if (targetTrack?.isSoloed == true) "De-solo" else "Solo"
        
        studioService.updateProjectState(
            current.copy(tracks = updatedTracks),
            "$action track '${targetTrack?.name}'"
        )
    }

    // --- Multi-track Timeline Engine Operations (Milestone 3B.2) ---

    val zoomScale = MutableStateFlow(10f) // pixels per second on the timeline ruler
    val snapGrid = MutableStateFlow("Off") // "Off", "Beat" (1s), "Bar" (4s), "2 Bars" (8s)
    val isRippleEditEnabled = MutableStateFlow(false) // Slide subsequent clips when editing length

    fun updateZoomScale(scale: Float) {
        zoomScale.value = scale.coerceIn(2f..40f)
    }

    fun updateSnapGrid(division: String) {
        snapGrid.value = division
    }

    fun toggleRippleEdit() {
        isRippleEditEnabled.value = !isRippleEditEnabled.value
        logOperation("Ripple Editing set to: ${if (isRippleEditEnabled.value) "ON" else "OFF"}")
    }

    fun getSnappedTime(rawTime: Double): Double {
        return when (snapGrid.value) {
            "Beat" -> Math.round(rawTime).toDouble()
            "Bar" -> {
                // 1 Bar at 120BPM 4/4 is 2.0 seconds (60s/120 * 4 = 2s)
                val barDuration = 2.0
                Math.round(rawTime / barDuration) * barDuration
            }
            "2 Bars" -> {
                val twoBarsDuration = 4.0
                Math.round(rawTime / twoBarsDuration) * twoBarsDuration
            }
            else -> rawTime
        }
    }

    fun moveClip(trackId: String, clipId: String, newStartSec: Double) {
        val current = currentProjectState.value ?: return
        val snappedStart = getSnappedTime(newStartSec).coerceAtLeast(0.0)
        
        var targetTrackName = ""
        var targetClipName = ""
        var delta = 0.0

        val updatedTracks = current.tracks.map { track ->
            if (track.trackId == trackId) {
                targetTrackName = track.name
                val updatedClips = track.clips.map { clip ->
                    if (clip.clipId == clipId) {
                        targetClipName = clip.name
                        delta = snappedStart - clip.startOffsetSec
                        clip.copy(startOffsetSec = snappedStart)
                    } else clip
                }
                
                // If Ripple editing is enabled, shift all clips starting after this clip by delta
                val finalClips = if (isRippleEditEnabled.value && delta != 0.0) {
                    val originalClip = track.clips.find { it.clipId == clipId } ?: return@map track
                    updatedClips.map { clip ->
                        if (clip.clipId != clipId && clip.startOffsetSec > originalClip.startOffsetSec) {
                            clip.copy(startOffsetSec = (clip.startOffsetSec + delta).coerceAtLeast(0.0))
                        } else clip
                    }
                } else updatedClips

                track.copy(clips = finalClips)
            } else track
        }

        studioService.updateProjectState(
            current.copy(tracks = updatedTracks),
            "Move clip '$targetClipName' in track '$targetTrackName' to ${String.format("%.2f", snappedStart)}s"
        )
        logOperation("Timeline: Moved '$targetClipName' to ${String.format("%.1f", snappedStart)}s")
    }

    fun trimClip(trackId: String, clipId: String, newStartSec: Double, newDurationSec: Double) {
        val current = currentProjectState.value ?: return
        val snappedStart = getSnappedTime(newStartSec).coerceAtLeast(0.0)
        val snappedDuration = getSnappedTime(newDurationSec).coerceAtLeast(0.5)

        var targetTrackName = ""
        var targetClipName = ""
        var delta = 0.0

        val updatedTracks = current.tracks.map { track ->
            if (track.trackId == trackId) {
                targetTrackName = track.name
                val originalClip = track.clips.find { it.clipId == clipId } ?: return@map track
                delta = snappedDuration - originalClip.durationSec

                val updatedClips = track.clips.map { clip ->
                    if (clip.clipId == clipId) {
                        targetClipName = clip.name
                        clip.copy(startOffsetSec = snappedStart, durationSec = snappedDuration)
                    } else clip
                }

                // If Ripple edit is active, slide following clips by delta
                val finalClips = if (isRippleEditEnabled.value && delta != 0.0) {
                    updatedClips.map { clip ->
                        if (clip.clipId != clipId && clip.startOffsetSec > originalClip.startOffsetSec) {
                            clip.copy(startOffsetSec = (clip.startOffsetSec + delta).coerceAtLeast(0.0))
                        } else clip
                    }
                } else updatedClips

                track.copy(clips = finalClips)
            } else track
        }

        studioService.updateProjectState(
            current.copy(tracks = updatedTracks),
            "Trimmed clip '$targetClipName' in '$targetTrackName'"
        )
        logOperation("Timeline: Trimmed '$targetClipName' (Duration: ${String.format("%.1f", snappedDuration)}s)")
    }

    fun splitClip(trackId: String, clipId: String, splitPointSec: Double) {
        val current = currentProjectState.value ?: return
        var targetTrackName = ""
        var targetClipName = ""

        val updatedTracks = current.tracks.map { track ->
            if (track.trackId == trackId) {
                targetTrackName = track.name
                val clip = track.clips.find { it.clipId == clipId } ?: return@map track
                
                // Split point must fall strictly inside the clip bounds
                if (splitPointSec <= clip.startOffsetSec || splitPointSec >= clip.startOffsetSec + clip.durationSec) {
                    return@map track
                }
                
                targetClipName = clip.name
                val duration1 = splitPointSec - clip.startOffsetSec
                val duration2 = (clip.startOffsetSec + clip.durationSec) - splitPointSec

                val clip1 = clip.copy(clipId = UUID.randomUUID().toString(), name = "${clip.name} (Pt 1)", durationSec = duration1)
                val clip2 = clip.copy(clipId = UUID.randomUUID().toString(), name = "${clip.name} (Pt 2)", startOffsetSec = splitPointSec, durationSec = duration2)

                val newClips = track.clips.toMutableList()
                val idx = newClips.indexOfFirst { it.clipId == clipId }
                if (idx != -1) {
                    newClips.removeAt(idx)
                    newClips.add(idx, clip2)
                    newClips.add(idx, clip1)
                }
                track.copy(clips = newClips)
            } else track
        }

        studioService.updateProjectState(
            current.copy(tracks = updatedTracks),
            "Split clip '$targetClipName' in '$targetTrackName' at ${String.format("%.1f", splitPointSec)}s"
        )
        logOperation("Timeline: Split '$targetClipName' at ${String.format("%.1f", splitPointSec)}s")
    }

    fun mergeClips(trackId: String, clipId1: String, clipId2: String) {
        val current = currentProjectState.value ?: return
        var targetTrackName = ""

        val updatedTracks = current.tracks.map { track ->
            if (track.trackId == trackId) {
                targetTrackName = track.name
                val c1 = track.clips.find { it.clipId == clipId1 } ?: return@map track
                val c2 = track.clips.find { it.clipId == clipId2 } ?: return@map track

                // Order clips by start offset
                val first = if (c1.startOffsetSec < c2.startOffsetSec) c1 else c2
                val second = if (c1.startOffsetSec < c2.startOffsetSec) c2 else c1

                val newStart = first.startOffsetSec
                val newEnd = second.startOffsetSec + second.durationSec
                val mergedClip = first.copy(
                    clipId = UUID.randomUUID().toString(),
                    name = "${first.name.substringBefore(" (Pt")}+Merged",
                    startOffsetSec = newStart,
                    durationSec = newEnd - newStart,
                    colorHex = "#9F75FF"
                )

                val newClips = track.clips.filter { it.clipId != clipId1 && it.clipId != clipId2 }.toMutableList()
                newClips.add(mergedClip)
                newClips.sortBy { it.startOffsetSec }

                track.copy(clips = newClips)
            } else track
        }

        studioService.updateProjectState(
            current.copy(tracks = updatedTracks),
            "Merged clips in track '$targetTrackName'"
        )
        logOperation("Timeline: Merged adjacent clips in '$targetTrackName'")
    }

    fun deleteClip(trackId: String, clipId: String) {
        val current = currentProjectState.value ?: return
        var targetTrackName = ""
        var targetClipName = ""

        val updatedTracks = current.tracks.map { track ->
            if (track.trackId == trackId) {
                targetTrackName = track.name
                targetClipName = track.clips.find { it.clipId == clipId }?.name ?: "Clip"
                track.copy(clips = track.clips.filter { it.clipId != clipId })
            } else track
        }

        studioService.updateProjectState(
            current.copy(tracks = updatedTracks),
            "Deleted clip '$targetClipName' from '$targetTrackName'"
        )
        logOperation("Timeline: Deleted '$targetClipName'")
    }

    fun addNewTrack(name: String, type: String, folder: String?) {
        val current = currentProjectState.value ?: return
        val newTrack = StudioTrackState(
            trackId = UUID.randomUUID().toString(),
            name = name,
            type = type,
            clips = emptyList(),
            folderGroup = if (folder.isNullOrBlank()) null else folder
        )
        val updatedTracks = current.tracks.toMutableList()
        updatedTracks.add(newTrack)

        studioService.updateProjectState(
            current.copy(tracks = updatedTracks),
            "Add custom track '$name' ($type)"
        )
        logOperation("Tracks: Added empty track '$name' of type $type")
    }

    fun updateTrackFolderGroup(trackId: String, folderName: String?) {
        val current = currentProjectState.value ?: return
        val updatedTracks = current.tracks.map { track ->
            if (track.trackId == trackId) {
                track.copy(folderGroup = if (folderName.isNullOrBlank()) null else folderName)
            } else track
        }
        val targetTrackName = current.tracks.find { it.trackId == trackId }?.name ?: "Track"

        studioService.updateProjectState(
            current.copy(tracks = updatedTracks),
            "Assign track '$targetTrackName' to folder '${folderName ?: "None"}'"
        )
        logOperation("Tracks: Track folder changed for '$targetTrackName'")
    }

    // import helpers
    fun createEmptyClipOnTrack(trackId: String, startSec: Double, durationSec: Double) {
        val current = currentProjectState.value ?: return
        val targetTrack = current.tracks.find { it.trackId == trackId } ?: return
        
        val colors = listOf("#9F75FF", "#FF759F", "#2FD6AA", "#F9D142")
        val randColor = colors[Math.abs(trackId.hashCode()) % colors.size]

        val newClip = StudioClipState(
            clipId = UUID.randomUUID().toString(),
            name = "Synthesized Loop Segment",
            startOffsetSec = getSnappedTime(startSec).coerceAtLeast(0.0),
            durationSec = getSnappedTime(durationSec).coerceAtLeast(1.0),
            colorHex = randColor
        )

        val updatedTracks = current.tracks.map { track ->
            if (track.trackId == trackId) {
                track.copy(clips = track.clips + newClip)
            } else track
        }

        studioService.updateProjectState(
            current.copy(tracks = updatedTracks),
            "Synthesized clip added to track '${targetTrack.name}'"
        )
        logOperation("Timeline: Inserted new segment at ${String.format("%.1f", startSec)}s")
    }

    // --- Assets Browser ---

    fun getProjectAssets(): List<File> {
        return studioService.getProjectAssets()
    }

    fun importAssetFile(file: File) {
        val res = studioService.addAssetToProject(file)
        res.fold(
            onSuccess = { dest -> logOperation("Imported asset: ${dest.name}") },
            onFailure = { err -> logOperation("Asset import failed: ${err.message}") }
        )
    }

    // --- Export Trigger ---

    fun triggerOfflineBounceExport(format: ExportFormat, durationSec: Int = 15) {
        val current = currentProjectState.value ?: return
        
        val volumes = current.tracks.associate { it.trackId to it.volumeDb }
        val pans = current.tracks.associate { it.trackId to it.pan }

        val metadata = ExportMetadata(
            title = current.name,
            artist = "SurMaya Producer",
            album = "SurMaya Studio Masters",
            genre = "Indian Classical Fusion"
        )

        logOperation("Starting offline master render to ${format.displayName}...")
        
        exportEngine.startExport(
            format = format,
            metadata = metadata,
            durationSec = durationSec,
            channelVolumes = volumes,
            channelPans = pans,
            masterFaderDb = 0.0f
        )
    }

    fun cancelExport() {
        exportEngine.cancelExport()
        logOperation("Render export cancelled by user.")
    }

    // --- Factory ---
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StudioViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StudioViewModel(
                    application,
                    ServiceLocator.getStudioService(application)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

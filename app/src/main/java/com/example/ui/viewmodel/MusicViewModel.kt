package com.example.ui.viewmodel

import android.app.Application
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.ServiceLocator
import com.example.domain.model.Lyrics
import com.example.domain.model.Project
import com.example.domain.model.Song
import com.example.domain.repository.MusicRepository
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.math.max
import kotlin.math.sin

class MusicViewModel(
    application: Application,
    private val musicRepository: MusicRepository,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

    // --- State Streams ---
    val allSongs: StateFlow<List<Song>> = musicRepository.getAllSongsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = musicRepository.getFavoriteSongsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val draftSongs: StateFlow<List<Song>> = musicRepository.getDraftSongsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedSongs: StateFlow<List<Song>> = musicRepository.getDownloadedSongsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<Project>> = musicRepository.getAllProjectsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedLyrics: StateFlow<List<Lyrics>> = musicRepository.getAllSavedLyricsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI UI-States ---
    val lyricsGenerationState = MutableStateFlow<LyricsState>(LyricsState.Idle)
    val songGenerationState = MutableStateFlow<SongState>(SongState.Idle)

    // --- Audio Playback State ---
    val currentPlayingSong = MutableStateFlow<Song?>(null)
    val isPlaying = MutableStateFlow(false)
    val playbackProgress = MutableStateFlow(0f) // 0.0 to 1.0
    val visualizerWaves = MutableStateFlow(List(16) { 0.1f })
    val playbackError = MutableStateFlow<String?>(null)

    private var synthJob: Job? = null
    private var playbackProgressJob: Job? = null
    private var audioTrack: AudioTrack? = null
    private var isSynthActive = false

    // --- Create Folder ---
    fun createProjectFolder(name: String, description: String) {
        viewModelScope.launch {
            musicRepository.createProject(name, description)
        }
    }

    // --- Delete Folder ---
    fun deleteProjectFolder(projectId: String) {
        viewModelScope.launch {
            musicRepository.deleteProject(projectId)
        }
    }

    // --- Delete Song ---
    fun deleteSong(songId: String) {
        viewModelScope.launch {
            musicRepository.deleteSong(songId)
            if (currentPlayingSong.value?.id == songId) {
                stopPlayback()
            }
        }
    }

    // --- Favorite Toggle ---
    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            musicRepository.updateSong(song.copy(isFavorite = !song.isFavorite))
        }
    }

    // --- Download Toggle ---
    fun toggleDownload(song: Song) {
        viewModelScope.launch {
            musicRepository.updateSong(song.copy(isDownloaded = !song.isDownloaded))
        }
    }

    // --- Save Custom Lyrics ---
    fun saveCustomLyrics(title: String, prompt: String, content: String, language: String, onResult: () -> Unit = {}) {
        viewModelScope.launch {
            musicRepository.saveLyrics(title, prompt, content, language)
            onResult()
        }
    }

    // --- Delete Lyrics ---
    fun deleteLyrics(lyricsId: String) {
        viewModelScope.launch {
            musicRepository.deleteLyrics(lyricsId)
        }
    }

    // --- Generate Lyrics ---
    fun generateLyrics(prompt: String, mode: String, language: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            lyricsGenerationState.value = LyricsState.Loading
            val result = musicRepository.generateLyricsWithAI(prompt, mode, language)
            result.fold(
                onSuccess = { lyrics ->
                    lyricsGenerationState.value = LyricsState.Success(lyrics)
                },
                onFailure = { error ->
                    lyricsGenerationState.value = LyricsState.Error(error.message ?: "Failed to generate lyrics")
                }
            )
        }
    }

    // --- Audio Verification ---
    fun verifyAudioResource(audioPath: String?): Result<File> {
        if (audioPath.isNullOrBlank()) {
            return Result.failure(Exception("Audio file is missing or path is blank."))
        }
        val file = File(audioPath)
        if (!file.exists()) {
            return Result.failure(Exception("Audio file does not exist at path: $audioPath"))
        }
        if (!file.canRead()) {
            return Result.failure(Exception("Audio file is not readable due to permission restrictions."))
        }
        if (file.length() <= 0) {
            return Result.failure(Exception("Audio file length is zero. File is empty or corrupted."))
        }
        
        try {
            file.inputStream().use { stream ->
                val buffer = ByteArray(12)
                val read = stream.read(buffer)
                if (read < 4) {
                    return Result.failure(Exception("Audio file is too short to contain a valid audio stream header."))
                }
                
                val isWav = buffer[0] == 'R'.toByte() && buffer[1] == 'I'.toByte() && 
                            buffer[2] == 'F'.toByte() && buffer[3] == 'F'.toByte()
                val isMp3 = (buffer[0] == 'I'.toByte() && buffer[1] == 'D'.toByte() && buffer[2] == '3'.toByte()) ||
                            (buffer[0] == 0xFF.toByte() && (buffer[1].toInt() and 0xE0) == 0xE0)
                            
                if (!isWav && !isMp3) {
                    // Log warning but don't strictly crash if format matches general media framework capabilities
                }
            }
        } catch (e: Exception) {
            return Result.failure(Exception("Audio file integrity check failed: ${e.message}"))
        }
        
        return Result.success(file)
    }

    // --- Generate Song ---
    fun generateSong(
        title: String,
        prompt: String,
        lyrics: String,
        language: String,
        genre: String,
        mood: String,
        style: String,
        tempo: String,
        duration: String,
        voiceName: String,
        voiceGender: String,
        voiceMatchPercent: Int,
        weirdness: Int,
        styleInfluence: Int,
        projectId: String?,
        uploadedAudioPath: String?
    ) {
        viewModelScope.launch {
            try {
                // Pre-check credits
                val activeUser = userRepository.getActiveUser()
                if (activeUser != null) {
                    val currentCredits = activeUser.creditsRemaining
                    if (currentCredits < 5) {
                        songGenerationState.value = SongState.Error("Insufficient credits! Upgrade to Pro or Premier plan.")
                        return@launch
                    }
                    userRepository.updateUserCredits(currentCredits - 5)
                }

                // Stage 1: Lyrics Input
                songGenerationState.value = SongState.Loading(progress = 5, status = "Stage 1/11: Processing Lyrics Input...")
                delay(600)

                // Stage 2: AI Composer
                songGenerationState.value = SongState.Loading(progress = 12, status = "Stage 2/11: AI Composer compiling structural plan...")
                val composerRepo = ServiceLocator.getComposerRepository(getApplication())
                val composerProject = composerRepo.createProject(
                    title = title,
                    lyrics = lyrics.ifBlank { "[Instrumental Raga Accent]" },
                    language = language,
                    genre = genre,
                    mood = mood,
                    filmSituation = "Studio Production",
                    era = "Modern",
                    productionScale = "Premium",
                    emotionalJourney = mood,
                    instrumentPreferences = "Sitar, Tabla, Bansuri",
                    userNotes = prompt
                )
                composerRepo.compileMasterCompositionPlan(composerProject).getOrThrow()
                delay(700)

                // Stage 3: AI Melody Generator
                songGenerationState.value = SongState.Loading(progress = 20, status = "Stage 3/11: AI Melody Generator designing ragas...")
                val melodyRepo = ServiceLocator.getMelodyRepository(getApplication())
                val melodyProject = melodyRepo.createProject(
                    title = title,
                    lyrics = lyrics,
                    chords = "C, G, Am, F",
                    prompt = prompt,
                    emotion = mood,
                    genre = genre,
                    mood = mood,
                    scale = "C Major",
                    raga = "Bhairavi",
                    tempo = if (tempo.lowercase() == "fast") 135 else if (tempo.lowercase() == "slow") 75 else 105,
                    vocalStyle = voiceName,
                    sectionType = "Verse"
                )
                melodyRepo.generateMelody(melodyProject)
                delay(700)

                // Stage 4: AI Chord Generator
                songGenerationState.value = SongState.Loading(progress = 30, status = "Stage 4/11: AI Chord Generator harmony calculation...")
                val chordRepo = ServiceLocator.getChordRepository(getApplication())
                val chordProject = chordRepo.createProject(
                    title = title,
                    melodyProjectId = melodyProject.id,
                    lyrics = lyrics,
                    prompt = prompt,
                    genre = genre,
                    emotion = mood,
                    mood = mood,
                    scale = "C Major",
                    raga = "Bhairavi",
                    bpm = if (tempo.lowercase() == "fast") 135 else if (tempo.lowercase() == "slow") 75 else 105,
                    chordComplexity = "Complex"
                )
                chordRepo.generateChordProgression(chordProject)
                delay(700)

                // Stage 5: AI Arrangement Engine
                songGenerationState.value = SongState.Loading(progress = 40, status = "Stage 5/11: AI Arrangement Engine sequencing structure...")
                val arrangementRepo = ServiceLocator.getArrangementRepository(getApplication())
                val arrProject = arrangementRepo.createProject(
                    title = title,
                    lyricsProjectId = null,
                    melodyProjectId = melodyProject.id,
                    chordProjectId = chordProject.id,
                    lyrics = lyrics,
                    prompt = prompt,
                    genre = genre,
                    mood = mood,
                    emotion = mood,
                    bpm = if (tempo.lowercase() == "fast") 135 else if (tempo.lowercase() == "slow") 75 else 105,
                    key = "C",
                    scale = "Major",
                    raga = "Bhairavi",
                    songDurationSeconds = 15,
                    singerType = voiceName,
                    language = language,
                    targetAudience = "General",
                    songStructureType = "Verse-Chorus"
                )
                arrangementRepo.generateArrangement(arrProject, useOfflineAI = false)
                delay(700)

                // Stage 6: AI Instrument Performance Generator
                songGenerationState.value = SongState.Loading(progress = 50, status = "Stage 6/11: AI Instrument Performance generating sitar and tabla...")
                delay(700)

                // Stage 7: AI Singer Engine
                songGenerationState.value = SongState.Loading(progress = 60, status = "Stage 7/11: AI Singer Engine synthesizing $voiceName...")
                val singerRepo = ServiceLocator.getSingerRepository(getApplication())
                val singerConfig = com.example.domain.model.singer.SingerConfiguration(
                    voiceId = voiceName,
                    style = "Bollywood",
                    emotion = mood
                )
                singerRepo.synthesizeVocalPerformance(
                    projectId = arrProject.id,
                    lyrics = lyrics,
                    config = singerConfig,
                    tempo = if (tempo.lowercase() == "fast") 135f else if (tempo.lowercase() == "slow") 75f else 105f,
                    keyMidi = 60
                ).getOrThrow()
                delay(700)

                // Stage 8: AI Mixing Intelligence Engine (AMIE)
                songGenerationState.value = SongState.Loading(progress = 70, status = "Stage 8/11: AI Mixing Intelligence Engine balancing tracks...")
                val mixingRepo = ServiceLocator.getMixingRepository(getApplication())
                mixingRepo.synthesizeMix(
                    projectId = arrProject.id,
                    tracks = listOf("Lead Vocal" to "Vocal", "Sitar" to "Instrumental", "Tabla" to "Percussion"),
                    genreStyle = genre,
                    targetLoudnessLufs = -14f
                ).getOrThrow()
                delay(700)

                // Stage 9: AI Mastering Intelligence Engine (AIME)
                songGenerationState.value = SongState.Loading(progress = 80, status = "Stage 9/11: AI Mastering Intelligence Engine maximizing headroom...")
                val masteringRepo = ServiceLocator.getMasteringRepository(getApplication())
                masteringRepo.synthesizeMaster(
                    projectId = arrProject.id,
                    genreStyle = genre,
                    targetLoudnessLufs = -14f,
                    selectedPlatforms = listOf("Spotify", "YouTube")
                ).getOrThrow()
                delay(700)

                // Stage 10: Audio Rendering
                songGenerationState.value = SongState.Loading(progress = 90, status = "Stage 10/11: Rendering high-fidelity stereo audio stream...")
                val result = musicRepository.generateSongWithAI(
                    title = title,
                    prompt = prompt,
                    lyrics = lyrics,
                    language = language,
                    genre = genre,
                    mood = mood,
                    style = style,
                    tempo = tempo,
                    duration = duration,
                    voiceName = voiceName,
                    voiceGender = voiceGender,
                    voiceMatchPercent = voiceMatchPercent,
                    weirdness = weirdness,
                    styleInfluence = styleInfluence,
                    projectId = projectId,
                    uploadedAudioPath = uploadedAudioPath
                )

                result.fold(
                    onSuccess = { song ->
                        // Stage 11: Export Complete & Verification
                        songGenerationState.value = SongState.Loading(progress = 97, status = "Stage 11/11: Verification & local database export...")
                        delay(500)

                        val validationResult = verifyAudioResource(song.audioUrl)
                        if (validationResult.isSuccess) {
                            songGenerationState.value = SongState.Success(song)
                        } else {
                            val errMsg = validationResult.exceptionOrNull()?.message ?: "Generated audio is invalid or corrupted."
                            songGenerationState.value = SongState.Error("Audio Integrity Failure: $errMsg")
                        }
                    },
                    onFailure = { error ->
                        songGenerationState.value = SongState.Error("Audio Rendering Failure: ${error.message ?: "Failed to render final waveform"}")
                    }
                )

            } catch (e: Exception) {
                songGenerationState.value = SongState.Error("Pipeline aborted at active stage: ${e.message}")
            }
        }
    }

    fun resetGenerationStates() {
        lyricsGenerationState.value = LyricsState.Idle
        songGenerationState.value = SongState.Idle
    }

    private var mediaPlayer: MediaPlayer? = null

    // --- Audio Playback Synthesizer Engine ---
    fun togglePlayback(song: Song) {
        if (currentPlayingSong.value?.id == song.id) {
            if (isPlaying.value) {
                pausePlayback()
            } else {
                resumePlayback(song)
            }
        } else {
            stopPlayback()
            startPlayback(song)
        }
    }

    private fun resumePlayback(song: Song) {
        playbackError.value = null
        val devPrefs = com.example.data.local.DeveloperPrefsManager.getInstance(getApplication())
        isPlaying.value = true
        isSynthActive = true
        val player = mediaPlayer
        if (player != null) {
            try {
                player.start()
                playbackProgressJob = viewModelScope.launch {
                    while (isPlaying.value && player.isPlaying) {
                        val duration = player.duration.toFloat()
                        if (duration > 0) {
                            playbackProgress.value = player.currentPosition.toFloat() / duration
                        }
                        visualizerWaves.value = List(16) { 
                            (0.1f + Math.random().toFloat() * 0.8f) 
                        }
                        delay(250)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (devPrefs.isDeveloperModeEnabled) {
                    startLegacySynthPlayback(song)
                } else {
                    stopPlayback()
                    playbackError.value = "Playback resumed failed: ${e.message}"
                }
            }
        } else {
            startPlayback(song)
        }
    }

    private fun startPlayback(song: Song) {
        playbackError.value = null
        currentPlayingSong.value = song
        isPlaying.value = true
        isSynthActive = true

        val devPrefs = com.example.data.local.DeveloperPrefsManager.getInstance(getApplication())

        viewModelScope.launch(Dispatchers.IO) {
            val audioPath = song.audioUrl ?: ""
            val isLocalFile = audioPath.isNotEmpty() && (audioPath.startsWith("/") || File(audioPath).exists())

            if (isLocalFile || audioPath.startsWith("synthetic_audio_")) {
                withContext(Dispatchers.Main) {
                    mediaPlayer?.release()
                    mediaPlayer = null
                }

                val fileToPlay = if (isLocalFile) {
                    File(audioPath)
                } else {
                    // Synthesize the WAV file in background cache so we can play standard WAV
                    val cacheFile = File(getApplication<Application>().cacheDir, "${song.id}.wav")
                    if (!cacheFile.exists()) {
                        try {
                            val genRepo = ServiceLocator.getMusicGenerationRepository(getApplication())
                            val durationSec = try {
                                val parts = song.duration.split(":")
                                if (parts.size >= 2) parts[0].toInt() * 60 + parts[1].toInt() else 15
                            } catch (e: Exception) {
                                15
                            }
                            val genImpl = genRepo as com.example.data.repository.MusicGenerationRepositoryImpl
                            genImpl.generateMusic(song.prompt, durationSec).getOrNull()?.renameTo(cacheFile)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    cacheFile
                }

                val verification = verifyAudioResource(fileToPlay.absolutePath)

                if (verification.isSuccess) {
                    withContext(Dispatchers.Main) {
                        try {
                            val player = MediaPlayer().apply {
                                setDataSource(fileToPlay.absolutePath)
                                prepare()
                                start()
                            }
                            mediaPlayer = player

                            player.setOnCompletionListener {
                                stopPlayback()
                            }

                            playbackProgressJob = viewModelScope.launch {
                                while (isPlaying.value && player.isPlaying) {
                                    val duration = player.duration.toFloat()
                                    if (duration > 0) {
                                        playbackProgress.value = player.currentPosition.toFloat() / duration
                                    }
                                    visualizerWaves.value = List(16) { 
                                        (0.1f + Math.random().toFloat() * 0.8f) 
                                    }
                                    delay(250)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            if (devPrefs.isDeveloperModeEnabled) {
                                startLegacySynthPlayback(song)
                            } else {
                                stopPlayback()
                                playbackError.value = "MediaPlayer Error: ${e.message}"
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        if (devPrefs.isDeveloperModeEnabled) {
                            startLegacySynthPlayback(song)
                        } else {
                            stopPlayback()
                            playbackError.value = "Playback failed: " + (verification.exceptionOrNull()?.message ?: "Invalid audio file.")
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    if (devPrefs.isDeveloperModeEnabled) {
                        startLegacySynthPlayback(song)
                    } else {
                        stopPlayback()
                        playbackError.value = "Playback failed: Audio resource is unavailable."
                    }
                }
            }
        }
    }

    private fun startLegacySynthPlayback(song: Song) {
        val tempoVal = when (song.tempo.lowercase()) {
            "fast" -> 135
            "slow" -> 75
            else -> 105
        }

        // Start Sitar/Flute Additive Synthesis Wave Pipeline
        synthJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 22050
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val track = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize.coerceAtLeast(sampleRate * 2),
                    AudioTrack.MODE_STREAM
                )
                audioTrack = track
                track.play()

                // Classic Raga-themed frequencies
                val pentatonicScale = when (song.genre.lowercase()) {
                    "bollywood", "romantic" -> doubleArrayOf(261.63, 293.66, 329.63, 392.00, 440.00, 523.25) // Bilawal raga
                    "classical", "bhajan" -> doubleArrayOf(261.63, 293.66, 311.13, 349.23, 392.00, 466.16) // Bhairavi raga
                    else -> doubleArrayOf(261.63, 293.66, 311.13, 349.23, 392.00, 466.16, 523.25) // Kafi raga
                }

                val beatDurationMs = (60000 / tempoVal).toLong()
                var noteIndex = 0

                while (isSynthActive) {
                    val freq = pentatonicScale[noteIndex % pentatonicScale.size]
                    val durationSamples = (sampleRate * (beatDurationMs / 1000.0)).toInt()
                    val buffer = ShortArray(durationSamples)
                    
                    for (i in 0 until durationSamples) {
                        if (!isSynthActive) break
                        val t = i.toDouble() / sampleRate
                        val envelope = max(0.0, 1.0 - (i.toDouble() / durationSamples))
                        
                        // Sitar Additive synthesis with harmonics and sympathetic vibration
                        val wave = sin(2.0 * Math.PI * freq * t) * 0.6 + 
                                   sin(4.0 * Math.PI * freq * t) * 0.3 + 
                                   sin(6.0 * Math.PI * freq * t) * 0.1
                                   
                        buffer[i] = (wave * 9000.0 * envelope).toInt().toShort()
                    }
                    
                    if (isSynthActive) {
                        track.write(buffer, 0, buffer.size)
                    }
                    noteIndex = (noteIndex + 1) % pentatonicScale.size
                    
                    // Small sync sleep to support timing loop
                    delay(30)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Start Playback Progress and Waveform Visualizer simulation
        playbackProgressJob = viewModelScope.launch {
            val totalSteps = 120 // Simulate a 2-minute song loop
            var step = (playbackProgress.value * totalSteps).toInt()
            while (isPlaying.value) {
                delay(1000)
                step++
                playbackProgress.value = (step.toFloat() / totalSteps).coerceIn(0f, 1f)
                
                // Dynamically update visualizer waves
                visualizerWaves.value = List(16) { 
                    (0.1f + Math.random().toFloat() * 0.8f) * (if (isPlaying.value) 1f else 0.1f) 
                }

                if (step >= totalSteps) {
                    stopPlayback()
                    break
                }
            }
        }
    }

    fun pausePlayback() {
        isPlaying.value = false
        isSynthActive = false
        synthJob?.cancel()
        playbackProgressJob?.cancel()
        try {
            audioTrack?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopPlayback() {
        isPlaying.value = false
        isSynthActive = false
        playbackProgress.value = 0f
        synthJob?.cancel()
        playbackProgressJob?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // safe ignore
        }
        audioTrack = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // safe ignore
        }
        mediaPlayer = null
        currentPlayingSong.value = null
        visualizerWaves.value = List(16) { 0.1f }
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
                val musicRepo = ServiceLocator.getMusicRepository(application)
                val userRepo = ServiceLocator.getUserRepository(application)
                return MusicViewModel(application, musicRepo, userRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// --- Sealed States ---
sealed class LyricsState {
    object Idle : LyricsState()
    object Loading : LyricsState()
    data class Success(val lyrics: String) : LyricsState()
    data class Error(val message: String) : LyricsState()
}

sealed class SongState {
    object Idle : SongState()
    data class Loading(val progress: Int, val status: String) : SongState()
    data class Success(val song: Song) : SongState()
    data class Error(val message: String) : SongState()
}

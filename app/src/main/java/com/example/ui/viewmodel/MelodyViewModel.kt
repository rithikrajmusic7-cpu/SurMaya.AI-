package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.mapper.toDomain
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.ServiceLocator
import com.example.domain.model.melody.*
import com.example.domain.repository.MelodyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MelodyViewModel(
    application: Application,
    private val melodyRepository: MelodyRepository
) : AndroidViewModel(application) {

    val allProjects: StateFlow<List<MelodyProject>> = melodyRepository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProject = MutableStateFlow<MelodyProject?>(null)
    val selectedProject: StateFlow<MelodyProject?> = _selectedProject.asStateFlow()

    private val _generationState = MutableStateFlow(MelodyGenerationState.IDLE)
    val generationState: StateFlow<MelodyGenerationState> = _generationState.asStateFlow()

    private val _generatedPlan = MutableStateFlow<GeneratedMelodyPlan?>(null)
    val generatedPlan: StateFlow<GeneratedMelodyPlan?> = _generatedPlan.asStateFlow()

    private val _exportedContent = MutableStateFlow<String?>(null)
    val exportedContent: StateFlow<String?> = _exportedContent.asStateFlow()

    // Real-time synth playback tracker
    private val _activePlayingNoteIndex = MutableStateFlow<Int?>(-1)
    val activePlayingNoteIndex: StateFlow<Int?> = _activePlayingNoteIndex.asStateFlow()

    private var playbackJob: Job? = null
    private var synthTrack: AudioTrack? = null

    init {
        // Automatically deserialize previously saved melody plan when project changes
        viewModelScope.launch {
            _selectedProject.collect { proj ->
                _exportedContent.value = null
                if (proj != null && !proj.currentMelodyJson.isNullOrBlank()) {
                    try {
                        val moshi = com.squareup.moshi.Moshi.Builder()
                            .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                            .build()
                        val plan = moshi.adapter(GeneratedMelodyPlan::class.java).fromJson(proj.currentMelodyJson)
                        _generatedPlan.value = plan
                    } catch (e: Exception) {
                        Log.e("MelodyViewModel", "Failed to restore melody plan: ${e.message}")
                        _generatedPlan.value = null
                    }
                } else {
                    _generatedPlan.value = null
                }
            }
        }
    }

    fun createProject(
        title: String,
        lyrics: String,
        chords: String,
        prompt: String,
        emotion: String,
        genre: String,
        mood: String,
        scale: String,
        raga: String,
        tempo: Int,
        vocalStyle: String,
        sectionType: String
    ) {
        viewModelScope.launch {
            val newProj = melodyRepository.createProject(
                title = title,
                lyrics = lyrics,
                chords = chords,
                prompt = prompt,
                emotion = emotion,
                genre = genre,
                mood = mood,
                scale = scale,
                raga = raga,
                tempo = tempo,
                vocalStyle = vocalStyle,
                sectionType = sectionType
            )
            _selectedProject.value = newProj
        }
    }

    fun selectProject(project: MelodyProject?) {
        _selectedProject.value = project
        _exportedContent.value = null
        stopMelodyPlayback()
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            melodyRepository.deleteProject(projectId)
            if (_selectedProject.value?.id == projectId) {
                _selectedProject.value = null
            }
        }
    }

    fun updateProjectDetails(
        lyrics: String,
        chords: String,
        prompt: String,
        emotion: String,
        genre: String,
        mood: String,
        scale: String,
        raga: String,
        tempo: Int,
        vocalStyle: String,
        sectionType: String
    ) {
        val current = _selectedProject.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                lyrics = lyrics,
                chords = chords,
                prompt = prompt,
                emotion = emotion,
                genre = genre,
                mood = mood,
                scale = scale,
                raga = raga,
                tempo = tempo,
                vocalStyle = vocalStyle,
                sectionType = sectionType
            )
            melodyRepository.updateProject(updated)
            _selectedProject.value = updated
        }
    }

    fun generateMelody() {
        val project = _selectedProject.value ?: return
        _generationState.value = MelodyGenerationState.PLANNING
        
        viewModelScope.launch {
            try {
                delay(800) // Aesthetic planning delay
                _generationState.value = MelodyGenerationState.GENERATING
                delay(1200) // Synthesizing / pipeline delay
                _generationState.value = MelodyGenerationState.EVALUATING
                delay(600) // Aesthetic scorer latency
                
                val plan = melodyRepository.generateMelody(project)
                _generatedPlan.value = plan
                
                // Refresh our project entity reference from local database to sync Json
                val db = AppDatabase.getDatabase(getApplication())
                val updatedEntity = db.melodyDao().getProjectById(project.id)
                if (updatedEntity != null) {
                    _selectedProject.value = updatedEntity.toDomain()
                }
                
                _generationState.value = MelodyGenerationState.COMPLETE
            } catch (e: Exception) {
                Log.e("MelodyViewModel", "Failed to generate melody: ${e.message}")
                _generationState.value = MelodyGenerationState.ERROR
            }
        }
    }

    fun exportMelody(format: String) {
        val plan = _generatedPlan.value ?: return
        viewModelScope.launch {
            val content = melodyRepository.exportMelody(plan, format)
            _exportedContent.value = content
        }
    }

    /**
     * ADVANCED AUDIO DSP SYNTHESIZER
     * Streams real-time sine wave notes via android AudioTrack in background thread.
     */
    fun playMelodyPlayback() {
        val plan = _generatedPlan.value ?: return
        val notes = plan.noteSequence
        if (notes.isEmpty()) return

        stopMelodyPlayback()

        playbackJob = viewModelScope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            try {
                synthTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize.coerceAtLeast(4096))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                synthTrack?.play()

                notes.forEachIndexed { index, note ->
                    _activePlayingNoteIndex.value = index
                    
                    val durationMs = (note.durationSeconds * 1000).toLong()
                    val totalSamples = (sampleRate * note.durationSeconds).toInt()
                    val samples = ShortArray(totalSamples)
                    val frequency = note.pitchHz

                    // Synthesize beautiful sinusoidal wave with fade-in and fade-out envelope
                    for (i in 0 until totalSamples) {
                        val time = i.toDouble() / sampleRate
                        val rawSine = Math.sin(2.0 * Math.PI * frequency * time)
                        
                        // Apply envelope (attack & decay) to remove harsh audio clicks
                        val envelope = when {
                            i < sampleRate * 0.05 -> i.toDouble() / (sampleRate * 0.05) // Attack (50ms)
                            i > totalSamples - sampleRate * 0.05 -> (totalSamples - i).toDouble() / (sampleRate * 0.05) // Decay (50ms)
                            else -> 1.0
                        }
                        
                        samples[i] = (rawSine * 32767.0 * 0.6 * envelope).toInt().toShort()
                    }

                    synthTrack?.write(samples, 0, totalSamples)
                    delay(durationMs)
                }
            } catch (e: Exception) {
                Log.e("MelodyViewModel", "DSP synthesizer playback failed: ${e.message}", e)
            } finally {
                _activePlayingNoteIndex.value = -1
                synthTrack?.stop()
                synthTrack?.release()
                synthTrack = null
            }
        }
    }

    fun stopMelodyPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        _activePlayingNoteIndex.value = -1
        try {
            synthTrack?.stop()
            synthTrack?.release()
            synthTrack = null
        } catch (e: Exception) {
            // Safe ignore
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopMelodyPlayback()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = ServiceLocator.getMelodyRepository(context)
            return MelodyViewModel(context.applicationContext as Application, repo) as T
        }
    }
}

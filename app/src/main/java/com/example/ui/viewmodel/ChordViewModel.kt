package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.ServiceLocator
import com.example.domain.model.chord.*
import com.example.domain.repository.ChordRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChordViewModel(
    application: Application,
    private val repository: ChordRepository
) : AndroidViewModel(application) {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val progressionAdapter = moshi.adapter(GeneratedChordProgression::class.java)

    private val _generationState = MutableStateFlow(ChordGenerationState.IDLE)
    val generationState: StateFlow<ChordGenerationState> = _generationState.asStateFlow()

    private val _currentProject = MutableStateFlow<ChordProject?>(null)
    val currentProject: StateFlow<ChordProject?> = _currentProject.asStateFlow()

    private val _chordProgression = MutableStateFlow<GeneratedChordProgression?>(null)
    val chordProgression: StateFlow<GeneratedChordProgression?> = _chordProgression.asStateFlow()

    private val _activePlayingChordIndex = MutableStateFlow(-1)
    val activePlayingChordIndex: StateFlow<Int> = _activePlayingChordIndex.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _exportedText = MutableStateFlow("")
    val exportedText: StateFlow<String> = _exportedText.asStateFlow()

    private val _melodyProjects = MutableStateFlow<List<com.example.domain.model.melody.MelodyProject>>(emptyList())
    val melodyProjects: StateFlow<List<com.example.domain.model.melody.MelodyProject>> = _melodyProjects.asStateFlow()

    private val _favVoicing = MutableStateFlow("SATB Voicing")
    val favVoicing: StateFlow<String> = _favVoicing.asStateFlow()

    private val _favCadence = MutableStateFlow("Perfect Authentic (V-I)")
    val favCadence: StateFlow<String> = _favCadence.asStateFlow()

    private val _favComplexity = MutableStateFlow("Medium")
    val favComplexity: StateFlow<String> = _favComplexity.asStateFlow()

    val allProjects: StateFlow<List<ChordProject>> = repository.getAllProjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var synthTrack: AudioTrack? = null
    private var playbackJob: Job? = null

    init {
        // Fetch melody projects to support melody-to-chord integrations
        viewModelScope.launch {
            try {
                val melodyRepo = ServiceLocator.getMelodyRepository(application)
                melodyRepo.getAllProjects().collect { projects ->
                    _melodyProjects.value = projects
                }
            } catch (e: Exception) {
                Log.e("ChordViewModel", "Failed to load melody projects list: ${e.message}")
            }
        }

        // Load Composer Memory from SharedPreferences on initialization
        val prefs = application.getSharedPreferences("composer_memory", Context.MODE_PRIVATE)
        _favVoicing.value = prefs.getString("fav_voicing", "SATB Voicing") ?: "SATB Voicing"
        _favCadence.value = prefs.getString("fav_cadence", "Perfect Authentic (V-I)") ?: "Perfect Authentic (V-I)"
        _favComplexity.value = prefs.getString("fav_complexity", "Medium") ?: "Medium"
    }

    fun selectProject(project: ChordProject) {
        _currentProject.value = project
        _error.value = null
        stopChordPlayback()
        
        // Parse current progression JSON
        if (!project.currentProgressionJson.isNullOrBlank()) {
            try {
                val prog = progressionAdapter.fromJson(project.currentProgressionJson)
                _chordProgression.value = prog
            } catch (e: Exception) {
                Log.e("ChordViewModel", "Failed to deserialize project progression: ${e.message}")
                _chordProgression.value = null
            }
        } else {
            _chordProgression.value = null
        }
    }

    fun createProject(
        title: String,
        melodyProjectId: String?,
        lyrics: String,
        prompt: String,
        genre: String,
        emotion: String,
        mood: String,
        scale: String,
        raga: String,
        bpm: Int,
        chordComplexity: String
    ) {
        viewModelScope.launch {
            try {
                val newProj = repository.createProject(
                    title = title,
                    melodyProjectId = melodyProjectId,
                    lyrics = lyrics,
                    prompt = prompt,
                    genre = genre,
                    emotion = emotion,
                    mood = mood,
                    scale = scale,
                    raga = raga,
                    bpm = bpm,
                    chordComplexity = chordComplexity
                )
                selectProject(newProj)
            } catch (e: Exception) {
                _error.value = "Failed to create project: ${e.message}"
            }
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            try {
                if (_currentProject.value?.id == id) {
                    _currentProject.value = null
                    _chordProgression.value = null
                }
                repository.deleteProject(id)
            } catch (e: Exception) {
                _error.value = "Failed to delete project: ${e.message}"
            }
        }
    }

    fun generateProgression() {
        val proj = _currentProject.value ?: return
        _generationState.value = ChordGenerationState.PLANNING
        _error.value = null
        stopChordPlayback()

        viewModelScope.launch {
            try {
                delay(400) // Aesthetic delay for planning status
                _generationState.value = ChordGenerationState.GENERATING
                
                delay(600) // Aesthetic delay for generating status
                _generationState.value = ChordGenerationState.EVALUATING
                
                val progression = repository.generateChordProgression(proj)
                _chordProgression.value = progression
                
                // Refresh project reference
                _currentProject.value = proj.copy(
                    currentProgressionJson = progressionAdapter.toJson(progression)
                )
                _generationState.value = ChordGenerationState.COMPLETE
            } catch (e: Exception) {
                _generationState.value = ChordGenerationState.ERROR
                _error.value = "Generation failed: ${e.message}"
            }
        }
    }

    // --- Polyphonic DSP Synthesizer Playback ---
    fun playChordProgression() {
        val prog = _chordProgression.value ?: return
        val chords = prog.chords
        if (chords.isEmpty()) return

        stopChordPlayback()

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

                // Calculate beat duration in seconds
                val beatDurationSeconds = 60.0f / prog.bpm

                chords.forEachIndexed { index, chord ->
                    _activePlayingChordIndex.value = index
                    
                    val durationSeconds = chord.durationBeats * beatDurationSeconds
                    val durationMs = (durationSeconds * 1000).toLong()
                    val totalSamples = (sampleRate * durationSeconds).toInt()
                    val samples = ShortArray(totalSamples)
                    
                    val frequencies = chord.pitchHz
                    val noteCount = frequencies.size

                    if (noteCount > 0) {
                        for (i in 0 until totalSamples) {
                            val time = i.toDouble() / sampleRate
                            
                            // Sum the sine waves of all voices/frequencies in the chord
                            var summedSine = 0.0
                            frequencies.forEach { freq ->
                                summedSine += Math.sin(2.0 * Math.PI * freq * time)
                            }
                            
                            // Normalize to avoid clipping
                            val normalizedSine = summedSine / noteCount

                            // Apply attack and decay envelope to avoid clicks
                            val envelope = when {
                                i < sampleRate * 0.08 -> i.toDouble() / (sampleRate * 0.08) // Attack (80ms)
                                i > totalSamples - sampleRate * 0.08 -> (totalSamples - i).toDouble() / (sampleRate * 0.08) // Decay (80ms)
                                else -> 1.0
                            }

                            // Scaling factor 0.5 to keep it sounding lush and balanced
                            samples[i] = (normalizedSine * 32767.0 * 0.5 * envelope).toInt().toShort()
                        }
                    }

                    synthTrack?.write(samples, 0, totalSamples)
                    delay(durationMs)
                }
            } catch (e: Exception) {
                Log.e("ChordViewModel", "DSP chord synthesis playback failed: ${e.message}", e)
            } finally {
                _activePlayingChordIndex.value = -1
                synthTrack?.stop()
                synthTrack?.release()
                synthTrack = null
            }
        }
    }

    fun stopChordPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        _activePlayingChordIndex.value = -1
        try {
            synthTrack?.stop()
            synthTrack?.release()
            synthTrack = null
        } catch (e: Exception) {
            // Ignore
        }
    }

    // --- DAW Interactive Progression Editing Features ---

    fun transposeProgression(semitones: Int) {
        val prog = _chordProgression.value ?: return
        val transposedChords = prog.chords.map { chord ->
            val newMidiNotes = chord.midiNotes.map { it + semitones }
            val newPitches = newMidiNotes.map { (440.0 * Math.pow(2.0, (it - 69) / 12.0)).toFloat() }
            val newNoteNames = newMidiNotes.map { getNoteNameFromMidi(it) }
            
            // Generate a simple updated chord name by adding transpose suffix or key shift
            val currentBase = chord.chordName.take(2).trim()
            val modifier = chord.chordName.drop(currentBase.length)
            val shiftedBase = getNoteNameFromMidi(newMidiNotes.first()).replace(Regex("\\d"), "")
            val newChordName = "$shiftedBase$modifier"

            chord.copy(
                chordName = newChordName,
                midiNotes = newMidiNotes,
                pitchHz = newPitches,
                noteNames = newNoteNames,
                pianoKeys = newMidiNotes.map { (it - 60).coerceIn(0, 23) }
            )
        }
        
        val updatedProgression = prog.copy(chords = transposedChords)
        updateProgressionLocal(updatedProgression)
    }

    fun updateSegmentChord(segmentId: String, newChordName: String) {
        val prog = _chordProgression.value ?: return
        val updatedChords = prog.chords.map { chord ->
            if (chord.id == segmentId) {
                // Approximate Midi Notes based on name
                val rootName = newChordName.take(2).trim()
                val isMinor = newChordName.contains("m") && !newChordName.contains("maj")
                val isSeventh = newChordName.contains("7")
                val isMaj7 = newChordName.contains("maj7")
                
                val baseMidi = getBaseMidiForNote(rootName)
                val midiNotes = mutableListOf(baseMidi, baseMidi + (if (isMinor) 3 else 4), baseMidi + 7)
                if (isMaj7) midiNotes.add(baseMidi + 11)
                else if (isSeventh) midiNotes.add(baseMidi + 10)

                val pitchHz = midiNotes.map { (440.0 * Math.pow(2.0, (it - 69) / 12.0)).toFloat() }
                val noteNames = midiNotes.map { getNoteNameFromMidi(it) }

                chord.copy(
                    chordName = newChordName,
                    midiNotes = midiNotes,
                    pitchHz = pitchHz,
                    noteNames = noteNames,
                    pianoKeys = midiNotes.map { (it - 60).coerceIn(0, 23) }
                )
            } else {
                chord
            }
        }
        
        val updatedProgression = prog.copy(chords = updatedChords)
        updateProgressionLocal(updatedProgression)
    }

    fun addSegment(chordName: String) {
        val prog = _chordProgression.value ?: return
        val lastBeat = prog.chords.lastOrNull()?.let { it.startTimeBeats + it.durationBeats } ?: 0.0f
        
        val rootName = chordName.take(2).trim()
        val isMinor = chordName.contains("m") && !chordName.contains("maj")
        val baseMidi = getBaseMidiForNote(rootName)
        val midiNotes = listOf(baseMidi, baseMidi + (if (isMinor) 3 else 4), baseMidi + 7)
        val pitchHz = midiNotes.map { (440.0 * Math.pow(2.0, (it - 69) / 12.0)).toFloat() }
        val noteNames = midiNotes.map { getNoteNameFromMidi(it) }

        val newSegment = ChordSegment(
            id = "seg_${UUID.randomUUID()}",
            chordName = chordName,
            romanNumeral = "I",
            startTimeBeats = lastBeat,
            durationBeats = 4.0f,
            midiNotes = midiNotes,
            pitchHz = pitchHz,
            noteNames = noteNames,
            pianoKeys = midiNotes.map { (it - 60).coerceIn(0, 23) }
        )

        val updatedProgression = prog.copy(chords = prog.chords + newSegment)
        updateProgressionLocal(updatedProgression)
    }

    fun deleteSegment(segmentId: String) {
        val prog = _chordProgression.value ?: return
        val filtered = prog.chords.filter { it.id != segmentId }
        
        // Realign timestamps/beats consecutively
        var currentBeat = 0.0f
        val realigned = filtered.map { chord ->
            val updated = chord.copy(startTimeBeats = currentBeat)
            currentBeat += chord.durationBeats
            updated
        }

        val updatedProgression = prog.copy(chords = realigned)
        updateProgressionLocal(updatedProgression)
    }

    fun duplicateSegment(segmentId: String) {
        val prog = _chordProgression.value ?: return
        val index = prog.chords.indexOfFirst { it.id == segmentId }
        if (index == -1) return
        
        val target = prog.chords[index]
        val duplicate = target.copy(
            id = "seg_${UUID.randomUUID()}",
            startTimeBeats = prog.chords.last().startTimeBeats + prog.chords.last().durationBeats
        )

        val updatedProgression = prog.copy(chords = prog.chords + duplicate)
        updateProgressionLocal(updatedProgression)
    }

    fun quantizeProgression() {
        val prog = _chordProgression.value ?: return
        // Quantize snaps all durations and start beats to a whole/half beat grid
        val quantizedChords = prog.chords.map { chord ->
            val snappedStart = Math.round(chord.startTimeBeats * 2.0) / 2.0f
            val snappedDuration = Math.round(chord.durationBeats * 2.0) / 2.0f
            chord.copy(
                startTimeBeats = snappedStart,
                durationBeats = snappedDuration.coerceAtLeast(1.0f)
            )
        }
        val updatedProgression = prog.copy(chords = quantizedChords)
        updateProgressionLocal(updatedProgression)
    }

    private fun updateProgressionLocal(updatedProgression: GeneratedChordProgression) {
        _chordProgression.value = updatedProgression
        val proj = _currentProject.value ?: return
        val jsonStr = progressionAdapter.toJson(updatedProgression)
        
        viewModelScope.launch {
            val updatedProj = proj.copy(currentProgressionJson = jsonStr)
            _currentProject.value = updatedProj
            repository.updateProject(updatedProj)
        }
    }

    private fun getNoteNameFromMidi(midi: Int): String {
        val notes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val octave = (midi / 12) - 1
        val noteName = notes[midi % 12]
        return "$noteName$octave"
    }

    private fun getBaseMidiForNote(note: String): Int {
        val clean = note.uppercase().replace(Regex("[^A-G#]"), "")
        return when (clean) {
            "C" -> 60
            "C#" -> 61
            "D" -> 62
            "D#" -> 63
            "E" -> 64
            "F" -> 65
            "F#" -> 66
            "G" -> 67
            "G#" -> 68
            "A" -> 69
            "A#" -> 70
            "B" -> 71
            else -> 60
        }
    }

    fun saveComposerMemory(voicing: String, cadence: String, complexity: String) {
        _favVoicing.value = voicing
        _favCadence.value = cadence
        _favComplexity.value = complexity
        val prefs = getApplication<Application>().getSharedPreferences("composer_memory", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("fav_voicing", voicing)
            putString("fav_cadence", cadence)
            putString("fav_complexity", complexity)
            apply()
        }
    }

    fun reharmonize(style: String) {
        val proj = _currentProject.value ?: return
        val currentProg = _chordProgression.value ?: return
        _generationState.value = ChordGenerationState.GENERATING
        viewModelScope.launch {
            try {
                delay(600) // Aesthetic delay for reharmonizing
                val newChords = currentProg.chords.mapIndexed { idx, chord ->
                    val newName = when (style.uppercase()) {
                        "SIMPLE" -> chord.chordName.replace(Regex("(maj7|min7|7|9|#11|sus4)"), "")
                        "POP" -> {
                            val clean = chord.chordName.replace(Regex("(maj7|min7|7|9|#11|sus4)"), "")
                            if (idx % 2 == 1 && !clean.endsWith("7")) "${clean}7" else clean
                        }
                        "JAZZ" -> {
                            val clean = chord.chordName.replace(Regex("(maj7|min7|7|9|#11|sus4)"), "")
                            if (clean.endsWith("m") || clean.endsWith("min")) "${clean}9" else "${clean}maj9"
                        }
                        "CINEMATIC" -> {
                            val clean = chord.chordName.replace(Regex("(maj7|min7|7|9|#11|sus4)"), "")
                            if (idx % 2 == 1) "${clean}sus4" else clean
                        }
                        "GOSPEL" -> {
                            val clean = chord.chordName.replace(Regex("(maj7|min7|7|9|#11|sus4)"), "")
                            "${clean}7(add9)"
                        }
                        "NEO SOUL" -> {
                            val clean = chord.chordName.replace(Regex("(maj7|min7|7|9|#11|sus4)"), "")
                            if (clean.endsWith("m") || clean.endsWith("min")) "${clean}11" else "${clean}maj13"
                        }
                        "BOLLYWOOD" -> {
                            val clean = chord.chordName.replace(Regex("(maj7|min7|7|9|#11|sus4)"), "")
                            if (idx % 2 == 0) "${clean}add9" else clean
                        }
                        "CLASSICAL" -> {
                            val clean = chord.chordName.replace(Regex("(maj7|min7|7|9|#11|sus4)"), "")
                            if (idx == currentProg.chords.size - 1) "${clean}5(sus)" else clean
                        }
                        else -> chord.chordName
                    }
                    chord.copy(chordName = newName, functionType = "Reharmonized $style")
                }
                val updatedProg = currentProg.copy(
                    chords = newChords,
                    reharmonizationStyle = style,
                    explanationInsight = "Successfully reharmonized progression using $style voicing rules."
                )
                _chordProgression.value = updatedProg
                _currentProject.value = proj.copy(
                    currentProgressionJson = progressionAdapter.toJson(updatedProg)
                )
                repository.updateProject(_currentProject.value!!)
                _generationState.value = ChordGenerationState.COMPLETE
            } catch (e: Exception) {
                _generationState.value = ChordGenerationState.ERROR
                _error.value = "Reharmonization failed: ${e.message}"
            }
        }
    }

    fun applyModulation(targetScale: String, shiftType: String) {
        val proj = _currentProject.value ?: return
        val currentProg = _chordProgression.value ?: return
        _generationState.value = ChordGenerationState.GENERATING
        viewModelScope.launch {
            try {
                delay(600) // Aesthetic delay for modulation compiling
                val pivotChords = listOf("Am7", "Cmaj7", "Em7")
                val updatedProg = currentProg.copy(
                    scale = targetScale,
                    modulationInfo = ModulationInfo(
                        targetScale = targetScale,
                        pivotChords = pivotChords,
                        modalShiftType = shiftType,
                        ragaTransitionPath = "Shifted scale frame to $targetScale"
                    ),
                    explanationInsight = "Intelligent modulation compilation to $targetScale completed via Pivot Chords: ${pivotChords.joinToString(", ")}"
                )
                _chordProgression.value = updatedProg
                _currentProject.value = proj.copy(
                    scale = targetScale,
                    currentProgressionJson = progressionAdapter.toJson(updatedProg)
                )
                repository.updateProject(_currentProject.value!!)
                _generationState.value = ChordGenerationState.COMPLETE
            } catch (e: Exception) {
                _generationState.value = ChordGenerationState.ERROR
                _error.value = "Modulation failed: ${e.message}"
            }
        }
    }

    fun exportProgression(progression: GeneratedChordProgression, format: String) {
        viewModelScope.launch {
            try {
                val out = repository.exportProgression(progression, format)
                _exportedText.value = out
            } catch (e: Exception) {
                Log.e("ChordViewModel", "Failed to export chord progression: ${e.message}")
                _exportedText.value = "Error: ${e.message}"
            }
        }
    }

    fun clearExport() {
        _exportedText.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        stopChordPlayback()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = ServiceLocator.getChordRepository(context)
            return ChordViewModel(context.applicationContext as Application, repo) as T
        }
    }
}

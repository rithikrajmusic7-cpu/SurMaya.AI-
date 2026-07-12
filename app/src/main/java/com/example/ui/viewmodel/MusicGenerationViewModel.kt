package com.example.ui.viewmodel

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.ServiceLocator
import com.example.domain.repository.MusicGenerationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class MusicGenerationViewModel(
    private val repository: MusicGenerationRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _generatedFile = MutableStateFlow<File?>(null)
    val generatedFile: StateFlow<File?> = _generatedFile

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    fun generateMusic(prompt: String, durationSec: Int) {
        viewModelScope.launch {
            _isGenerating.value = true
            _errorMessage.value = null
            _generatedFile.value = null
            stopAudio()

            val result = repository.generateMusic(prompt, durationSec)
            _isGenerating.value = false
            result.fold(
                onSuccess = { file ->
                    _generatedFile.value = file
                    playAudio(file)
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Failed to generate AI music"
                }
            )
        }
    }

    fun generateVoiceSample(prompt: String, voiceName: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            _errorMessage.value = null
            _generatedFile.value = null
            stopAudio()

            val result = repository.generateVoiceSample(prompt, voiceName)
            _isGenerating.value = false
            result.fold(
                onSuccess = { file ->
                    _generatedFile.value = file
                    playAudio(file)
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Failed to generate AI voice sample"
                }
            )
        }
    }

    fun playAudio(file: File) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                stopAudio()
                val player = MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    prepare()
                    start()
                }
                mediaPlayer = player
                _isPlaying.value = true
                _generatedFile.value = file

                player.setOnCompletionListener {
                    _isPlaying.value = false
                    _playbackProgress.value = 1.0f
                    stopProgressTracker()
                }

                startProgressTracker(player)
            } catch (e: Exception) {
                _errorMessage.value = "Playback error: ${e.message}"
            }
        }
    }

    fun togglePlayback() {
        val player = mediaPlayer
        val file = _generatedFile.value
        if (player != null) {
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
                stopProgressTracker()
            } else {
                player.start()
                _isPlaying.value = true
                startProgressTracker(player)
            }
        } else if (file != null) {
            playAudio(file)
        }
    }

    fun stopAudio() {
        _isPlaying.value = false
        _playbackProgress.value = 0f
        stopProgressTracker()
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
    }

    private fun startProgressTracker(player: MediaPlayer) {
        stopProgressTracker()
        progressJob = viewModelScope.launch(Dispatchers.Main) {
            while (player.isPlaying) {
                val duration = player.duration.toFloat()
                if (duration > 0) {
                    _playbackProgress.value = player.currentPosition.toFloat() / duration
                }
                delay(250)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = ServiceLocator.getMusicGenerationRepository(application)
            return MusicGenerationViewModel(repo, application) as T
        }
    }
}

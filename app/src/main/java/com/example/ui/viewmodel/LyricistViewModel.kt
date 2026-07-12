package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.ServiceLocator
import com.example.data.remote.gateway.lyrics.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.UUID

class LyricistViewModel(
    application: Application,
    private val aiLyricistGateway: AILyricistGateway
) : AndroidViewModel(application) {

    private val _allProjects = MutableStateFlow<List<LyricProject>>(emptyList())
    val allProjects: StateFlow<List<LyricProject>> = _allProjects.asStateFlow()

    private val _currentProject = MutableStateFlow<LyricProject?>(null)
    val currentProject: StateFlow<LyricProject?> = _currentProject.asStateFlow()

    private val _lyricsParams = MutableStateFlow(LyricsGenerationParams(prompt = ""))
    val lyricsParams: StateFlow<LyricsGenerationParams> = _lyricsParams.asStateFlow()

    private val _selectedText = MutableStateFlow<String?>(null)
    val selectedText: StateFlow<String?> = _selectedText.asStateFlow()

    private val _qualityReport = MutableStateFlow<QualityScoreReport?>(null)
    val qualityReport: StateFlow<QualityScoreReport?> = _qualityReport.asStateFlow()

    private val _smartSuggestions = MutableStateFlow<SmartSuggestions?>(null)
    val smartSuggestions: StateFlow<SmartSuggestions?> = _smartSuggestions.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var analysisJob: Job? = null
    private var autoSaveJob: Job? = null

    init {
        loadAllProjects()
    }

    fun loadAllProjects() {
        viewModelScope.launch {
            _allProjects.value = aiLyricistGateway.getAllProjects()
        }
    }

    fun updateLyricsParams(params: LyricsGenerationParams) {
        _lyricsParams.value = params
    }

    fun selectTextSegment(text: String?) {
        _selectedText.value = if (text.isNullOrBlank()) null else text
    }

    fun selectProject(project: LyricProject) {
        _currentProject.value = project
        _errorMessage.value = null
        triggerAnalysisAndSuggestions(project.currentLyrics, project.language)
    }

    fun deselectProject() {
        _currentProject.value = null
        _selectedText.value = null
        _qualityReport.value = null
        _smartSuggestions.value = null
    }

    fun createNewProject() {
        val blankProject = LyricProject(
            id = UUID.randomUUID().toString(),
            title = "Untitled Song",
            currentLyrics = """[Verse 1]
Write your lyrics here...""",
            language = "Hindi",
            versions = listOf(
                LyricVersion(
                    id = UUID.randomUUID().toString(),
                    label = "v1 - Draft",
                    content = "[Verse 1]\nWrite your lyrics here...",
                    author = "User"
                )
            ),
            chatHistory = listOf(
                LyricChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "ai",
                    text = "Welcome to your AI Lyricist Workspace! Edit your lyrics on the left, or use the chat assistant on the right to improve your verses!"
                )
            )
        )
        viewModelScope.launch {
            aiLyricistGateway.saveProject(blankProject)
            selectProject(blankProject)
            loadAllProjects()
        }
    }

    fun updateLyricsInEditor(content: String) {
        val project = _currentProject.value ?: return
        if (project.currentLyrics == content) return

        val updated = project.copy(
            currentLyrics = content,
            lastAutoSaved = System.currentTimeMillis()
        )
        _currentProject.value = updated

        // Debounce Quality Analysis and Autosave
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            delay(1500) // Debounce delay
            triggerAnalysisAndSuggestions(content, project.language)
        }

        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(3000) // Autosave debounce
            val nextVerNum = project.versions.size + 1
            // Append a new version if there are significant changes
            val finalProject = if (project.versions.lastOrNull()?.content != content) {
                val newVer = LyricVersion(
                    id = UUID.randomUUID().toString(),
                    label = "v$nextVerNum - Auto Saved",
                    content = content,
                    author = "User"
                )
                updated.copy(versions = project.versions + newVer)
            } else {
                updated
            }
            aiLyricistGateway.saveProject(finalProject)
            loadAllProjects()
        }
    }

    fun generateLyricsStructured(language: String) {
        val params = _lyricsParams.value
        if (params.prompt.isBlank()) {
            _errorMessage.value = "Prompt is required to generate lyrics!"
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            _errorMessage.value = null
            val result = aiLyricistGateway.generateLyricsStructured(params, language)
            _isGenerating.value = false
            result.fold(
                onSuccess = { project ->
                    selectProject(project)
                    loadAllProjects()
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Failed to generate structured lyrics"
                }
            )
        }
    }

    fun generateLyricsChat(message: String) {
        val project = _currentProject.value ?: return
        if (message.isBlank()) return

        viewModelScope.launch {
            _isGenerating.value = true
            _errorMessage.value = null
            val result = aiLyricistGateway.generateLyricsChat(project, message, _selectedText.value)
            _isGenerating.value = false
            result.fold(
                onSuccess = { updatedProject ->
                    selectProject(updatedProject)
                    _selectedText.value = null // clear selection after application
                    loadAllProjects()
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Failed to update lyrics via chat"
                }
            )
        }
    }

    fun restoreVersion(version: LyricVersion) {
        val project = _currentProject.value ?: return
        val restored = project.copy(
            currentLyrics = version.content,
            lastAutoSaved = System.currentTimeMillis()
        )
        viewModelScope.launch {
            aiLyricistGateway.saveProject(restored)
            selectProject(restored)
            loadAllProjects()
        }
    }

    fun toggleVersionFavorite(versionId: String) {
        val project = _currentProject.value ?: return
        val updatedVersions = project.versions.map {
            if (it.id == versionId) it.copy(isFavorite = !it.isFavorite) else it
        }
        val updatedProject = project.copy(versions = updatedVersions)
        _currentProject.value = updatedProject
        viewModelScope.launch {
            aiLyricistGateway.saveProject(updatedProject)
        }
    }

    fun duplicateProject() {
        val project = _currentProject.value ?: return
        val duplicated = project.copy(
            id = UUID.randomUUID().toString(),
            title = "${project.title} (Copy)",
            createdTimestamp = System.currentTimeMillis(),
            lastAutoSaved = System.currentTimeMillis()
        )
        viewModelScope.launch {
            aiLyricistGateway.saveProject(duplicated)
            selectProject(duplicated)
            loadAllProjects()
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            aiLyricistGateway.deleteProject(projectId)
            if (_currentProject.value?.id == projectId) {
                deselectProject()
            }
            loadAllProjects()
        }
    }

    fun undoLastEdit() {
        val project = _currentProject.value ?: return
        val versions = project.versions
        if (versions.isEmpty()) return

        val currentIndex = versions.indexOfFirst { it.content == project.currentLyrics }
        if (currentIndex > 0) {
            val prev = versions[currentIndex - 1]
            val restored = project.copy(
                currentLyrics = prev.content,
                lastAutoSaved = System.currentTimeMillis()
            )
            viewModelScope.launch {
                aiLyricistGateway.saveProject(restored)
                selectProject(restored)
                loadAllProjects()
            }
        } else if (currentIndex == -1 && versions.isNotEmpty()) {
            val last = versions.last()
            val restored = project.copy(
                currentLyrics = last.content,
                lastAutoSaved = System.currentTimeMillis()
            )
            viewModelScope.launch {
                aiLyricistGateway.saveProject(restored)
                selectProject(restored)
                loadAllProjects()
            }
        }
    }

    fun redoLastEdit() {
        val project = _currentProject.value ?: return
        val versions = project.versions
        val currentIndex = versions.indexOfFirst { it.content == project.currentLyrics }
        if (currentIndex != -1 && currentIndex < versions.size - 1) {
            val next = versions[currentIndex + 1]
            val restored = project.copy(
                currentLyrics = next.content,
                lastAutoSaved = System.currentTimeMillis()
            )
            viewModelScope.launch {
                aiLyricistGateway.saveProject(restored)
                selectProject(restored)
                loadAllProjects()
            }
        }
    }

    fun renameVersion(versionId: String, newLabel: String) {
        val project = _currentProject.value ?: return
        val updatedVersions = project.versions.map {
            if (it.id == versionId) it.copy(label = newLabel) else it
        }
        val updatedProject = project.copy(versions = updatedVersions)
        _currentProject.value = updatedProject
        viewModelScope.launch {
            aiLyricistGateway.saveProject(updatedProject)
            loadAllProjects()
        }
    }

    fun deleteVersion(versionId: String) {
        val project = _currentProject.value ?: return
        if (project.versions.size <= 1) return
        val updatedVersions = project.versions.filter { it.id != versionId }
        val updatedProject = project.copy(versions = updatedVersions)
        _currentProject.value = updatedProject
        viewModelScope.launch {
            aiLyricistGateway.saveProject(updatedProject)
            loadAllProjects()
        }
    }

    fun branchVersion(version: LyricVersion, newTitle: String) {
        val project = _currentProject.value ?: return
        val branchedProject = LyricProject(
            id = UUID.randomUUID().toString(),
            title = newTitle,
            currentLyrics = version.content,
            language = project.language,
            createdTimestamp = System.currentTimeMillis(),
            versions = listOf(
                version.copy(
                    id = UUID.randomUUID().toString(),
                    label = "v1 - Branched from ${version.label}",
                    timestamp = System.currentTimeMillis(),
                    versionNumber = 1,
                    editSummary = "Branched song start"
                )
            ),
            genre = version.genre.ifBlank { project.genre },
            mood = version.mood.ifBlank { project.mood },
            prompt = project.prompt,
            chatHistory = listOf(
                LyricChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "ai",
                    text = "Branched from project '${project.title}' (version '${version.label}') successfully!"
                )
            )
        )
        viewModelScope.launch {
            aiLyricistGateway.saveProject(branchedProject)
            selectProject(branchedProject)
            loadAllProjects()
        }
    }

    fun updateProjectSettings(title: String, genre: String, mood: String, story: String, songStructure: String) {
        val project = _currentProject.value ?: return
        val updated = project.copy(
            title = title,
            genre = genre,
            mood = mood,
            story = story,
            songStructure = songStructure,
            lastAutoSaved = System.currentTimeMillis()
        )
        _currentProject.value = updated
        viewModelScope.launch {
            aiLyricistGateway.saveProject(updated)
            loadAllProjects()
        }
    }

    private fun triggerAnalysisAndSuggestions(lyrics: String, language: String) {
        viewModelScope.launch {
            _qualityReport.value = aiLyricistGateway.analyzeQuality(lyrics, language)
            _smartSuggestions.value = aiLyricistGateway.getSmartSuggestions(lyrics, language)
        }
    }

    class Factory(
        private val application: Application,
        private val aiLyricistGateway: AILyricistGateway
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LyricistViewModel::class.java)) {
                return LyricistViewModel(application, aiLyricistGateway) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

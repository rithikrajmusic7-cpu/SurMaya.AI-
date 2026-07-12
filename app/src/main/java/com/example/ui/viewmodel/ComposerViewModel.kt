package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.ServiceLocator
import com.example.domain.model.composer.*
import com.example.domain.repository.ComposerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ComposerViewModel(
    application: Application,
    private val composerRepository: ComposerRepository
) : AndroidViewModel(application) {

    // All available composition projects
    val allProjects: StateFlow<List<ComposerProject>> = composerRepository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently selected composition project
    private val _selectedProject = MutableStateFlow<ComposerProject?>(null)
    val selectedProject: StateFlow<ComposerProject?> = _selectedProject.asStateFlow()

    // Current compilation / generation state of the plan
    private val _compilationState = MutableStateFlow<PlanCompilationState>(PlanCompilationState.Idle)
    val compilationState: StateFlow<PlanCompilationState> = _compilationState.asStateFlow()

    // Versions of the currently selected project
    val projectVersions: StateFlow<List<CompositionVersion>> = _selectedProject
        .flatMapLatest { project ->
            if (project != null) {
                composerRepository.getVersionsForProject(project.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Export formats state
    private val _exportedContent = MutableStateFlow<String?>(null)
    val exportedContent: StateFlow<String?> = _exportedContent.asStateFlow()

    fun createProject(
        title: String,
        lyrics: String,
        language: String,
        genre: String,
        mood: String,
        filmSituation: String,
        era: String,
        productionScale: String,
        emotionalJourney: String,
        instrumentPreferences: String,
        userNotes: String
    ) {
        viewModelScope.launch {
            val newProj = composerRepository.createProject(
                title = title,
                lyrics = lyrics,
                language = language,
                genre = genre,
                mood = mood,
                filmSituation = filmSituation,
                era = era,
                productionScale = productionScale,
                emotionalJourney = emotionalJourney,
                instrumentPreferences = instrumentPreferences,
                userNotes = userNotes
            )
            _selectedProject.value = newProj
        }
    }

    fun selectProject(project: ComposerProject?) {
        _selectedProject.value = project
        _exportedContent.value = null
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            composerRepository.deleteProject(projectId)
            if (_selectedProject.value?.id == projectId) {
                _selectedProject.value = null
            }
        }
    }

    fun updateProjectDetails(
        lyrics: String,
        filmSituation: String,
        era: String,
        productionScale: String,
        emotionalJourney: String,
        instrumentPreferences: String,
        userNotes: String
    ) {
        val current = _selectedProject.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                lyrics = lyrics,
                filmSituation = filmSituation,
                era = era,
                productionScale = productionScale,
                emotionalJourney = emotionalJourney,
                instrumentPreferences = instrumentPreferences,
                userNotes = userNotes
            )
            composerRepository.updateProject(updated)
            _selectedProject.value = updated
        }
    }

    fun compileCompositionPlan() {
        val project = _selectedProject.value ?: return
        _compilationState.value = PlanCompilationState.Compiling(10, "Initializing Composer Operating System...")
        
        viewModelScope.launch {
            try {
                _compilationState.value = PlanCompilationState.Compiling(35, "Analyzing Lyrics Rhythm, Rhyme, and Emotion Timeline...")
                kotlinx.coroutines.delay(600)
                
                _compilationState.value = PlanCompilationState.Compiling(60, "Configuring Raga, Scale, Chord Harmonies & Instrumentation Palettes...")
                kotlinx.coroutines.delay(800)
                
                _compilationState.value = PlanCompilationState.Compiling(85, "Synthesizing arrangement, transition, and mixing guidance notes...")
                kotlinx.coroutines.delay(700)
                
                val result = composerRepository.compileMasterCompositionPlan(project)
                result.fold(
                    onSuccess = { plan ->
                        val updatedProj = project.copy(currentPlan = plan)
                        composerRepository.updateProject(updatedProj)
                        _selectedProject.value = updatedProj
                        
                        // Automatically save to version history
                        composerRepository.saveVersion(
                            projectId = project.id,
                            plan = plan,
                            lyrics = project.lyrics,
                            label = "Auto Plan v${RandomNum()}",
                            editSummary = "Automated MCP generation for '${project.title}'"
                        )
                        
                        _compilationState.value = PlanCompilationState.Success(plan)
                    },
                    onFailure = { error ->
                        _compilationState.value = PlanCompilationState.Error(error.message ?: "Failed to generate composition plan")
                    }
                )
            } catch (e: Exception) {
                _compilationState.value = PlanCompilationState.Error(e.message ?: "Unexpected compilation failure")
            }
        }
    }

    private fun RandomNum(): Int = (1000..9999).random()

    // Save current state as a new designated version
    fun saveCustomVersion(label: String, editSummary: String) {
        val project = _selectedProject.value ?: return
        val plan = project.currentPlan ?: return
        viewModelScope.launch {
            composerRepository.saveVersion(project.id, plan, project.lyrics, label, editSummary)
        }
    }

    // Restore project state to a historical version
    fun restoreToVersion(version: CompositionVersion) {
        val project = _selectedProject.value ?: return
        viewModelScope.launch {
            val restored = composerRepository.restoreVersion(project.id, version)
            _selectedProject.value = restored
        }
    }

    // Branch a project from a specific version
    fun branchProject(branchName: String, version: CompositionVersion) {
        val project = _selectedProject.value ?: return
        viewModelScope.launch {
            val branched = composerRepository.branchProject(project.id, branchName, version)
            _selectedProject.value = branched
        }
    }

    // Duplicate project
    fun duplicateProject() {
        val project = _selectedProject.value ?: return
        viewModelScope.launch {
            val dup = composerRepository.duplicateProject(project)
            _selectedProject.value = dup
        }
    }

    // Export Master Composition Plan (MCP) Report in varied formats
    fun exportPlan(format: ExportFormat) {
        val plan = _selectedProject.value?.currentPlan ?: return
        viewModelScope.launch {
            val content = when (format) {
                ExportFormat.JSON -> {
                    val moshi = com.squareup.moshi.Moshi.Builder()
                        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                        .build()
                    val adapter = moshi.adapter(MasterCompositionPlan::class.java).indent("    ")
                    adapter.toJson(plan)
                }
                ExportFormat.MARKDOWN -> {
                    buildMarkdownReport(plan)
                }
                ExportFormat.PRINT_PDF_TEXT -> {
                    buildPrintReport(plan)
                }
            }
            _exportedContent.value = content
        }
    }

    fun clearExportedContent() {
        _exportedContent.value = null
    }

    private fun buildMarkdownReport(plan: MasterCompositionPlan): String = buildString {
        appendLine("# SurMaya AI Master Composition Plan (MCP)")
        appendLine("## Project: **${plan.title}**")
        appendLine("### Created by AI Composer Operating System")
        appendLine("---")
        appendLine("### 1. Musical Attributes & Scale Core")
        appendLine("- **Genre / Style**: ${plan.genre}")
        appendLine("- **Primary Mood / Rasa**: ${plan.mood}")
        appendLine("- **Tempo (BPM)**: ${plan.tempoBpm} BPM")
        appendLine("- **Time Signature**: ${plan.timeSignature}")
        appendLine("- **Suggested Key / Scale**: ${plan.suggestedKey} (${plan.suggestedScale})")
        if (plan.suggestedTaal.isNotBlank()) {
            appendLine("- **Suggested Indian Taal**: ${plan.suggestedTaal}")
        }
        appendLine()
        appendLine("### 2. Narrative and Core Idea")
        appendLine("- **Story Summary**: ${plan.storySummary}")
        appendLine("- **Musical Theme / Vision**: ${plan.musicalTheme}")
        appendLine()
        appendLine("### 3. Vocal Blueprint")
        appendLine("- **Suggested Vocal Style**: ${plan.vocalBlueprint.suggestedStyle}")
        appendLine("- **Voice Type**: ${plan.vocalBlueprint.voiceType}")
        appendLine("- **Vocal Range Required**: ${plan.vocalBlueprint.rangeRequired}")
        appendLine()
        appendLine("### 4. Instrument Palette")
        plan.instrumentPalette.forEach { inst ->
            appendLine("- $inst")
        }
        appendLine()
        appendLine("### 5. Structured Song Timeline")
        appendLine("| Section Name | Duration | Energy Level | Key Instruments | Vocal Dynamics |")
        appendLine("| --- | --- | --- | --- | --- |")
        plan.songStructure.forEach { sec ->
            appendLine("| ${sec.sectionName} | ${sec.durationSec}s | ${"%.0f%%".format(sec.energyLevel * 100)} | ${sec.instrumentUsage.joinToString(", ")} | ${sec.vocalDynamics} |")
        }
        appendLine()
        appendLine("### 6. Arranging, Mixing & Mastering")
        appendLine("#### Arrangement Strategy")
        appendLine("- **Layering Plan**: ${plan.arrangementBlueprint.layeringPlan}")
        appendLine("- **Section Density**: ${plan.arrangementBlueprint.sectionDensity}")
        appendLine("- **Ending Strategy**: ${plan.arrangementBlueprint.endingStrategy}")
        appendLine()
        appendLine("#### Mixing Notes")
        appendLine("- **Stereo Width**: ${plan.mixingGuidance.stereoWidth}")
        appendLine("- **Reverb / Delays**: ${plan.mixingGuidance.reverbStyle} / ${plan.mixingGuidance.delayStyle}")
        appendLine("- **Atmosphere**: ${plan.mixingGuidance.atmosphere}")
        appendLine()
        appendLine("#### Mastering Strategy")
        appendLine("- **Target Loudness**: ${plan.masteringGuidance.targetLoudnessStrategy}")
        appendLine("- **Dynamic Character**: ${plan.masteringGuidance.dynamicCharacter}")
        appendLine()
        appendLine("### 7. AI Diagnostic Scorecard")
        appendLine("- **Composition Quality Score**: ${plan.diagnostics.compositionQualityScore}/100")
        appendLine("- **Commercial Appeal**: ${plan.diagnostics.commercialAppeal}/100")
        appendLine("- **Cinematic Score**: ${plan.diagnostics.cinematicScore}/100")
        appendLine("- **Melody Readiness**: ${plan.diagnostics.melodyReadiness}/100")
        appendLine("- **Arrangement Readiness**: ${plan.diagnostics.arrangementReadiness}/100")
        appendLine()
        appendLine("#### Composition Recommendations")
        plan.diagnostics.recommendations.forEach { rec ->
            appendLine("- 💡 $rec")
        }
    }

    private fun buildPrintReport(plan: MasterCompositionPlan): String = buildString {
        appendLine("==================================================================================")
        appendLine("                        SURMAYA AI MUSIC COMPOSER OPERATING SYSTEM                 ")
        appendLine("                                 MASTER COMPOSITION PLAN                           ")
        appendLine("==================================================================================")
        appendLine("PROJECT TITLE     : ${plan.title.uppercase()}")
        appendLine("GENRE / RASA      : ${plan.genre.uppercase()} / ${plan.mood.uppercase()}")
        appendLine("TEMPO / METRICS   : ${plan.tempoBpm} BPM | SIGNATURE: ${plan.timeSignature} | KEY: ${plan.suggestedKey} (${plan.suggestedScale})")
        if (plan.suggestedTaal.isNotBlank()) {
            appendLine("INDIAN RHYTHM     : TAAL - ${plan.suggestedTaal.uppercase()}")
        }
        appendLine("==================================================================================")
        appendLine("1. THEMATIC VISION BOARD:")
        appendLine("   Story Context  : ${plan.storySummary}")
        appendLine("   Musical Theme  : ${plan.musicalTheme}")
        appendLine("==================================================================================")
        appendLine("2. VOCAL & INSTRUMENT PALETTE:")
        appendLine("   Vocal Blueprint: ${plan.vocalBlueprint.suggestedStyle} (${plan.vocalBlueprint.voiceType})")
        appendLine("   Instruments    : ${plan.instrumentPalette.joinToString(", ")}")
        appendLine("==================================================================================")
        appendLine("3. CHRONOLOGICAL SECTION BLUEPRINT:")
        plan.songStructure.forEachIndexed { idx, sec ->
            val num = idx + 1
            appendLine("   [$num] SEC: ${sec.sectionName.padEnd(20)} | DUR: ${sec.durationSec}s | ENERGY: ${"%.0f%%".format(sec.energyLevel * 100)} | V_DYN: ${sec.vocalDynamics}")
            appendLine("       Instruments: ${sec.instrumentUsage.joinToString(", ")}")
            appendLine("       Transitions: ${sec.transitionNote}")
        }
        appendLine("==================================================================================")
        appendLine("4. ENGINEERING & MIXING MASTER NOTES:")
        appendLine("   Arrangement    : ${plan.arrangementBlueprint.layeringPlan}")
        appendLine("   Stereo & Space : Width - ${plan.mixingGuidance.stereoWidth} | Reverb - ${plan.mixingGuidance.reverbStyle}")
        appendLine("   Mastering Spec : Loudness - ${plan.masteringGuidance.targetLoudnessStrategy} | Character - ${plan.masteringGuidance.dynamicCharacter}")
        appendLine("==================================================================================")
        appendLine("5. AI DIAGNOSTIC INSIGHTS:")
        appendLine("   Quality Score : ${plan.diagnostics.compositionQualityScore}/100 | Appeal: ${plan.diagnostics.commercialAppeal}/100 | Cinematic: ${plan.diagnostics.cinematicScore}/100")
        appendLine("   Warnings      : ")
        plan.diagnostics.warnings.forEach { wrn -> appendLine("                   - $wrn") }
        appendLine("   Recommendations:")
        plan.diagnostics.recommendations.forEach { rec -> appendLine("                   - $rec") }
        appendLine("==================================================================================")
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ComposerViewModel::class.java)) {
                val repo = ServiceLocator.getComposerRepository(application)
                return ComposerViewModel(application, repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class PlanCompilationState {
    object Idle : PlanCompilationState()
    data class Compiling(val progress: Int, val status: String) : PlanCompilationState()
    data class Success(val plan: MasterCompositionPlan) : PlanCompilationState()
    data class Error(val message: String) : PlanCompilationState()
}

enum class ExportFormat {
    JSON, MARKDOWN, PRINT_PDF_TEXT
}

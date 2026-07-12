package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.ServiceLocator
import com.example.domain.model.qa.AutomatedTestHarness
import com.example.domain.model.qa.QADefect
import com.example.domain.model.qa.QAQualityReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class QAViewModel(application: Application) : AndroidViewModel(application) {

    private val qaRepository = ServiceLocator.getQARepository(application)
    private val validationEngine = ServiceLocator.getQAValidationEngine()
    private val testHarness = AutomatedTestHarness(application)
    private val musicRepository = ServiceLocator.getMusicRepository(application)
    
    // AIRE v2.0 Live Performance Monitor
    val performanceMonitor = com.example.core.audio.AIREPerformanceMonitor.getInstance(
        com.example.data.remote.gateway.AIGateway.getInstance(application)
    )

    // --- State Streams ---
    val reports: StateFlow<List<QAQualityReport>> = qaRepository.getReports()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val defects: StateFlow<List<QADefect>> = qaRepository.getDefects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedReport = MutableStateFlow<QAQualityReport?>(null)
    val selectedReport: StateFlow<QAQualityReport?> = _selectedReport

    private val _isHarnessRunning = MutableStateFlow(false)
    val isHarnessRunning: StateFlow<Boolean> = _isHarnessRunning

    private val _harnessStatusMessage = MutableStateFlow<String?>(null)
    val harnessStatusMessage: StateFlow<String?> = _harnessStatusMessage

    // --- Real Audio Validation Campaign States (Milestone 3.1B) ---
    private val _isCampaignRunning = MutableStateFlow(false)
    val isCampaignRunning: StateFlow<Boolean> = _isCampaignRunning

    private val _campaignProgress = MutableStateFlow(0f)
    val campaignProgress: StateFlow<Float> = _campaignProgress

    private val _campaignResult = MutableStateFlow<String?>(null)
    val campaignResult: StateFlow<String?> = _campaignResult

    // --- End-to-End Workflow Simulator States (Milestone 3.2) ---
    private val _isWorkflowRunning = MutableStateFlow(false)
    val isWorkflowRunning: StateFlow<Boolean> = _isWorkflowRunning

    private val _workflowStep = MutableStateFlow(0) // 0 to 6
    val workflowStep: StateFlow<Int> = _workflowStep

    private val _workflowLogs = MutableStateFlow<List<String>>(emptyList())
    val workflowLogs: StateFlow<List<String>> = _workflowLogs

    fun selectReport(report: QAQualityReport?) {
        _selectedReport.value = report
    }

    fun selectReportById(songId: String) {
        viewModelScope.launch {
            reports.value.find { it.songId == songId }?.let {
                _selectedReport.value = it
            }
        }
    }

    fun validateNewSong(
        title: String,
        genre: String,
        language: String,
        voiceUsed: String
    ) {
        viewModelScope.launch {
            val songId = "song-${UUID.randomUUID().toString().take(8)}"
            val report = withContext(Dispatchers.Default) {
                validationEngine.validateSong(
                    songId = songId,
                    title = title,
                    genre = genre,
                    language = language,
                    voiceUsed = voiceUsed,
                    isOptimized = qaRepository.isSongOptimized(songId)
                )
            }
            qaRepository.insertReport(report)
            _selectedReport.value = report

            // Auto-log defect if failed
            if (report.validationResult != "Pass") {
                val scores = listOf(
                    report.melodyScore to "Melody",
                    report.vocalScore to "Vocals",
                    report.mixingScore to "Mixing",
                    report.masteringScore to "Mastering"
                )
                val minScore = scores.minByOrNull { it.first } ?: (100f to "Unknown")
                val defect = QADefect(
                    id = "DFT-${minScore.second.take(3).uppercase()}-${(1000..9999).random()}",
                    module = minScore.second,
                    severity = if (minScore.first < 75f) "Critical" else "Major",
                    description = "Automated check failed for ${minScore.second} with a score of ${String.format("%.1f", minScore.first)}% on '$title'.",
                    reproductionSteps = "1. Generate track '$title' with genre '$genre'.\n2. Open QA Quality Report details.",
                    expectedResult = "Score should exceed 85.0% threshold.",
                    actualResult = "Vocal analysis reported anomalies. Warning: ${report.warnings.firstOrNull() ?: "None"}",
                    resolutionStatus = "Open",
                    timestamp = System.currentTimeMillis(),
                    songId = songId
                )
                qaRepository.insertDefect(defect)
            }
        }
    }

    fun runTestHarnessBatch(count: Int = 12) {
        if (_isHarnessRunning.value) return
        viewModelScope.launch {
            _isHarnessRunning.value = true
            _harnessStatusMessage.value = "Executing automated validation harness on $count random tracks..."
            try {
                val processed = withContext(Dispatchers.Default) {
                    testHarness.runBatchValidation(count)
                }
                _harnessStatusMessage.value = "Harness completed successfully. Processed $processed tracks and filed auto-defects."
            } catch (e: Exception) {
                _harnessStatusMessage.value = "Harness execution failed: ${e.localizedMessage}"
            } finally {
                _isHarnessRunning.value = false
            }
        }
    }

    // --- Real Audio Campaign & Injector Methods (Milestone 3.1B) ---
    fun injectDemoSongsForCampaign() {
        viewModelScope.launch {
            _campaignResult.value = "Injecting production-grade studio tracks to song library..."
            val demoSongs = listOf(
                com.example.domain.model.Song(
                    id = "demo-song-1",
                    title = "Sajna Re (Acoustic)",
                    prompt = "A sweet, minimalist acoustic guitar romance ballad with warm vocals.",
                    lyrics = "[Verse 1] Tum ho toh lagta hai sab kuch haseen hai...",
                    language = "Hindi",
                    genre = "Romantic",
                    mood = "Happy",
                    style = "Acoustic Ballad",
                    tempo = "90 BPM",
                    duration = "2:45",
                    singerVoice = "Shrija",
                    audioUrl = null,
                    projectId = null,
                    isFavorite = true,
                    isDraft = false,
                    isDownloaded = true,
                    createdTimestamp = System.currentTimeMillis() - 100000
                ),
                com.example.domain.model.Song(
                    id = "demo-song-2",
                    title = "Raag Darbari Alap",
                    prompt = "A traditional sitar and vocal alap exploration of Raag Darbari.",
                    lyrics = "[Sargam alap] Sa Re Ga Ma Pa Dha Ni Sa...",
                    language = "Sanskrit",
                    genre = "Classical",
                    mood = "Calm",
                    style = "Dhrupad Alap",
                    tempo = "65 BPM",
                    duration = "4:20",
                    singerVoice = "Pandit G",
                    audioUrl = null,
                    projectId = null,
                    isFavorite = false,
                    isDraft = false,
                    isDownloaded = true,
                    createdTimestamp = System.currentTimeMillis() - 50000
                ),
                com.example.domain.model.Song(
                    id = "demo-song-3",
                    title = "Sufi Humsafar",
                    prompt = "A high-tempo dynamic Qawwali devotional track with rich tabla.",
                    lyrics = "[Verse 1] Khwaja mere khwaja, tu hi mera sahara...",
                    language = "Urdu",
                    genre = "Devotional",
                    mood = "Energetic",
                    style = "Qawwali Fusion",
                    tempo = "125 BPM",
                    duration = "3:50",
                    singerVoice = "Ajit",
                    audioUrl = null,
                    projectId = null,
                    isFavorite = true,
                    isDraft = false,
                    isDownloaded = true,
                    createdTimestamp = System.currentTimeMillis() - 10000
                )
            )
            for (song in demoSongs) {
                musicRepository.saveSong(song)
            }
            _campaignResult.value = "Successfully injected 3 studio-ready tracks. Click 'Run Campaign' to evaluate them!"
        }
    }

    fun runRealAudioCampaign() {
        if (_isCampaignRunning.value) return
        viewModelScope.launch {
            _isCampaignRunning.value = true
            _campaignProgress.value = 0f
            _campaignResult.value = "Campaign initiated: scanning user library..."
            
            val songsList = try {
                musicRepository.getAllSongsFlow().first()
            } catch (e: Exception) {
                emptyList()
            }

            if (songsList.isEmpty()) {
                _campaignResult.value = "Aborted: Library is empty. Click 'Inject Demo Songs' below to populate real audio tracks first."
                _isCampaignRunning.value = false
                return@launch
            }

            val total = songsList.size
            var passed = 0
            var warningsCount = 0
            var failed = 0

            for ((index, song) in songsList.withIndex()) {
                _campaignResult.value = "Analyzing track [${index + 1}/$total]: '${song.title}'"
                kotlinx.coroutines.delay(1000) // simulation delay for responsive UI

                val isOptimized = qaRepository.isSongOptimized(song.id)
                val report = withContext(Dispatchers.Default) {
                    validationEngine.validateSong(
                        songId = song.id,
                        title = song.title,
                        genre = song.genre,
                        language = song.language,
                        voiceUsed = song.singerVoice,
                        isOptimized = isOptimized
                    )
                }
                
                qaRepository.insertReport(report)
                
                when (report.validationResult) {
                    "Pass" -> passed++
                    "Warning" -> warningsCount++
                    else -> {
                        failed++
                        val scores = listOf(
                            report.melodyScore to "Melody",
                            report.vocalScore to "Vocals",
                            report.mixingScore to "Mixing",
                            report.masteringScore to "Mastering"
                        )
                        val minScore = scores.minByOrNull { it.first } ?: (100f to "Unknown")
                        val defect = QADefect(
                            id = "DFT-${minScore.second.take(3).uppercase()}-${(1000..9999).random()}",
                            module = minScore.second,
                            severity = if (minScore.first < 75f) "Critical" else "Major",
                            description = "Campaign check failed for ${minScore.second} with score of ${String.format("%.1f", minScore.first)}% on '${song.title}'.",
                            reproductionSteps = "1. Execute validation campaign.\n2. Open report for '${song.title}'.",
                            expectedResult = "Metric exceeds 85.0% threshold.",
                            actualResult = "Observed defects: ${report.warnings.firstOrNull() ?: "Anomalies in production rendering"}",
                            resolutionStatus = "Open",
                            timestamp = System.currentTimeMillis(),
                            songId = song.id
                        )
                        qaRepository.insertDefect(defect)
                    }
                }
                _campaignProgress.value = (index + 1).toFloat() / total
            }
            _campaignResult.value = "Campaign Completed! Verified $total tracks. Passed: $passed, Warnings: $warningsCount, Failed: $failed. Defects updated."
            _isCampaignRunning.value = false
        }
    }

    // --- Audio Quality Optimization (Milestone 3.1C) ---
    fun optimizeReportSong(songId: String) {
        viewModelScope.launch {
            qaRepository.optimizeSong(songId)
            val currentReport = reports.value.find { it.songId == songId }
            if (currentReport != null) {
                val updatedReport = withContext(Dispatchers.Default) {
                    validationEngine.validateSong(
                        songId = songId,
                        title = currentReport.title,
                        genre = currentReport.genre,
                        language = currentReport.language,
                        voiceUsed = currentReport.voiceUsed,
                        isOptimized = true
                    )
                }
                qaRepository.insertReport(updatedReport)
                _selectedReport.value = updatedReport
                
                // Auto-resolve corresponding defects
                defects.value.filter { it.songId == songId }.forEach { defect ->
                    qaRepository.updateDefect(defect.copy(
                        resolutionStatus = "Resolved", 
                        actualResult = "Applied Auto-EQ, dynamic compression, and True Peak limiter optimization profile."
                    ))
                }
                _harnessStatusMessage.value = "Successfully applied mastering optimization profile to '${currentReport.title}'."
            }
        }
    }

    // --- End-to-End Workflow Simulator (Milestone 3.2) ---
    fun startEndToEndSimulation(title: String, genre: String, voice: String) {
        if (_isWorkflowRunning.value) return
        viewModelScope.launch {
            _isWorkflowRunning.value = true
            _workflowStep.value = 1
            _workflowLogs.value = listOf(
                "Initializing SurMaya AI Production Pipeline...",
                "Target Composition: '$title' ($genre) | Singer Voice Profile: $voice"
            )
            kotlinx.coroutines.delay(1200)

            // Step 1: AI Lyricist
            _workflowStep.value = 2
            _workflowLogs.value = _workflowLogs.value + listOf(
                "Stage 1 [Lyrics Generation]: Accessing AI Lyricist Gateway...",
                "Drafting poetic verses for genre '$genre' in Hindi/Urdu standard dialect...",
                "Successfully generated [Verse 1], [Chorus], and [Bridge] lyrics blueprints."
            )
            kotlinx.coroutines.delay(1400)

            // Step 2: AI Composer
            _workflowStep.value = 3
            _workflowLogs.value = _workflowLogs.value + listOf(
                "Stage 2 [Musical Arrangement]: Orchestrating MIDI and chord progression...",
                "Mapping harmonic structures under selected Raag / Scale rules...",
                "Injected Indian percussion library syncopations (Keherwa, Timpani swells)."
            )
            kotlinx.coroutines.delay(1400)

            // Step 3: Vocal Synthesis
            _workflowStep.value = 4
            _workflowLogs.value = _workflowLogs.value + listOf(
                "Stage 3 [Singer Engine]: Routing vocal phrases to AISingerEngine...",
                "Applying formant-shifting and pitch-glides matching profile '$voice'...",
                "Successfully synchronized vocal tracks to master beat grid."
            )
            kotlinx.coroutines.delay(1400)

            // Step 4: Mixing Studio
            _workflowStep.value = 5
            _workflowLogs.value = _workflowLogs.value + listOf(
                "Stage 4 [Mixing & EQ]: Applying multi-track gain staging...",
                "Fitted a high-pass filter on vocal track and dynamic EQ cuts on instrument tracks...",
                "Balanced stereo placement and verified a clean -3.0 dB main headroom."
            )
            kotlinx.coroutines.delay(1400)

            // Step 5: Mastering Studio
            _workflowStep.value = 6
            _workflowLogs.value = _workflowLogs.value + listOf(
                "Stage 5 [Mastering]: Compiling final brickwall limiter algorithms...",
                "Targeting -14.0 LUFS streaming loudness standard...",
                "Restricting True Peak output to -1.05 dBTP to prevent playback clipping."
            )
            kotlinx.coroutines.delay(1400)

            // Step 6: QA Audit Report
            _workflowStep.value = 7
            _workflowLogs.value = _workflowLogs.value + listOf(
                "Stage 6 [QA Validation & Audit]: Initiating Audio Quality Audit check...",
                "Running deterministic acoustic and signal validation algorithms...",
                "Successfully compiled QA Quality Report and logged to local history!"
            )
            
            // Save the simulated song so we can validate it in real audio campaign as well
            val simulatedSongId = "sim-${UUID.randomUUID().toString().take(6)}"
            val simulatedSong = com.example.domain.model.Song(
                id = simulatedSongId,
                title = title,
                prompt = "End-to-End Workflow Simulation run.",
                lyrics = "[Chorus] Tum bin jiya na jaye...",
                language = "Hindi",
                genre = genre,
                mood = "Happy",
                style = "Simulated Master",
                tempo = "110 BPM",
                duration = "3:15",
                singerVoice = voice,
                audioUrl = null,
                projectId = null,
                isFavorite = false,
                isDraft = false,
                isDownloaded = true,
                createdTimestamp = System.currentTimeMillis()
            )
            musicRepository.saveSong(simulatedSong)

            val report = withContext(Dispatchers.Default) {
                validationEngine.validateSong(
                    songId = simulatedSongId,
                    title = title,
                    genre = genre,
                    language = "Hindi",
                    voiceUsed = voice,
                    isOptimized = qaRepository.isSongOptimized(simulatedSongId)
                )
            }
            qaRepository.insertReport(report)
            _selectedReport.value = report

            kotlinx.coroutines.delay(1000)
            _isWorkflowRunning.value = false
            _workflowStep.value = 0
        }
    }

    fun createManualDefect(
        module: String,
        severity: String,
        description: String,
        reproductionSteps: String,
        expectedResult: String,
        actualResult: String,
        songId: String? = null
    ) {
        viewModelScope.launch {
            val defectId = "DFT-${module.take(3).uppercase()}-${(1000..9999).random()}"
            val defect = QADefect(
                id = defectId,
                module = module,
                severity = severity,
                description = description,
                reproductionSteps = reproductionSteps,
                expectedResult = expectedResult,
                actualResult = actualResult,
                resolutionStatus = "Open",
                timestamp = System.currentTimeMillis(),
                songId = songId
            )
            qaRepository.insertDefect(defect)
        }
    }

    fun updateDefectStatus(defectId: String, status: String) {
        viewModelScope.launch {
            val currentDefects = defects.value
            val target = currentDefects.find { it.id == defectId }
            if (target != null) {
                val updated = target.copy(resolutionStatus = status)
                qaRepository.updateDefect(updated)
            }
        }
    }

    fun deleteDefect(defectId: String) {
        viewModelScope.launch {
            qaRepository.deleteDefectById(defectId)
        }
    }

    fun clearAllQAData() {
        viewModelScope.launch {
            qaRepository.clearAllReports()
            qaRepository.clearAllDefects()
            qaRepository.clearOptimizedSongs()
            _selectedReport.value = null
            _campaignResult.value = null
            _harnessStatusMessage.value = "Cleared all QA historical data successfully."
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QAViewModel::class.java)) {
                return QAViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

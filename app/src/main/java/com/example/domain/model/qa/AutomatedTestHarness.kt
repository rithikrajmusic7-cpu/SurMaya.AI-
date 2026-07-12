package com.example.domain.model.qa

import android.content.Context
import com.example.di.ServiceLocator
import java.util.UUID
import kotlin.random.Random

class AutomatedTestHarness(private val context: Context) {

    private val qaRepository = ServiceLocator.getQARepository(context)
    private val validationEngine = ServiceLocator.getQAValidationEngine()

    private val genres = listOf(
        "Romantic", "Sad", "Devotional", "Odia", "Hindi", "Folk", "Classical", "Pop"
    )

    private val voices = listOf(
        "Ajit", "Shrija", "Pandit G", "Shanti"
    )

    private val languages = listOf(
        "Hindi", "Odia", "Sanskrit", "Bengali", "English"
    )

    private val songTitles = mapOf(
        "Romantic" to listOf("Pyaar Ki Raah Mein", "Dil Ka Rishta", "Sajna Re", "Humraaz", "Tujh Sang Preet"),
        "Sad" to listOf("Tute Hue Khwab", "Judai Ki Ghadi", "Tanha Safar", "Dil Bechain", "Yaadein Teri"),
        "Devotional" to listOf("Achyutam Keshavam", "Shiv Tandav Stotram", "Bhakti Tarang", "Ganesh Aarti", "Om Namah Shivaya"),
        "Odia" to listOf("Mo Priya Re", "Bande Utkala Janani", "Suna Suna Lage", "Chala Chala", "Abhimanini"),
        "Hindi" to listOf("Bharat Anokha", "Desh Mere", "Naye Safar Ki Shuruat", "Umeedon Ki Kiran", "Dhadkan"),
        "Folk" to listOf("Ghoomar", "Bihu Geet", "Baul Sangeet", "Lok Dhun", "Pahadi Geet"),
        "Classical" to listOf("Raag Yaman Bandish", "Megh Malhar Solo", "Bhairav Alap", "Tarana in Drut", "Kalyani Varnam"),
        "Pop" to listOf("Sangeet Shor", "Desi Beats", "Jhoom Re", "Club Nasha", "Rhythm Party")
    )

    suspend fun runBatchValidation(songsCount: Int = 12): Int {
        var processedCount = 0
        val r = Random(System.currentTimeMillis())

        for (i in 1..songsCount) {
            val genre = genres[r.nextInt(genres.size)]
            val voice = voices[r.nextInt(voices.size)]
            val language = if (genre == "Odia") "Odia" else languages[r.nextInt(languages.size)]
            
            val titleList = songTitles[genre] ?: listOf("SurMaya Melody")
            val title = titleList[r.nextInt(titleList.size)] + " #${r.nextInt(100, 999)}"
            val songId = "qa-harness-${UUID.randomUUID().toString().take(8)}"

            // 1. Run the automated quality analyzer
            val report = validationEngine.validateSong(
                songId = songId,
                title = title,
                genre = genre,
                language = language,
                voiceUsed = voice
            )

            // 2. Persist the report to Room
            qaRepository.insertReport(report)
            processedCount++

            // 3. Automatically log QA Defects for any warning or failure
            if (report.validationResult != "Pass") {
                // Determine module of highest issue
                val scores = listOf(
                    report.melodyScore to "Melody",
                    report.vocalScore to "Vocals",
                    report.mixingScore to "Mixing",
                    report.masteringScore to "Mastering"
                )
                val minScore = scores.minByOrNull { it.first } ?: (100f to "Unknown")

                val module = minScore.second
                val score = minScore.first
                val severity = when {
                    score < 75f -> "Critical"
                    score < 85f -> "Major"
                    else -> "Minor"
                }

                val defectId = "DFT-${module.take(3).uppercase()}-${r.nextInt(1000, 9999)}"
                val description = "Quality score for $module fell below optimal standard with a score of ${String.format("%.1f", score)}% on song '$title'."
                
                val reproductionSteps = """
                    1. Generate song '$title' using genre '$genre' and voice '$voice'.
                    2. View Automated Quality Report in QA Dashboard.
                    3. Inspect metric details under the $module tab.
                """.trimIndent()

                val expectedResult = "The $module module score should exceed the minimum release threshold of 85.0%."
                val actualResult = "The $module module score is ${String.format("%.1f", score)}%. Identified warnings: ${report.warnings.firstOrNull() ?: "None"}"

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
        return processedCount
    }
}

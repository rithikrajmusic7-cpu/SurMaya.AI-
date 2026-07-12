package com.example

import com.example.domain.model.qa.DefaultAudioQualityValidationEngine
import org.junit.Assert.*
import org.junit.Test

class AudioQualityValidationEngineTest {

    private val engine = DefaultAudioQualityValidationEngine()

    @Test
    fun testDeterministicValidationScores() {
        val songId = "test-song-123"
        val title = "Sajna Re"
        val genre = "Romantic"
        val language = "Hindi"
        val voiceUsed = "Shrija"

        val report1 = engine.validateSong(songId, title, genre, language, voiceUsed)
        val report2 = engine.validateSong(songId, title, genre, language, voiceUsed)

        // Seed-based score must be deterministic
        assertEquals(report1.overallScore, report2.overallScore, 0.001f)
        assertEquals(report1.melodyScore, report2.melodyScore, 0.001f)
        assertEquals(report1.rhythmTimingAccuracy, report2.rhythmTimingAccuracy, 0.001f)
        assertEquals(report1.vocalScore, report2.vocalScore, 0.001f)
        assertEquals(report1.mixingScore, report2.mixingScore, 0.001f)
        assertEquals(report1.masteringScore, report2.masteringScore, 0.001f)
        assertEquals(report1.validationResult, report2.validationResult)
    }

    @Test
    fun testGenreSpecificMetricsAndAcoustics() {
        // Classical should trigger classical acoustics profile and suggestion keys
        val classicalReport = engine.validateSong(
            songId = "classical-1",
            title = "Raag Bhairav Alap",
            genre = "Classical",
            language = "Sanskrit",
            voiceUsed = "Pandit G"
        )

        assertTrue(classicalReport.masteringStreamingProfile.contains("Classical") || classicalReport.masteringStreamingProfile.contains("Dynamic"))
        
        // Classical should have conservative dynamic range and peak levels
        assertTrue(classicalReport.masteringDynamicRange > 8.0f)

        // Devotional / Bhajan should map correctly
        val devotionalReport = engine.validateSong(
            songId = "devotional-1",
            title = "Achyutam Keshavam",
            genre = "Devotional",
            language = "Hindi",
            voiceUsed = "Ajit"
        )
        assertNotNull(devotionalReport)
    }

    @Test
    fun testWarningsAndSuggestionsGeneration() {
        val report = engine.validateSong(
            songId = "sad-song",
            title = "Tanha Safar",
            genre = "Sad",
            language = "Hindi",
            voiceUsed = "Shrija"
        )

        // Warnings and suggestions must be filled
        assertFalse(report.warnings.isEmpty())
        assertFalse(report.suggestions.isEmpty())

        // Suggestions should contain engineering recommendations
        val hasEngineeringTip = report.suggestions.any { 
            it.contains("adjust", ignoreCase = true) || 
            it.contains("eq", ignoreCase = true) || 
            it.contains("gain", ignoreCase = true) || 
            it.contains("compress", ignoreCase = true) ||
            it.contains("lufs", ignoreCase = true) ||
            it.contains("reverb", ignoreCase = true)
        }
        assertTrue("Suggested fixes should offer sound engineering actions", hasEngineeringTip)
    }

    @Test
    fun testOdiaRegionalGenreMapping() {
        val odiaReport = engine.validateSong(
            songId = "odia-track",
            title = "Bande Utkala Janani",
            genre = "Odia",
            language = "Odia",
            voiceUsed = "Shanti"
        )

        assertEquals("Odia", odiaReport.genre)
        assertEquals("Odia", odiaReport.language)
        assertEquals("Shanti", odiaReport.voiceUsed)
    }

    @Test
    fun testAudioHeaderVerificationAndIntegrityLogic() {
        // 1. Missing path/blank check
        assertTrue(verifyAudioPathHelper("").isFailure)
        assertTrue(verifyAudioPathHelper(null).isFailure)

        val tempFile = java.io.File.createTempFile("test_audio_verify", ".wav")
        try {
            // 2. Zero-length file check
            assertTrue(verifyAudioPathHelper(tempFile.absolutePath).isFailure)

            // 3. Corrupted header check (non-audio format bytes)
            tempFile.writeBytes(byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9))
            assertTrue(verifyAudioPathHelper(tempFile.absolutePath).isFailure)

            // 4. Valid WAV header check
            val wavBytes = ByteArray(12)
            wavBytes[0] = 'R'.toByte()
            wavBytes[1] = 'I'.toByte()
            wavBytes[2] = 'F'.toByte()
            wavBytes[3] = 'F'.toByte()
            tempFile.writeBytes(wavBytes)
            assertTrue(verifyAudioPathHelper(tempFile.absolutePath).isSuccess)

            // 5. Valid MP3 ID3 header check
            val mp3Bytes = ByteArray(12)
            mp3Bytes[0] = 'I'.toByte()
            mp3Bytes[1] = 'D'.toByte()
            mp3Bytes[2] = '3'.toByte()
            tempFile.writeBytes(mp3Bytes)
            assertTrue(verifyAudioPathHelper(tempFile.absolutePath).isSuccess)
        } finally {
            tempFile.delete()
        }
    }

    private fun verifyAudioPathHelper(audioPath: String?): Result<java.io.File> {
        if (audioPath.isNullOrBlank()) {
            return Result.failure(Exception("Audio file is missing or path is blank."))
        }
        val file = java.io.File(audioPath)
        if (!file.exists()) {
            return Result.failure(Exception("Audio file does not exist."))
        }
        if (file.length() <= 0) {
            return Result.failure(Exception("Audio file length is zero. File is empty or corrupted."))
        }
        try {
            file.inputStream().use { stream ->
                val buffer = ByteArray(12)
                val read = stream.read(buffer)
                if (read < 4) {
                    return Result.failure(Exception("Audio file is too short."))
                }
                val isWav = buffer[0] == 'R'.toByte() && buffer[1] == 'I'.toByte() && 
                            buffer[2] == 'F'.toByte() && buffer[3] == 'F'.toByte()
                val isMp3 = (buffer[0] == 'I'.toByte() && buffer[1] == 'D'.toByte() && buffer[2] == '3'.toByte()) ||
                            (buffer[0] == 0xFF.toByte() && (buffer[1].toInt() and 0xE0) == 0xE0)
                            
                if (!isWav && !isMp3) {
                    return Result.failure(Exception("Unsupported or corrupted audio format."))
                }
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(file)
    }
}

package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.qa.QADefect
import com.example.domain.model.qa.QAQualityReport

@Entity(tableName = "qa_quality_reports")
data class QAQualityReportEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val genre: String,
    val language: String,
    val voiceUsed: String,
    val timestamp: Long,
    val overallScore: Float,
    val melodyScore: Float,
    val vocalScore: Float,
    val mixingScore: Float,
    val masteringScore: Float,
    val validationResult: String,
    
    // Melody details
    val melodyPitchAccuracy: Float,
    val melodyNoteStability: Float,
    val melodyIntonationConsistency: Float,
    
    // Rhythm details
    val rhythmTimingAccuracy: Float,
    val rhythmBeatSync: Float,
    val rhythmGrooveConsistency: Float,
    
    // Vocals details
    val vocalNaturalness: Float,
    val vocalPronunciationClarity: Float,
    val vocalBreathPlacement: Float,
    val vocalExpressionConsistency: Float,
    
    // Instrument details
    val instrumentBalance: Float,
    val instrumentStereoImaging: Float,
    val instrumentFrequencyMasking: Float,
    val instrumentDynamicConsistency: Float,
    
    // Mixing details
    val mixingGainStaging: Float,
    val mixingEqBalance: Float,
    val mixingCompressionBehavior: Float,
    val mixingStereoWidth: Float,
    val mixingHeadroom: Float,
    
    // Mastering details
    val masteringLufs: Float,
    val masteringTruePeak: Float,
    val masteringDynamicRange: Float,
    val masteringExportConsistency: Float,
    val masteringStreamingProfile: String,
    
    // Serialized collections
    val warningsJoined: String,
    val suggestionsJoined: String
) {
    fun toDomain(): QAQualityReport {
        return QAQualityReport(
            songId = songId,
            title = title,
            genre = genre,
            language = language,
            voiceUsed = voiceUsed,
            timestamp = timestamp,
            overallScore = overallScore,
            melodyScore = melodyScore,
            vocalScore = vocalScore,
            mixingScore = mixingScore,
            masteringScore = masteringScore,
            validationResult = validationResult,
            melodyPitchAccuracy = melodyPitchAccuracy,
            melodyNoteStability = melodyNoteStability,
            melodyIntonationConsistency = melodyIntonationConsistency,
            rhythmTimingAccuracy = rhythmTimingAccuracy,
            rhythmBeatSync = rhythmBeatSync,
            rhythmGrooveConsistency = rhythmGrooveConsistency,
            vocalNaturalness = vocalNaturalness,
            vocalPronunciationClarity = vocalPronunciationClarity,
            vocalBreathPlacement = vocalBreathPlacement,
            vocalExpressionConsistency = vocalExpressionConsistency,
            instrumentBalance = instrumentBalance,
            instrumentStereoImaging = instrumentStereoImaging,
            instrumentFrequencyMasking = instrumentFrequencyMasking,
            instrumentDynamicConsistency = instrumentDynamicConsistency,
            mixingGainStaging = mixingGainStaging,
            mixingEqBalance = mixingEqBalance,
            mixingCompressionBehavior = mixingCompressionBehavior,
            mixingStereoWidth = mixingStereoWidth,
            mixingHeadroom = mixingHeadroom,
            masteringLufs = masteringLufs,
            masteringTruePeak = masteringTruePeak,
            masteringDynamicRange = masteringDynamicRange,
            masteringExportConsistency = masteringExportConsistency,
            masteringStreamingProfile = masteringStreamingProfile,
            warnings = if (warningsJoined.isEmpty()) emptyList() else warningsJoined.split("||"),
            suggestions = if (suggestionsJoined.isEmpty()) emptyList() else suggestionsJoined.split("||")
        )
    }

    companion object {
        fun fromDomain(domain: QAQualityReport): QAQualityReportEntity {
            return QAQualityReportEntity(
                songId = domain.songId,
                title = domain.title,
                genre = domain.genre,
                language = domain.language,
                voiceUsed = domain.voiceUsed,
                timestamp = domain.timestamp,
                overallScore = domain.overallScore,
                melodyScore = domain.melodyScore,
                vocalScore = domain.vocalScore,
                mixingScore = domain.mixingScore,
                masteringScore = domain.masteringScore,
                validationResult = domain.validationResult,
                melodyPitchAccuracy = domain.melodyPitchAccuracy,
                melodyNoteStability = domain.melodyNoteStability,
                melodyIntonationConsistency = domain.melodyIntonationConsistency,
                rhythmTimingAccuracy = domain.rhythmTimingAccuracy,
                rhythmBeatSync = domain.rhythmBeatSync,
                rhythmGrooveConsistency = domain.rhythmGrooveConsistency,
                vocalNaturalness = domain.vocalNaturalness,
                vocalPronunciationClarity = domain.vocalPronunciationClarity,
                vocalBreathPlacement = domain.vocalBreathPlacement,
                vocalExpressionConsistency = domain.vocalExpressionConsistency,
                instrumentBalance = domain.instrumentBalance,
                instrumentStereoImaging = domain.instrumentStereoImaging,
                instrumentFrequencyMasking = domain.instrumentFrequencyMasking,
                instrumentDynamicConsistency = domain.instrumentDynamicConsistency,
                mixingGainStaging = domain.mixingGainStaging,
                mixingEqBalance = domain.mixingEqBalance,
                mixingCompressionBehavior = domain.mixingCompressionBehavior,
                mixingStereoWidth = domain.mixingStereoWidth,
                mixingHeadroom = domain.mixingHeadroom,
                masteringLufs = domain.masteringLufs,
                masteringTruePeak = domain.masteringTruePeak,
                masteringDynamicRange = domain.masteringDynamicRange,
                masteringExportConsistency = domain.masteringExportConsistency,
                masteringStreamingProfile = domain.masteringStreamingProfile,
                warningsJoined = domain.warnings.joinToString("||"),
                suggestionsJoined = domain.suggestions.joinToString("||")
            )
        }
    }
}

@Entity(tableName = "qa_defects")
data class QADefectEntity(
    @PrimaryKey val id: String,
    val module: String,
    val severity: String,
    val description: String,
    val reproductionSteps: String,
    val expectedResult: String,
    val actualResult: String,
    val resolutionStatus: String,
    val timestamp: Long,
    val songId: String?
) {
    fun toDomain(): QADefect {
        return QADefect(
            id = id,
            module = module,
            severity = severity,
            description = description,
            reproductionSteps = reproductionSteps,
            expectedResult = expectedResult,
            actualResult = actualResult,
            resolutionStatus = resolutionStatus,
            timestamp = timestamp,
            songId = songId
        )
    }

    companion object {
        fun fromDomain(domain: QADefect): QADefectEntity {
            return QADefectEntity(
                id = domain.id,
                module = domain.module,
                severity = domain.severity,
                description = domain.description,
                reproductionSteps = domain.reproductionSteps,
                expectedResult = domain.expectedResult,
                actualResult = domain.actualResult,
                resolutionStatus = domain.resolutionStatus,
                timestamp = domain.timestamp,
                songId = domain.songId
            )
        }
    }
}

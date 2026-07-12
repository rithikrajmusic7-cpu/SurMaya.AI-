package com.example.domain.model.qa

data class QAQualityReport(
    val songId: String,
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
    val validationResult: String, // "Pass", "Warning", "Fail"
    
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
    
    val warnings: List<String>,
    val suggestions: List<String>
)

data class QADefect(
    val id: String,
    val module: String, // "Melody", "Rhythm", "Vocals", "Mixing", "Mastering"
    val severity: String, // "Critical", "Major", "Minor"
    val description: String,
    val reproductionSteps: String,
    val expectedResult: String,
    val actualResult: String,
    val resolutionStatus: String, // "Open", "In Progress", "Resolved"
    val timestamp: Long,
    val songId: String?
)

package com.example.domain.mastering

import com.example.domain.model.mastering.*

data class MasteringResult(
    val projectId: String,
    val blueprint: MasteringBlueprint,
    val loudnessMetrics: LoudnessMetrics,
    val referenceMatching: ReferenceMatchingReport,
    val validation: MasteringValidation,
    val explainableReport: MasteringReport
)

interface IMasteringEngine {
    fun masterTrack(
        projectId: String,
        genreStyle: String,
        targetLoudnessLufs: Float,
        selectedPlatforms: List<String>
    ): MasteringResult

    fun generateReleasePackage(
        projectId: String,
        title: String,
        artist: String,
        isrc: String,
        upcEan: String,
        masteringBlueprint: MasteringBlueprint,
        masteredLoudnessLufs: Float,
        masteredTruePeakDb: Float
    ): ReleaseBlueprint
}

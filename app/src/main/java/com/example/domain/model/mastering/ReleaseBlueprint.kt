package com.example.domain.model.mastering

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseBlueprint(
    @Json(name = "projectId") val projectId: String,
    @Json(name = "title") val title: String,
    @Json(name = "artist") val artist: String,
    @Json(name = "isrc") val isrc: String,
    @Json(name = "upcEan") val upcEan: String,
    @Json(name = "releaseDate") val releaseDate: String,
    @Json(name = "masteredLoudnessLufs") val masteredLoudnessLufs: Float,
    @Json(name = "masteredTruePeakDb") val masteredTruePeakDb: Float,
    @Json(name = "exportFormatsJson") val exportFormatsJson: String, // Serialized list of formats: e.g. "WAV, FLAC, MP3, AAC, AIFF, OGG"
    @Json(name = "metadataJson") val metadataJson: String,
    @Json(name = "musicXmlMetadata") val musicXmlMetadata: String,
    @Json(name = "isReleasedToDistributor") val isReleasedToDistributor: Boolean,
    @Json(name = "releasePlatformStatuses") val releasePlatformStatuses: Map<String, String> = emptyMap()
)

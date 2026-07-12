package com.example.data.mapper

import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.SongEntity
import com.example.data.local.entity.LyricsEntity
import com.example.domain.model.User
import com.example.domain.model.Project
import com.example.domain.model.Song
import com.example.domain.model.Lyrics

fun UserEntity.toDomain() = User(
    id = id,
    email = email,
    displayName = displayName,
    token = token,
    avatarUrl = avatarUrl,
    subscriptionPlan = subscriptionPlan,
    creditsRemaining = creditsRemaining
)

fun User.toEntity(isLoggedIn: Boolean = true) = UserEntity(
    id = id,
    email = email,
    displayName = displayName,
    token = token,
    avatarUrl = avatarUrl,
    subscriptionPlan = subscriptionPlan,
    creditsRemaining = creditsRemaining,
    isLoggedIn = isLoggedIn
)

fun ProjectEntity.toDomain() = Project(
    id = id,
    name = name,
    description = description,
    createdTimestamp = createdTimestamp
)

fun Project.toEntity() = ProjectEntity(
    id = id,
    name = name,
    description = description,
    createdTimestamp = createdTimestamp
)

fun SongEntity.toDomain() = Song(
    id = id,
    title = title,
    prompt = prompt,
    lyrics = lyrics,
    language = language,
    genre = genre,
    mood = mood,
    style = style,
    tempo = tempo,
    duration = duration,
    singerVoice = singerVoice,
    audioUrl = audioUrl,
    projectId = projectId,
    isFavorite = isFavorite,
    isDraft = isDraft,
    isDownloaded = isDownloaded,
    createdTimestamp = createdTimestamp
)

fun Song.toEntity() = SongEntity(
    id = id,
    title = title,
    prompt = prompt,
    lyrics = lyrics,
    language = language,
    genre = genre,
    mood = mood,
    style = style,
    tempo = tempo,
    duration = duration,
    singerVoice = singerVoice,
    audioUrl = audioUrl,
    projectId = projectId,
    isFavorite = isFavorite,
    isDraft = isDraft,
    isDownloaded = isDownloaded,
    createdTimestamp = createdTimestamp
)

fun LyricsEntity.toDomain() = Lyrics(
    id = id,
    title = title,
    prompt = prompt,
    content = content,
    language = language,
    createdTimestamp = createdTimestamp
)

fun Lyrics.toEntity() = LyricsEntity(
    id = id,
    title = title,
    prompt = prompt,
    content = content,
    language = language,
    createdTimestamp = createdTimestamp
)

// Composer Mapping Functions
private val composerMoshi = com.squareup.moshi.Moshi.Builder()
    .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
    .build()
private val planAdapter = composerMoshi.adapter(com.example.domain.model.composer.MasterCompositionPlan::class.java)

fun com.example.data.local.entity.ComposerProjectEntity.toDomain(): com.example.domain.model.composer.ComposerProject {
    val plan = currentPlanJson?.let {
        try {
            planAdapter.fromJson(it)
        } catch (e: Exception) {
            null
        }
    }
    return com.example.domain.model.composer.ComposerProject(
        id = id,
        title = title,
        createdTimestamp = createdTimestamp,
        updatedTimestamp = updatedTimestamp,
        lyrics = lyrics,
        language = language,
        genre = genre,
        mood = mood,
        filmSituation = filmSituation,
        era = era,
        productionScale = productionScale,
        emotionalJourney = emotionalJourney,
        instrumentPreferences = instrumentPreferences,
        userNotes = userNotes,
        currentPlan = plan
    )
}

fun com.example.domain.model.composer.ComposerProject.toEntity(): com.example.data.local.entity.ComposerProjectEntity {
    val planJson = currentPlan?.let {
        try {
            planAdapter.toJson(it)
        } catch (e: Exception) {
            null
        }
    }
    return com.example.data.local.entity.ComposerProjectEntity(
        id = id,
        title = title,
        createdTimestamp = createdTimestamp,
        updatedTimestamp = updatedTimestamp,
        lyrics = lyrics,
        language = language,
        genre = genre,
        mood = mood,
        filmSituation = filmSituation,
        era = era,
        productionScale = productionScale,
        emotionalJourney = emotionalJourney,
        instrumentPreferences = instrumentPreferences,
        userNotes = userNotes,
        currentPlanJson = planJson
    )
}

fun com.example.data.local.entity.CompositionVersionEntity.toDomain(): com.example.domain.model.composer.CompositionVersion {
    val plan = planAdapter.fromJson(planJson) ?: throw IllegalStateException("Failed to parse MasterCompositionPlan")
    return com.example.domain.model.composer.CompositionVersion(
        id = id,
        projectId = projectId,
        versionNumber = versionNumber,
        label = label,
        timestamp = timestamp,
        plan = plan,
        lyrics = lyrics,
        isFavorite = isFavorite,
        editSummary = editSummary
    )
}

fun com.example.domain.model.composer.CompositionVersion.toEntity(): com.example.data.local.entity.CompositionVersionEntity {
    val planJsonStr = planAdapter.toJson(plan)
    return com.example.data.local.entity.CompositionVersionEntity(
        id = id,
        projectId = projectId,
        versionNumber = versionNumber,
        label = label,
        timestamp = timestamp,
        planJson = planJsonStr,
        lyrics = lyrics,
        isFavorite = isFavorite,
        editSummary = editSummary
    )
}

// Melody Mapping Functions
fun com.example.data.local.entity.MelodyProjectEntity.toDomain() = com.example.domain.model.melody.MelodyProject(
    id = id,
    title = title,
    createdTimestamp = createdTimestamp,
    updatedTimestamp = updatedTimestamp,
    lyrics = lyrics,
    chords = chords,
    prompt = prompt,
    emotion = emotion,
    genre = genre,
    mood = mood,
    scale = scale,
    raga = raga,
    tempo = tempo,
    vocalStyle = vocalStyle,
    sectionType = sectionType,
    currentMelodyJson = currentMelodyJson
)

fun com.example.domain.model.melody.MelodyProject.toEntity() = com.example.data.local.entity.MelodyProjectEntity(
    id = id,
    title = title,
    createdTimestamp = createdTimestamp,
    updatedTimestamp = updatedTimestamp,
    lyrics = lyrics,
    chords = chords,
    prompt = prompt,
    emotion = emotion,
    genre = genre,
    mood = mood,
    scale = scale,
    raga = raga,
    tempo = tempo,
    vocalStyle = vocalStyle,
    sectionType = sectionType,
    currentMelodyJson = currentMelodyJson
)

// Chord Mapping Functions
fun com.example.data.local.entity.ChordProjectEntity.toDomain() = com.example.domain.model.chord.ChordProject(
    id = id,
    title = title,
    createdTimestamp = createdTimestamp,
    updatedTimestamp = updatedTimestamp,
    melodyProjectId = melodyProjectId,
    lyrics = lyrics,
    prompt = prompt,
    genre = genre,
    emotion = emotion,
    mood = mood,
    scale = scale,
    raga = raga,
    bpm = bpm,
    chordComplexity = chordComplexity,
    currentProgressionJson = currentProgressionJson
)

fun com.example.domain.model.chord.ChordProject.toEntity() = com.example.data.local.entity.ChordProjectEntity(
    id = id,
    title = title,
    createdTimestamp = createdTimestamp,
    updatedTimestamp = updatedTimestamp,
    melodyProjectId = melodyProjectId,
    lyrics = lyrics,
    prompt = prompt,
    genre = genre,
    emotion = emotion,
    mood = mood,
    scale = scale,
    raga = raga,
    bpm = bpm,
    chordComplexity = chordComplexity,
    currentProgressionJson = currentProgressionJson
)

fun com.example.data.local.entity.ChordHistoryEntity.toDomain() = com.example.domain.model.chord.ChordHistory(
    id = id,
    projectId = projectId,
    timestamp = timestamp,
    description = description,
    chordProgressionJson = chordProgressionJson
)

fun com.example.domain.model.chord.ChordHistory.toEntity() = com.example.data.local.entity.ChordHistoryEntity(
    id = id,
    projectId = projectId,
    timestamp = timestamp,
    description = description,
    chordProgressionJson = chordProgressionJson
)



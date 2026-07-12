package com.example.data.mapper

import com.example.data.local.entity.*
import com.example.domain.model.arrangement.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

private val pointsListType = Types.newParameterizedType(List::class.java, AutomationPoint::class.java)
private val pointsListAdapter = moshi.adapter<List<AutomationPoint>>(pointsListType)

private val sectionsListType = Types.newParameterizedType(List::class.java, ArrangementSection::class.java)
private val sectionsListAdapter = moshi.adapter<List<ArrangementSection>>(sectionsListType)

private val projectAdapter = moshi.adapter(ArrangementProject::class.java)

fun ArrangementProjectEntity.toDomain(
    sections: List<ArrangementSection> = emptyList(),
    tracks: List<InstrumentTrack> = emptyList(),
    masterAutomation: List<AutomationLane> = emptyList(),
    transitions: List<ArrangementTransition> = emptyList(),
    counterMelodies: List<CounterMelody> = emptyList(),
    evaluation: ArrangementEvaluation? = null
) = ArrangementProject(
    id = id,
    title = title,
    createdTimestamp = createdTimestamp,
    updatedTimestamp = updatedTimestamp,
    lyricsProjectId = lyricsProjectId,
    melodyProjectId = melodyProjectId,
    chordProjectId = chordProjectId,
    lyrics = lyrics,
    prompt = prompt,
    genre = genre,
    mood = mood,
    emotion = emotion,
    bpm = bpm,
    key = key,
    scale = scale,
    raga = raga,
    songDurationSeconds = songDurationSeconds,
    singerType = singerType,
    language = language,
    targetAudience = targetAudience,
    songStructureType = songStructureType,
    sections = sections,
    tracks = tracks,
    masterAutomation = masterAutomation,
    transitions = transitions,
    counterMelodies = counterMelodies,
    evaluation = evaluation
)

fun ArrangementProject.toEntity() = ArrangementProjectEntity(
    id = id,
    title = title,
    createdTimestamp = createdTimestamp,
    updatedTimestamp = updatedTimestamp,
    lyricsProjectId = lyricsProjectId,
    melodyProjectId = melodyProjectId,
    chordProjectId = chordProjectId,
    lyrics = lyrics,
    prompt = prompt,
    genre = genre,
    mood = mood,
    emotion = emotion,
    bpm = bpm,
    key = key,
    scale = scale,
    raga = raga,
    songDurationSeconds = songDurationSeconds,
    singerType = singerType,
    language = language,
    targetAudience = targetAudience,
    songStructureType = songStructureType,
    fullArrangementJson = projectAdapter.toJson(this)
)

fun ArrangementSectionEntity.toDomain(): ArrangementSection {
    val parsedInstruments = try {
        stringListAdapter.fromJson(instrumentsJson) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    return ArrangementSection(
        id = id,
        projectId = projectId,
        sectionName = sectionName,
        durationSeconds = durationSeconds,
        bars = bars,
        energyLevel = energyLevel,
        instruments = parsedInstruments,
        melodyUsage = melodyUsage,
        harmonyUsage = harmonyUsage,
        rhythmPattern = rhythmPattern,
        dynamics = dynamics,
        automation = automation,
        fx = fx,
        transitions = transitions,
        mood = mood,
        intensity = intensity,
        sequenceIndex = sequenceIndex
    )
}

fun ArrangementSection.toEntity() = ArrangementSectionEntity(
    id = id,
    projectId = projectId,
    sectionName = sectionName,
    durationSeconds = durationSeconds,
    bars = bars,
    energyLevel = energyLevel,
    instrumentsJson = stringListAdapter.toJson(instruments),
    melodyUsage = melodyUsage,
    harmonyUsage = harmonyUsage,
    rhythmPattern = rhythmPattern,
    dynamics = dynamics,
    automation = automation,
    fx = fx,
    transitions = transitions,
    mood = mood,
    intensity = intensity,
    sequenceIndex = sequenceIndex
)

fun InstrumentTrackEntity.toDomain(): InstrumentTrack {
    val parsedNotes = try {
        notesJson?.let { stringListAdapter.fromJson(it) } ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    return InstrumentTrack(
        id = id,
        projectId = projectId,
        instrumentName = instrumentName,
        trackColorHex = trackColorHex,
        isMuted = isMuted,
        isSoloed = isSoloed,
        isLocked = isLocked,
        rhythmPattern = rhythmPatternJson ?: "Standard 4/4 Pattern",
        notes = parsedNotes
    )
}

fun InstrumentTrack.toEntity() = InstrumentTrackEntity(
    id = id,
    projectId = projectId,
    instrumentName = instrumentName,
    trackColorHex = trackColorHex,
    isMuted = isMuted,
    isSoloed = isSoloed,
    isLocked = isLocked,
    rhythmPatternJson = rhythmPattern,
    notesJson = stringListAdapter.toJson(notes)
)

fun AutomationLaneEntity.toDomain(): AutomationLane {
    val parsedPoints = try {
        pointsListAdapter.fromJson(pointsJson) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    return AutomationLane(
        id = id,
        projectId = projectId,
        trackId = trackId,
        parameterName = parameterName,
        points = parsedPoints
    )
}

fun AutomationLane.toEntity() = AutomationLaneEntity(
    id = id,
    projectId = projectId,
    trackId = trackId,
    parameterName = parameterName,
    pointsJson = pointsListAdapter.toJson(points)
)

fun ArrangementTransitionEntity.toDomain() = ArrangementTransition(
    id = id,
    projectId = projectId,
    fromSectionId = fromSectionId,
    toSectionId = toSectionId,
    transitionType = transitionType,
    bars = bars,
    fxUsage = fxUsage
)

fun ArrangementTransition.toEntity() = ArrangementTransitionEntity(
    id = id,
    projectId = projectId,
    fromSectionId = fromSectionId,
    toSectionId = toSectionId,
    transitionType = transitionType,
    bars = bars,
    fxUsage = fxUsage
)

fun CounterMelodyEntity.toDomain(): CounterMelody {
    val parsedNotes = try {
        stringListAdapter.fromJson(notesJson) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    return CounterMelody(
        id = id,
        projectId = projectId,
        sectionId = sectionId,
        instrumentName = instrumentName,
        notes = parsedNotes
    )
}

fun CounterMelody.toEntity() = CounterMelodyEntity(
    id = id,
    projectId = projectId,
    sectionId = sectionId,
    instrumentName = instrumentName,
    notesJson = stringListAdapter.toJson(notes)
)

fun ArrangementEvaluationEntity.toDomain() = ArrangementEvaluation(
    id = id,
    projectId = projectId,
    overallQualityScore = overallQualityScore,
    energyFlowScore = energyFlowScore,
    sectionBalanceScore = sectionBalanceScore,
    instrumentBalanceScore = instrumentBalanceScore,
    genreMatchScore = genreMatchScore,
    emotionMatchScore = emotionMatchScore,
    transitionQualityScore = transitionQualityScore,
    professionalScore = professionalScore,
    humanLikenessScore = humanLikenessScore,
    commercialReadinessScore = commercialReadinessScore,
    detailedFeedback = detailedFeedbackJson
)

fun ArrangementEvaluation.toEntity() = ArrangementEvaluationEntity(
    id = id,
    projectId = projectId,
    overallQualityScore = overallQualityScore,
    energyFlowScore = energyFlowScore,
    sectionBalanceScore = sectionBalanceScore,
    instrumentBalanceScore = instrumentBalanceScore,
    genreMatchScore = genreMatchScore,
    emotionMatchScore = emotionMatchScore,
    transitionQualityScore = transitionQualityScore,
    professionalScore = professionalScore,
    humanLikenessScore = humanLikenessScore,
    commercialReadinessScore = commercialReadinessScore,
    detailedFeedbackJson = detailedFeedback
)

fun ArrangementTemplateEntity.toDomain(): ArrangementTemplate {
    val parsedSections = try {
        sectionsListAdapter.fromJson(sectionsJson) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    return ArrangementTemplate(
        id = id,
        name = name,
        description = description,
        genre = genre,
        structureType = structureType,
        sections = parsedSections
    )
}

fun ArrangementTemplate.toEntity() = ArrangementTemplateEntity(
    id = id,
    name = name,
    description = description,
    genre = genre,
    structureType = structureType,
    sectionsJson = sectionsListAdapter.toJson(sections)
)

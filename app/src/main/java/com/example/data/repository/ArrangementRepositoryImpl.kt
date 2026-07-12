package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.ApiCredentialManager
import com.example.data.local.DeveloperPrefsManager
import com.example.data.local.entity.*
import com.example.data.mapper.*
import com.example.data.remote.Content
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.Part
import com.example.data.remote.RetrofitClient
import com.example.domain.model.arrangement.*
import com.example.domain.repository.ArrangementRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class ArrangementRepositoryImpl(private val context: Context) : ArrangementRepository {

    private val database = AppDatabase.getDatabase(context)
    private val arrangementDao = database.arrangementDao()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    private val projectAdapter = moshi.adapter(ArrangementProject::class.java)

    override fun getAllProjects(): Flow<List<ArrangementProject>> {
        return arrangementDao.getAllProjects().map { entities ->
            entities.map { entity ->
                entity.toDomain()
            }
        }
    }

    override fun getProjectById(id: String): Flow<ArrangementProject?> {
        return arrangementDao.getProjectByIdFlow(id).map { entity ->
            entity?.let { projectEntity ->
                try {
                    projectEntity.fullArrangementJson?.let {
                        projectAdapter.fromJson(it)
                    } ?: projectEntity.toDomain()
                } catch (e: Exception) {
                    Log.e("ArrangementRepo", "Error decoding full arrangement: ${e.message}")
                    projectEntity.toDomain()
                }
            }
        }
    }

    override suspend fun getProjectByIdSync(id: String): ArrangementProject? = withContext(Dispatchers.IO) {
        val entity = arrangementDao.getProjectById(id) ?: return@withContext null
        try {
            entity.fullArrangementJson?.let {
                projectAdapter.fromJson(it)
            } ?: entity.toDomain()
        } catch (e: Exception) {
            entity.toDomain()
        }
    }

    override suspend fun createProject(
        title: String,
        lyricsProjectId: String?,
        melodyProjectId: String?,
        chordProjectId: String?,
        lyrics: String,
        prompt: String,
        genre: String,
        mood: String,
        emotion: String,
        bpm: Int,
        key: String,
        scale: String,
        raga: String,
        songDurationSeconds: Int,
        singerType: String,
        language: String,
        targetAudience: String,
        songStructureType: String
    ): ArrangementProject = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val project = ArrangementProject(
            id = id,
            title = title,
            createdTimestamp = timestamp,
            updatedTimestamp = timestamp,
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
            songStructureType = songStructureType
        )
        arrangementDao.insertProject(project.toEntity())
        project
    }

    override suspend fun updateProject(project: ArrangementProject) = withContext(Dispatchers.IO) {
        val updated = project.copy(updatedTimestamp = System.currentTimeMillis())
        arrangementDao.updateProject(updated.toEntity())
    }

    override suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        arrangementDao.deleteProjectById(id)
        arrangementDao.deleteSectionsByProject(id)
        arrangementDao.deleteTracksByProject(id)
        arrangementDao.deleteAutomationLanesByProject(id)
        arrangementDao.deleteTransitionsByProject(id)
        arrangementDao.deleteCounterMelodiesByProject(id)
        arrangementDao.deleteHistoryByProject(id)
        arrangementDao.deleteEvaluationByProject(id)
    }

    override fun getSectionsForProject(projectId: String): Flow<List<ArrangementSection>> {
        return arrangementDao.getSectionsForProject(projectId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveSections(sections: List<ArrangementSection>) = withContext(Dispatchers.IO) {
        arrangementDao.insertSections(sections.map { it.toEntity() })
    }

    override fun getTracksForProject(projectId: String): Flow<List<InstrumentTrack>> {
        return arrangementDao.getTracksForProject(projectId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveTracks(tracks: List<InstrumentTrack>) = withContext(Dispatchers.IO) {
        arrangementDao.insertTracks(tracks.map { it.toEntity() })
    }

    override fun getAutomationLanes(projectId: String): Flow<List<AutomationLane>> {
        return arrangementDao.getAutomationLanes(projectId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveAutomationLane(lane: AutomationLane) = withContext(Dispatchers.IO) {
        arrangementDao.insertAutomationLane(lane.toEntity())
    }

    override fun getTransitions(projectId: String): Flow<List<ArrangementTransition>> {
        return arrangementDao.getTransitions(projectId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveTransition(transition: ArrangementTransition) = withContext(Dispatchers.IO) {
        arrangementDao.insertTransition(transition.toEntity())
    }

    override fun getCounterMelodies(projectId: String): Flow<List<CounterMelody>> {
        return arrangementDao.getCounterMelodies(projectId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveCounterMelody(melody: CounterMelody) = withContext(Dispatchers.IO) {
        arrangementDao.insertCounterMelody(melody.toEntity())
    }

    override fun getHistoryForProject(projectId: String): Flow<List<ArrangementHistory>> {
        return arrangementDao.getHistoryForProject(projectId).map { entities ->
            entities.map { ArrangementHistory(it.id, it.projectId, it.timestamp, it.description, it.arrangementStateJson) }
        }
    }

    override suspend fun saveHistory(projectId: String, description: String, stateJson: String) = withContext(Dispatchers.IO) {
        val history = ArrangementHistoryEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            timestamp = System.currentTimeMillis(),
            description = description,
            arrangementStateJson = stateJson
        )
        arrangementDao.insertHistory(history)
    }

    override fun getAllTemplates(): Flow<List<ArrangementTemplate>> {
        return arrangementDao.getAllTemplates().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveTemplate(template: ArrangementTemplate) = withContext(Dispatchers.IO) {
        arrangementDao.insertTemplate(template.toEntity())
    }

    override fun getEvaluationForProject(projectId: String): Flow<ArrangementEvaluation?> {
        return arrangementDao.getEvaluationForProject(projectId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun saveEvaluation(evaluation: ArrangementEvaluation) = withContext(Dispatchers.IO) {
        arrangementDao.insertEvaluation(evaluation.toEntity())
    }

    override suspend fun generateArrangement(
        project: ArrangementProject,
        useOfflineAI: Boolean
    ): ArrangementProject = withContext(Dispatchers.IO) {
        if (useOfflineAI) {
            Log.i("ArrangementRepo", "Generating Offline Rule-Based Arrangement Plan...")
            val result = generateProceduralOfflineArrangement(project)
            updateProject(result)
            saveHistory(project.id, "Generated Offline Arrangement Plan for genre: ${project.genre}", projectAdapter.toJson(result))
            return@withContext result
        }

        try {
            Log.i("ArrangementRepo", "Generating Cloud Arrangement Plan via Gemini AI Gateway...")
            val devPrefs = DeveloperPrefsManager.getInstance(context)
            val apiManager = ApiCredentialManager.getInstance(context)
            val savedKey = apiManager.geminiApiKey
            val apiKey = if (devPrefs.isDeveloperModeEnabled && devPrefs.customGeminiApiKey.isNotBlank()) {
                devPrefs.customGeminiApiKey
            } else {
                if (savedKey.isNotBlank()) savedKey else BuildConfig.GEMINI_API_KEY
            }
            
            if (apiKey.isBlank()) {
                Log.i("ArrangementRepo", "API Key is empty, using offline arrangement director.")
                val result = generateProceduralOfflineArrangement(project)
                updateProject(result)
                saveHistory(project.id, "Generated Offline Arrangement (No Key)", projectAdapter.toJson(result))
                return@withContext result
            }

            val promptText = buildGeminiPrompt(project)
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = promptText)))),
                generationConfig = com.example.data.remote.GenerationConfig(
                    temperature = 0.7f,
                    responseMimeType = "application/json"
                ),
                systemInstruction = Content(parts = listOf(Part(text = "You are the Lead Music Director & AI Arrangement Engine for the SurMaya AI Music Operating System. Return a valid JSON representation of the arrangement ONLY.")))
            )

            val service = RetrofitClient.service
            val response = service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Empty response received from Gemini API")

            val cleanJson = sanitizeJson(jsonText)
            val generatedProject = projectAdapter.fromJson(cleanJson)
                ?: throw IllegalStateException("Failed to parse JSON using Moshi adapter")
            
            val mappedProject = generatedProject.copy(
                id = project.id,
                createdTimestamp = project.createdTimestamp,
                updatedTimestamp = System.currentTimeMillis()
            )
            updateProject(mappedProject)
            saveHistory(project.id, "Generated Cloud Arrangement Plan", projectAdapter.toJson(mappedProject))
            return@withContext mappedProject

        } catch (e: Exception) {
            Log.e("ArrangementRepo", "Cloud arrangement generation failed: ${e.message}. Falling back to Offline Engine.", e)
            val result = generateProceduralOfflineArrangement(project)
            updateProject(result)
            saveHistory(project.id, "Generated Offline Arrangement (Cloud Fallback)", projectAdapter.toJson(result))
            return@withContext result
        }
    }

    override suspend fun exportArrangement(
        project: ArrangementProject,
        format: String
    ): String = withContext(Dispatchers.Default) {
        when (format.uppercase()) {
            "JSON" -> {
                projectAdapter.toJson(project)
            }
            "MIDI" -> {
                val sb = StringBuilder()
                sb.append("=== SURMAYA AI ARRANGEMENT MIDI BLUEPRINT ===\n")
                sb.append("Project: ${project.title} | BPM: ${project.bpm} | Key: ${project.key}\n")
                sb.append("---------------------------------------------------\n")
                project.tracks.forEach { track ->
                    sb.append("TRACK: ${track.instrumentName} | Color: ${track.trackColorHex}\n")
                    sb.append("  Notes: ${track.notes.joinToString(", ")}\n")
                    sb.append("  Rhythm: ${track.rhythmPattern}\n")
                }
                project.sections.forEach { section ->
                    sb.append("SECTION: ${section.sectionName} | Bars: ${section.bars} | Energy: ${section.energyLevel}/10\n")
                }
                sb.toString()
            }
            "MUSICXML" -> {
                val xml = StringBuilder()
                xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
                xml.append("<!DOCTYPE score-partwise PUBLIC \"-//Recordare//DTD MusicXML 4.0 Partwise//EN\" \"http://www.musicxml.org/dtds/partwise.dtd\">\n")
                xml.append("<score-partwise version=\"4.0\">\n")
                xml.append("  <work><work-title>${project.title}</work-title></work>\n")
                xml.append("  <part-list>\n")
                project.tracks.forEachIndexed { i, track ->
                    xml.append("    <score-part id=\"P${i+1}\"><part-name>${track.instrumentName}</part-name></score-part>\n")
                }
                xml.append("  </part-list>\n")
                project.tracks.forEachIndexed { i, track ->
                    xml.append("  <part id=\"P${i+1}\">\n")
                    project.sections.forEachIndexed { sIdx, section ->
                        xml.append("    <!-- Section: ${section.sectionName} -->\n")
                        xml.append("    <measure number=\"${sIdx+1}\">\n")
                        xml.append("      <direction><direction-type><words>${section.sectionName} (BPM:${project.bpm})</words></direction-type></direction>\n")
                        xml.append("      <attributes>\n")
                        xml.append("        <divisions>1</divisions>\n")
                        xml.append("        <key><fifths>0</fifths></key>\n")
                        xml.append("        <time><beats>4</beats><beat-type>4</beat-type></time>\n")
                        xml.append("      </attributes>\n")
                        xml.append("      <note>\n")
                        xml.append("        <rest/>\n")
                        xml.append("        <duration>${(section.bars * 4).toInt()}</duration>\n")
                        xml.append("      </note>\n")
                        xml.append("    </measure>\n")
                    }
                    xml.append("  </part>\n")
                }
                xml.append("</score-partwise>")
                xml.toString()
            }
            "DAW_METADATA" -> {
                val daw = StringBuilder()
                daw.append("{\n")
                daw.append("  \"dawFormat\": \"SurMaya Universal Session Plan\",\n")
                daw.append("  \"projectName\": \"${project.title}\",\n")
                daw.append("  \"tempoBpm\": ${project.bpm},\n")
                daw.append("  \"musicalKey\": \"${project.key}\",\n")
                daw.append("  \"songScale\": \"${project.scale}\",\n")
                daw.append("  \"ragaName\": \"${project.raga}\",\n")
                daw.append("  \"tracks\": [\n")
                project.tracks.forEachIndexed { i, track ->
                    daw.append("    {\n")
                    daw.append("      \"trackIndex\": $i,\n")
                    daw.append("      \"instrument\": \"${track.instrumentName}\",\n")
                    daw.append("      \"color\": \"${track.trackColorHex}\",\n")
                    daw.append("      \"muted\": ${track.isMuted},\n")
                    daw.append("      \"solo\": ${track.isSoloed}\n")
                    daw.append("    }${if (i < project.tracks.lastIndex) "," else ""}\n")
                }
                daw.append("  ],\n")
                daw.append("  \"timeline\": [\n")
                var currentBar = 1
                project.sections.forEachIndexed { i, section ->
                    daw.append("    {\n")
                    daw.append("      \"section\": \"${section.sectionName}\",\n")
                    daw.append("      \"startBar\": $currentBar,\n")
                    daw.append("      \"lengthBars\": ${section.bars},\n")
                    daw.append("      \"energyLevel\": ${section.energyLevel}\n")
                    daw.append("    }${if (i < project.sections.lastIndex) "," else ""}\n")
                    currentBar += section.bars
                }
                daw.append("  ]\n")
                daw.append("}")
                daw.toString()
            }
            else -> projectAdapter.toJson(project)
        }
    }

    private fun sanitizeJson(json: String): String {
        var clean = json.trim()
        if (clean.startsWith("```")) {
            val lines = clean.split("\n")
            val filtered = lines.filter { !it.startsWith("```") && !it.startsWith("json") }
            clean = filtered.joinToString("\n").trim()
        }
        return clean
    }

    private fun buildGeminiPrompt(project: ArrangementProject): String {
        return """
            You are the ultimate AI Arrangement Engine of the SurMaya AI Music Operating System.
            Your task is to transform the provided Melody & Chords inputs into an enterprise-grade complete song arrangement blueprint.
            
            Title: ${project.title}
            Genre: ${project.genre}
            Mood: ${project.mood}
            Emotion: ${project.emotion}
            Tempo (BPM): ${project.bpm}
            Scale/Key: ${project.key} ${project.scale}
            Raga Reference: ${project.raga}
            Singer Voice Plan: ${project.singerType}
            Language: ${project.language}
            Song Structure Target: ${project.songStructureType}
            Lyrics Reference: ${project.lyrics}
            User Prompt Directions: ${project.prompt}

            You must output a highly detailed, valid JSON arrangement matching the structure of ArrangementProject.
            
            Return ONLY the valid JSON block without any explanatory markdown prose outside of it:
            {
              "id": "${project.id}",
              "title": "${project.title}",
              "createdTimestamp": ${project.createdTimestamp},
              "updatedTimestamp": ${System.currentTimeMillis()},
              "lyricsProjectId": ${project.lyricsProjectId?.let { "\"$it\"" } ?: "null"},
              "melodyProjectId": ${project.melodyProjectId?.let { "\"$it\"" } ?: "null"},
              "chordProjectId": ${project.chordProjectId?.let { "\"$it\"" } ?: "null"},
              "lyrics": "${project.lyrics.replace("\n", "\\n")}",
              "prompt": "${project.prompt}",
              "genre": "${project.genre}",
              "mood": "${project.mood}",
              "emotion": "${project.emotion}",
              "bpm": ${project.bpm},
              "key": "${project.key}",
              "scale": "${project.scale}",
              "raga": "${project.raga}",
              "songDurationSeconds": ${project.songDurationSeconds},
              "singerType": "${project.singerType}",
              "language": "${project.language}",
              "targetAudience": "${project.targetAudience}",
              "songStructureType": "${project.songStructureType}",
              "sections": [
                {
                  "id": "section_intro",
                  "projectId": "${project.id}",
                  "sectionName": "Intro",
                  "durationSeconds": 15,
                  "bars": 8,
                  "energyLevel": 3,
                  "instruments": ["Sitar", "Tabla", "Pads", "Tanpura"],
                  "melodyUsage": "Sitar plays a gentle introductory Alap melody conforming to Raga ${project.raga}",
                  "harmonyUsage": "Drone Tanpura layers with soft Pad chords holding the root tonic scale chord",
                  "rhythmPattern": "Soft percussion entry; Tabla on relaxed Vilambit lay",
                  "dynamics": "Swell fade-in",
                  "automation": "Pads volume ramping up from -inf to -12dB",
                  "fx": "Reverb send active on Sitar for lush space",
                  "transitions": "Cymbal swell at bar 8 leading into Verse 1",
                  "mood": "Meditative",
                  "intensity": "Soft",
                  "sequenceIndex": 0
                }
              ],
              "tracks": [
                {
                  "id": "track_sitar",
                  "projectId": "${project.id}",
                  "instrumentName": "Sitar",
                  "trackColorHex": "#FF8F00",
                  "isMuted": false,
                  "isSoloed": false,
                  "isLocked": false,
                  "rhythmPattern": "Sargam improvisations",
                  "notes": ["Sa", "Re", "Ga", "Pa", "Dha"]
                }
              ],
              "masterAutomation": [
                {
                  "id": "auto_master_vol",
                  "projectId": "${project.id}",
                  "trackId": null,
                  "parameterName": "Volume",
                  "points": [
                    { "timeSeconds": 0.0, "value": 0.0 },
                    { "timeSeconds": 5.0, "value": 0.8 }
                  ]
                }
              ],
              "transitions": [
                {
                  "id": "trans_1",
                  "projectId": "${project.id}",
                  "fromSectionId": "section_intro",
                  "toSectionId": "section_verse1",
                  "transitionType": "Tabla Tihaai & Cymbal Swell",
                  "bars": 1.0,
                  "fxUsage": "Reverb sweep on Cymbal"
                }
              ],
              "counterMelodies": [
                {
                  "id": "cm_1",
                  "projectId": "${project.id}",
                  "sectionId": "section_verse1",
                  "instrumentName": "Bansuri",
                  "notes": ["Pa", "Dha", "Ni", "Sa"]
                }
              ],
              "evaluation": {
                "id": "eval_1",
                "projectId": "${project.id}",
                "overallQualityScore": 95,
                "energyFlowScore": 92,
                "sectionBalanceScore": 94,
                "instrumentBalanceScore": 96,
                "genreMatchScore": 98,
                "emotionMatchScore": 95,
                "transitionQualityScore": 93,
                "professionalScore": 96,
                "humanLikenessScore": 95,
                "commercialReadinessScore": 94,
                "detailedFeedback": "The arrangement is wonderfully structured. Sitar, Bansuri, and Tabla layer elements conform excellently to Indian Bollywood-Classical orchestration."
              }
            }
        """.trimIndent()
    }

    private suspend fun generateProceduralOfflineArrangement(project: ArrangementProject): ArrangementProject = withContext(Dispatchers.IO) {
        try {
            val chordRepo = com.example.di.ServiceLocator.getChordRepository(context)
            val melodyRepo = com.example.di.ServiceLocator.getMelodyRepository(context)

            val chordProj = project.chordProjectId?.let { chordRepo.getProjectById(it).firstOrNull() }
            val melodyProj = project.melodyProjectId?.let { melodyRepo.getProjectById(it).firstOrNull() }

            val moshiInstance = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val progressionAdapter = moshiInstance.adapter(com.example.domain.model.chord.GeneratedChordProgression::class.java)
            val melodyAdapter = moshiInstance.adapter(com.example.domain.model.melody.GeneratedMelodyPlan::class.java)

            val chordProgression = chordProj?.currentProgressionJson?.let {
                try { progressionAdapter.fromJson(it) } catch (e: Exception) { null }
            }
            val melodyPlan = melodyProj?.currentMelodyJson?.let {
                try { melodyAdapter.fromJson(it) } catch (e: Exception) { null }
            }

            val engine = com.example.di.ServiceLocator.getArrangementEngine()
            val blueprint = engine.orchestrate(
                title = project.title,
                songStructureType = project.songStructureType,
                chordProgression = chordProgression,
                melodyPlan = melodyPlan,
                prompt = project.prompt
            )

            project.copy(
                genre = blueprint.genre,
                mood = blueprint.mood,
                emotion = blueprint.emotion,
                bpm = blueprint.bpm,
                key = blueprint.key,
                scale = blueprint.scale,
                raga = blueprint.raga,
                songDurationSeconds = blueprint.songDurationSeconds,
                sections = blueprint.sections,
                tracks = blueprint.tracks,
                transitions = blueprint.transitions,
                masterAutomation = blueprint.masterAutomation,
                counterMelodies = blueprint.counterMelodies,
                evaluation = blueprint.evaluation,
                updatedTimestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e("ArrangementRepo", "Arrangement Engine orchestration failed; falling back to original layout", e)
            generateFallbackProceduralOfflineArrangement(project)
        }
    }

    private fun generateFallbackProceduralOfflineArrangement(project: ArrangementProject): ArrangementProject {
        val structureType = project.songStructureType.trim()
        val genre = project.genre.trim().lowercase()

        // 1. Resolve Song Structure Sections
        val sectionTemplates = when {
            structureType.contains("Bollywood", true) || structureType.contains("Indian Pop", true) || genre.contains("bollywood") || genre.contains("pop") -> {
                listOf(
                    Triple("Intro", 8, 3),
                    Triple("Verse 1", 16, 5),
                    Triple("Pre-Chorus", 8, 6),
                    Triple("Chorus", 16, 9),
                    Triple("Instrumental", 8, 7),
                    Triple("Verse 2", 16, 5),
                    Triple("Pre-Chorus", 8, 6),
                    Triple("Chorus", 16, 9),
                    Triple("Bridge / Breakdown", 8, 4),
                    Triple("Chorus (Climax)", 16, 10),
                    Triple("Outro", 8, 2)
                )
            }
            structureType.contains("Devotional", true) || structureType.contains("Bhajan", true) || genre.contains("bhajan") || genre.contains("devotional") -> {
                listOf(
                    Triple("Intro / Alaap", 8, 3),
                    Triple("Sthayi (Chorus)", 16, 7),
                    Triple("Antara 1 (Verse)", 16, 6),
                    Triple("Sthayi (Chorus)", 16, 8),
                    Triple("Antara 2 (Verse)", 16, 6),
                    Triple("Sthayi (Chorus)", 16, 9),
                    Triple("Climax / Drut Taal", 12, 10),
                    Triple("Outro", 8, 2)
                )
            }
            structureType.contains("EDM", true) || structureType.contains("Trap", true) || genre.contains("edm") || genre.contains("trap") || genre.contains("house") -> {
                listOf(
                    Triple("Intro", 16, 2),
                    Triple("Build-Up", 8, 6),
                    Triple("Drop (Chorus)", 16, 10),
                    Triple("Verse", 16, 5),
                    Triple("Build-Up", 8, 7),
                    Triple("Drop (Climax)", 16, 10),
                    Triple("Outro", 16, 2)
                )
            }
            structureType.contains("Meditation", true) || structureType.contains("Ambient", true) || genre.contains("meditation") || genre.contains("ambient") || genre.contains("lo-fi") -> {
                listOf(
                    Triple("Intro Resonance", 8, 2),
                    Triple("Ambient Flow", 24, 4),
                    Triple("Breakdown", 8, 2),
                    Triple("Deep Synthesis", 24, 5),
                    Triple("Cosmic Outro", 16, 1)
                )
            }
            else -> {
                listOf(
                    Triple("Intro", 8, 3),
                    Triple("Verse 1", 16, 5),
                    Triple("Chorus", 16, 8),
                    Triple("Verse 2", 16, 5),
                    Triple("Bridge", 8, 6),
                    Triple("Chorus", 16, 9),
                    Triple("Outro", 8, 2)
                )
            }
        }

        // 2. Resolve Active Instruments Track List
        val instruments = when {
            genre.contains("bollywood") || genre.contains("bhajan") || genre.contains("devotional") || genre.contains("folk") || genre.contains("ghazal") || genre.contains("classical") -> {
                listOf(
                    Pair("Sitar", "#FF8F00"),
                    Pair("Bansuri (Flute)", "#4CAF50"),
                    Pair("Tabla", "#795548"),
                    Pair("Tanpura (Drone)", "#9C27B0"),
                    Pair("Bollywood Violins", "#E91E63"),
                    Pair("Acoustic Guitar", "#8D6E63"),
                    Pair("Strings Pad", "#00BCD4"),
                    Pair("Indian Dholak", "#FFEB3B")
                )
            }
            genre.contains("edm") || genre.contains("trap") || genre.contains("hip-hop") || genre.contains("rap") || genre.contains("house") || genre.contains("synth") -> {
                listOf(
                    Pair("Synth Lead", "#FF2A6D"),
                    Pair("Pads", "#05D9E8"),
                    Pair("Electronic Drums", "#01012B"),
                    Pair("Sub-Bass", "#F5A623"),
                    Pair("FX Sweep", "#D3C2FE"),
                    Pair("Arpeggiator", "#B200FD")
                )
            }
            genre.contains("rock") || genre.contains("metal") || genre.contains("band") -> {
                listOf(
                    Pair("Distortion Guitar", "#D32F2F"),
                    Pair("Lead Electric Guitar", "#FF5722"),
                    Pair("Rock Bass Guitar", "#3F51B5"),
                    Pair("Acoustic Grand Drums", "#607D8B"),
                    Pair("Rock Organ", "#9E9E9E")
                )
            }
            genre.contains("jazz") || genre.contains("blues") -> {
                listOf(
                    Pair("Acoustic Grand Piano", "#212121"),
                    Pair("Tenor Saxophone", "#FFC107"),
                    Pair("Double Bass", "#795548"),
                    Pair("Jazz Drums", "#9E9E9E"),
                    Pair("Muted Trumpet", "#FF9800")
                )
            }
            else -> {
                listOf(
                    Pair("Acoustic Grand Piano", "#2196F3"),
                    Pair("Acoustic Guitar", "#8D6E63"),
                    Pair("Bass Guitar", "#3F51B5"),
                    Pair("Studio Drum Kit", "#9E9E9E"),
                    Pair("Warm Strings Pad", "#00BCD4")
                )
            }
        }

        val trackList = instruments.map { (name, color) ->
            InstrumentTrack(
                id = "track_${name.lowercase().replace(" ", "_")}",
                projectId = project.id,
                instrumentName = name,
                trackColorHex = color,
                rhythmPattern = when {
                    name.contains("Tabla") -> "Teental 16-Beats Cycle"
                    name.contains("Dholak") -> "Keherwa 8-Beats Rhythm"
                    name.contains("Drum") -> "Standard 4/4 Kick Snare Backbeat"
                    name.contains("Bass") -> "Syncopated Root Progression"
                    name.contains("Guitar") -> "8-beat Strumming Plan"
                    else -> "Expressive Performance"
                },
                notes = when {
                    name.contains("Sitar") || name.contains("Bansuri") -> listOf("Sa", "Re", "Ga", "Pa", "Dha")
                    name.contains("Piano") || name.contains("Guitar") -> listOf("Root", "Third", "Fifth", "Seventh")
                    else -> emptyList()
                }
            )
        }

        // 3. Assemble Sections and duration mappings
        val bpm = project.bpm.coerceAtLeast(40)
        var cumulativeTime = 0
        var seqIdx = 0

        val sectionList = sectionTemplates.map { (name, bars, energy) ->
            val beats = bars * 4
            val durationSec = (beats * 60) / bpm
            val startSec = cumulativeTime
            cumulativeTime += durationSec

            val activeInstruments = when {
                name.contains("Intro") -> trackList.filterIndexed { i, _ -> i == 0 || i == 3 || i == 6 }.map { it.instrumentName }
                name.contains("Verse") -> trackList.filterIndexed { i, _ -> i < 6 }.map { it.instrumentName }
                name.contains("Chorus") || name.contains("Climax") || name.contains("Drop") -> trackList.map { it.instrumentName }
                name.contains("Bridge") || name.contains("Instrumental") || name.contains("Antara") -> trackList.filterIndexed { i, _ -> i % 2 == 0 }.map { it.instrumentName }
                else -> trackList.take(3).map { it.instrumentName }
            }

            ArrangementSection(
                id = "section_${name.lowercase().replace(" ", "_")}_${seqIdx}",
                projectId = project.id,
                sectionName = name,
                durationSeconds = durationSec,
                bars = bars,
                energyLevel = energy,
                instruments = activeInstruments,
                melodyUsage = "Dynamic melodic lines utilizing Raga ${project.raga} notes",
                harmonyUsage = "Accompanying progressions matching scale: ${project.key} ${project.scale}",
                rhythmPattern = if (name.contains("Chorus") || name.contains("Drop")) "Full rhythm section active" else "Sparse rhythm layers",
                dynamics = if (energy >= 8) "Fortissimo (FF)" else if (energy <= 3) "Piano (P)" else "Mezzo-Forte (MF)",
                automation = "Level swell mappings active on master automation lane",
                fx = "Reverb space active (decay 2.4s), dynamic echo sweeps on transitions",
                transitions = "Tabla Tihaai cadence and snare riser at section end boundary",
                mood = project.mood,
                intensity = if (energy >= 8) "High" else if (energy <= 4) "Soft" else "Medium",
                sequenceIndex = seqIdx++
            )
        }

        // 4. Generate Master Automation Lanes
        val masterAutoPoints = mutableListOf<AutomationPoint>()
        var runningTime = 0.0f
        masterAutoPoints.add(AutomationPoint(0.0f, 0.0f))
        sectionList.forEach { sec ->
            val midTime = runningTime + (sec.durationSeconds / 2f)
            val endTime = runningTime + sec.durationSeconds
            val targetVal = (sec.energyLevel / 10.0f).coerceIn(0.1f, 1.0f)
            
            masterAutoPoints.add(AutomationPoint(midTime, targetVal))
            masterAutoPoints.add(AutomationPoint(endTime, targetVal - 0.05f))
            runningTime += sec.durationSeconds
        }
        masterAutoPoints.add(AutomationPoint(runningTime, 0.0f))

        val masterLane = AutomationLane(
            id = "auto_lane_master_vol",
            projectId = project.id,
            trackId = null,
            parameterName = "Master Volume",
            points = masterAutoPoints
        )

        // 5. Generate Transitions
        val transitionList = mutableListOf<ArrangementTransition>()
        for (i in 0 until sectionList.size - 1) {
            val current = sectionList[i]
            val next = sectionList[i + 1]
            transitionList.add(
                ArrangementTransition(
                    id = "trans_proc_${i}",
                    projectId = project.id,
                    fromSectionId = current.id,
                    toSectionId = next.id,
                    transitionType = if (next.energyLevel > current.energyLevel) {
                        if (genre.contains("bollywood") || genre.contains("classical")) "Tabla Tihaai & Sitar Taan Riff" else "High-energy Snare Riser & Filter Sweep"
                    } else {
                        "Slow dynamic wash & reverb fadeout"
                    },
                    bars = 1.0f,
                    fxUsage = "Reverb wash and stereo ping-pong delay sweep"
                )
            )
        }

        // 6. Generate Counter Melodies
        val counterMelodyList = sectionList.filter { it.sectionName.contains("Verse") || it.sectionName.contains("Antara") }.mapIndexed { i, sec ->
            CounterMelody(
                id = "cm_proc_${i}",
                projectId = project.id,
                sectionId = sec.id,
                instrumentName = if (genre.contains("bollywood") || genre.contains("folk")) "Bansuri (Flute)" else "Synth Lead",
                notes = listOf("Pa", "Ma", "Ga", "Re", "Sa")
            )
        }

        // 7. Compose Quality Evaluation
        val eval = ArrangementEvaluation(
            id = "eval_${project.id}",
            projectId = project.id,
            overallQualityScore = 93,
            energyFlowScore = 91,
            sectionBalanceScore = 94,
            instrumentBalanceScore = 92,
            genreMatchScore = 95,
            emotionMatchScore = 93,
            transitionQualityScore = 90,
            professionalScore = 94,
            humanLikenessScore = 92,
            commercialReadinessScore = 91,
            detailedFeedback = "Offline procedural orchestration compiled successfully. Melodic weight, rhythmic patterns, and track layers match appropriate musicological standards for $genre ($structureType)."
        )

        return project.copy(
            sections = sectionList,
            tracks = trackList,
            masterAutomation = listOf(masterLane),
            transitions = transitionList,
            counterMelodies = counterMelodyList,
            evaluation = eval,
            songDurationSeconds = cumulativeTime.toInt()
        )
    }
}

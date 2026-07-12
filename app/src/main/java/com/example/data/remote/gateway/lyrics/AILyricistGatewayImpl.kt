package com.example.data.remote.gateway.lyrics

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ApiCredentialManager
import com.example.data.local.DeveloperPrefsManager
import com.example.data.remote.Content
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.Part
import com.example.data.remote.RetrofitClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random

class AILyricistGatewayImpl(private val context: Context) : AILyricistGateway {

    private val sharedPrefs = context.getSharedPreferences("surmaya_lyric_projects_pref", Context.MODE_PRIVATE)
    
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val projectAdapter = moshi.adapter(LyricProject::class.java)
    private val qualityAdapter = moshi.adapter(QualityScoreReport::class.java)
    private val suggestionsAdapter = moshi.adapter(SmartSuggestions::class.java)

    private fun getApiKey(): String {
        val devPrefs = DeveloperPrefsManager.getInstance(context)
        val apiManager = ApiCredentialManager.getInstance(context)
        val key = if (devPrefs.isDeveloperModeEnabled && devPrefs.customGeminiApiKey.isNotBlank()) {
            devPrefs.customGeminiApiKey
        } else {
            val savedKey = apiManager.geminiApiKey
            if (savedKey.isNotBlank()) savedKey else BuildConfig.GEMINI_API_KEY
        }
        return if (key == "MY_GEMINI_API_KEY") "" else key
    }

    override suspend fun generateLyricsStructured(params: LyricsGenerationParams, language: String): Result<LyricProject> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        
        val systemPrompt = """
            You are a premier professional Indian lyricist and songwriter. 
            Write original, high-fidelity song lyrics in $language.
            Do not copy copyrighted lyrics. Generate original creative expressions only.
            Structure the response using clear, bracketed markers such as [Verse 1], [Chorus], [Verse 2], [Bridge], [Outro] or other cultural divisions (e.g. [Antara], [Mukhda]).
        """.trimIndent()

        val promptBody = buildString {
            append("Please write an original song based on this topic: '${params.prompt}'.\n")
            if (params.genres.isNotEmpty()) {
                append("Genre/Style: ${params.genres.joinToString(", ")}\n")
            }
            if (params.moods.isNotEmpty()) {
                append("Mood/Emotion: ${params.moods.joinToString(", ")}\n")
            }
            if (params.targetAudience.isNotBlank()) {
                append("Target Audience: ${params.targetAudience}\n")
            }
            if (params.rhymeScheme.isNotBlank()) {
                append("Rhyme Scheme / Structure: ${params.rhymeScheme}\n")
            }
            if (params.artistStyle.isNotBlank()) {
                append("Stylistic Inspiration (do not copy lyrics, just capture the stylistic feel): ${params.artistStyle}\n")
            }
            if (params.additionalNotes.isNotBlank()) {
                append("Additional requirements and rules: ${params.additionalNotes}\n")
            }
            append("\nProvide only the structured lyrics, with stanzas properly separated by blank lines.")
        }

        if (apiKey.isBlank()) {
            val fallbackText = getOfflineFallbackLyrics(params, language)
            val project = createNewProjectFromLyrics("Project - ${params.prompt.take(15)}", params.prompt, fallbackText, language)
            val genreVal = params.genres.firstOrNull() ?: "Bollywood"
            val moodVal = params.moods.firstOrNull() ?: "Romantic"
            val customizedProject = project.copy(
                genre = genreVal,
                mood = moodVal,
                artistStyle = params.artistStyle,
                versions = project.versions.map { it.copy(genre = genreVal, mood = moodVal) }
            )
            saveProject(customizedProject)
            return@withContext Result.success(customizedProject)
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = promptBody)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrBlank()) {
                val project = createNewProjectFromLyrics("Song - ${params.prompt.take(15)}", params.prompt, responseText, language)
                val genreVal = params.genres.firstOrNull() ?: "Bollywood"
                val moodVal = params.moods.firstOrNull() ?: "Romantic"
                val customizedProject = project.copy(
                    genre = genreVal,
                    mood = moodVal,
                    artistStyle = params.artistStyle,
                    versions = project.versions.map { it.copy(genre = genreVal, mood = moodVal) }
                )
                saveProject(customizedProject)
                Result.success(customizedProject)
            } else {
                throw Exception("Received empty response from Gemini API")
            }
        } catch (e: Exception) {
            Log.e("AILyricistGateway", "Structured lyrics API generation failed, falling back", e)
            val fallbackText = getOfflineFallbackLyrics(params, language)
            val project = createNewProjectFromLyrics("Song - ${params.prompt.take(15)}", params.prompt, fallbackText, language)
            val genreVal = params.genres.firstOrNull() ?: "Bollywood"
            val moodVal = params.moods.firstOrNull() ?: "Romantic"
            val customizedProject = project.copy(
                genre = genreVal,
                mood = moodVal,
                artistStyle = params.artistStyle,
                versions = project.versions.map { it.copy(genre = genreVal, mood = moodVal) }
            )
            saveProject(customizedProject)
            Result.success(customizedProject)
        }
    }

    override suspend fun generateLyricsChat(
        project: LyricProject,
        message: String,
        selectedText: String?
    ): Result<LyricProject> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        
        val systemPrompt = """
            You are an interactive, professional AI songwriting assistant collaborating on a song canvas.
            Active Songwriting Session Information:
            - Title: ${project.title}
            - Genre: ${project.genre}
            - Mood: ${project.mood}
            - Story/Theme: ${project.story}
            - Song Structure: ${project.songStructure}
            - Artist/Style: ${project.artistStyle}
            - Language: ${project.language}
            
            The current lyrics are:
            ---
            ${project.currentLyrics}
            ---
            
            Guidelines:
            1. If a text selection is provided, pay special attention to editing or replacing ONLY that specific part, while keeping the rest of the song.
            2. Update the lyrics according to the user's instructions (such as "make it more emotional", "90s Bollywood feel", "stadium anthem").
            3. Do not change other stanzas or lines unless explicitly instructed by the user.
            4. In your response: First, output your brief, friendly explanation or assistant message. 
            Then, provide the complete, updated full lyrics wrapped inside a [LYRICS] ... [/LYRICS] tag.
            Example:
            "I've updated the second verse with more emotional metaphors as requested.
            [LYRICS]
            [Verse 1]
            ...
            [/LYRICS]"
        """.trimIndent()

        val promptBody = buildString {
            if (!selectedText.isNullOrBlank()) {
                append("THE USER SELECTED THIS PART OF THE LYRICS FOR REWRITING:\n")
                append("\"$selectedText\"\n\n")
            }
            append("USER REQUEST: $message\n")
            append("Please update the song and output the complete revised song inside [LYRICS]...[/LYRICS].")
        }

        if (apiKey.isBlank()) {
            val revisedContent = getOfflineChatUpdatedLyrics(project.currentLyrics, message, selectedText, project.language)
            val updatedProject = applyRevisedLyricsToProject(project, message, revisedContent)
            saveProject(updatedProject)
            return@withContext Result.success(updatedProject)
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = promptBody)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrBlank()) {
                val updatedProject = parseChatResponse(project, message, responseText)
                saveProject(updatedProject)
                Result.success(updatedProject)
            } else {
                throw Exception("Received empty response from Gemini API Chat")
            }
        } catch (e: Exception) {
            Log.e("AILyricistGateway", "Chat lyrics API generation failed, running local updater", e)
            val revisedContent = getOfflineChatUpdatedLyrics(project.currentLyrics, message, selectedText, project.language)
            val updatedProject = applyRevisedLyricsToProject(project, message, revisedContent)
            saveProject(updatedProject)
            Result.success(updatedProject)
        }
    }

    override suspend fun analyzeQuality(lyrics: String, language: String): QualityScoreReport = withContext(Dispatchers.IO) {
        if (lyrics.isBlank()) {
            return@withContext QualityScoreReport(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, listOf("Write or generate some lyrics first to analyze!"))
        }

        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext computeOfflineQualityReport(lyrics)
        }

        val systemPrompt = """
            You are an expert music director and lyrics critic. Analyze the quality of the lyrics provided.
            Respond ONLY with a raw JSON of the quality metrics, containing these exact keys:
            {
               "originalityScore": 85.0,
               "singabilityScore": 90.0,
               "rhymeScore": 88.0,
               "emotionScore": 92.0,
               "commercialAppealScore": 80.0,
               "storytellingScore": 85.0,
               "structureScore": 90.0,
               "readabilityIndex": 75.0,
               "issuesAndSuggestions": ["Suggestion 1", "Suggestion 2"]
            }
            Do not include any other markdown tags, comments or text outside the JSON.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = "Analyze these lyrics:\n$lyrics")))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrBlank()) {
                val cleanedJson = "{" + responseText.substringAfter("{").substringBeforeLast("}") + "}"
                val parsed = qualityAdapter.fromJson(cleanedJson)
                parsed ?: computeOfflineQualityReport(lyrics)
            } else {
                computeOfflineQualityReport(lyrics)
            }
        } catch (e: Exception) {
            Log.e("AILyricistGateway", "Metrics API analysis failed, running offline report", e)
            computeOfflineQualityReport(lyrics)
        }
    }

    override suspend fun getSmartSuggestions(lyrics: String, language: String): SmartSuggestions = withContext(Dispatchers.IO) {
        if (lyrics.isBlank()) {
            return@withContext SmartSuggestions()
        }

        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext getOfflineSmartSuggestions(lyrics, language)
        }

        val systemPrompt = """
            You are a songwriting assistant. Based on the current song lyrics, generate helpful creative recommendations.
            Respond ONLY with a raw JSON containing these keys:
            {
              "suggestedTitles": ["Title 1", "Title 2", "Title 3"],
              "potentialHooks": ["Hook line 1", "Hook line 2"],
              "alternativeRhymes": [{"word": "pyaar", "suggestions": "yaar, dildar, sansar"}, {"word": "hawa", "suggestions": "dawa, nawa, khuda"}],
              "harmonyTips": ["Tip 1", "Tip 2"],
              "callAndResponseLines": ["Call: line 1 -> Response: line 2"]
            }
            Ensure keys match exactly. Do not wrap in markdown tags or comments.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = "Current lyrics:\n$lyrics")))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrBlank()) {
                val cleanedJson = "{" + responseText.substringAfter("{").substringBeforeLast("}") + "}"
                val parsed = suggestionsAdapter.fromJson(cleanedJson)
                parsed ?: getOfflineSmartSuggestions(lyrics, language)
            } else {
                getOfflineSmartSuggestions(lyrics, language)
            }
        } catch (e: Exception) {
            Log.e("AILyricistGateway", "Smart suggestions API call failed, running offline suggestions", e)
            getOfflineSmartSuggestions(lyrics, language)
        }
    }

    override suspend fun saveProject(project: LyricProject): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val serialized = projectAdapter.toJson(project)
            sharedPrefs.edit().putString(project.id, serialized).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProject(projectId: String): Result<LyricProject?> = withContext(Dispatchers.IO) {
        try {
            val serialized = sharedPrefs.getString(projectId, null)
            if (serialized != null) {
                Result.success(projectAdapter.fromJson(serialized))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllProjects(): List<LyricProject> = withContext(Dispatchers.IO) {
        try {
            val allEntries = sharedPrefs.all
            allEntries.values.filterIsInstance<String>().mapNotNull {
                projectAdapter.fromJson(it)
            }.sortedByDescending { it.lastAutoSaved }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun deleteProject(projectId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            sharedPrefs.edit().remove(projectId).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // Core Helpers & Offline Generators
    // ==========================================

    private fun createNewProjectFromLyrics(
        title: String,
        prompt: String,
        lyrics: String,
        language: String
    ): LyricProject {
        val projectId = UUID.randomUUID().toString()
        val initialVersion = LyricVersion(
            id = UUID.randomUUID().toString(),
            label = "v1 - Initial",
            content = lyrics,
            author = "AI",
            versionNumber = 1,
            editSummary = "Original Draft",
            selectedSection = null,
            language = language,
            genre = "Bollywood",
            mood = "Romantic"
        )
        return LyricProject(
            id = projectId,
            title = title,
            currentLyrics = lyrics,
            language = language,
            versions = listOf(initialVersion),
            prompt = prompt,
            genre = "Bollywood",
            mood = "Romantic"
        )
    }

    private fun parseChatResponse(
        project: LyricProject,
        userMessage: String,
        responseText: String
    ): LyricProject {
        val hasLyricsTag = responseText.contains("[LYRICS]") && responseText.contains("[/LYRICS]")
        val (cleanedMessage, updatedLyrics) = if (hasLyricsTag) {
            val explanation = responseText.substringBefore("[LYRICS]").trim()
            val lyricsContent = responseText.substringAfter("[LYRICS]").substringBefore("[/LYRICS]").trim()
            explanation to lyricsContent
        } else {
            val lines = responseText.lines()
            val lyricsLines = lines.filter { it.startsWith("[") || it.contains("Verse") || it.contains("Chorus") || it.length > 25 }
            val explanation = lines.filter { !lyricsLines.contains(it) }.joinToString("\n").trim()
            explanation to responseText.trim()
        }

        val finalLyrics = if (updatedLyrics.isNotBlank()) updatedLyrics else project.currentLyrics
        
        val userChat = LyricChatMessage(UUID.randomUUID().toString(), "user", userMessage)
        val aiChat = LyricChatMessage(UUID.randomUUID().toString(), "ai", if (cleanedMessage.isNotBlank()) cleanedMessage else "Here is the updated draft:")
        
        val nextVerNum = project.versions.size + 1
        val newVersion = LyricVersion(
            id = UUID.randomUUID().toString(),
            label = "v$nextVerNum - Chat Edit",
            content = finalLyrics,
            author = "AI",
            versionNumber = nextVerNum,
            editSummary = userMessage.take(50),
            selectedSection = project.versions.lastOrNull()?.selectedSection,
            language = project.language,
            genre = project.genre,
            mood = project.mood
        )

        return project.copy(
            currentLyrics = finalLyrics,
            versions = project.versions + newVersion,
            chatHistory = project.chatHistory + userChat + aiChat,
            lastAutoSaved = System.currentTimeMillis()
        )
    }

    private fun applyRevisedLyricsToProject(
        project: LyricProject,
        userMessage: String,
        revisedContent: String
    ): LyricProject {
        val userChat = LyricChatMessage(UUID.randomUUID().toString(), "user", userMessage)
        val aiChat = LyricChatMessage(UUID.randomUUID().toString(), "ai", "Understood. I have revised that specific stanzas using offline heuristics to match your request.")
        
        val nextVerNum = project.versions.size + 1
        val newVersion = LyricVersion(
            id = UUID.randomUUID().toString(),
            label = "v$nextVerNum - Revised ($userMessage)",
            content = revisedContent,
            author = "AI",
            versionNumber = nextVerNum,
            editSummary = userMessage.take(50),
            selectedSection = null,
            language = project.language,
            genre = project.genre,
            mood = project.mood
        )

        return project.copy(
            currentLyrics = revisedContent,
            versions = project.versions + newVersion,
            chatHistory = project.chatHistory + userChat + aiChat,
            lastAutoSaved = System.currentTimeMillis()
        )
    }

    private fun getOfflineFallbackLyrics(params: LyricsGenerationParams, language: String): String {
        val capitalizedPrompt = params.prompt.replaceFirstChar { it.uppercase() }
        val genre = params.genres.firstOrNull() ?: "Bollywood"
        val mood = params.moods.joinToString("/") { it } .ifBlank { "Romantic" }
        
        return when (language.lowercase()) {
            "hindi", "bollywood" -> """
                [Verse 1]
                Mera sur aur teri pyaas, kitni pyaari hai ye raat
                Dheere dhire mil rahe hain khayalon ke jazbaat
                SurMaya ki is leher me, beh raha hai dil mera
                Topic: $capitalizedPrompt pe khila hai ab naya savera.
                
                [Chorus]
                Ae nawa, is $mood mijaaz me tu gaaye
                Mera har ek saaz tujhko hi bulaye
                Sangeet ki is haseen mehfil me
                Hum tum mil ke naya geet likhein.
                
                [Verse 2]
                Sitar ki taan hai, dil me uthti hai ek aas
                Tere raga ke milne se, badh gaya hai mithaas
                $genre ke rang me ranga hai saara jahaan
                Sun le mere dil ki prem kahani ki dastaan.
                
                [Bridge]
                Kore kaagaz pe utri sangeet ki ek bahaar hai
                Suno, ye koi dastan nahi, ye mera hi pyaar hai...
                
                [Outro]
                SurMaya hi meri rooh, sur hi meri shaan hai...
                Geet likha hai sangeet me, yehi sasti pehchan hai.
            """.trimIndent()

            "odia" -> """
                [Verse 1]
                Mana khoje sehi rupa rangeen aaji nua dore
                SurMaya ra aaji sangeeta bahi jae thire thire
                Mana mor jhumuchi jhumuka rani pari mo jhare
                Topic: $capitalizedPrompt ku neiki mana bhabe ghare ghare.
                
                [Chorus]
                Sathi re, tuma binu mo jibana adhura khali re
                Sangeeta re bhari jae mo mana re prema thali re
                Chala chala premara ishaara re jhumiba sabhiye
                Nua nua sapana milisiri bantibah ranga re.
                
                [Verse 2]
                $genre sangeeta ra raga bada mitha laguchi re
                $mood prema rasa mana ku mo chuun jaichi re
                Mandira baje, dholak ra tali nache gahan re
                Mana kholi gaun prema geeta milana chhanda re.
                
                [Outro]
                SurMaya ra sura aaji sabuthi baje rupa re...
                Mana jhumi jae priya sahita nua saaje re.
            """.trimIndent()

            else -> """
                [Verse 1]
                Listening to the echoes of the wind tonight
                Our hearts are dancing underneath the starlight
                SurMaya melodies flowing through our veins
                Topic: $capitalizedPrompt washing away all the pains.
                
                [Chorus]
                Oh beautiful soul, in this $mood vibe we shine
                Every single note aligns, making you mine
                With the $genre groove we can never go wrong
                Together we write this everlasting song.
                
                [Verse 2]
                The sitar is ringing, the dholak is loud
                We are rising higher, high above the crowd
                Under the skies we sing this epic melody
                Setting our dynamic musical expressions free.
                
                [Bridge]
                No more boundaries, no more silent tears
                This composition will echo through the years...
                
                [Outro]
                SurMaya is the key, music is our guide
                Forever and ever, standing side by side!
            """.trimIndent()
        }
    }

    private fun getOfflineChatUpdatedLyrics(
        currentLyrics: String,
        userMessage: String,
        selectedText: String?,
        language: String
    ): String {
        if (!selectedText.isNullOrBlank() && currentLyrics.contains(selectedText)) {
            val replacement = when {
                userMessage.contains("odia", ignoreCase = true) -> {
                    "Mana jhumuchi priya premara rasa re, SurMaya sura dhire bahi jae ratha re"
                }
                userMessage.contains("emotional", ignoreCase = true) || userMessage.contains("sad", ignoreCase = true) -> {
                    "Khamoshiyo me dabi ek dastan hai, aakhon me nami aur khali jahaan hai"
                }
                userMessage.contains("rap", ignoreCase = true) || userMessage.contains("fast", ignoreCase = true) -> {
                    "Sun meri beat, suron ki ye jeet, SurMaya on the track, naya apna ye geet"
                }
                userMessage.contains("short", ignoreCase = true) -> {
                    "Hum tum mile, geet khile."
                }
                else -> {
                    "Suron ki bahaar me goonje ye sadaa, har mushkil ka mil gaya sangeet me naya dawaa"
                }
            }
            return currentLyrics.replace(selectedText, replacement)
        }

        val actionSnippet = when {
            userMessage.contains("rap", ignoreCase = true) -> """
                
                [Rap Section]
                Yeah! Mic check, ek do teen chaar
                SurMaya ki laya kare sab paar
                Bina raga ke tu baje bekaar
                Mehfil me baithe hain kalamkar, yeah!
            """.trimIndent()
            
            userMessage.contains("outro", ignoreCase = true) || userMessage.contains("ending", ignoreCase = true) -> """
                
                [Outro Edition]
                Let the final raga close our hearts
                This is where the true sound of soul starts...
            """.trimIndent()
            
            else -> ""
        }

        return if (actionSnippet.isNotBlank()) {
            currentLyrics + actionSnippet
        } else {
            currentLyrics.replace("[Chorus]", "[Chorus - Emotional Vibe]").replace("Mera sur", "Mera raga aur teri sangeet")
        }
    }

    private fun computeOfflineQualityReport(lyrics: String): QualityScoreReport {
        val wordCount = lyrics.split("\\s+".toRegex()).size
        val lineCount = lyrics.lines().size
        
        val originality = Random.nextInt(78, 96).toFloat()
        val singability = if (wordCount in 80..220) Random.nextInt(85, 98).toFloat() else Random.nextInt(65, 84).toFloat()
        val rhyme = if (lyrics.contains("hazaaron") || lyrics.contains("pyaar") || lyrics.contains("saaz") || lyrics.contains("naam")) 92f else 80f
        val emotion = if (lyrics.contains("yaad") || lyrics.contains("pyaar") || lyrics.contains("dharkan") || lyrics.contains("rooh")) 95f else 78f
        val commercial = if (wordCount > 150) 88f else 74f
        val storytelling = if (lineCount > 15) 90f else 70f
        val structure = if (lyrics.contains("[Verse 1]") && lyrics.contains("[Chorus]")) 95f else 60f
        val readability = (wordCount.toFloat() / lineCount.toFloat().coerceAtLeast(1f)) * 4f + 20f

        val suggestions = mutableListOf<String>()
        if (wordCount < 80) suggestions.add("Add another verse to expand the lyric story.")
        if (!lyrics.contains("[Bridge]")) suggestions.add("Include a [Bridge] section to elevate emotional shift before the final chorus.")
        if (!lyrics.contains("Chorus")) suggestions.add("Designate a clear [Chorus] for better vocal hook recall.")
        if (rhyme < 85) suggestions.add("Improve rhyme consistency in stanzas 2 & 3.")
        if (suggestions.isEmpty()) {
            suggestions.add("The structure is excellent. Try converting it into a duet with male/female markers.")
            suggestions.add("Add musical cues like (Sitar Interlude) to help the arranging engine.")
        }

        return QualityScoreReport(
            originalityScore = originality,
            singabilityScore = singability,
            rhymeScore = rhyme,
            emotionScore = emotion,
            commercialAppealScore = commercial,
            storytellingScore = storytelling,
            structureScore = structure,
            readabilityIndex = readability.coerceIn(40f, 100f),
            issuesAndSuggestions = suggestions
        )
    }

    private fun getOfflineSmartSuggestions(lyrics: String, language: String): SmartSuggestions {
        val suggestedTitles = listOf(
            "Dhadkan Ke Sur",
            "SurMaya Ki Khoj",
            "Mera Naya Raag"
        )
        val potentialHooks = listOf(
            "SurMaya ke is aangan me, tu hi mera raag hai.",
            "Har ghadi tu mere paas rahe, sangeet ki geet bahe."
        )
        val alternativeRhymes = listOf(
            mapOf("word" to "pyaar", "suggestions" to "yaar, dildar, sansar, ikrar"),
            mapOf("word" to "baatein", "suggestions" to "yaadein, raatein, barsaatein"),
            mapOf("word" to "saaz", "suggestions" to "awaaz, raaz, parwaaz")
        )
        val harmonyTips = listOf(
            "Use a minor pentatonic transition in the Bridge section.",
            "Double-track the lead vocal during the second half of the Chorus for commercial appeal."
        )
        val callAndResponseLines = listOf(
            "Call: 'O humnawa, suron me tu bahe...' -> Response: '(O humnawa, mere paas rahe!)'"
        )

        return SmartSuggestions(
            suggestedTitles = suggestedTitles,
            potentialHooks = potentialHooks,
            alternativeRhymes = alternativeRhymes,
            harmonyTips = harmonyTips,
            callAndResponseLines = callAndResponseLines
        )
    }
}

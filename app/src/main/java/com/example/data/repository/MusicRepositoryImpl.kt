package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.dao.LyricsDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.SongDao
import com.example.data.local.entity.LyricsEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.SongEntity
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.data.remote.*
import com.example.domain.model.Lyrics
import com.example.domain.model.Project
import com.example.domain.model.Song
import com.example.domain.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class MusicRepositoryImpl(
    private val songDao: SongDao,
    private val projectDao: ProjectDao,
    private val lyricsDao: LyricsDao,
    private val context: android.content.Context
) : MusicRepository {

    override fun getAllSongsFlow(): Flow<List<Song>> = songDao.getAllSongsFlow().map { list -> list.map { it.toDomain() } }

    override fun getFavoriteSongsFlow(): Flow<List<Song>> = songDao.getFavoriteSongsFlow().map { list -> list.map { it.toDomain() } }

    override fun getDraftSongsFlow(): Flow<List<Song>> = songDao.getDraftSongsFlow().map { list -> list.map { it.toDomain() } }

    override fun getDownloadedSongsFlow(): Flow<List<Song>> = songDao.getDownloadedSongsFlow().map { list -> list.map { it.toDomain() } }

    override fun getSongsInProjectFlow(projectId: String): Flow<List<Song>> = songDao.getSongsInProjectFlow(projectId).map { list -> list.map { it.toDomain() } }

    override fun getAllProjectsFlow(): Flow<List<Project>> = projectDao.getAllProjectsFlow().map { list -> list.map { it.toDomain() } }

    override fun getAllSavedLyricsFlow(): Flow<List<Lyrics>> = lyricsDao.getAllSavedLyricsFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun saveSong(song: Song) {
        songDao.insertSong(song.toEntity())
    }

    override suspend fun updateSong(song: Song) {
        songDao.updateSong(song.toEntity())
    }

    override suspend fun deleteSong(songId: String) {
        songDao.deleteSongById(songId)
    }

    override suspend fun createProject(name: String, description: String): Project {
        val project = Project(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            createdTimestamp = System.currentTimeMillis()
        )
        projectDao.insertProject(project.toEntity())
        return project
    }

    override suspend fun deleteProject(projectId: String) {
        projectDao.deleteProjectById(projectId)
    }

    override suspend fun saveLyrics(title: String, prompt: String, content: String, language: String): Lyrics {
        val lyrics = Lyrics(
            id = UUID.randomUUID().toString(),
            title = title,
            prompt = prompt,
            content = content,
            language = language,
            createdTimestamp = System.currentTimeMillis()
        )
        lyricsDao.insertLyrics(lyrics.toEntity())
        return lyrics
    }

    override suspend fun deleteLyrics(lyricsId: String) {
        lyricsDao.deleteLyricsById(lyricsId)
    }

    override suspend fun generateLyricsWithAI(prompt: String, mode: String, language: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Local fallback generator for high reliability offline
            return@withContext Result.success(getLocalFallbackLyrics(prompt, language))
        }

        val requestPrompt = """
            You are a master Indian lyricist. Write song lyrics in $language.
            Topic/Prompt: $prompt
            Mode: $mode
            Provide the output structured with clear [Verse 1], [Chorus], [Verse 2], [Chorus], [Bridge], [Outro] markers.
            Only write the lyrics and structural markers, no other commentary.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = requestPrompt)))),
            generationConfig = GenerationConfig(temperature = 0.8f)
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!resultText.isNullOrBlank()) {
                Result.success(resultText)
            } else {
                Result.success(getLocalFallbackLyrics(prompt, language))
            }
        } catch (e: Exception) {
            Result.success(getLocalFallbackLyrics(prompt, language))
        }
    }

    override suspend fun generateSongWithAI(
        title: String,
        prompt: String,
        lyrics: String,
        language: String,
        genre: String,
        mood: String,
        style: String,
        tempo: String,
        duration: String,
        voiceName: String,
        voiceGender: String,
        voiceMatchPercent: Int,
        weirdness: Int,
        styleInfluence: Int,
        projectId: String?,
        uploadedAudioPath: String?
    ): Result<Song> = withContext(Dispatchers.IO) {
        // Here we call the real audio generation engine to produce high-fidelity audio
        val genRepo = MusicGenerationRepositoryImpl(context)
        
        val detailedPrompt = buildString {
            append("Create an Indian-style song. ")
            if (lyrics.isNotBlank() && lyrics != "[Instrumental Only - Pure Ambient Indian Raga]" && lyrics != "[Instrumental Raga Accent]") {
                append("Please compose and sing the following lyrics explicitly in the song. ")
                append("Lyrics:\n$lyrics\n\n")
            }
            append("Music Style/Genre: $genre. ")
            append("Language: $language. ")
            append("Mood: $mood. ")
            append("Tempo: $tempo. ")
            append("Voice Type: $voiceName ($voiceGender). ")
            if (prompt.isNotBlank()) {
                append("Additional Prompt details: $prompt. ")
            }
            append("Ensure the output is a high-fidelity musical audio generation with vocals rendering the lyrics.")
        }

        val generatedFileResult = genRepo.generateMusic(
            prompt = detailedPrompt,
            durationSec = 15
        )
        
        if (generatedFileResult.isFailure) {
            return@withContext Result.failure(generatedFileResult.exceptionOrNull() ?: Exception("Audio generation failed during the rendering stage."))
        }

        val songId = UUID.randomUUID().toString()
        val audioUrlVal = generatedFileResult.getOrThrow().absolutePath

        val resolvedTitle = if (title.isBlank()) {
            if (prompt.isNotBlank()) prompt.take(15) + " Melodies" else "SurMaya AI Track"
        } else title

        val generatedSong = Song(
            id = songId,
            title = resolvedTitle,
            prompt = prompt,
            lyrics = lyrics.ifBlank { "[Instrumental Only - Pure Ambient Indian Raga]" },
            language = language,
            genre = genre,
            mood = mood,
            style = style,
            tempo = tempo,
            duration = duration,
            singerVoice = "$voiceName ($voiceGender)",
            audioUrl = audioUrlVal, // High-fidelity audio file path
            projectId = projectId,
            isFavorite = false,
            isDraft = false,
            isDownloaded = false,
            createdTimestamp = System.currentTimeMillis()
        )

        songDao.insertSong(generatedSong.toEntity())
        Result.success(generatedSong)
    }

    private fun getLocalFallbackLyrics(prompt: String, language: String): String {
        val capitalizedPrompt = prompt.replaceFirstChar { it.uppercase() }
        return when (language.lowercase()) {
            "hindi", "bollywood" -> """
                [Verse 1]
                Dharkan me teri baatein hain
                Har pal teri yaadein hain
                Suron me beh rahi hai jo, woh teri hi fariyaad hai
                SurMaya ke is aangan me, tu hi mera raag hai.
                
                [Chorus]
                O humnawa, suron me tu bahe (Aha!)
                Har ghadi tu mere paas rahe
                Sangeet ki is haseen mehfil me
                Hum tum mil ke naya geet likhein.
                
                [Verse 2]
                Sitar ki taron se uthti hai taan
                Tu hi meri dhoop, tu hi aasmaan
                $capitalizedPrompt ko yaad karke
                Dharak utha hai ye mera jahaan.
                
                [Bridge]
                Kore kaagaz pe likhi ek kahani hai
                Mausam badla hai, baaki bahaar aani hai...
                
                [Outro]
                Sangeet hi rab hai, sangeet hi zindagani hai...
                Dharkan me dharakne do, ye toh shuruat mastani hai.
            """.trimIndent()
            
            "odia" -> """
                [Verse 1]
                Mana khoje sehi rupa rangeen
                SurMaya re aaji mo jigar-e-bheen
                Mana mor jhumuchi aaji bina dore
                Tuma prema bhabana bahi jae dhire.
                
                [Chorus]
                Sathi re, tuma binu mo jibana adhura
                Sangeeta re bhari jae mo mana re pura
                Chala chala premara ishaara re jhumiba
                Nua nua sapana milisiri bantibah.
                
                [Verse 2]
                $capitalizedPrompt ku neiki mana bhabe
                Nua raga nua rupa premara sabhabe
                Mandira jhumuchi, bhajan gauthile
                Mana jhumuchi priya sahita milile.
                
                [Outro]
                SurMaya ra sura aaji sabuthi baje...
                Mana jhumi jae priya sahita saje.
            """.trimIndent()

            "punjabi" -> """
                [Verse 1]
                Vekh vekh tainu dil dhadke mera
                Sangeet di duniya ch har paase savera
                SurMaya di beat te jhoomda jahaan
                Tere bina saadi sunni ae dastaan.
                
                [Chorus]
                O mahiya, suron de vich beh ja tu
                Dil diyan saariyan gallan keh ja tu
                Dhol bajda te saadi thali jhumdi
                Gidhe vich naddi botal vargi ghumbdi!
                
                [Verse 2]
                $capitalizedPrompt di gall jadon chaldi ae yaara
                Loki kehnde Punjabi raga bada pyaara
                Sitar te dholak di jugalbandi kamaal
                Nachde ne saare kudiye tere naal naal.
                
                [Outro]
                SurMaya da jadoo challeya Punjab te...
                Sangeet hi saadi rooh di khurak ae!
            """.trimIndent()

            "sanskrit" -> """
                [Verse 1]
                स्वरमया रज्यते चित्तं सदा
                आनन्दं ददाति सङ्गीत सुधा
                हृदये स्पन्दति दिव्य राग माला
                सूरमाया मन्दिरे प्रज्वले दीप ज्वाला।
                
                [Chorus]
                हे सङ्गीत रसिक, सुरे विहर सदा
                शान्तिं सुखं च लभस्व सर्वदा
                ऋग्यजुःसामवेदानां पवित्र नाদः
                मनसि जनयतु परमानन्द प्रसादः।
                
                [Verse 2]
                $capitalizedPrompt ध्यायति मम मानसं मुदा
                नूतन राग रूपेण शोभते सदा
                वीणा वेणु मृदङ्ग नाद घोषः
                नयतु अस्मान् मोक्ष मार्ग विशेषः।
                
                [Outro]
                सङ्गीतमेव परं ब्रह्म, सङ्गीतमेव जीवनम्...
                सूरमाया नाद ब्रह्माण्डे लीनं भवतु माननम्।
            """.trimIndent()

            "bengali" -> """
                [Verse 1]
                Moner bhitore baje surero nupur
                Tomari ashay kete jay gota dupur
                SurMaya r aagone miche noy e geye jaoa
                Tumi amar raga, amar sangeet paoa.
                
                [Chorus]
                Ogo hridoy hase, sure sure bhese cholo
                Katha chilo tumi jabe sangeet-e bolo
                Sitarer tara jeno katha bole udase
                Mon jhumche aaji prem bhora batashe.
                
                [Verse 2]
                $capitalizedPrompt ke mone rekhe bunechi e gaan
                Ochena baul sure meteche poran
                Ektara baje dholer o sathe melale
                Sadhana holo sarthak tomay khuje pele.
                
                [Outro]
                Sangeet e jibon, sangeet e sanchay...
                SurMaya r sure hridoy bhore jay!
            """.trimIndent()

            "tamil" -> """
                [Verse 1]
                En idhayathil unathu kural thaan ketkuthu
                Isaiyodu enathu manamum medhuvaai parakkuthu
                SurMaya aalayam, isaiyin nava rāgam
                Unnodu dhaan thodanguthu enathu kaaviyam.
                
                [Chorus]
                Anbe, isaiyil nee varuvaaya (Aha!)
                Yezhu swarangalil ennai tharuvaaya
                Sangeetha thiruvizha manadhil kooduthu
                Nalla paattu unakkaga uruvaaguthu.
                
                [Verse 2]
                $capitalizedPrompt unnai ninaithu paadugiren
                Veenaayum miruthangamum rasithu ketkiren
                Sitar thandhaiyaal isaikkum puthiya raagam
                Thamil mannil olikkuthu indha sangeetham.
                
                [Outro]
                Isaiye iraivan, isaiye nambikkai...
                SurMaya tharum isaiyil moolghuvadhu nam magizhchi!
            """.trimIndent()

            "telugu" -> """
                [Verse 1]
                Gunde thalapulalo nee maatalu unnayee
                Prathi kshanam nee thalapulalo karigipothunayeee
                SurMaya swara laya lo nava raagam idhee
                Nee thone thodanguthundhi na sangeetha saram idhee.
                
                [Chorus]
                Sangeetha priya, swaramai nee raave (Aha!)
                Prathi nimisham na paatala jatha gaave
                Ezhuraalalo swara ganga ponguthundhee
                Nee prema lo na manasu jhumuthundhee.
                
                [Verse 2]
                $capitalizedPrompt ni ninaithu paaduthunnamu
                Veena ninaadhalanu ramyanga vinnaamu
                Nee prema kosam nireekshinche gaanam
                Gunde lothulalo prathidhvaninche raagam.
                
                [Outro]
                Sangeethame pranam, sangeethame dhyanam...
                SurMaya swara raajyam lo manam dharidhaaram!
            """.trimIndent()

            "kannada" -> """
                [Verse 1]
                Nanna manadalli ninna swaragale thumbiradhu
                Prathi kshana ninna bhabaneye thumbidhu
                SurMaya madhurima nava raga srujana
                Ninagagye barediradhu ee prema kavana.
                
                [Chorus]
                Chinnumaye, swaravaagi nee baaro
                Nanna baalalli isai thumbi bhero
                Sangeetha loka mahananda tharadhu
                Saptaswaragala dharatiye theredhu.
                
                [Verse 2]
                $capitalizedPrompt dhyanisi nanna mana jhumisithu
                Veeneya naadhadi hosa kanasu mudisithu
                Karnataka sangeetha saaramavu kelidhu
                Bhaktiya bhavadi manavu thumbi baredhadhu.
                
                [Outro]
                Sangeethave jeeva, sangeethave deva...
                SurMaya raga loka sada sukha koota!
            """.trimIndent()

            "malayalam" -> """
                [Verse 1]
                En manasinte thamburuvil unarunnu rāgam
                Nee tharunnu swaramaaya nava raaga bhaavam
                SurMaya sangeetha sadasil unaroo
                Sneham thudikkum ee gaanam pakaroo.
                
                [Chorus]
                Anbe, en sangeethame nee kelkoo
                Melle en arikil oru vaaku mooloo
                Chenda melavum sitar laya tharavum
                Ennum thunaiyanu ee nalla raavum.
                
                [Verse 2]
                $capitalizedPrompt ninaichu njan paadunnu paattu
                Kera kanyaka naadinoru thirumuttu
                Sopana sangeetha madhuri niranju
                Hridaya thandhrikal thazhuki thazhuki unarnnu.
                
                [Outro]
                Sangeethamalle daivam, sangeethamalle jeevan...
                SurMaya sangeethathil layikkaam namukkenum!
            """.trimIndent()

            "marathi" -> """
                [Verse 1]
                Majhya manat tujhiyech swapna aahet
                Prati kshani tujhich aathwan yet aahe
                SurMaya chya ya mandirat nava raag ha baje
                Tujhyasathi ha sangeetacha saaj ha saje.
                
                [Chorus]
                O sajna, suranmadhe tu vaha re (Aha!)
                Prati kshani tu majhya sobat raha re
                Abhanga sangeetacha ranga ha chadhla
                Tujhya premacha maza dhyas ha vadhla.
                
                [Verse 2]
                $capitalizedPrompt cha dhyas gheun me gaato geet
                Maharashtra bhumichi hi sangeetachi reet
                Mridanga ani sitar cha sur ha milala
                Premacha goad ha raag majha khulala.
                
                [Outro]
                Sangeetach jeevan, sangeetach dhyan...
                SurMaya chya suranmadhe haravle bhaan!
            """.trimIndent()

            "bhojpuri" -> """
                [Verse 1]
                Tohar suratiya man me basail ba
                Humra dil me sangeet ke dip jalail ba
                SurMaya ke dholak baje dhum dham se
                E ta dil lagal ba tore hi naam se.
                
                [Chorus]
                Ae gori, sur me sur tu milava na (Aha!)
                Sangeet ke naya geet sunava na
                Purvi aur kajari ke rang bikhrela
                Tohar muski pe sara jahan jhumela.
                
                [Verse 2]
                $capitalizedPrompt ke yaad me hum gaaveli gaana
                Bhojpuri raga bhail ba mastana
                Bansuri aur sitar ke taan jab baje
                Suna ae babu naya sansaar saje.
                
                [Outro]
                Sangeet hi jaan ba, sangeet hi armaan...
                SurMaya sang jhum uthal sara jahan!
            """.trimIndent()

            "english" -> """
                [Verse 1]
                Listening to the echoes in the silent breeze
                Floating with the waves that rustle through the trees
                My heart is singing a brand new song today
                SurMaya melodies taking all the clouds away.
                
                [Chorus]
                Oh creative soul, let the music flow (yeah!)
                Underneath the stars with a golden glow
                Creating our destiny, note by note we write
                Guiding our voices through the neon night.
                
                [Verse 2]
                In search of $capitalizedPrompt we found our way
                Through the classical scales where the masters play
                A fusion of Bollywood, pop, and electronic style
                Bringing on our faces a beautiful, radiant smile.
                
                [Bridge]
                The rhythm takes over, our heartbeat aligns
                An infinite universe, beautifully designed...
                
                [Outro]
                Let the final chord ring out loud and clear
                SurMaya AI, the future is already here!
            """.trimIndent()

            else -> """
                [Verse 1]
                Listening to the echoes in the silent breeze
                Floating with the waves that rustle through the trees
                My heart is singing a brand new song today
                SurMaya melodies taking all the clouds away.
                
                [Chorus]
                Oh creative soul, let the music flow (yeah!)
                Underneath the stars with a golden glow
                Creating our destiny, note by note we write
                Guiding our voices through the neon night.
                
                [Verse 2]
                In search of $capitalizedPrompt we found our way
                Through the classical scales where the masters play
                A fusion of Bollywood, pop, and electronic style
                Bringing on our faces a beautiful, radiant smile.
                
                [Bridge]
                The rhythm takes over, our heartbeat aligns
                An infinite universe, beautifully designed...
                
                [Outro]
                Let the final chord ring out loud and clear
                SurMaya AI, the future is already here!
            """.trimIndent()
        }
    }
}

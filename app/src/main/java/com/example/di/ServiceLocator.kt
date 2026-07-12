package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.repository.MusicRepositoryImpl
import com.example.data.repository.UserRepositoryImpl
import com.example.data.repository.MusicGenerationRepositoryImpl
import com.example.domain.repository.MusicRepository
import com.example.domain.repository.UserRepository
import com.example.domain.repository.MusicGenerationRepository

object ServiceLocator {
    @Volatile
    var applicationContext: Context? = null

    @Volatile
    private var database: AppDatabase? = null
    
    @Volatile
    private var userRepository: UserRepository? = null
    
    @Volatile
    private var musicRepository: MusicRepository? = null

    @Volatile
    private var musicGenerationRepository: MusicGenerationRepository? = null

    @Volatile
    private var composerRepository: com.example.domain.repository.ComposerRepository? = null

    @Volatile
    private var melodyRepository: com.example.domain.repository.MelodyRepository? = null

    @Volatile
    private var chordRepository: com.example.domain.repository.ChordRepository? = null

    @Volatile
    private var arrangementRepository: com.example.domain.repository.ArrangementRepository? = null

    @Volatile
    private var arrangementEngine: com.example.domain.model.arrangement.ArrangementEngine? = null

    @Volatile
    private var singerRepository: com.example.domain.repository.SingerRepository? = null

    @Volatile
    private var singerEngine: com.example.domain.model.singer.AISingerEngine? = null

    @Volatile
    private var mixingRepository: com.example.domain.repository.MixingRepository? = null

    @Volatile
    private var mixingEngine: com.example.domain.model.mixing.AIMixingEngine? = null

    @Volatile
    private var masteringRepository: com.example.domain.repository.MasteringRepository? = null

    @Volatile
    private var masteringEngine: com.example.domain.mastering.IMasteringEngine? = null

    @Volatile
    private var qaRepository: com.example.domain.repository.QARepository? = null

    @Volatile
    private var qaValidationEngine: com.example.domain.model.qa.IAudioQualityValidationEngine? = null

    @Volatile
    private var voiceGateway: com.example.data.remote.gateway.voice.VoiceGatewayImpl? = null

    @Volatile
    private var aiLyricistGateway: com.example.data.remote.gateway.lyrics.AILyricistGateway? = null

    @Volatile
    private var studioService: com.example.domain.service.StudioService? = null

    fun getStudioService(context: Context): com.example.domain.service.StudioService {
        applicationContext = context.applicationContext
        return studioService ?: synchronized(this) {
            val service = com.example.data.service.StudioServiceImpl(context.applicationContext, getMusicRepository(context))
            studioService = service
            service
        }
    }

    fun getAILyricistGateway(context: Context): com.example.data.remote.gateway.lyrics.AILyricistGateway {
        applicationContext = context.applicationContext
        return aiLyricistGateway ?: synchronized(this) {
            val gateway = com.example.data.remote.gateway.lyrics.AILyricistGatewayImpl(context.applicationContext)
            aiLyricistGateway = gateway
            gateway
        }
    }

    fun getVoiceGateway(context: Context): com.example.data.remote.gateway.voice.VoiceGatewayImpl {
        applicationContext = context.applicationContext
        return voiceGateway ?: synchronized(this) {
            val gateway = com.example.data.remote.gateway.voice.VoiceGatewayImpl.getInstance(context.applicationContext)
            voiceGateway = gateway
            gateway
        }
    }

    private fun getDatabase(context: Context): AppDatabase {
        applicationContext = context.applicationContext
        return database ?: synchronized(this) {
            val db = AppDatabase.getDatabase(context)
            database = db
            db
        }
    }

    fun getUserRepository(context: Context): UserRepository {
        applicationContext = context.applicationContext
        return userRepository ?: synchronized(this) {
            val repo = UserRepositoryImpl(getDatabase(context).userDao(), context.applicationContext)
            userRepository = repo
            repo
        }
    }

    fun getMusicRepository(context: Context): MusicRepository {
        applicationContext = context.applicationContext
        return musicRepository ?: synchronized(this) {
            val db = getDatabase(context)
            val repo = MusicRepositoryImpl(db.songDao(), db.projectDao(), db.lyricsDao(), context.applicationContext)
            musicRepository = repo
            repo
        }
    }

    fun getMusicGenerationRepository(context: Context): MusicGenerationRepository {
        applicationContext = context.applicationContext
        return musicGenerationRepository ?: synchronized(this) {
            val repo = MusicGenerationRepositoryImpl(context.applicationContext)
            musicGenerationRepository = repo
            repo
        }
    }

    fun getComposerRepository(context: Context): com.example.domain.repository.ComposerRepository {
        applicationContext = context.applicationContext
        return composerRepository ?: synchronized(this) {
            val db = getDatabase(context)
            val repo = com.example.data.repository.ComposerRepositoryImpl(db.composerDao(), context.applicationContext)
            composerRepository = repo
            repo
        }
    }

    fun getMelodyRepository(context: Context): com.example.domain.repository.MelodyRepository {
        applicationContext = context.applicationContext
        return melodyRepository ?: synchronized(this) {
            val db = getDatabase(context)
            val repo = com.example.data.repository.MelodyRepositoryImpl(db.melodyDao(), context.applicationContext)
            melodyRepository = repo
            repo
        }
    }

    fun getChordRepository(context: Context): com.example.domain.repository.ChordRepository {
        applicationContext = context.applicationContext
        return chordRepository ?: synchronized(this) {
            val repo = com.example.data.repository.ChordRepositoryImpl(context.applicationContext)
            chordRepository = repo
            repo
        }
    }

    fun getArrangementRepository(context: Context): com.example.domain.repository.ArrangementRepository {
        applicationContext = context.applicationContext
        return arrangementRepository ?: synchronized(this) {
            val repo = com.example.data.repository.ArrangementRepositoryImpl(context.applicationContext)
            arrangementRepository = repo
            repo
        }
    }

    fun getArrangementEngine(): com.example.domain.model.arrangement.ArrangementEngine {
        return arrangementEngine ?: synchronized(this) {
            val engine = com.example.data.repository.ArrangementEngineImpl()
            arrangementEngine = engine
            engine
        }
    }

    fun getSingerRepository(context: Context): com.example.domain.repository.SingerRepository {
        applicationContext = context.applicationContext
        return singerRepository ?: synchronized(this) {
            val repo = com.example.data.repository.SingerRepositoryImpl(getSingerEngine())
            singerRepository = repo
            repo
        }
    }

    fun getSingerEngine(): com.example.domain.model.singer.AISingerEngine {
        return singerEngine ?: synchronized(this) {
            val engine = com.example.domain.model.singer.AISingerEngine()
            singerEngine = engine
            engine
        }
    }

    fun getMixingRepository(context: Context): com.example.domain.repository.MixingRepository {
        applicationContext = context.applicationContext
        return mixingRepository ?: synchronized(this) {
            val db = getDatabase(context)
            val repo = com.example.data.repository.MixingRepositoryImpl(db.mixingDao(), getMixingEngine())
            mixingRepository = repo
            repo
        }
    }

    fun getMixingEngine(): com.example.domain.model.mixing.AIMixingEngine {
        return mixingEngine ?: synchronized(this) {
            val engine = com.example.domain.model.mixing.AIMixingEngine()
            mixingEngine = engine
            engine
        }
    }

    fun getMasteringRepository(context: Context): com.example.domain.repository.MasteringRepository {
        applicationContext = context.applicationContext
        return masteringRepository ?: synchronized(this) {
            val db = getDatabase(context)
            val repo = com.example.data.repository.MasteringRepositoryImpl(db.masteringDao(), getMasteringEngine())
            masteringRepository = repo
            repo
        }
    }

    fun getMasteringEngine(): com.example.domain.mastering.IMasteringEngine {
        return masteringEngine ?: synchronized(this) {
            val engine = com.example.data.mastering.AIMasteringEngine()
            masteringEngine = engine
            engine
        }
    }

    fun getQARepository(context: Context): com.example.domain.repository.QARepository {
        applicationContext = context.applicationContext
        return qaRepository ?: synchronized(this) {
            val db = getDatabase(context)
            val repo = com.example.data.repository.QARepositoryImpl(db.qaDao(), context.applicationContext)
            qaRepository = repo
            repo
        }
    }

    fun getQAValidationEngine(): com.example.domain.model.qa.IAudioQualityValidationEngine {
        return qaValidationEngine ?: synchronized(this) {
            val engine = com.example.domain.model.qa.DefaultAudioQualityValidationEngine()
            qaValidationEngine = engine
            engine
        }
    }
}

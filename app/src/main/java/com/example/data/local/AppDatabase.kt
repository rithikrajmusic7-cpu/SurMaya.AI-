package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.LyricsDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.SongDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.ComposerDao
import com.example.data.local.entity.LyricsEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.SongEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.ComposerProjectEntity
import com.example.data.local.entity.CompositionVersionEntity

import com.example.data.local.dao.MelodyDao
import com.example.data.local.entity.MelodyProjectEntity
import com.example.data.local.dao.ChordDao
import com.example.data.local.dao.ArrangementDao
import com.example.data.local.entity.ChordProjectEntity
import com.example.data.local.entity.ChordHistoryEntity
import com.example.data.local.entity.ChordTemplateEntity
import com.example.data.local.entity.ArrangementProjectEntity
import com.example.data.local.entity.ArrangementSectionEntity
import com.example.data.local.entity.InstrumentTrackEntity
import com.example.data.local.entity.AutomationLaneEntity
import com.example.data.local.entity.ArrangementTransitionEntity
import com.example.data.local.entity.CounterMelodyEntity
import com.example.data.local.entity.ArrangementHistoryEntity
import com.example.data.local.entity.ArrangementTemplateEntity
import com.example.data.local.entity.ArrangementEvaluationEntity
import com.example.data.local.entity.ArrangementCacheEntity
import com.example.data.local.entity.MixProjectEntity
import com.example.data.local.entity.MasteringProjectEntity
import com.example.data.local.entity.MasteringHistoryEntity
import com.example.data.local.entity.MasteringPresetEntity
import com.example.data.local.dao.MixingDao
import com.example.data.local.dao.MasteringDao
import com.example.data.local.dao.QADao
import com.example.data.local.entity.QAQualityReportEntity
import com.example.data.local.entity.QADefectEntity

@Database(
    entities = [
        UserEntity::class,
        ProjectEntity::class,
        SongEntity::class,
        LyricsEntity::class,
        ComposerProjectEntity::class,
        CompositionVersionEntity::class,
        MelodyProjectEntity::class,
        ChordProjectEntity::class,
        ChordHistoryEntity::class,
        ChordTemplateEntity::class,
        ArrangementProjectEntity::class,
        ArrangementSectionEntity::class,
        InstrumentTrackEntity::class,
        AutomationLaneEntity::class,
        ArrangementTransitionEntity::class,
        CounterMelodyEntity::class,
        ArrangementHistoryEntity::class,
        ArrangementTemplateEntity::class,
        ArrangementEvaluationEntity::class,
        ArrangementCacheEntity::class,
        MixProjectEntity::class,
        MasteringProjectEntity::class,
        MasteringHistoryEntity::class,
        MasteringPresetEntity::class,
        QAQualityReportEntity::class,
        QADefectEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun songDao(): SongDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun composerDao(): ComposerDao
    abstract fun melodyDao(): MelodyDao
    abstract fun chordDao(): ChordDao
    abstract fun arrangementDao(): ArrangementDao
    abstract fun mixingDao(): MixingDao
    abstract fun masteringDao(): MasteringDao
    abstract fun qaDao(): QADao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "surmaya_ai_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

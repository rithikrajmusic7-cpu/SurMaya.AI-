package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.LyricsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricsDao {
    @Query("SELECT * FROM saved_lyrics ORDER BY createdTimestamp DESC")
    fun getAllSavedLyricsFlow(): Flow<List<LyricsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyrics(lyrics: LyricsEntity)

    @Delete
    suspend fun deleteLyrics(lyrics: LyricsEntity)

    @Query("DELETE FROM saved_lyrics WHERE id = :lyricsId")
    suspend fun deleteLyricsById(lyricsId: String)
}

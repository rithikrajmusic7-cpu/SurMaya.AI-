package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY createdTimestamp DESC")
    fun getAllSongsFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY createdTimestamp DESC")
    fun getFavoriteSongsFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isDraft = 1 ORDER BY createdTimestamp DESC")
    fun getDraftSongsFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isDownloaded = 1 ORDER BY createdTimestamp DESC")
    fun getDownloadedSongsFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE projectId = :projectId ORDER BY createdTimestamp DESC")
    fun getSongsInProjectFlow(projectId: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: String): SongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Delete
    suspend fun deleteSong(song: SongEntity)

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteSongById(songId: String)
}

package com.example.domain.repository

import com.example.domain.model.Song
import com.example.domain.model.Project
import com.example.domain.model.Lyrics
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getAllSongsFlow(): Flow<List<Song>>
    fun getFavoriteSongsFlow(): Flow<List<Song>>
    fun getDraftSongsFlow(): Flow<List<Song>>
    fun getDownloadedSongsFlow(): Flow<List<Song>>
    fun getSongsInProjectFlow(projectId: String): Flow<List<Song>>
    fun getAllProjectsFlow(): Flow<List<Project>>
    fun getAllSavedLyricsFlow(): Flow<List<Lyrics>>

    suspend fun saveSong(song: Song)
    suspend fun updateSong(song: Song)
    suspend fun deleteSong(songId: String)

    suspend fun createProject(name: String, description: String): Project
    suspend fun deleteProject(projectId: String)

    suspend fun saveLyrics(title: String, prompt: String, content: String, language: String): Lyrics
    suspend fun deleteLyrics(lyricsId: String)

    suspend fun generateLyricsWithAI(prompt: String, mode: String, language: String): Result<String>
    suspend fun generateSongWithAI(
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
    ): Result<Song>
}

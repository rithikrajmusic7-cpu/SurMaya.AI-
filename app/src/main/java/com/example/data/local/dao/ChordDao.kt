package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ChordProjectEntity
import com.example.data.local.entity.ChordHistoryEntity
import com.example.data.local.entity.ChordTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChordDao {
    @Query("SELECT * FROM chord_projects ORDER BY updatedTimestamp DESC")
    fun getAllProjects(): Flow<List<ChordProjectEntity>>

    @Query("SELECT * FROM chord_projects WHERE id = :id")
    suspend fun getProjectById(id: String): ChordProjectEntity?

    @Query("SELECT * FROM chord_projects WHERE id = :id")
    fun getProjectByIdFlow(id: String): Flow<ChordProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ChordProjectEntity)

    @Update
    suspend fun updateProject(project: ChordProjectEntity)

    @Query("DELETE FROM chord_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)

    // History Queries
    @Query("SELECT * FROM chord_history WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getHistoryForProject(projectId: String): Flow<List<ChordHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ChordHistoryEntity)

    @Query("DELETE FROM chord_history WHERE projectId = :projectId")
    suspend fun deleteHistoryByProject(projectId: String)

    // Template Queries
    @Query("SELECT * FROM chord_templates")
    fun getAllTemplates(): Flow<List<ChordTemplateEntity>>

    @Query("SELECT * FROM chord_templates WHERE genre = :genre")
    fun getTemplatesByGenre(genre: String): Flow<List<ChordTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: ChordTemplateEntity)
}

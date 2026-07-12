package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.MasteringHistoryEntity
import com.example.data.local.entity.MasteringPresetEntity
import com.example.data.local.entity.MasteringProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MasteringDao {
    // Project Queries
    @Query("SELECT * FROM mastering_projects ORDER BY updatedTimestamp DESC")
    fun getAllProjects(): Flow<List<MasteringProjectEntity>>

    @Query("SELECT * FROM mastering_projects WHERE id = :id")
    suspend fun getProjectById(id: String): MasteringProjectEntity?

    @Query("SELECT * FROM mastering_projects WHERE id = :id")
    fun getProjectByIdFlow(id: String): Flow<MasteringProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: MasteringProjectEntity)

    @Update
    suspend fun updateProject(project: MasteringProjectEntity)

    @Query("DELETE FROM mastering_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)

    // History Queries
    @Query("SELECT * FROM mastering_history WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getHistoryForProject(projectId: String): Flow<List<MasteringHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: MasteringHistoryEntity)

    @Query("DELETE FROM mastering_history WHERE id = :id")
    suspend fun deleteHistoryById(id: String)

    // Preset Queries
    @Query("SELECT * FROM mastering_presets")
    fun getAllPresetsFlow(): Flow<List<MasteringPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: MasteringPresetEntity)

    @Query("DELETE FROM mastering_presets WHERE id = :id")
    suspend fun deletePresetById(id: String)
}

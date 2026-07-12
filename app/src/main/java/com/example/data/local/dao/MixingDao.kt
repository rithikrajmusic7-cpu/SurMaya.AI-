package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.MixProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MixingDao {
    @Query("SELECT * FROM mix_projects ORDER BY updatedTimestamp DESC")
    fun getAllProjects(): Flow<List<MixProjectEntity>>

    @Query("SELECT * FROM mix_projects WHERE id = :id")
    suspend fun getProjectById(id: String): MixProjectEntity?

    @Query("SELECT * FROM mix_projects WHERE id = :id")
    fun getProjectByIdFlow(id: String): Flow<MixProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: MixProjectEntity)

    @Update
    suspend fun updateProject(project: MixProjectEntity)

    @Query("DELETE FROM mix_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}

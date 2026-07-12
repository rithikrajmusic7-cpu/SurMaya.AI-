package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.MelodyProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MelodyDao {
    @Query("SELECT * FROM melody_projects ORDER BY updatedTimestamp DESC")
    fun getAllProjects(): Flow<List<MelodyProjectEntity>>

    @Query("SELECT * FROM melody_projects WHERE id = :id")
    suspend fun getProjectById(id: String): MelodyProjectEntity?

    @Query("SELECT * FROM melody_projects WHERE id = :id")
    fun getProjectByIdFlow(id: String): Flow<MelodyProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: MelodyProjectEntity)

    @Update
    suspend fun updateProject(project: MelodyProjectEntity)

    @Query("DELETE FROM melody_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}

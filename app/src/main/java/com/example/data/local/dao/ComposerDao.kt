package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ComposerProjectEntity
import com.example.data.local.entity.CompositionVersionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComposerDao {
    @Query("SELECT * FROM composer_projects ORDER BY updatedTimestamp DESC")
    fun getAllProjects(): Flow<List<ComposerProjectEntity>>

    @Query("SELECT * FROM composer_projects WHERE id = :id")
    suspend fun getProjectById(id: String): ComposerProjectEntity?

    @Query("SELECT * FROM composer_projects WHERE id = :id")
    fun getProjectByIdFlow(id: String): Flow<ComposerProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ComposerProjectEntity)

    @Update
    suspend fun updateProject(project: ComposerProjectEntity)

    @Query("DELETE FROM composer_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)

    @Query("SELECT * FROM composition_versions WHERE projectId = :projectId ORDER BY versionNumber DESC")
    fun getVersionsForProject(projectId: String): Flow<List<CompositionVersionEntity>>

    @Query("SELECT * FROM composition_versions WHERE id = :id")
    suspend fun getVersionById(id: String): CompositionVersionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: CompositionVersionEntity)

    @Query("DELETE FROM composition_versions WHERE projectId = :projectId")
    suspend fun deleteVersionsForProject(projectId: String)

    @Query("DELETE FROM composition_versions WHERE id = :id")
    suspend fun deleteVersionById(id: String)
}

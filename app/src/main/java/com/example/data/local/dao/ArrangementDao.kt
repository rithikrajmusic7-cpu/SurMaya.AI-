package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArrangementDao {
    @Query("SELECT * FROM arrangement_projects ORDER BY updatedTimestamp DESC")
    fun getAllProjects(): Flow<List<ArrangementProjectEntity>>

    @Query("SELECT * FROM arrangement_projects WHERE id = :id")
    suspend fun getProjectById(id: String): ArrangementProjectEntity?

    @Query("SELECT * FROM arrangement_projects WHERE id = :id")
    fun getProjectByIdFlow(id: String): Flow<ArrangementProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ArrangementProjectEntity)

    @Update
    suspend fun updateProject(project: ArrangementProjectEntity)

    @Query("DELETE FROM arrangement_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)

    // Sections
    @Query("SELECT * FROM arrangement_sections WHERE projectId = :projectId ORDER BY sequenceIndex ASC")
    fun getSectionsForProject(projectId: String): Flow<List<ArrangementSectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<ArrangementSectionEntity>)

    @Query("DELETE FROM arrangement_sections WHERE projectId = :projectId")
    suspend fun deleteSectionsByProject(projectId: String)

    // Instrument Tracks
    @Query("SELECT * FROM instrument_tracks WHERE projectId = :projectId")
    fun getTracksForProject(projectId: String): Flow<List<InstrumentTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<InstrumentTrackEntity>)

    @Query("DELETE FROM instrument_tracks WHERE projectId = :projectId")
    suspend fun deleteTracksByProject(projectId: String)

    // Automation Lanes
    @Query("SELECT * FROM automation_lanes WHERE projectId = :projectId")
    fun getAutomationLanes(projectId: String): Flow<List<AutomationLaneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutomationLane(lane: AutomationLaneEntity)

    @Query("DELETE FROM automation_lanes WHERE projectId = :projectId")
    suspend fun deleteAutomationLanesByProject(projectId: String)

    // Transitions
    @Query("SELECT * FROM arrangement_transitions WHERE projectId = :projectId")
    fun getTransitions(projectId: String): Flow<List<ArrangementTransitionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransition(transition: ArrangementTransitionEntity)

    @Query("DELETE FROM arrangement_transitions WHERE projectId = :projectId")
    suspend fun deleteTransitionsByProject(projectId: String)

    // Counter Melodies
    @Query("SELECT * FROM counter_melodies WHERE projectId = :projectId")
    fun getCounterMelodies(projectId: String): Flow<List<CounterMelodyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounterMelody(melody: CounterMelodyEntity)

    @Query("DELETE FROM counter_melodies WHERE projectId = :projectId")
    suspend fun deleteCounterMelodiesByProject(projectId: String)

    // History
    @Query("SELECT * FROM arrangement_history WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getHistoryForProject(projectId: String): Flow<List<ArrangementHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ArrangementHistoryEntity)

    @Query("DELETE FROM arrangement_history WHERE projectId = :projectId")
    suspend fun deleteHistoryByProject(projectId: String)

    // Templates
    @Query("SELECT * FROM arrangement_templates")
    fun getAllTemplates(): Flow<List<ArrangementTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: ArrangementTemplateEntity)

    // Evaluations
    @Query("SELECT * FROM arrangement_evaluations WHERE projectId = :projectId LIMIT 1")
    fun getEvaluationForProject(projectId: String): Flow<ArrangementEvaluationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluation(evaluation: ArrangementEvaluationEntity)

    @Query("DELETE FROM arrangement_evaluations WHERE projectId = :projectId")
    suspend fun deleteEvaluationByProject(projectId: String)

    // Cache
    @Query("SELECT * FROM arrangement_cache WHERE promptHash = :hash LIMIT 1")
    suspend fun getCacheByHash(hash: String): ArrangementCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: ArrangementCacheEntity)
}

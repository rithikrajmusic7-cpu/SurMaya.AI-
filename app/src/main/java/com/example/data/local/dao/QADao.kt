package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.QADefectEntity
import com.example.data.local.entity.QAQualityReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QADao {
    @Query("SELECT * FROM qa_quality_reports ORDER BY timestamp DESC")
    fun getAllQualityReportsFlow(): Flow<List<QAQualityReportEntity>>

    @Query("SELECT * FROM qa_quality_reports WHERE songId = :songId LIMIT 1")
    fun getQualityReportByIdFlow(songId: String): Flow<QAQualityReportEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQualityReport(report: QAQualityReportEntity)

    @Query("DELETE FROM qa_quality_reports WHERE songId = :songId")
    suspend fun deleteQualityReportById(songId: String)

    @Query("DELETE FROM qa_quality_reports")
    suspend fun clearAllQualityReports()

    @Query("SELECT * FROM qa_defects ORDER BY timestamp DESC")
    fun getAllDefectsFlow(): Flow<List<QADefectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefect(defect: QADefectEntity)

    @Update
    suspend fun updateDefect(defect: QADefectEntity)

    @Query("DELETE FROM qa_defects WHERE id = :id")
    suspend fun deleteDefectById(id: String)

    @Query("DELETE FROM qa_defects")
    suspend fun clearAllDefects()
}

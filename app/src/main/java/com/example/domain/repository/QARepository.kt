package com.example.domain.repository

import com.example.domain.model.qa.QADefect
import com.example.domain.model.qa.QAQualityReport
import kotlinx.coroutines.flow.Flow

interface QARepository {
    fun getReports(): Flow<List<QAQualityReport>>
    fun getReportById(songId: String): Flow<QAQualityReport?>
    suspend fun insertReport(report: QAQualityReport)
    suspend fun deleteReportById(songId: String)
    
    fun getDefects(): Flow<List<QADefect>>
    suspend fun insertDefect(defect: QADefect)
    suspend fun updateDefect(defect: QADefect)
    suspend fun deleteDefectById(id: String)
    
    suspend fun clearAllReports()
    suspend fun clearAllDefects()

    // Optimization tracking
    fun isSongOptimized(songId: String): Boolean
    fun optimizeSong(songId: String)
    fun clearOptimizedSongs()
}

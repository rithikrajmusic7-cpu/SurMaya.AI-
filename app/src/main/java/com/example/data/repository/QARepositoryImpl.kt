package com.example.data.repository

import android.content.Context
import com.example.data.local.dao.QADao
import com.example.data.local.entity.QADefectEntity
import com.example.data.local.entity.QAQualityReportEntity
import com.example.domain.model.qa.QADefect
import com.example.domain.model.qa.QAQualityReport
import com.example.domain.repository.QARepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QARepositoryImpl(
    private val qaDao: QADao,
    private val context: Context
) : QARepository {

    override fun getReports(): Flow<List<QAQualityReport>> {
        return qaDao.getAllQualityReportsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getReportById(songId: String): Flow<QAQualityReport?> {
        return qaDao.getQualityReportByIdFlow(songId).map { it?.toDomain() }
    }

    override suspend fun insertReport(report: QAQualityReport) {
        qaDao.insertQualityReport(QAQualityReportEntity.fromDomain(report))
    }

    override suspend fun deleteReportById(songId: String) {
        qaDao.deleteQualityReportById(songId)
    }

    override fun getDefects(): Flow<List<QADefect>> {
        return qaDao.getAllDefectsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertDefect(defect: QADefect) {
        qaDao.insertDefect(QADefectEntity.fromDomain(defect))
    }

    override suspend fun updateDefect(defect: QADefect) {
        qaDao.updateDefect(QADefectEntity.fromDomain(defect))
    }

    override suspend fun deleteDefectById(id: String) {
        qaDao.deleteDefectById(id)
    }

    override suspend fun clearAllReports() {
        qaDao.clearAllQualityReports()
    }

    override suspend fun clearAllDefects() {
        qaDao.clearAllDefects()
    }

    // --- Optimization Tracking ---
    private val prefs by lazy {
        context.getSharedPreferences("surmaya_qa_prefs", Context.MODE_PRIVATE)
    }

    override fun isSongOptimized(songId: String): Boolean {
        val optimizedSet = prefs.getStringSet("optimized_songs", emptySet()) ?: emptySet()
        return optimizedSet.contains(songId)
    }

    override fun optimizeSong(songId: String) {
        val optimizedSet = prefs.getStringSet("optimized_songs", emptySet())?.toMutableSet() ?: mutableSetOf()
        optimizedSet.add(songId)
        prefs.edit().putStringSet("optimized_songs", optimizedSet).apply()
    }

    override fun clearOptimizedSongs() {
        prefs.edit().remove("optimized_songs").apply()
    }
}

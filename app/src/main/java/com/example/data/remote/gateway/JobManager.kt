package com.example.data.remote.gateway

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

// ==========================================
// Centralized Job Status Manager
// ==========================================

class JobManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: JobManager? = null

        fun getInstance(context: Context): JobManager {
            return INSTANCE ?: synchronized(this) {
                val instance = JobManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val _activeJobs = MutableStateFlow<Map<String, MusicJobResponse>>(emptyMap())
    val activeJobs: StateFlow<Map<String, MusicJobResponse>> = _activeJobs

    /**
     * Spawns a new track generation session state representation.
     */
    fun createTrackGenerationJob(providerName: String, lyricsPrompt: String): String {
        val jobId = "job_" + UUID.randomUUID().toString().take(12)
        val initialJob = MusicJobResponse(
            jobId = jobId,
            providerName = providerName,
            status = JobStatus.QUEUED,
            progress = 0.0f,
            estimatedSecondsRemaining = 120,
            audioUrl = null,
            lyrics = lyricsPrompt
        )
        updateJob(jobId, initialJob)
        Log.i("JobManager", "Asynchronous generation job registered: $jobId (Provider: $providerName)")
        return jobId
    }

    /**
     * Updates the status or progress percentage of a registered AI generation job.
     */
    fun updateJob(jobId: String, updatedState: MusicJobResponse) {
        val jobs = _activeJobs.value.toMutableMap()
        jobs[jobId] = updatedState
        _activeJobs.value = jobs
        Log.d("JobManager", "Job $jobId updated to status: ${updatedState.status} (Progress: ${updatedState.progress * 100}%)")
    }

    /**
     * Retrieves the status of a specific job.
     */
    fun getJob(jobId: String): MusicJobResponse? {
        return _activeJobs.value[jobId]
    }
}

package com.example.data.remote.gateway.voice

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VoiceJobQueue {

    private val _jobs = MutableStateFlow<Map<String, VoiceJob>>(emptyMap())
    val jobs: StateFlow<Map<String, VoiceJob>> = _jobs

    fun enqueueJob(job: VoiceJob) {
        val current = _jobs.value.toMutableMap()
        current[job.jobId] = job
        _jobs.value = current
        Log.i("VoiceJobQueue", "Enqueued voice job: ${job.jobId} Status: ${job.status}")
    }

    fun updateJobStatus(jobId: String, status: VoiceJobStatus, progress: Float, estSeconds: Int, modelId: String? = null, error: String? = null) {
        val current = _jobs.value.toMutableMap()
        val job = current[jobId] ?: return
        current[jobId] = job.copy(
            status = status,
            progress = progress,
            estimatedSecondsRemaining = estSeconds,
            targetVoiceModelId = modelId ?: job.targetVoiceModelId,
            errorDetails = error
        )
        _jobs.value = current
        Log.d("VoiceJobQueue", "Job $jobId updated to $status (Progress: ${progress * 100}%)")
    }

    fun getJob(jobId: String): VoiceJob? {
        return _jobs.value[jobId]
    }

    fun getAllJobs(): List<VoiceJob> {
        return _jobs.value.values.toList()
    }
}

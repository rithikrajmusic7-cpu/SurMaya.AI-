# SurMaya AI - Audio Generation Job Pipeline (v1.0)
## Document Information
* **Project**: SurMaya AI Music Operating System
* **Domain**: Distributed Jobs, Task Scheduling & Audio Pipeline
* **Version**: 1.0.0-draft
* **Author**: Chief Technology Officer & Backend Architect

---

## 1. Multi-Step State Machine
Every song generation requested in SurMaya is modeled as a transactional, state-preserving state machine. The state is serialized inside our Room database, allowing complex multi-modal generations to survive app crashes or operating system processes kills.

```
       +-------------------------------------------------------+
       |                        PENDING                        |
       +---------------------------+---------------------------+
                                   |
                                   v
       +-------------------------------------------------------+
       |                  LYRICS_GENERATING                    |
       +---------------------------+---------------------------+
                                   |
                                   v
       +-------------------------------------------------------+
       |                   LYRICS_GENERATED                    |
       +---------------------------+---------------------------+
                                   |
                                   v
       +-------------------------------------------------------+
       |                   AUDIO_GENERATING                    |
       +---------------------------+---------------------------+
                                   |
                                   v
       +-------------------------------------------------------+
       |                  VOCALS_SYNTHESIZING                  |
       +---------------------------+---------------------------+
                                   |
                                   v
       +-------------------------------------------------------+
       |                     VOCALS_MERGING                    |
       +---------------------------+---------------------------+
                                   |
                                   v
       +-------------------------------------------------------+
       |                     POST_PROCESSING                   |
       +---------------------------+---------------------------+
                                   |
                                   v
       +-------------------------------------------------------+
       |                        COMPLETED                      |
       +-------------------------------------------------------+
```

---

## 2. WorkManager Integration & Persistent Queue
For heavy multi-step downloads, synthesis loops, and post-processing, SurMaya relies on a custom `CoroutineWorker` scheduled with Jetpack WorkManager. This guarantees execution even if the user leaves the application.

### 2.1 Job Entity Representation (Room Database)
The local state database maps every active process using the following structured SQLite schema representation:

```kotlin
package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generation_jobs")
data class JobEntity(
    @PrimaryKey val jobId: String,
    val userId: String,
    val prompt: String,
    val genre: String,
    val status: String, // PENDING, LYRICS_GENERATING, AUDIO_GENERATING, COMPLETED, FAILED
    val progress: Float, // 0.0 to 1.0
    val lyricsId: String?,
    val audioUrl: String?,
    val localAudioPath: String?,
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)
```

### 2.2 WorkManager Enqueueing Logic
Jobs are enqueued using unique chain parameters to prevent parallel generation duplicates:

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()

val workRequest = OneTimeWorkRequestBuilder<MusicGenerationWorker>()
    .setConstraints(constraints)
    .setInputData(workDataOf("KEY_JOB_ID" to uniqueJobId))
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
    .build()

WorkManager.getInstance(context)
    .enqueueUniqueWork(
        "music_gen_$uniqueJobId",
        ExistingWorkPolicy.KEEP,
        workRequest
    )
```

---

## 3. Crash Recovery & Resilience Strategy
Mobile devices suffer from sudden lifecycle terminations. Our architecture guarantees crash recovery using transactional persistence:

1. **Transactional Database Commits**: Every state transition inside the Worker writes atomically to Room.
2. **Boot Recovery Receiver**: A `BroadcastReceiver` listening to `BOOT_COMPLETED` checks for interrupted jobs (status `PROCESSING`, `AUDIO_GENERATING`) and schedules recovery workers.
3. **Graceful Work Resumption**: When a worker boots, it inspects the last recorded step.
   * If `LYRICS_GENERATED` was completed but `AUDIO_GENERATING` crashed, the recovery engine **skips** the lyrics LLM invocation and restarts directly with the audio generation step, preserving API tokens.
4. **Exponential Network Back-off**: Retries are throttled exponentially (10s, 20s, 40s) up to a max retry ceiling of 3 before the job transitions to `FAILED`.

---

## 4. Polling & Streaming Updates for Jetpack Compose
To feed the UI with smooth, real-time feedback during generations, SurMaya bypasses raw one-shot HTTP calls in favor of transactional state streams.

### 4.1 Flow Propagation Pattern
The repository exposes a cold flow of the specific job's status directly from the database, ensuring single source of truth propagation:

```kotlin
package com.example.data.repository

import com.example.data.local.dao.JobDao
import com.example.data.local.entity.JobEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class JobPipelineRepository(private val jobDao: JobDao) {
    /**
     * Streams the real-time state changes of a specific generation job.
     */
    fun monitorJob(jobId: String): Flow<JobEntity?> {
        return jobDao.observeJobById(jobId).distinctUntilChanged()
    }
}
```

### 4.2 Compose Presentation Consumption
The screen view models collect from this Flow, emitting highly contextual states to the Compose Canvas:

```kotlin
// In ViewModel:
val activeJobState: StateFlow<JobUiState> = repository.monitorJob(currentJobId)
    .map { entity ->
        when (entity?.status) {
            "LYRICS_GENERATING" -> JobUiState.GeneratingLyrics(entity.progress)
            "AUDIO_GENERATING" -> JobUiState.GeneratingAudio(entity.progress)
            "COMPLETED" -> JobUiState.Success(entity.localAudioPath)
            "FAILED" -> JobUiState.Error(entity.errorMessage ?: "Unknown Error")
            else -> JobUiState.Idle
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JobUiState.Idle)
```

package com.example.data.remote.gateway

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

// ==========================================
// Production-Grade Download Pipeline Manager
// ==========================================

class DownloadManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: DownloadManager? = null

        fun getInstance(context: Context): DownloadManager {
            return INSTANCE ?: synchronized(this) {
                val instance = DownloadManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress

    /**
     * Downloads an audio or project asset file with status tracking and automatic retries.
     * Supports WAV, MP3, FLAC, ZIP, and custom stems.
     */
    suspend fun downloadAsset(
        urlStr: String,
        targetFileName: String,
        maxRetries: Int = 3
    ): Result<File> = withContext(Dispatchers.IO) {
        val downloadId = UUID.randomUUID().toString()
        var currentAttempt = 0
        var lastError: Exception? = null

        while (currentAttempt < maxRetries) {
            currentAttempt++
            try {
                Log.i("DownloadManager", "Initiating download (Attempt $currentAttempt/$maxRetries): $urlStr")
                val destinationFile = File(context.filesDir, targetFileName)
                
                // Establish secure URL Connection
                val url = URL(urlStr)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.requestMethod = "GET"

                connection.connect()
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw SurMayaException.DownloadFailure("Server returned bad status code: ${connection.responseCode}")
                }

                val fileLength = connection.contentLength
                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(destinationFile)

                val data = ByteArray(4096)
                var total: Long = 0
                var count: Int

                while (inputStream.read(data).also { count = it } != -1) {
                    total += count
                    if (fileLength > 0) {
                        val progressValue = total.toFloat() / fileLength
                        updateProgress(downloadId, progressValue)
                    }
                    outputStream.write(data, 0, count)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                connection.disconnect()

                updateProgress(downloadId, 1.0f)
                Log.i("DownloadManager", "Download successfully completed: ${destinationFile.absolutePath}")
                return@withContext Result.success(destinationFile)

            } catch (e: Exception) {
                lastError = e
                Log.e("DownloadManager", "Error downloading asset on attempt $currentAttempt", e)
                if (currentAttempt < maxRetries) {
                    delay(1500L * currentAttempt) // Back-off
                }
            }
        }

        Result.failure(SurMayaException.DownloadFailure(lastError?.message ?: "Unknown download failure"))
    }

    private fun updateProgress(downloadId: String, value: Float) {
        val currentMap = _downloadProgress.value.toMutableMap()
        currentMap[downloadId] = value
        _downloadProgress.value = currentMap
    }
}

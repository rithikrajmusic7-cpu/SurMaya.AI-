package com.example.data.remote.gateway.voice

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class VoiceRecordingModuleImpl(private val context: Context) : VoiceRecordingModule {

    private var mediaRecorder: MediaRecorder? = null
    private var isRecordingNow = false
    private var startTimeMillis: Long = 0
    private var currentOutputFile: File? = null

    override fun startRecording(outputFile: File): Result<Unit> {
        if (isRecordingNow) {
            return Result.failure(IllegalStateException("Already recording a vocal session."))
        }

        return try {
            currentOutputFile = outputFile
            
            // Modern MediaRecorder construction
            @Suppress("DEPRECATION")
            val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            isRecordingNow = true
            startTimeMillis = System.currentTimeMillis()
            Log.i("VoiceRecordingModule", "Started voice recording session: ${outputFile.absolutePath}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("VoiceRecordingModule", "Failed to prepare or start recording", e)
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecordingNow = false
            Result.failure(VoiceException.RecordingFailed(e.message ?: "Hardware prep error"))
        }
    }

    override fun stopRecording(): Result<Unit> {
        if (!isRecordingNow) {
            return Result.success(Unit)
        }

        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecordingNow = false
            Log.i("VoiceRecordingModule", "Voice recording session successfully stopped.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("VoiceRecordingModule", "Error stopping voice recording", e)
            mediaRecorder = null
            isRecordingNow = false
            Result.failure(VoiceException.RecordingFailed("Failed to cleanly flush audio stream."))
        }
    }

    override fun isRecording(): Boolean = isRecordingNow

    override fun getRecordingDurationSec(): Int {
        if (!isRecordingNow) return 0
        return ((System.currentTimeMillis() - startTimeMillis) / 1000).toInt()
    }

    override fun getInputLeveldB(): Float {
        if (!isRecordingNow) return -120f
        return try {
            val maxAmplitude = mediaRecorder?.maxAmplitude ?: 0
            if (maxAmplitude > 0) {
                val db = 20 * kotlin.math.log10(maxAmplitude.toDouble() / 32767.0)
                db.toFloat().coerceIn(-120f, 0f)
            } else {
                -120f
            }
        } catch (e: Exception) {
            -120f
        }
    }

    override fun trimAudio(inputFile: File, outputFile: File, startMs: Long, endMs: Long): Result<File> {
        return try {
            // Simplified audio trimmer for standard formats by trimming byte structures
            FileInputStream(inputFile).use { fis ->
                FileOutputStream(outputFile).use { fos ->
                    val fileLength = inputFile.length()
                    val sampleRate = 44100
                    val numChannels = 1
                    val bytesPerSecond = sampleRate * numChannels * 2 // 16-bit
                    
                    val startByteOffset = (startMs / 1000.0) * bytesPerSecond
                    val endByteOffset = (endMs / 1000.0) * bytesPerSecond
                    
                    val skipBytes = startByteOffset.toLong().coerceAtMost(fileLength)
                    val takeBytes = (endByteOffset - startByteOffset).toLong().coerceAtMost(fileLength - skipBytes)

                    fis.skip(skipBytes)
                    val buffer = ByteArray(4096)
                    var bytesWritten: Long = 0
                    var read: Int

                    while (fis.read(buffer).also { read = it } != -1) {
                        val remaining = takeBytes - bytesWritten
                        if (remaining <= 0) break
                        
                        val writeSize = if (read > remaining) remaining.toInt() else read
                        fos.write(buffer, 0, writeSize)
                        bytesWritten += writeSize
                    }
                }
            }
            Log.i("VoiceRecordingModule", "Completed audio trim process. Out: ${outputFile.absolutePath}")
            Result.success(outputFile)
        } catch (e: Exception) {
            Log.e("VoiceRecordingModule", "Failed trimming voice file", e)
            Result.failure(e)
        }
    }
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.gateway.voice.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VoiceEngineTest {

    private lateinit var testFolder: File
    private lateinit var sampleFile: File

    @Before
    fun setUp() {
        // Prepare sandboxed virtual local assets
        testFolder = File("build/test_voice_sandbox")
        testFolder.mkdirs()
        sampleFile = File(testFolder, "test_sample.wav")
        sampleFile.writeBytes(ByteArray(2048)) // Write synthetic bytes for wave structure
    }

    @Test
    fun testVoiceConsentManager() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val consentManager = VoiceConsentManagerImpl(context)
        val signer = "Test Singer"
        val consentText = "I confirm ownership of the vocal sample."
        val signature = "digitally_signed_by_test_singer"

        val result = consentManager.recordConsent(signer, consentText, signature)
        assertTrue(result.isSuccess)
        
        val consent = result.getOrThrow()
        assertEquals(signer, consent.signerName)
        assertEquals(consentText, consent.consentText)
        assertNotNull(consent.signatureHash)
    }

    @Test
    fun testAudioUploadValidator() {
        val validator = VoiceUploadModuleImpl()
        
        // 1. Test non-existent file handling
        val missingFile = File(testFolder, "missing.wav")
        val resultMissing = validator.validateAudioFile(missingFile)
        assertTrue(resultMissing.isFailure)

        // 2. Test extremely tiny / invalid clip rejection
        val tinyFile = File(testFolder, "tiny.wav")
        tinyFile.writeBytes(ByteArray(128))
        val resultTiny = validator.validateAudioFile(tinyFile)
        assertTrue(resultTiny.isSuccess)
        val report = resultTiny.getOrThrow()
        assertFalse(report.isAccepted)
        assertTrue(report.issuesDetected.any { it.contains("too small") || it.contains("short") })
    }

    @Test
    fun testVoiceVerificationEngine() = runTest {
        val verificationEngine = VoiceVerificationEngineImpl()
        
        val fileA = File(testFolder, "fingerprint_a.wav")
        val fileB = File(testFolder, "fingerprint_b.wav")
        
        fileA.writeBytes(ByteArray(1024) { 0x0A.toByte() })
        fileB.writeBytes(ByteArray(1024) { 0x0B.toByte() })

        val result = verificationEngine.verifyVoiceSignature(fileA, fileB)
        assertTrue(result.isSuccess)
        
        val match = result.getOrThrow()
        assertNotNull(match.status)
        assertTrue(match.similarityPercentage in 0f..100f)
        assertTrue(match.confidencePercentage in 0f..100f)
    }

    @Test
    fun testVoiceJobQueue() {
        val queue = VoiceJobQueue()
        
        val job = VoiceJob(
            jobId = "test_job_123",
            voiceName = "Lata Vocal Model",
            providerName = "elevenlabs_voice",
            status = VoiceJobStatus.QUEUED,
            progress = 0.0f,
            estimatedSecondsRemaining = 60,
            targetVoiceModelId = null
        )
        
        queue.enqueueJob(job)
        assertEquals(1, queue.getAllJobs().size)
        assertEquals(VoiceJobStatus.QUEUED, queue.getJob("test_job_123")?.status)

        queue.updateJobStatus("test_job_123", VoiceJobStatus.RUNNING, 0.5f, 30)
        val updated = queue.getJob("test_job_123")
        assertNotNull(updated)
        assertEquals(VoiceJobStatus.RUNNING, updated?.status)
        assertEquals(0.5f, updated?.progress ?: 0f)
        assertEquals(30, updated?.estimatedSecondsRemaining)
    }
}

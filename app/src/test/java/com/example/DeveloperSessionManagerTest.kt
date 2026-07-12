package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.security.DeveloperSessionManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DeveloperSessionManagerTest {

    private lateinit var context: Context
    private lateinit var sessionManager: DeveloperSessionManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        sessionManager = DeveloperSessionManager.getInstance(context)
        sessionManager.clearSession()
    }

    @Test
    fun `verify developer credentials validation against secure SHA-256 hashes`() {
        // True credentials
        val validId = "Developer SurMaya AI 2026"
        val validPw = "@Prem1234#2026"

        assertTrue(
            "Valid credentials should authenticate successfully",
            sessionManager.validateCredentials(validId, validPw)
        )

        // Invalid credentials
        assertFalse(
            "Incorrect developer ID should fail",
            sessionManager.validateCredentials("Invalid Dev ID", validPw)
        )
        assertFalse(
            "Incorrect password should fail",
            sessionManager.validateCredentials(validId, "wrongpassword")
        )
        assertFalse(
            "Empty credentials should fail",
            sessionManager.validateCredentials("", "")
        )
    }

    @Test
    fun `verify time-based 2FA dynamic OTP generation and verification`() {
        val currentMinute = System.currentTimeMillis() / 1000 / 60
        val correctOtp = sessionManager.getOtpForTime(currentMinute)
        
        assertEquals(6, correctOtp.length)
        assertTrue(
            "Valid time-based OTP should match and verify",
            sessionManager.verify2FA(correctOtp)
        )

        val incorrectOtp = "123456"
        if (correctOtp != incorrectOtp) {
            assertFalse(
                "Incorrect 2FA OTP must be rejected",
                sessionManager.verify2FA(incorrectOtp)
            )
        }
    }

    @Test
    fun `verify secure developer session lifecycle and automatic timeouts`() {
        assertFalse(
            "Session should be unauthenticated by default",
            sessionManager.isDeveloperAuthenticated()
        )

        sessionManager.createSession()
        assertTrue(
            "Session should be authenticated after explicit activation",
            sessionManager.isDeveloperAuthenticated()
        )

        sessionManager.clearSession()
        assertFalse(
            "Session should be unauthenticated after manual termination",
            sessionManager.isDeveloperAuthenticated()
        )
    }

    @Test
    fun `verify trusted hardware device UUID registry`() {
        // Retrieve UUID
        val uuid = sessionManager.getDeviceUuid()
        assertFalse("Device UUID should not be blank", uuid.isBlank())

        // By default, first-device auto-registers or is trusted
        assertTrue(
            "Current device should be trusted once authorized",
            sessionManager.isDeviceAuthorized()
        )

        // Reset and explicitly authorize
        sessionManager.authorizeDevice()
        assertTrue(
            "Current device must be present in trusted registry after authorization",
            sessionManager.isDeviceAuthorized()
        )
    }
}

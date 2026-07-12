package com.example.core.security

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest
import java.util.UUID

class DeveloperSessionManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("surmaya_secure_dev_session", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: DeveloperSessionManager? = null

        fun getInstance(context: Context): DeveloperSessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DeveloperSessionManager(context).also { INSTANCE = it }
            }
        }

        // SHA-256 of expected developer identifier
        private const val EXPECTED_DEV_ID_HASH = "839a1f25c630728d4166bff0c15ee3b4c61f73d9cf63181e0b8263a3fcc9f2ff"
        
        // SHA-256 of expected developer passcode
        private const val EXPECTED_DEV_PW_HASH = "e77139e15b7e298864fbbf95af97b1dbfd19fe781572ee1e5c95be4ef10a91f7"

        private const val SESSION_TIMEOUT_MS = 15 * 60 * 1000 // 15 minutes session timeout
    }

    /**
     * Helper to compute SHA-256 of a string
     */
    private fun sha256(input: String): String {
        return try {
            val bytes = input.toByteArray(Charsets.UTF_8)
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(bytes)
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Checks if developer mode is authenticated and within valid session lifetime
     */
    fun isDeveloperAuthenticated(): Boolean {
        val isAuthenticated = prefs.getBoolean("dev_auth_active", false)
        if (!isAuthenticated) return false

        val lastActivity = prefs.getLong("dev_last_activity", 0L)
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastActivity > SESSION_TIMEOUT_MS) {
            // Session expired
            clearSession()
            return false
        }

        // Update last activity to extend session
        prefs.edit().putLong("dev_last_activity", currentTime).apply()
        return true
    }

    /**
     * Authenticates the Developer credentials against cryptographic hashes
     */
    fun validateCredentials(developerId: String, password: String): Boolean {
        val inputIdHash = sha256(developerId.trim())
        val inputPwHash = sha256(password)

        return inputIdHash == EXPECTED_DEV_ID_HASH && inputPwHash == EXPECTED_DEV_PW_HASH
    }

    /**
     * Verifies the 2FA dynamic OTP
     * We support a time-based verification code. 
     * Formula: Generates a 6-digit TOTP code based on the current minute, so it changes every minute.
     * Developers can use the pre-shared algorithm or see the challenge on screen in debug environments.
     */
    fun verify2FA(otp: String): Boolean {
        val cleanedOtp = otp.trim()
        if (cleanedOtp.length != 6) return false

        val currentMinute = System.currentTimeMillis() / 1000 / 60
        val expectedOtp1 = getOtpForTime(currentMinute)
        val expectedOtp2 = getOtpForTime(currentMinute - 1) // Allow 1 minute clock drift grace period

        return cleanedOtp == expectedOtp1 || cleanedOtp == expectedOtp2
    }

    /**
     * Helper to derive a 6-digit OTP from a minute timestamp
     */
    fun getOtpForTime(minute: Long): String {
        val seed = minute * 41662 + 9973
        val code = (seed % 900000) + 100000
        return code.toString()
    }

    /**
     * Generates or retrieves a unique Hardware ID / UUID for trusted device verification
     */
    fun getDeviceUuid(): String {
        var uuid = prefs.getString("device_uuid", "") ?: ""
        if (uuid.isEmpty()) {
            uuid = try {
                Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID) ?: UUID.randomUUID().toString()
            } catch (e: Exception) {
                UUID.randomUUID().toString()
            }
            prefs.edit().putString("device_uuid", uuid).apply()
        }
        return uuid
    }

    /**
     * Checks if the current hardware device is in the authorized registry list
     */
    fun isDeviceAuthorized(): Boolean {
        val uuid = getDeviceUuid()
        val authorizedDevices = prefs.getStringSet("authorized_devices_registry", emptySet()) ?: emptySet()
        // Default to true if the list is empty (e.g. first device registers automatically) or if explicit authorized
        return authorizedDevices.isEmpty() || authorizedDevices.contains(uuid)
    }

    /**
     * Authorizes the current device in the registry
     */
    fun authorizeDevice() {
        val uuid = getDeviceUuid()
        val authorizedDevices = prefs.getStringSet("authorized_devices_registry", emptySet())?.toMutableSet() ?: mutableSetOf()
        authorizedDevices.add(uuid)
        prefs.edit().putStringSet("authorized_devices_registry", authorizedDevices).apply()
    }

    /**
     * Activates the authenticated developer session
     */
    fun createSession() {
        prefs.edit()
            .putBoolean("dev_auth_active", true)
            .putLong("dev_last_activity", System.currentTimeMillis())
            .apply()
    }

    /**
     * Clears session and logs out Developer
     */
    fun clearSession() {
        prefs.edit()
            .putBoolean("dev_auth_active", false)
            .putLong("dev_last_activity", 0L)
            .apply()
    }
}

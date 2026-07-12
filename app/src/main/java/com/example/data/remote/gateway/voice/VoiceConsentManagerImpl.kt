package com.example.data.remote.gateway.voice

import android.content.Context
import android.util.Log
import java.security.MessageDigest
import java.util.UUID

class VoiceConsentManagerImpl(private val context: Context) : VoiceConsentManager {

    // Simple persistent storage for recorded consents
    private val sharedPrefs = context.getSharedPreferences("surmaya_voice_consents", Context.MODE_PRIVATE)

    override suspend fun recordConsent(
        signerName: String,
        consentText: String,
        digitalSignature: String
    ): Result<VoiceConsent> {
        if (signerName.isBlank() || digitalSignature.isBlank()) {
            return Result.failure(VoiceException.ConsentMissing())
        }

        return try {
            val uniqueId = "consent_" + UUID.randomUUID().toString().take(8)
            val rawString = "$signerName|$consentText|$digitalSignature|${System.currentTimeMillis()}"
            val signatureHash = sha256(rawString)

            val consent = VoiceConsent(
                id = uniqueId,
                signerName = signerName,
                consentText = consentText,
                signatureHash = signatureHash
            )

            // Persist locally
            sharedPrefs.edit()
                .putString("consent_hash_${signerName.lowercase().trim()}", signatureHash)
                .putLong("consent_time_${signerName.lowercase().trim()}", consent.timestamp)
                .apply()

            Log.i("VoiceConsentManager", "Cryptographic user ownership consent recorded: Signer=$signerName, ID=$uniqueId")
            Result.success(consent)
        } catch (e: Exception) {
            Log.e("VoiceConsentManager", "Failed to compile digital consent log", e)
            Result.failure(e)
        }
    }

    override suspend fun verifyConsent(signerName: String): Boolean {
        val key = "consent_hash_${signerName.lowercase().trim()}"
        val hasConsent = sharedPrefs.contains(key)
        Log.d("VoiceConsentManager", "Consent check for signer '$signerName': Active=$hasConsent")
        return hasConsent
    }

    private fun sha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            input.hashCode().toString()
        }
    }
}

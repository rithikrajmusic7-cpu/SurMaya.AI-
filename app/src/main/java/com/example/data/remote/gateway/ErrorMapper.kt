package com.example.data.remote.gateway

import java.io.IOException

// ==========================================
// Centralized Error Codes & Mappings
// ==========================================

sealed class SurMayaException(message: String, val code: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkFailure(cause: Throwable) : SurMayaException("Network is unreachable or failed.", "NET_ERR_01", cause)
    class AuthenticationFailure(message: String) : SurMayaException(message, "AUTH_ERR_02")
    class InvalidApiKey : SurMayaException("The configured API key is invalid or unauthorized.", "API_KEY_ERR_03")
    class RateLimitExceeded : SurMayaException("Rate limit exceeded. Please slow down requests.", "RATE_LIMIT_ERR_04")
    class RequestTimeout : SurMayaException("Request timed out.", "TIMEOUT_ERR_05")
    class ProviderOffline(provider: String) : SurMayaException("AI Provider '$provider' is offline or unavailable.", "PROVIDER_OFFLINE_ERR_06")
    class ServerError(provider: String, details: String) : SurMayaException("Downstream server error on '$provider': $details", "SERVER_ERR_07")
    class ParsingError(cause: Throwable) : SurMayaException("Failed to parse provider response model.", "PARSE_ERR_08", cause)
    class DownloadFailure(details: String) : SurMayaException("Failed to download or resume audio file: $details", "DOWNLOAD_ERR_09")
    class StorageFailure(details: String) : SurMayaException("Local file storage full or permission denied: $details", "STORAGE_ERR_10")
}

object ErrorMapper {
    /**
     * Maps raw exceptions to common, structured SurMaya exceptions.
     */
    fun mapException(throwable: Throwable, providerName: String = "Generic"): SurMayaException {
        return when (throwable) {
            is SurMayaException -> throwable
            is IOException -> SurMayaException.NetworkFailure(throwable)
            is java.net.SocketTimeoutException -> SurMayaException.RequestTimeout()
            is retrofit2.HttpException -> {
                when (throwable.code()) {
                    401 -> SurMayaException.AuthenticationFailure("API Access Unauthorized on $providerName. Please verify keys.")
                    403 -> SurMayaException.InvalidApiKey()
                    429 -> SurMayaException.RateLimitExceeded()
                    in 500..599 -> SurMayaException.ServerError(providerName, throwable.message() ?: "Internal Server Error")
                    else -> SurMayaException.ServerError(providerName, "HTTP Error Code: ${throwable.code()}")
                }
            }
            else -> SurMayaException.ParsingError(throwable)
        }
    }
}

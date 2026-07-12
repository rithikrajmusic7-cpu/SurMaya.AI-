package com.example.data.remote

import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okio.Buffer
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

data class ApiTransaction(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val method: String,
    val url: String,
    val requestBody: String?,
    val responseCode: Int,
    val responseMessage: String,
    val responseHeaders: Map<String, String>,
    val responseBodyString: String?,
    val isSuccess: Boolean,
    val verificationStatus: String
)

object GeminiDiagnostics {
    private val _logs = MutableStateFlow<List<ApiTransaction>>(emptyList())
    val logs: StateFlow<List<ApiTransaction>> = _logs.asStateFlow()

    fun addLog(transaction: ApiTransaction) {
        synchronized(this) {
            val currentList = _logs.value.toMutableList()
            currentList.add(0, transaction) // Newest first
            if (currentList.size > 50) {
                currentList.removeAt(currentList.lastIndex)
            }
            _logs.value = currentList
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}

class GeminiDiagnosticInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val context = com.example.di.ServiceLocator.applicationContext
        if (context != null) {
            val credentialManager = com.example.data.local.ApiCredentialManager.getInstance(context)
            val devPrefs = com.example.data.local.DeveloperPrefsManager.getInstance(context)
            
            // 1. Check custom endpoint from secure manager first, then dev preferences
            val customEndpoint = credentialManager.apiEndpoint.ifBlank { devPrefs.customApiEndpoint }
            if (customEndpoint.isNotBlank()) {
                try {
                    val customUrl = customEndpoint.toHttpUrlOrNull()
                    if (customUrl != null) {
                        val newUrl = request.url.newBuilder()
                            .scheme(customUrl.scheme)
                            .host(customUrl.host)
                            .port(customUrl.port)
                            .build()
                        request = request.newBuilder().url(newUrl).build()
                    }
                } catch (e: Exception) {
                    // Fallback to original
                }
            }

            // 2. Resolve target API key from secure manager, fallback to dev preferences, then BuildConfig
            val targetKey = credentialManager.geminiApiKey.ifBlank {
                if (devPrefs.isDeveloperModeEnabled) devPrefs.customGeminiApiKey else ""
            }.ifBlank {
                com.example.BuildConfig.GEMINI_API_KEY
            }

            // 3. Inject/Override the "key" query parameter if it exists or is required for Gemini services
            val currentKeyQuery = request.url.queryParameter("key")
            if (currentKeyQuery != null && targetKey.isNotBlank() && targetKey != "MY_GEMINI_API_KEY") {
                val newUrl = request.url.newBuilder()
                    .setQueryParameter("key", targetKey)
                    .build()
                request = request.newBuilder().url(newUrl).build()
            }
        }
        val method = request.method
        val url = request.url.toString()

        // Read request body safely
        var requestBodyStr: String? = null
        try {
            request.body?.let { body ->
                val buffer = Buffer()
                body.writeTo(buffer)
                requestBodyStr = buffer.readUtf8()
            }
        } catch (e: Exception) {
            requestBodyStr = "[Error reading request body: ${e.message}]"
        }

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: IOException) {
            // Log networking exception
            val failedTransaction = ApiTransaction(
                method = method,
                url = url,
                requestBody = requestBodyStr,
                responseCode = -1,
                responseMessage = "Connection Failed",
                responseHeaders = emptyMap(),
                responseBodyString = null,
                isSuccess = false,
                verificationStatus = "Network Error: ${e.message}"
            )
            GeminiDiagnostics.addLog(failedTransaction)
            throw e
        }

        val code = response.code
        val message = response.message
        val headersMap = mutableMapOf<String, String>()
        val headers: Headers = response.headers
        for (i in 0 until headers.size) {
            headersMap[headers.name(i)] = headers.value(i)
        }

        // Read response body safely without consuming the stream
        var responseBodyStr: String? = null
        var verification = "Unknown"
        var isSuccessStatus = response.isSuccessful

        try {
            val responseBody = response.peekBody(2 * 1024 * 1024) // Peek up to 2MB
            responseBodyStr = responseBody.string()

            if (isSuccessStatus) {
                verification = verifyResponseBody(responseBodyStr, url)
            } else {
                verification = parseErrorDetails(responseBodyStr)
                isSuccessStatus = false
            }
        } catch (e: Exception) {
            verification = "Error inspecting response: ${e.message}"
            isSuccessStatus = false
        }

        val transaction = ApiTransaction(
            method = method,
            url = url,
            requestBody = requestBodyStr,
            responseCode = code,
            responseMessage = message,
            responseHeaders = headersMap,
            responseBodyString = responseBodyStr,
            isSuccess = isSuccessStatus,
            verificationStatus = verification
        )
        GeminiDiagnostics.addLog(transaction)

        return response
    }

    private fun verifyResponseBody(body: String?, url: String): String {
        if (body.isNullOrBlank()) {
            return "Empty response body"
        }

        // Check if this is a file stream or simple file download (e.g., streaming audio URL)
        if (url.contains(".mp3") || url.contains(".wav") || url.contains("audio")) {
            return "Valid audio streaming response (Direct URL stream)"
        }

        try {
            val json = JSONObject(body)
            
            // Check if there are candidates
            if (json.has("candidates")) {
                val candidates = json.getJSONArray("candidates")
                if (candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val part = parts.getJSONObject(0)
                        
                        // Check if it's text
                        val text = part.optString("text", "")
                        
                        // Check if it's inlineData (such as audio base64 data)
                        val inlineData = part.optJSONObject("inlineData")
                        if (inlineData != null) {
                            val mimeType = inlineData.optString("mimeType", "")
                            val dataStr = inlineData.optString("data", "")
                            if (dataStr.isNotEmpty()) {
                                val bytesCount = try {
                                    Base64.decode(dataStr, Base64.DEFAULT).size
                                } catch (e: Exception) {
                                    -1
                                }
                                return if (bytesCount > 0) {
                                    "Success: Valid Base64 Audio Data ($mimeType, Size: ${bytesCount / 1024} KB)"
                                } else {
                                    "Error: Found inlineData block but Base64 decoding failed"
                                }
                            } else {
                                return "Error: Found inlineData block but 'data' attribute is empty"
                            }
                        }
                        
                        if (text.isNotEmpty()) {
                            return "Success: Standard Text Response (Length: ${text.length} chars). Preview: '${text.take(60)}...'"
                        }
                    }
                }
                return "Error: Empty or malformed candidates array in Gemini response"
            }
            
            // Check if there is an explicit error object returned inside success HTTP response code
            if (json.has("error")) {
                return parseErrorDetails(body)
            }
            
        } catch (e: Exception) {
            return "Raw response size: ${body.length} characters (Non-JSON or parse error: ${e.message})"
        }

        return "Response parsed successfully (No explicit text or inline audio detected)"
    }

    private fun parseErrorDetails(body: String?): String {
        if (body.isNullOrBlank()) {
            return "HTTP Error (No detail body returned)"
        }
        try {
            val json = JSONObject(body)
            if (json.has("error")) {
                val errorObj = json.getJSONObject("error")
                val code = errorObj.optInt("code", -1)
                val msg = errorObj.optString("message", "Unknown error")
                val status = errorObj.optString("status", "")
                return "API Error $code ($status): $msg"
            }
        } catch (e: Exception) {
            // Ignore JSON parse exception and return simple excerpt
        }
        return "HTTP Error: ${body.take(200)}"
    }
}

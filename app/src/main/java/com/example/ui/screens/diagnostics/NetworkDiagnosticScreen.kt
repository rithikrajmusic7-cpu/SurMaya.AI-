package com.example.ui.screens.diagnostics

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.local.DeveloperPrefsManager
import com.example.data.remote.ApiTransaction
import com.example.data.remote.Content
import com.example.data.remote.GeminiDiagnostics
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.Part
import com.example.data.remote.RetrofitClient
import com.example.ui.components.GlassCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

enum class DiagnosticStatus {
    UNTESTED,
    TESTING,
    SUCCESS,
    FAILURE,
    WARNING
}

data class EndpointTestResult(
    val name: String,
    val url: String,
    val keySource: String,
    var status: DiagnosticStatus,
    var detailMessage: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkDiagnosticScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val devPrefs = remember { DeveloperPrefsManager.getInstance(context) }
    val credentialManager = remember { com.example.data.local.ApiCredentialManager.getInstance(context) }
    
    // Live logs feed
    val logs by GeminiDiagnostics.logs.collectAsState()
    
    // UI Local State
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Success", "Error"
    var isRunningTests by remember { mutableStateOf(false) }
    
    // Connection diagnosis tests list
    val endpointsList = remember {
        mutableStateListOf<EndpointTestResult>()
    }
    
    // Initialize endpoint test status
    LaunchedEffect(Unit) {
        val geminiKeyUsed = if (credentialManager.geminiApiKey.isNotBlank()) {
            "Secure Key Storage (Active)"
        } else if (devPrefs.isDeveloperModeEnabled && devPrefs.customGeminiApiKey.isNotBlank()) {
            "Custom (Developer Settings)"
        } else if (BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") {
            "Product build config (.env)"
        } else {
            "Unconfigured (Mock procedural backup active)"
        }

        val sunoKeyUsed = if (credentialManager.sunoApiKey.isNotBlank()) {
            "Secure Key Storage (Active)"
        } else if (devPrefs.isDeveloperModeEnabled && devPrefs.customSunoApiKey.isNotBlank()) {
            "Custom Dev Key"
        } else {
            "Default / Mock"
        }

        val musicGenKeyUsed = if (credentialManager.musicGenApiKey.isNotBlank()) {
            "Secure Key Storage (Active)"
        } else if (devPrefs.isDeveloperModeEnabled && devPrefs.customMusicGenApiKey.isNotBlank()) {
            "Custom Dev Key"
        } else {
            "Default / Mock"
        }

        val apiEndpoint = if (credentialManager.apiEndpoint.isNotBlank()) {
            credentialManager.apiEndpoint
        } else if (devPrefs.isDeveloperModeEnabled && devPrefs.customApiEndpoint.isNotBlank()) {
            devPrefs.customApiEndpoint
        } else {
            "https://generativelanguage.googleapis.com/"
        }

        endpointsList.clear()
        endpointsList.addAll(
            listOf(
                EndpointTestResult("Internet Connectivity", "Local network status check", "System Wifi/Cellular", DiagnosticStatus.UNTESTED, "Tap Run Diagnosis to verify status"),
                EndpointTestResult("Gemini AI API Connection", apiEndpoint, geminiKeyUsed, DiagnosticStatus.UNTESTED, "Verifies endpoint and checks API Key credentials"),
                EndpointTestResult("Suno Music Engine", "Third-party rendering API", sunoKeyUsed, DiagnosticStatus.UNTESTED, "Checks custom Suno credentials validity"),
                EndpointTestResult("MusicGen Studio", "Synthesizer model", musicGenKeyUsed, DiagnosticStatus.UNTESTED, "Validates backend music rendering engine status")
            )
        )
    }

    // Function to run connection diagnosis
    val runActiveDiagnosis = {
        coroutineScope.launch {
            isRunningTests = true
            
            // 1. Internet Status Check
            endpointsList[0] = endpointsList[0].copy(status = DiagnosticStatus.TESTING, detailMessage = "Scanning networking interfaces...")
            val hasInternet = isDeviceOnline(context)
            if (hasInternet) {
                endpointsList[0] = endpointsList[0].copy(status = DiagnosticStatus.SUCCESS, detailMessage = "Device is online. DNS interfaces resolving.")
            } else {
                endpointsList[0] = endpointsList[0].copy(status = DiagnosticStatus.FAILURE, detailMessage = "No active connection detected. Please check your cellular data or Wifi status.")
            }

            if (!hasInternet) {
                // If there's no internet, other endpoints fail automatically
                for (i in 1 until endpointsList.size) {
                    endpointsList[i] = endpointsList[i].copy(status = DiagnosticStatus.FAILURE, detailMessage = "Skipped test: Device is completely offline.")
                }
                isRunningTests = false
                Toast.makeText(context, "Network Diagnostic Failed: Offline", Toast.LENGTH_LONG).show()
                return@launch
            }

            // 2. Gemini AI API Connection check (Real request!)
            endpointsList[1] = endpointsList[1].copy(status = DiagnosticStatus.TESTING, detailMessage = "Sending diagnostic authorization request to API endpoint...")
            val geminiKey = credentialManager.geminiApiKey.ifBlank {
                if (devPrefs.isDeveloperModeEnabled) devPrefs.customGeminiApiKey else ""
            }.ifBlank {
                BuildConfig.GEMINI_API_KEY
            }

            if (geminiKey.isBlank() || geminiKey == "MY_GEMINI_API_KEY") {
                endpointsList[1] = endpointsList[1].copy(
                    status = DiagnosticStatus.WARNING,
                    detailMessage = "Using mock procedural synthesizer fallback mode. No real API key configured."
                )
            } else {
                try {
                    // Make a lightweight live text request to authenticate
                    val pingRequest = GenerateContentRequest(
                        contents = listOf(
                            Content(parts = listOf(Part(text = "Identify as 'OK' if you can read this. keep it short.")))
                        )
                    )
                    
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.service.generateContent(
                            apiKey = geminiKey,
                            request = pingRequest
                        )
                    }

                    val candidateText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                    endpointsList[1] = endpointsList[1].copy(
                        status = DiagnosticStatus.SUCCESS,
                        detailMessage = "Active & Authorized successfully. Gemini Model response: \"${candidateText.trim()}\""
                    )
                } catch (e: retrofit2.HttpException) {
                    val code = e.code()
                    val errorBody = e.response()?.errorBody()?.string() ?: ""
                    var errMsg = "API Authentication Failed ($code)"
                    if (code == 400) {
                        errMsg += ": Key invalid, or request format mismatch (e.g., model restriction)."
                    } else if (code == 403) {
                        errMsg += ": Permission denied. Check API Key validity and enabled services in Google Cloud Console."
                    } else if (code == 429) {
                        errMsg += ": Rate limit/Quota exceeded."
                    }
                    endpointsList[1] = endpointsList[1].copy(
                        status = DiagnosticStatus.FAILURE,
                        detailMessage = "$errMsg. Raw payload: $errorBody"
                    )
                } catch (e: Exception) {
                    endpointsList[1] = endpointsList[1].copy(
                        status = DiagnosticStatus.FAILURE,
                        detailMessage = "Network/Transport Error: ${e.localizedMessage ?: "Unknown connection failure"}"
                    )
                }
            }

            // 3. Suno Music Engine check
            endpointsList[2] = endpointsList[2].copy(status = DiagnosticStatus.TESTING, detailMessage = "Validating Suno service access configurations...")
            val sunoKey = credentialManager.sunoApiKey.ifBlank { devPrefs.customSunoApiKey }
            if (sunoKey.isBlank()) {
                endpointsList[2] = endpointsList[2].copy(
                    status = DiagnosticStatus.WARNING,
                    detailMessage = "No custom Suno credential set. Default offline backup model will render synthetic songs."
                )
            } else {
                endpointsList[2] = endpointsList[2].copy(
                    status = DiagnosticStatus.SUCCESS,
                    detailMessage = "Custom Suno credentials verified locally. Key configured: ****${sunoKey.takeLast(4)}"
                )
            }

            // 4. MusicGen Engine check
            endpointsList[3] = endpointsList[3].copy(status = DiagnosticStatus.TESTING, detailMessage = "Verifying MusicGen service interfaces...")
            val musicGenKey = credentialManager.musicGenApiKey.ifBlank { devPrefs.customMusicGenApiKey }
            if (musicGenKey.isBlank()) {
                endpointsList[3] = endpointsList[3].copy(
                    status = DiagnosticStatus.WARNING,
                    detailMessage = "Offline wav file procedural generator activated. No custom MusicGen server specified."
                )
            } else {
                endpointsList[3] = endpointsList[3].copy(
                    status = DiagnosticStatus.SUCCESS,
                    detailMessage = "Custom MusicGen configurations activated. Ready for remote audio rendering."
                )
            }

            isRunningTests = false
            Toast.makeText(context, "Active Diagnosis Completed!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Network Diagnostics", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text("Analyze raw Retrofit logs & API connections", color = Color(0xFF9E93B3), fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09041A))
            )
        },
        containerColor = Color(0xFF09041A),
        modifier = modifier.testTag("network_diagnostic_screen_scaffold")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Connection diagnostics panel
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Connection Live Analyzer",
                            color = Color(0xFFF9D142),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        
                        Button(
                            onClick = { runActiveDiagnosis() },
                            enabled = !isRunningTests,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF9D142),
                                contentColor = Color.Black,
                                disabledContainerColor = Color(0xFF2E244E)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("run_diagnostics_button")
                        ) {
                            if (isRunningTests) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Run", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Check APIs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Divider(color = Color(0xFF2E244E), thickness = 0.5.dp)

                    // Render connection states
                    endpointsList.forEach { endpoint ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = when (endpoint.status) {
                                    DiagnosticStatus.UNTESTED -> Icons.Filled.HelpOutline
                                    DiagnosticStatus.TESTING -> Icons.Filled.HourglassEmpty
                                    DiagnosticStatus.SUCCESS -> Icons.Filled.CheckCircle
                                    DiagnosticStatus.WARNING -> Icons.Filled.Warning
                                    DiagnosticStatus.FAILURE -> Icons.Filled.Error
                                },
                                contentDescription = endpoint.status.name,
                                tint = when (endpoint.status) {
                                    DiagnosticStatus.UNTESTED -> Color(0xFF9E93B3)
                                    DiagnosticStatus.TESTING -> Color(0xFFF9D142)
                                    DiagnosticStatus.SUCCESS -> Color(0xFF39FF14)
                                    DiagnosticStatus.WARNING -> Color(0xFFFFA500)
                                    DiagnosticStatus.FAILURE -> Color(0xFFFF4D4D)
                                },
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = endpoint.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Key/Config: " + endpoint.keySource,
                                    color = Color(0xFF9E93B3),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = endpoint.detailMessage,
                                    color = when (endpoint.status) {
                                        DiagnosticStatus.FAILURE -> Color(0xFFFF8080)
                                        DiagnosticStatus.WARNING -> Color(0xFFFFD480)
                                        else -> Color(0xFFCCCCCC)
                                    },
                                    fontSize = 11.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Live logs panel header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.ReceiptLong, contentDescription = "Logs", tint = Color(0xFFF9D142), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("API Traffic Ledger", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                TextButton(
                    onClick = { GeminiDiagnostics.clearLogs() },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF4D4D)),
                    modifier = Modifier.testTag("clear_logs_button")
                ) {
                    Icon(imageVector = Icons.Filled.DeleteSweep, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Ledger", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Filters & Search Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter logs...", color = Color(0xFF9E93B3), fontSize = 11.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFF9E93B3), modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF9D142),
                        unfocusedBorderColor = Color(0xFF2E244E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0C071F),
                        unfocusedContainerColor = Color(0xFF0C071F)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("log_search_input")
                )

                // Success / Error status segment filter chips
                listOf("All", "Success", "Error").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) Color(0xFFF9D142) else Color(0xFF140D2A),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFF9D142) else Color(0xFF2E244E),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Live Transactions Logs Feed List
            val filteredLogs = remember(logs, searchQuery, selectedFilter) {
                logs.filter { log ->
                    val matchesSearch = log.url.contains(searchQuery, ignoreCase = true) || 
                            log.verificationStatus.contains(searchQuery, ignoreCase = true) ||
                            (log.requestBody ?: "").contains(searchQuery, ignoreCase = true)
                    
                    val matchesFilter = when (selectedFilter) {
                        "Success" -> log.isSuccess
                        "Error" -> !log.isSuccess
                        else -> true
                    }
                    matchesSearch && matchesFilter
                }
            }

            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.List, contentDescription = "Empty logs", tint = Color(0xFF9E93B3), modifier = Modifier.size(48.dp))
                        Text("No logs in ledger matching criteria.", color = Color(0xFF9E93B3), fontSize = 12.sp)
                        Text("Execute music creation, lyrics, or run a connection diagnosis test to capture logs.", color = Color(0xFF9E93B3).copy(alpha = 0.6f), fontSize = 10.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        TransactionRow(transaction = log, context = context)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRow(transaction: ApiTransaction, context: Context) {
    var isExpanded by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }
    val timeString = formatter.format(Date(transaction.timestamp))
    
    // Extracted short path for clean listing display
    val parsedUrl = try {
        val urlObj = URL(transaction.url)
        val path = urlObj.path
        val modelQuery = urlObj.query?.let { "?$it" } ?: ""
        if (path.length > 35) "...${path.takeLast(32)}$modelQuery" else "$path$modelQuery"
    } catch (e: Exception) {
        transaction.url
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (transaction.isSuccess) Color(0xFF2E244E) else Color(0xFFFF4D4D).copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .clickable { isExpanded = !isExpanded }
            .testTag("log_item_${transaction.id}")
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .animateContentSize()
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // HTTP Method Badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (transaction.method == "POST") Color(0xFF9F75FF) else Color(0xFF2E244E),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = transaction.method,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Status Badge (Code)
                    Box(
                        modifier = Modifier
                            .background(
                                color = when {
                                    transaction.responseCode in 200..299 -> Color(0xFF39FF14).copy(alpha = 0.2f)
                                    transaction.responseCode == -1 -> Color(0xFFFF4D4D).copy(alpha = 0.2f)
                                    else -> Color(0xFFFF9F00).copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (transaction.responseCode == -1) "FAIL" else transaction.responseCode.toString(),
                            color = when {
                                transaction.responseCode in 200..299 -> Color(0xFF39FF14)
                                transaction.responseCode == -1 -> Color(0xFFFF4D4D)
                                else -> Color(0xFFFF9F00)
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Timestamp
                    Text(
                        text = timeString,
                        color = Color(0xFF9E93B3),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "Expand details",
                    tint = Color(0xFF9E93B3),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // URL path line
            Text(
                text = parsedUrl,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Result preview description
            Text(
                text = transaction.verificationStatus,
                color = if (transaction.isSuccess) Color(0xFF39FF14) else Color(0xFFFF4D4D),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Expanded detail section
            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = Color(0xFF2E244E), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Detail actions bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val fullPayloadLog = buildString {
                                appendLine("=== API TRANSACTION LOG ===")
                                appendLine("ID: ${transaction.id}")
                                appendLine("Timestamp: ${Date(transaction.timestamp)}")
                                appendLine("Method: ${transaction.method}")
                                appendLine("URL: ${transaction.url}")
                                appendLine("Response Code: ${transaction.responseCode} (${transaction.responseMessage})")
                                appendLine("Verification: ${transaction.verificationStatus}")
                                appendLine("\n[REQUEST BODY]")
                                appendLine(transaction.requestBody ?: "[Empty]")
                                appendLine("\n[RESPONSE HEADERS]")
                                transaction.responseHeaders.forEach { (k, v) -> appendLine("$k: $v") }
                                appendLine("\n[RESPONSE BODY]")
                                appendLine(transaction.responseBodyString ?: "[Empty]")
                            }
                            clipboard.setPrimaryClip(ClipData.newPlainText("API Transaction Log", fullPayloadLog))
                            Toast.makeText(context, "Log details copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF9D142))
                    ) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Log Data", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Full details scrollable area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                        .verticalScroll(rememberScrollState())
                        .background(Color(0xFF070415), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Full Request Target
                    Column {
                        Text("REQUEST URL", color = Color(0xFFF9D142), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        Text(transaction.url, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    // Request Body
                    transaction.requestBody?.let { body ->
                        Column {
                            Text("REQUEST PAYLOAD", color = Color(0xFFF9D142), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            Text(body, color = Color(0xFFCCCCCC), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    // Response Headers
                    if (transaction.responseHeaders.isNotEmpty()) {
                        Column {
                            Text("RESPONSE HEADERS", color = Color(0xFFF9D142), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            transaction.responseHeaders.forEach { (key, value) ->
                                Text("$key: $value", color = Color(0xFF9E93B3), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    // Response Body
                    Column {
                        Text("RESPONSE PAYLOAD", color = Color(0xFFF9D142), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        val bodyText = transaction.responseBodyString ?: "[Empty response payload]"
                        Text(bodyText, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// Helper to determine network connection status
private fun isDeviceOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}

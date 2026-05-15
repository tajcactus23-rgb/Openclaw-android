package com.openhands.android.presentation.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RuntimeDiagnostics(
    val networkStatus: String,
    val deviceInfo: String,
    val apiTests: List<ApiTestResult>,
    val connectionLogs: List<LogEntry>,
    val timestamp: String
)

data class ApiTestResult(val name: String, val success: Boolean, val message: String)
data class LogEntry(val timestamp: String, val level: String, val message: String)

// SECTION 12: RUNTIME MONITOR - Full diagnostics with API test
@Composable
fun RuntimeMonitorScreen() {
    val context = LocalContext.current
    var diagnostics by remember { mutableStateOf<RuntimeDiagnostics?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var apiUrl by remember { mutableStateOf("https://app.all-hands.dev") }

    fun gatherDiagnostics() {
        isLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            // Network
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connMgr.activeNetwork
            val caps = network?.let { connMgr.getNetworkCapabilities(it) }
            val networkStatus = when {
                caps == null -> "No network"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Connected"
            }

            // Device
            val deviceInfo = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}), ${Build.MANUFACTURER} ${Build.MODEL}"

            // API tests
            val apiTests = mutableListOf<ApiTestResult>()
            try {
                val url = URL("$apiUrl/api/v1/user")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                // Will fail without auth - that's expected
                apiTests.add(ApiTestResult("GET /api/v1/user", conn.responseCode in 200..299, "Code: ${conn.responseCode}"))
                conn.disconnect()
            } catch (e: Exception) {
                apiTests.add(ApiTestResult("GET /api/v1/user", false, "Error: ${e.message}"))
            }

            // Test automation endpoint
            try {
                val url = URL("$apiUrl/api/automation/v1")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                apiTests.add(ApiTestResult("GET /api/automation/v1", conn.responseCode in 200..299, "Code: ${conn.responseCode}"))
                conn.disconnect()
            } catch (e: Exception) {
                apiTests.add(ApiTestResult("GET /api/automation/v1", false, "Error: ${e.message}"))
            }

            // Connection logs (simulated - would need real session)
            val connectionLogs = mutableListOf<LogEntry>()
            connectionLogs.add(LogEntry(SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()), "INFO", "Starting diagnostics"))
            connectionLogs.add(LogEntry(SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()), "DEBUG", "Network: $networkStatus"))
            connectionLogs.add(LogEntry(SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()), "INFO", "API test completed"))

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            diagnostics = RuntimeDiagnostics(networkStatus, deviceInfo, apiTests, connectionLogs, timestamp)
            isLoading = false
        }
    }

    fun exportDiagnostics(): String = buildString {
        appendLine("# OpenHands Runtime Diagnostics")
        appendLine("Generated: ${diagnostics?.timestamp}")
        appendLine()
        appendLine("## Network")
        appendLine(diagnostics?.networkStatus ?: "N/A")
        appendLine()
        appendLine("## Device")
        appendLine(diagnostics?.deviceInfo ?: "N/A")
        appendLine()
        appendLine("## API Tests")
        diagnostics?.apiTests?.forEach { test ->
            appendLine("- ${test.name}: ${if (test.success) "✅" else "❌"} ${test.message}")
        }
        appendLine()
        appendLine("## Logs")
        diagnostics?.connectionLogs?.forEach { log ->
            appendLine("[${log.timestamp}] ${log.level}: ${log.message}")
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Runtime Monitor", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(value = apiUrl, onValueChange = { apiUrl = it },
            label = { Text("API URL") }, modifier = Modifier.fillMaxWidth())

        Button(onClick = { gatherDiagnostics() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.NetworkCheck, null)
            Text(if (isLoading) " Gathering..." else " Run Diagnostics")
        }

        diagnostics?.let { diag ->
            Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Network", style = MaterialTheme.typography.titleMedium)
                    Text(diag.networkStatus, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Device", style = MaterialTheme.typography.titleMedium)
                    Text(diag.deviceInfo, style = MaterialTheme.typography.bodySmall)
                }
            }

            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("API Tests", style = MaterialTheme.typography.titleMedium)
                    diag.apiTests.forEach { test ->
                        Row(Modifier.fillMaxWidth()) {
                            Text(if (test.success) "✅" else "❌", Modifier.padding(end = 4.dp))
                            Text("${test.name}: ${test.message}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Connection Logs", style = MaterialTheme.typography.titleMedium)
                    diag.connectionLogs.take(10).forEach { log ->
                        Text("[${log.timestamp}] ${log.level}: ${log.message}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Sandbox/Commands - ADAPTER_REQUIRED
        Text("OpenHands Sandbox", style = MaterialTheme.typography.titleMedium)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Sandbox Status", style = MaterialTheme.typography.bodyMedium)
                Text("ADAPTER_REQUIRED - Start session from Dashboard", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Command History", style = MaterialTheme.typography.bodyMedium)
                Text("ADAPTER_REQUIRED - Requires active session", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("File Actions", style = MaterialTheme.typography.bodyMedium)
                Text("ADAPTER_REQUIRED - Requires active session", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
            }
        }

        Button(onClick = { /* Export */ }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            enabled = diagnostics != null) {
            Icon(Icons.Default.Save, null)
            Text("Export Diagnostics")
        }

        Spacer(Modifier.height(16.dp))
    }
}
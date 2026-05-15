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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Diagnostics(
    val apiResult: String,
    val networkStatus: String,
    val deviceInfo: String,
    val appLogs: String,
    val timestamp: String
)

@Composable
fun RuntimeMonitorScreen() {
    val context = LocalContext.current
    var diagnostics by remember { mutableStateOf<Diagnostics?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun gatherDiagnostics() {
        isLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            // Network status
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

            // Device info
            val deviceInfo = buildString {
                appendLine("Android: ${Build.VERSION.RELEASE}")
                appendLine("SDK: ${Build.VERSION.SDK_INT}")
                appendLine("Model: ${Build.MANUFACTURER} ${Build.MODEL}")
            }

            // App logs (last few lines from logcat)
            var appLogs = ""
            try {
                val p = Runtime.getRuntime().exec("logcat -d -t 20 *:W", null, null)
                val lines = BufferedReader(InputStreamReader(p.inputStream)).readLines()
                    .filter { it.contains("openhands") || it.contains("OpenHands") }
                appLogs = lines.take(10).joinToString("\n")
            } catch (e: Exception) {
                appLogs = "Log access limited"
            }

            // API test result
            val apiResult = "Add profile in Settings to test API"

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

            diagnostics = Diagnostics(apiResult, networkStatus, deviceInfo, appLogs, timestamp)
            isLoading = false
        }
    }

    fun exportDiagnostics(): String = buildString {
        appendLine("# OpenHands Android Diagnostics")
        appendLine()
        appendLine("## Timestamp")
        appendLine(diagnostics?.timestamp ?: "N/A")
        appendLine()
        appendLine("## Network")
        appendLine(diagnostics?.networkStatus ?: "N/A")
        appendLine()
        appendLine("## Device")
        appendLine(diagnostics?.deviceInfo ?: "N/A")
        appendLine()
        appendLine("## API")
        appendLine(diagnostics?.apiResult ?: "N/A")
        appendLine()
        appendLine("## Recent Logs")
        appendLine(diagnostics?.appLogs ?: "N/A")
        appendLine()
        appendLine("## Runtime")
        appendLine("Status: ADAPTER REQUIRED - OpenHands connection via Settings")
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text("Runtime Monitor", style = MaterialTheme.typography.headlineMedium)

        Button(
            onClick = { gatherDiagnostics() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.NetworkCheck, null)
            Text(if (isLoading) " Gathering..." else " Gather Diagnostics")
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
                    Text("API", style = MaterialTheme.typography.titleMedium)
                    Text(diag.apiResult, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (diag.appLogs.isNotBlank()) {
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Recent Logs", style = MaterialTheme.typography.titleMedium)
                        Text(diag.appLogs.take(500), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Text("OpenHands Runtime", style = MaterialTheme.typography.titleMedium)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Sandbox Status", style = MaterialTheme.typography.bodyMedium)
                Text("ADAPTER REQUIRED - Start session from Dashboard", 
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Command History", style = MaterialTheme.typography.bodyMedium)
                Text("ADAPTER REQUIRED - Requires active session", 
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("File Actions", style = MaterialTheme.typography.bodyMedium)
                Text("ADAPTER REQUIRED - Requires active session", 
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
            }
        }

        Button(
            onClick = { /* Share - needs intent */ },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Icon(Icons.Default.Share, null)
            Text("Export Diagnostics")
        }

        Text("Export: ADAPTER REQUIRED", 
            style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))

        Spacer(Modifier.padding(16.dp))
    }
}
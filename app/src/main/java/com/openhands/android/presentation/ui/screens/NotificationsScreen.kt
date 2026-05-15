@file:SuppressLint("MissingPermission", "NotificationPermission")

package com.openhands.android.presentation.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.annotation.SuppressLint
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

@Composable
fun NotificationsScreen() {
    val context = LocalContext.current
    var status by remember { mutableStateOf("Initialize...") }
    var hasPermission by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Create channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel("task_completion", "Task Completion", NotificationManager.IMPORTANCE_HIGH).apply { description = "Task completion" })
            nm.createNotificationChannel(NotificationChannel("task_failure", "Task Failure", NotificationManager.IMPORTANCE_HIGH).apply { description = "Task failure" })
            nm.createNotificationChannel(NotificationChannel("connection_status", "Connection Status", NotificationManager.IMPORTANCE_DEFAULT).apply { description = "Connection status" })
            status = "Channels created"
        }
        
        // Check permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            status = if (hasPermission) "Ready" else "Permission needed"
        }
    }

    fun tryNotify() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
            status = "Grant POST_NOTIFICATIONS in device Settings"
            return
        }
        try {
            val b = NotificationCompat.Builder(context, "task_completion")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Test")
                .setContentText("OpenHands test")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
            NotificationManagerCompat.from(context).notify(1, b.build())
            status = "Notification sent"
        } catch (e: SecurityException) {
            status = "Permission denied"
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Notifications", style = MaterialTheme.typography.headlineMedium)
        Text(status, style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Channels:", style = MaterialTheme.typography.titleMedium)
                Text("task_completion", style = MaterialTheme.typography.bodySmall)
                Text("task_failure", style = MaterialTheme.typography.bodySmall)
                Text("connection_status", style = MaterialTheme.typography.bodySmall)
            }
        }

        Button(onClick = { tryNotify() }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text("Test Notification")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Android 13+ Permission:", style = MaterialTheme.typography.titleMedium)
                    Text(if (hasPermission) "✅ Granted" else "❌ Required", style = MaterialTheme.typography.bodyMedium)
                    Text("Grant in Settings > Notifications > Allow", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("WorkManager:", style = MaterialTheme.typography.titleMedium)
                Text("ADAPTER_REQUIRED - backend needed", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
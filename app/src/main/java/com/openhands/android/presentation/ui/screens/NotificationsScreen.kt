package com.openhands.android.presentation.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

// SECTION 8: NOTIFICATIONS + WORKMANAGER
@Composable
fun NotificationsScreen() {
    val context = LocalContext.current
    var notificationStatus by remember { mutableStateOf("Channels not created") }
    
    // Create notification channels on first composition
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            
            // Task completion channel
            nm.createNotificationChannel(
                NotificationChannel("task_completion", "Task Completion",
                    NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Notifications when tasks complete"
                }
            )
            // Task failure channel  
            nm.createNotificationChannel(
                NotificationChannel("task_failure", "Task Failure",
                    NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Notifications when tasks fail"
                }
            )
            // Connection status channel
            nm.createNotificationChannel(
                NotificationChannel("connection_status", "Connection Status",
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Connection status changes"
                }
            )
            notificationStatus = "3 channels created"
        } else {
            notificationStatus = "API < 26 - no channels needed"
        }
    }

    // Test notification
    fun showTestNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                notificationStatus = "Permission required"
                return
            }
        }
        
        val builder = NotificationCompat.Builder(context, "task_completion")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Test Notification")
            .setContentText("OpenHands test")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        
        try {
            NotificationManagerCompat.from(context).notify(1, builder.build())
            notificationStatus = "Test notification shown"
        } catch (e: SecurityException) {
            notificationStatus = "Permission denied"
        }
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Notifications", style = MaterialTheme.typography.headlineMedium)

        Text("Channel Status: $notificationStatus", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))

        Text("Notification Channels", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Column(Modifier.padding(8.dp)) {
                Text("• task_completion (HIGH)", Modifier.padding(2.dp))
                Text("• task_failure (HIGH)", Modifier.padding(2.dp))
                Text("• connection_status (DEFAULT)", Modifier.padding(2.dp))
            }
        }

        Button(
            onClick = { showTestNotification() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Test Notification")
        }

        Text("WorkManager", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Column(Modifier.padding(8.dp)) {
                Text("Periodic sync with OpenHands Cloud", Modifier.padding(2.dp))
                Text("Status: ADAPTER REQUIRED", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

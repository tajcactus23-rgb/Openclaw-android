package com.openhands.android.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openhands.android.domain.model.CapabilityState

// SECTION 4: MOBILE DASHBOARD
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sessions by viewModel.activeSessions.collectAsState()
    val tasks by viewModel.recentTasks.collectAsState()
    val logs by viewModel.recentLogs.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dashboard", style = MaterialTheme.typography.headlineLarge)
                IconButton(onClick = { viewModel.refreshDashboard() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }
        
        // Runtime Status
        item { RuntimeStatusCard(uiState.runtimeStatus) }
        
        // Agent & Model
        item { AgentModelCard(uiState.currentAgent, uiState.currentModel) }
        
        // Active Sessions
        item { Text("Active Sessions", style = MaterialTheme.typography.titleMedium) }
        if (uiState.sessionsCapability == CapabilityState.ADAPTER_REQUIRED) {
            item { CapabilityCard("Sessions", "Requires adapter - no public API") }
        } else if (uiState.sessionsCapability == CapabilityState.DISCONNECTED) {
            item { CapabilityCard("Sessions", "Connect to view sessions") }
        } else if (sessions.isEmpty()) {
            item { CapabilityCard("Sessions", "No active sessions") }
        } else {
            sessions.forEach { session -> item { SessionCard(session) } }
        }
        
        // Recent Tasks
        item { 
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Recent Tasks", style = MaterialTheme.typography.titleMedium) 
        }
        if (uiState.tasksCapability == CapabilityState.ADAPTER_REQUIRED) {
            item { CapabilityCard("Tasks", "Requires adapter - no public API") }
        } else if (uiState.tasksCapability == CapabilityState.DISCONNECTED) {
            item { CapabilityCard("Tasks", "Connect to view tasks") }
        } else if (tasks.isEmpty()) {
            item { CapabilityCard("Tasks", "No recent tasks") }
        } else {
            tasks.forEach { task -> item { TaskCard(task) } }
        }
        
        // Logs
        item { 
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Recent Logs", style = MaterialTheme.typography.titleMedium) 
        }
        if (uiState.logsCapability == CapabilityState.ADAPTER_REQUIRED) {
            item { CapabilityCard("Logs", "Requires adapter - no public API") }
        } else if (uiState.logsCapability == CapabilityState.DISCONNECTED) {
            item { CapabilityCard("Logs", "Connect to view logs") }
        } else if (logs.isEmpty()) {
            item { CapabilityCard("Logs", "No logs available") }
        } else {
            logs.take(10).forEach { log -> item { LogCard(log) } }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable private fun RuntimeStatusCard(status: com.openhands.android.domain.model.RuntimeStatus) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
        containerColor = when {
            status.isConnected -> Color(0xFF1B5E20).copy(alpha = 0.15f)
            status.isRunning -> Color(0xFF1565C0).copy(alpha = 0.15f)
            else -> Color(0xFFB71C1C).copy(alpha = 0.15f)
        }
    )) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (status.isConnected) Icons.Default.Cloud else Icons.Default.CloudOff, null,
                    tint = if (status.isConnected) Color(0xFF4CAF50) else Color(0xFFF44336))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(when { status.isConnected -> "Connected"; status.isRunning -> "Running"; else -> "Disconnected" },
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    status.serverUrl?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                status.activeSandboxes?.let { Text("$it sandboxes", style = MaterialTheme.typography.titleSmall) }
                status.uptime?.let { Text("Uptime: $it", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable private fun AgentModelCard(agentName: String?, model: String?) {
    Card(Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Cloud, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Agent", style = MaterialTheme.typography.labelMedium)
                    Text(agentName ?: "Not connected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ModelTraining, null)
                Spacer(Modifier.width(4.dp))
                Text(model ?: "N/A", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable private fun SessionCard(session: com.openhands.android.domain.model.AgentSession) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
        containerColor = when (session.status) {
            "active" -> MaterialTheme.colorScheme.primaryContainer
            "running" -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    )) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(session.name, style = MaterialTheme.typography.titleSmall)
                session.workspace?.let { Text("Workspace: $it", style = MaterialTheme.typography.bodySmall) }
                Text("Started: ${session.startedAt}", style = MaterialTheme.typography.labelSmall)
            }
            Surface(color = when (session.status) { "active" -> Color(0xFF4CAF50) "running" -> Color(0xFF2196F3) else -> Color(0xFF9E9E9E) }.copy(alpha = 0.2f)) {
                Text(session.status.uppercase(), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable private fun TaskCard(task: com.openhands.android.domain.model.TaskSummary) {
    Card(Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(when (task.status) { "completed" -> Icons.Default.History "running" -> Icons.Default.Terminal else -> Icons.Default.Monitor }, null, Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(task.title, style = MaterialTheme.typography.bodyMedium)
                    Text(task.description, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
            }
            Text(task.status, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable private fun LogCard(log: com.openhands.android.domain.model.LogEntry) {
    Surface(Modifier.fillMaxWidth(), color = when (log.level) { "ERROR" -> Color(0xFFF44336).copy(alpha = 0.1f) "WARN" -> Color(0xFFFF9800).copy(alpha = 0.1f) "INFO" -> Color(0xFF2196F3).copy(alpha = 0.1f) else -> Color.Transparent }) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("[${log.timestamp}]", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(log.message, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
            Text(log.level, style = MaterialTheme.typography.labelSmall, color = when (log.level) { "ERROR" -> Color(0xFFF44336) "WARN" -> Color(0xFFFF9800) else -> MaterialTheme.colorScheme.outline })
        }
    }
}

@Composable private fun EmptyCard(message: String) {
    Card(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable private fun CapabilityCard(feature: String, message: String) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0).copy(alpha = 0.15f))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(feature, style = MaterialTheme.typography.titleMedium)
            Text(message, style = MaterialTheme.typography.bodySmall)
            Text("ADAPTER REQUIRED", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1565C0))
        }
    }
}
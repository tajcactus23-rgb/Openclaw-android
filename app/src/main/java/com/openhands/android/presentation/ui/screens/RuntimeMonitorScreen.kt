package com.openhands.android.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openhands.android.data.remote.QueueStatusResponse
import com.openhands.android.data.remote.RuntimeEvent
import com.openhands.android.data.remote.RuntimeSessionResponse
import com.openhands.android.data.remote.StreamState
import com.openhands.android.presentation.viewmodel.RuntimeMonitorViewModel

/**
 * RuntimeMonitorScreen with SSE streaming.
 * 
 * Shows:
 * - Stream connection state
 * - Connection quality indicator
 * - Live queue changes via SSE
 * - Live session events via SSE
 * - Fallback to polling when stream unavailable
 */
@Composable
fun RuntimeMonitorScreen(
    viewModel: RuntimeMonitorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with connection status
        ConnectionHeader(
            streamState = state.streamState,
            connectionQuality = state.connectionQuality,
            onRefresh = { viewModel.refresh() },
            isLoading = state.isLoading
        )
        
        // Error banner
        state.error?.let { error ->
            ErrorBanner(error = error, onDismiss = { viewModel.clearError() })
        }
        
        // Sessions section
        SessionsSection(
            sessions = state.sessions,
            onCreateSession = { viewModel.createSession(it) },
            onEndSession = { viewModel.endSession(it) }
        )
        
        // Queue section
        QueueSection(
            queueStatus = state.queueStatus,
            onQueueExecution = { viewModel.queueExecution(it) }
        )
        
        // Events section (last 10)
        EventsSection(events = state.events.takeLast(10))
        
        Spacer(Modifier.height(16.dp))
    }
}

/**
 * Connection header with quality indicator.
 */
@Composable
private fun ConnectionHeader(
    streamState: StreamState,
    connectionQuality: Int,
    onRefresh: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stream state icon
            Icon(
                imageVector = when (streamState) {
                    StreamState.CONNECTED -> Icons.Default.Cloud
                    StreamState.CONNECTING, StreamState.RECONNECTING -> Icons.Default.Cloud
                    StreamState.DISCONNECTED, StreamState.ERROR -> Icons.Default.CloudOff
                },
                contentDescription = "Stream state",
                tint = when (streamState) {
                    StreamState.CONNECTED -> Color(0xFF4CAF50)
                    StreamState.CONNECTING, StreamState.RECONNECTING -> Color(0xFFFF9800)
                    StreamState.DISCONNECTED, StreamState.ERROR -> Color(0xFFF44336)
                },
                modifier = Modifier.size(32.dp)
            )
            
            // Quality indicator
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = when (streamState) {
                        StreamState.CONNECTED -> "Live connected"
                        StreamState.CONNECTING -> "Connecting..."
                        StreamState.RECONNECTING -> "Reconnecting..."
                        StreamState.DISCONNECTED -> "Disconnected"
                        StreamState.ERROR -> "Connection error"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                if (streamState == StreamState.CONNECTED) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { connectionQuality / 100f },
                            modifier = Modifier.weight(1f).height(4.dp),
                            color = Color(0xFF4CAF50),
                        )
                        Text(
                            text = "$connectionQuality%",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            
            // Refresh button
            IconButton(onClick = onRefresh, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Refresh, "Refresh")
                }
            }
        }
    }
}

/**
 * Error banner.
 */
@Composable
private fun ErrorBanner(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFF44336),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            Button(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

/**
 * Sessions section.
 */
@Composable
private fun SessionsSection(
    sessions: List<RuntimeSessionResponse>,
    onCreateSession: (String) -> Unit,
    onEndSession: (String) -> Unit
) {
    Text(
        text = "Sessions",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    if (sessions.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No active sessions", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "ADAPTER_REQUIRED - Terminal requires OpenHands Cloud adapter",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1565C0)
                )
            }
        }
    } else {
        LazyColumn(modifier = Modifier.height(150.dp)) {
            items(sessions) { session ->
                SessionCard(
                    session = session,
                    onEnd = { onEndSession(session.id) }
                )
            }
        }
    }
    
    // Quick actions
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Button(
            onClick = { onCreateSession("terminal") },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Layers, null, modifier = Modifier.size(18.dp))
            Text(" Terminal", modifier = Modifier.padding(start = 4.dp))
        }
        Spacer(Modifier.size(8.dp))
        Button(
            onClick = { onCreateSession("browser") },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Layers, null, modifier = Modifier.size(18.dp))
            Text(" Browser", modifier = Modifier.padding(start = 4.dp))
        }
    }
}

/**
 * Session card.
 */
@Composable
private fun SessionCard(
    session: RuntimeSessionResponse,
    onEnd: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(session.id.take(8) + "...", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Type: ${session.runtime_type}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Status: ${session.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (session.status) {
                        "running" -> Color(0xFF4CAF50)
                        "terminated" -> Color(0xFFF44336)
                        else -> Color(0xFFFF9800)
                    }
                )
            }
            Button(onClick = onEnd) {
                Text("End")
            }
        }
    }
}

/**
 * Queue section.
 */
@Composable
private fun QueueSection(
    queueStatus: QueueStatusResponse?,
    onQueueExecution: (String) -> Unit
) {
    Text(
        text = "Execution Queue",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            val queued = queueStatus?.queued ?: 0
            Text(
                "Queued: $queued",
                style = MaterialTheme.typography.bodyMedium
            )
            queueStatus?.let { queue ->
                if (queue.items.isNotEmpty()) {
                    Text("Items:", style = MaterialTheme.typography.bodySmall)
                    queue.items.forEach { item ->
                        Row {
                            Text("- ${item.execution_id.take(8)}: ", style = MaterialTheme.typography.bodySmall)
                            Text("${item.workflow_id}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
    
    Button(
        onClick = { onQueueExecution("workflow") },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Icon(Icons.Default.QueuePlayNext, null)
        Text(" Queue Execution")
    }
}

/**
 * Events section (last 10).
 */
@Composable
private fun EventsSection(events: List<RuntimeEvent>) {
    Text(
        text = "Live Events",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            if (events.isEmpty()) {
                Text(
                    "No events yet - connect to see live updates",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                events.forEach { event ->
                    val (text, color) = when (event) {
                        is RuntimeEvent.SessionStart -> "Session started: ${event.sessionId}" to Color(0xFF4CAF50)
                        is RuntimeEvent.SessionEnd -> "Session ended: ${event.sessionId}" to Color(0xFFF44336)
                        is RuntimeEvent.QueueChange -> "Queue: ${event.queued}" to Color(0xFF2196F3)
                        is RuntimeEvent.ExecutionLog -> "Log: ${event.message}" to Color(0xFF9E9E9E)
                        is RuntimeEvent.ExecutionComplete -> "Complete: ${event.executionId}" to Color(0xFF4CAF50)
                        is RuntimeEvent.Error -> "Error: ${event.message}" to Color(0xFFF44336)
                        is RuntimeEvent.KeepAlive -> "Keepalive" to Color(0xFF9E9E9E)
                        is RuntimeEvent.Disconnected -> "Disconnected" to Color(0xFFF44336)
                        is RuntimeEvent.Connecting -> "Connecting" to Color(0xFFFF9800)
                        is RuntimeEvent.Reconnecting -> "Reconnecting" to Color(0xFFFF9800)
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }
            }
        }
    }
}
package com.openhands.android.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// SECTION 4 cont: Sessions
@Composable
fun SessionsScreen() {
    val sessions = remember { emptyList<Map<String, String>>() }
    
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Column {
                Text("Active Sessions", style = MaterialTheme.typography.headlineMedium)
                Text("PLACEHOLDER - API not available", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF9800))
            }
        }
        item { Text("Manage your OpenHands agent sessions", style = MaterialTheme.typography.bodySmall) }
        
        if (sessions.isEmpty()) {
            item { 
                Card(Modifier.fillMaxWidth()) { 
                    Text("No active sessions - ADAPTER REQUIRED", Modifier.padding(16.dp)) 
                } 
            }
        } else {
            sessions.forEach { session ->
                item { 
                    Card(Modifier.fillMaxWidth()) { 
                        Column(Modifier.padding(16.dp)) {
                            Text(session["name"] ?: "", style = MaterialTheme.typography.titleMedium)
                            Text("Status: ${session["status"]}", style = MaterialTheme.typography.bodySmall)
                        }
                    } 
                }
            }
        }
    }
}
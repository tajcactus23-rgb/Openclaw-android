package com.openhands.android.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// SECTION 4 cont: Sessions
@Composable
fun SessionsScreen() {
    val sessions = remember { listOf(
        mapOf("id" to "1", "name" to "Active Session", "status" to "running"),
        mapOf("id" to "2", "name" to "Completed", "status" to "completed")
    ) }
    
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Active Sessions", style = MaterialTheme.typography.headlineMedium) }
        item { Text("Manage your OpenHands agent sessions", style = MaterialTheme.typography.bodySmall) }
        
        sessions.forEach { session ->
            item { Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(session["name"] ?: "", style = MaterialTheme.typography.titleMedium)
                        Text("Status: ${session["status"]}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } }
        }
    }
}

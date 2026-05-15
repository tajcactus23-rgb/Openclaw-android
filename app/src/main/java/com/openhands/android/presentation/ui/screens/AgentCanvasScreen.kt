package com.openhands.android.presentation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp

// SECTION 10: AGENT CANVAS
@Composable
fun AgentCanvasScreen() {
    var workflow by remember { mutableStateOf("") }
    
    Column(
        Modifier.fillMaxSize().padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Agent Canvas", style = MaterialTheme.typography.headlineMedium)
        
        Text("Workflow Builder", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = workflow,
            onValueChange = { workflow = it },
            label = { Text("Workflow JSON...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 8
        )
        
        Text("Node Types", style = MaterialTheme.typography.titleMedium)
        Text("Prompt | File | Skill | Tool | Model | Output", Modifier.padding(4.dp))
        
        Button(
            onClick = { /* run */ },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            enabled = false
        ) {
            Text("Run Workflow")
        }
        
        Text("ADAPTER REQUIRED - Workflow execution needs OpenHands project adapter", 
            style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
        
        Spacer(Modifier.height(80.dp))
    }
}
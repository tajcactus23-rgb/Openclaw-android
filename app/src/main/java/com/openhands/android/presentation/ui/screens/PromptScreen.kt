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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// SECTION 5: PROMPT BUILDER
@Composable
fun PromptScreen() {
    var prompt by remember { mutableStateOf("") }
    var savedPrompts by remember { mutableStateOf(listOf<String>()) }
    
    Column(
        Modifier.fillMaxSize().padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Prompt Builder", style = MaterialTheme.typography.headlineMedium)
        
        Text("Templates", style = MaterialTheme.typography.titleMedium)
        Text("Code Review | Debug | Explain | Tests", Modifier.padding(vertical = 8.dp))
        
        Text("Saved", style = MaterialTheme.typography.titleMedium)
        if (savedPrompts.isEmpty()) {
            Text("No saved prompts", Modifier.padding(vertical = 4.dp))
        }
        
        Text("Prompt", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Enter prompt...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 6
        )
        
        Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text("Variables: {file}, {repo}, {issue}, {pr}", Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }
        
        Button(
            onClick = { /* send */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        ) {
            Text("Send to OpenHands")
        }
        
        Text(
            "ADAPTER REQUIRED - Connect profile in Settings to enable send",
            style = MaterialTheme.typography.bodySmall,
            color = androidx.compose.ui.graphics.Color(0xFF1565C0),
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(Modifier.height(80.dp))
    }
}
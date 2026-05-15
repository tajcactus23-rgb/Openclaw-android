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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

// SECTION 5: PROMPT BUILDER - with real API wiring
@Composable
fun PromptScreen(
    viewModel: PromptViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        Modifier.fillMaxSize().padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Prompt Builder", style = MaterialTheme.typography.headlineMedium)

        // Error display
        if (uiState.error != null) {
            Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Error: ${uiState.error}", Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
            }
        }

        // Success display
        if (uiState.automationCreated != null) {
            Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Automation created: ${uiState.automationCreated!!.name} (ID: ${uiState.automationCreated!!.id})", 
                    Modifier.padding(12.dp), color = Color(0xFF2E7D32))
            }
        }

        Text("Templates", style = MaterialTheme.typography.titleMedium)
        Text("Code Review | Debug | Explain | Tests", Modifier.padding(vertical = 8.dp))

        Text("Prompt", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = uiState.prompt,
            onValueChange = { viewModel.onPromptChanged(it) },
            label = { Text("Enter prompt...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 6,
            enabled = !uiState.isLoading
        )

        Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text("Variables: {file}, {repo}, {issue}, {pr}", Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = { viewModel.sendPrompt() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canSend && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(Modifier.height(20.dp))
            } else {
                Text("Send to OpenHands")
            }
        }

        Text(
            "Sends via POST /api/automation/v1/preset/prompt",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF1565C0),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(80.dp))
    }
}

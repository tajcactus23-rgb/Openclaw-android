package com.openhands.android.presentation.ui.screens.terminal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.unit.dp

@Composable
fun TerminalScreen() {
    var command by remember { mutableStateOf("") }
    
    val history = remember {
        listOf(
            TerminalLine("1", "ls -la", "workspace/\n  app/\n  build.gradle", 0),
            TerminalLine("2", "pwd", "/workspace", 0)
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Terminal", style = MaterialTheme.typography.headlineMedium)
        
        Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(history) { line ->
                    Column {
                        Text("\$ ${line.command}", style = MaterialTheme.typography.bodyMedium)
                        Text(line.output, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        
        val submit = { /* TODO: implement */ }
        OutlinedTextField(
            value = command,
            onValueChange = { command = it },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            placeholder = { Text("Command") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { submit() })
        )
    }
}

private data class TerminalLine(val id: String, val command: String, val output: String, val exitCode: Int)
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// SECTION 6: SKILL BUILDER
@Composable
fun SkillsScreen() {
    var skillContent by remember { mutableStateOf("") }
    var trigger by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf(listOf("code-review", "debug", "git", "test")) }
    
    Column(
        Modifier.fillMaxSize().padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Skill Builder", style = MaterialTheme.typography.headlineMedium)
        
        Text("Active Skills", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(Modifier.padding(12.dp)) {
                skills.forEach { name ->
                    Text("• $name", Modifier.padding(vertical = 2.dp))
                }
            }
        }
        
        Text("Trigger Editor", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = trigger,
            onValueChange = { trigger = it },
            label = { Text("Trigger phrase") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Text("SKILL.md Editor", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = skillContent,
            onValueChange = { skillContent = it },
            label = { Text("Skill content...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 8
        )
        
        Button(
            onClick = { skills = skills + listOf("new-skill") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Add Skill")
        }
        
        Button(
            onClick = { /* validate */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        ) {
            Text("Validate (Requires Attached Project)")
        }
        
        Card(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("Skill Format:", style = MaterialTheme.typography.labelMedium)
                Text("# Skill Name\n> Description\n\n## Triggers\n- trigger-phrase\n\n## Actions\n- action", style = MaterialTheme.typography.bodySmall)
            }
        }
        
        Text(
            "ADAPTER REQUIRED - Attach to project to enable validation and sync",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF1565C0)
        )
        
        Spacer(Modifier.height(80.dp))
    }
}
package com.openhands.android.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    data class Skill(val name: String, val content: String, val trigger: String)
    
    var skills by remember { mutableStateOf(listOf<Skill>()) }
    var newSkillName by remember { mutableStateOf("") }
    var newSkillContent by remember { mutableStateOf("") }

    fun saveSkill() {
        if (newSkillName.isNotBlank()) {
            skills = skills + Skill(newSkillName, newSkillContent, "skill:$newSkillName")
            newSkillName = ""
            newSkillContent = ""
        }
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Skill Builder", style = MaterialTheme.typography.headlineMedium)

        Text("Saved Skills", style = MaterialTheme.typography.titleMedium)
        
        if (skills.isEmpty()) {
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("No skills saved. Add one below.", Modifier.padding(12.dp))
            }
        } else {
            skills.forEach { skill: Skill ->
                Card(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(skill.name, style = MaterialTheme.typography.titleMedium)
                            Text("Trigger: ${skill.trigger}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { skills = skills - skill }) { 
                            Icon(Icons.Default.Delete, "Delete") 
                        }
                    }
                }
            }
        }

        Text("Create Skill", style = MaterialTheme.typography.titleMedium)
        
        OutlinedTextField(
            value = newSkillName,
            onValueChange = { newSkillName = it },
            label = { Text("Skill name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = newSkillContent,
            onValueChange = { newSkillContent = it },
            label = { Text("SKILL.md content...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 6
        )

        Button(
            onClick = { saveSkill() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            enabled = newSkillName.isNotBlank()
        ) {
            Icon(Icons.Default.Add, null); Text("Save Skill")
        }

        Text("Sync Status: LOCAL ONLY (cloud sync ADAPTER_REQUIRED)", 
            style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))

        Spacer(Modifier.height(80.dp))
    }
}

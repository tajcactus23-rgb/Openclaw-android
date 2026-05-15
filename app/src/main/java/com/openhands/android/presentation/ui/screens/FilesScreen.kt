package com.openhands.android.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// SECTION 7: FILE HUB
@Composable
fun FilesScreen() {
    val files = remember { listOf("app-debug.apk", "settings.gradle", "build.gradle") }
    
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("File Hub", style = MaterialTheme.typography.headlineMedium) }
        
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { /* Pick file */ }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Folder, null); Text("Browse") }
            Button(onClick = { /* Attach */ }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.AttachFile, null); Text("Attach") }
        } }
        
        item { Text("Workspace Files", style = MaterialTheme.typography.titleMedium) }
        files.forEach { name ->
            item { Card(Modifier.fillMaxWidth()) { Text(name, Modifier.padding(12.dp)) } }
        }
    }
}

package com.openhands.android.presentation.ui.screens

import android.net.Uri
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// SECTION 7: FILE HUB - with SAF picker
@Composable
fun FilesScreen() {
    val context = LocalContext.current
    var files by remember { mutableStateOf(listOf<Uri>()) }
    var lastPickedUri by remember { mutableStateOf<Uri?>(null) }

    // SAF file picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            files = files + it
            lastPickedUri = it
        }
    }

    // Share launcher
    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("File Hub", style = MaterialTheme.typography.headlineMedium) }
        
        if (lastPickedUri != null) {
            item { Text("Last picked: ${lastPickedUri?.lastPathSegment}", style = MaterialTheme.typography.bodySmall) }
        }

        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { filePickerLauncher.launch(arrayOf("*/*")) }, modifier = Modifier.weight(1f)) { 
                Icon(Icons.Default.Folder, null); Text("Browse") 
            }
            Button(onClick = { }, modifier = Modifier.weight(1f), enabled = false) { 
                Icon(Icons.Default.AttachFile, null); Text("Export") 
            }
        } }

        item { Text("Attached Files", style = MaterialTheme.typography.titleMedium) }

        if (files.isEmpty()) {
            item { Card(Modifier.fillMaxWidth()) { Text("No files attached. Tap Browse to add.", Modifier.padding(12.dp)) } }
        } else {
            items(files) { uri ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f).padding(4.dp)) {
                            Text(uri.lastPathSegment ?: "Unknown", style = MaterialTheme.typography.titleMedium)
                            Text(uri.toString(), style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            IconButton(onClick = {
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    type = context.contentResolver.getType(uri)
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                shareLauncher.launch(Intent.createChooser(share, "Share"))
                            }) { Icon(Icons.Default.Share, "Share") }
                            IconButton(onClick = { files = files - uri }) { Icon(Icons.Default.Delete, "Remove") }
                        }
                    }
                }
            }
        }
    }
}

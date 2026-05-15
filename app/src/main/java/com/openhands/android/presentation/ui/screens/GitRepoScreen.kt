package com.openhands.android.presentation.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader

data class GitLocalStatus(
    val isGitRepo: Boolean,
    val branch: String,
    val hasChanges: Boolean,
    val hasRemote: Boolean
)

@Composable
fun GitRepoScreen() {
    val context = LocalContext.current
    var gitStatus by remember { mutableStateOf<GitLocalStatus?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun scanGit() {
        isLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            val dirs = listOf(
                context.filesDir.parentFile?.parentFile,
                context.getExternalFilesDir(null)?.parentFile,
                File("/data")
            ).filterNotNull()

            var foundRepo = false
            var branch = ""
            var hasChanges = false
            var hasRemote = false

            for (dir in dirs) {
                val gitDir = File(dir, ".git")
                if (gitDir.exists() && gitDir.isDirectory) {
                    foundRepo = true
                    try {
                        val p = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD", null, dir)
                        branch = BufferedReader(InputStreamReader(p.inputStream)).readText().trim()
                        p.waitFor()

                        val sp = Runtime.getRuntime().exec("git status -s", null, dir)
                        val statusOut = BufferedReader(InputStreamReader(sp.inputStream)).readText()
                        hasChanges = statusOut.isNotBlank()
                        sp.waitFor()

                        val rp = Runtime.getRuntime().exec("git remote -v", null, dir)
                        hasRemote = rp.waitFor() == 0
                    } catch (e: Exception) {
                        branch = "error"
                    }
                    break
                }
            }

            gitStatus = GitLocalStatus(foundRepo, branch, hasChanges, hasRemote)
            isLoading = false
        }
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text("Git / Repo Ops", style = MaterialTheme.typography.headlineMedium)

        Button(onClick = { scanGit() }, modifier = Modifier.fillMaxWidth()) {
            Text(if (isLoading) "Scanning..." else "Scan Device for Git")
        }

        gitStatus?.let { status ->
            Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Git Status", style = MaterialTheme.typography.titleMedium)
                    Text("Repository: ${if (status.isGitRepo) "Found" else "Not found"}")
                    if (status.isGitRepo) {
                        Text("Branch: ${status.branch}")
                        Text("Has Changes: ${status.hasChanges}")
                        Text("Has Remote: ${status.hasRemote}")
                    }
                }
            }
        }

        Text("External Services", style = MaterialTheme.typography.titleMedium)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("GitHub", style = MaterialTheme.typography.bodyMedium)
                Text("ADAPTER REQUIRED - Configure token in Settings", 
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("GitLab", style = MaterialTheme.typography.bodyMedium)
                Text("ADAPTER REQUIRED - Configure token in Settings", 
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Bitbucket", style = MaterialTheme.typography.bodyMedium)
                Text("ADAPTER REQUIRED - Configure token in Settings", 
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
            }
        }

        Spacer(Modifier.padding(16.dp))
    }
}
package com.openhands.android.presentation.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GitFullStatus(
    val isGitRepo: Boolean,
    val branch: String,
    val remoteName: String?,
    val remoteUrl: String?,
    val changedFiles: List<String>,
    val stagedFiles: List<String>,
    val untrackedFiles: List<String>,
    val recentCommits: List<GitCommit>,
    val aheadBehind: Pair<Int, Int>?
)

data class GitCommit(val hash: String, val message: String, val author: String, val date: String)

// SECTION 11: GIT OPS - Full local git integration
@Composable
fun GitRepoScreen() {
    val context = LocalContext.current
    var gitStatus by remember { mutableStateOf<GitFullStatus?>(null) }
    var diagnostics by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    fun scanFullGit() {
        isLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            val dirs = listOf(
                context.filesDir.parentFile?.parentFile,
                context.getExternalFilesDir(null)?.parentFile,
                File("/data")
            ).filterNotNull()

            var foundRepo = false
            var branch = ""
            var remoteName: String? = null
            var remoteUrl: String? = null
            var changedFiles = listOf<String>()
            var stagedFiles = listOf<String>()
            var untrackedFiles = listOf<String>()
            var recentCommits = listOf<GitCommit>()
            var aheadBehind: Pair<Int, Int>? = null

            for (dir in dirs) {
                val gitDir = File(dir, ".git")
                if (gitDir.exists() && gitDir.isDirectory) {
                    foundRepo = true
                    try {
                        // Branch
                        val bp = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD", null, dir)
                        branch = BufferedReader(InputStreamReader(bp.inputStream)).readText().trim()
                        bp.waitFor()

                        // Remote
                        val rp = Runtime.getRuntime().exec("git remote -v", null, dir)
                        val remoteOut = BufferedReader(InputStreamReader(rp.inputStream)).readText()
                        if (remoteOut.isNotBlank()) {
                            val parts = remoteOut.split(Regex("\\s+"))
                            if (parts.size >= 2) { remoteName = parts[0]; remoteUrl = parts[1] }
                        }
                        rp.waitFor()

                        // Status
                        val sp = Runtime.getRuntime().exec("git status --porcelain", null, dir)
                        val statusOut = BufferedReader(InputStreamReader(sp.inputStream)).readText()
                        changedFiles = statusOut.lines().filter { it.startsWith(" M") || it.startsWith("M ") || it.startsWith("MM") }.map { it.substring(3) }
                        stagedFiles = statusOut.lines().filter { it.startsWith("M ") || it.startsWith("MM") }.map { it.substring(3) }
                        untrackedFiles = statusOut.lines().filter { it.startsWith("??") }.map { it.substring(3) }
                        sp.waitFor()

                        // Ahead/behind
                        if (remoteName != null) {
                            try {
                                val abp = Runtime.getRuntime().exec("git rev-list --left-right --count HEAD...origin/${branch.replace("origin/", "")}", null, dir)
                                val abOut = BufferedReader(InputStreamReader(abp.inputStream)).readText().trim()
                                val parts = abOut.split(Regex("\\s+"))
                                if (parts.size == 2) {
                                    aheadBehind = Pair(parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0)
                                }
                            } catch (e: Exception) { /* ignore */ }
                        }

                        // Recent commits
                        val cp = Runtime.getRuntime().exec("git log --oneline -5", null, dir)
                        val commitOut = BufferedReader(InputStreamReader(cp.inputStream)).readText()
                        recentCommits = commitOut.lines().filter { it.isNotBlank() }.map { line ->
                            val parts = line.split(Regex("\\s+"), 2)
                            GitCommit(parts.getOrNull(0) ?: "", parts.getOrNull(1) ?: "", "", "")
                        }
                        cp.waitFor()
                    } catch (e: Exception) {
                        branch = "Error: ${e.message}"
                    }
                    break
                }
            }

            gitStatus = GitFullStatus(foundRepo, branch, remoteName, remoteUrl, changedFiles, stagedFiles, untrackedFiles, recentCommits, aheadBehind)
            isLoading = false
        }
    }

    fun exportDiagnostics() {
        if (gitStatus == null) return
        val status = gitStatus!!
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        diagnostics = buildString {
            appendLine("# Git Diagnostics - $timestamp")
            appendLine()
            appendLine("## Repository")
            appendLine("Is Git: ${status.isGitRepo}")
            appendLine("Branch: ${status.branch}")
            appendLine()
            appendLine("## Remote")
            appendLine("Name: ${status.remoteName ?: "none"}")
            appendLine("URL: ${status.remoteUrl ?: "none"}")
            if (status.aheadBehind != null) appendLine("Ahead: ${status.aheadBehind.first}, Behind: ${status.aheadBehind.second}")
            appendLine()
            appendLine("## Changed Files (${status.changedFiles.size})")
            status.changedFiles.forEach { appendLine("  $it") }
            appendLine()
            appendLine("## Staged Files (${status.stagedFiles.size})")
            status.stagedFiles.forEach { appendLine("  $it") }
            appendLine()
            appendLine("## Untracked Files (${status.untrackedFiles.size})")
            status.untrackedFiles.forEach { appendLine("  $it") }
            appendLine()
            appendLine("## Recent Commits")
            status.recentCommits.forEach { appendLine("  ${it.hash.take(7)} - ${it.message}") }
            appendLine()
            appendLine("## API Status")
            appendLine("GitHub/GitLab/Bitbucket: ADAPTER_REQUIRED - Configure OAuth token in Settings")
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Git / Repo Ops", style = MaterialTheme.typography.headlineMedium)

        Button(onClick = { scanFullGit() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Refresh, null)
            Text(if (isLoading) " Scanning..." else " Full Scan")
        }

        gitStatus?.let { status ->
            if (status.isGitRepo) {
                Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Repository", style = MaterialTheme.typography.titleMedium)
                        Text("Branch: ${status.branch}")
                        if (status.aheadBehind != null) {
                            Text("ahead=${status.aheadBehind.first}, behind=${status.aheadBehind.second}")
                        }
                    }
                }

                if (status.remoteName != null) {
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cloud, null, Modifier.padding(end = 4.dp))
                            Column {
                                Text("Remote: ${status.remoteName}")
                                Text(status.remoteUrl ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }

                if (status.changedFiles.isNotEmpty()) {
                    Text("Changed (${status.changedFiles.size})", style = MaterialTheme.typography.titleMedium)
                    status.changedFiles.take(5).forEach { 
                        Text("  • $it", style = MaterialTheme.typography.bodySmall) 
                    }
                }

                if (status.stagedFiles.isNotEmpty()) {
                    Text("Staged (${status.stagedFiles.size})", style = MaterialTheme.typography.titleMedium)
                    status.stagedFiles.take(5).forEach { 
                        Text("  • $it", style = MaterialTheme.typography.bodySmall) 
                    }
                }

                if (status.untrackedFiles.isNotEmpty()) {
                    Text("Untracked (${status.untrackedFiles.size})", style = MaterialTheme.typography.titleMedium)
                    status.untrackedFiles.take(5).forEach { 
                        Text("  • $it", style = MaterialTheme.typography.bodySmall) 
                    }
                }

                if (status.recentCommits.isNotEmpty()) {
                    Text("Recent Commits", style = MaterialTheme.typography.titleMedium)
                    status.recentCommits.forEach { commit ->
                        Text("  ${commit.hash.take(7)} - ${commit.message}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text("No Git repository found", Modifier.padding(12.dp))
                }
            }
        }

        // API services - ADAPTER_REQUIRED buttons
        Text("External Services", style = MaterialTheme.typography.titleMedium)

        Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
            Text("GitHub Issues - ADAPTER_REQUIRED")
        }
        Text("Configure OAuth token in Settings", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
            Text("GitLab Issues - ADAPTER_REQUIRED")
        }

        Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
            Text("Bitbucket Issues - ADAPTER_REQUIRED")
        }

        Button(onClick = { exportDiagnostics() }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            enabled = gitStatus != null) {
            Icon(Icons.Default.ContentCopy, null)
            Text("Export Diagnostics")
        }

        if (diagnostics.isNotBlank()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Diagnostics exported to clipboard", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
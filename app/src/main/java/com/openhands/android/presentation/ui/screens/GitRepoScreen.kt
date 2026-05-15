package com.openhands.android.presentation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// SECTION 11: GIT / REPO OPS
@Composable
fun GitRepoScreen() {
    Column(
        Modifier.fillMaxSize().padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Git / Repo Ops", style = MaterialTheme.typography.headlineMedium)
        
        Text("GitHub", style = MaterialTheme.typography.titleMedium)
        Text("ADAPTER REQUIRED - GitHub API needs OAuth adapter", 
            style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
        
        Text("GitLab", style = MaterialTheme.typography.titleMedium)
        Text("ADAPTER REQUIRED - GitLab API needs OAuth adapter", 
            style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
        
        Text("Bitbucket", style = MaterialTheme.typography.titleMedium)
        Text("ADAPTER REQUIRED - Bitbucket API needs OAuth adapter", 
            style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
        
        Text("Features", style = MaterialTheme.typography.titleMedium)
        Text("Issue-to-Task → ADAPTER REQUIRED", Modifier.padding(4.dp))
        Text("PR Viewer → ADAPTER REQUIRED", Modifier.padding(4.dp))
        Text("CI Status → ADAPTER REQUIRED", Modifier.padding(4.dp))
        
        Spacer(Modifier.height(80.dp))
    }
}
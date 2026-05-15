package com.openhands.android.presentation.ui.screens.webview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Web
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
import androidx.compose.ui.unit.dp
import com.openhands.android.domain.model.WebApp

@Composable
fun WebViewScreen() {
    var currentUrl by remember { mutableStateOf("") }
    
    val webApps = remember {
        listOf(
            WebApp("1", "OpenHands Cloud", "https://app.all-hands.dev", category = "OpenHands"),
            WebApp("2", "GitHub", "https://github.com", category = "Development"),
            WebApp("3", "Documentation", "https://docs.openhands.dev", category = "OpenHands")
        )
    }

    val groupedApps = webApps.groupBy { it?.category ?: "Other" }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Web Apps", style = MaterialTheme.typography.headlineMedium)
        
        // WebView placeholder - URL input triggers preview
        if (currentUrl.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Web, contentDescription = null, modifier = Modifier.padding(8.dp))
                    Text("Web Preview: $currentUrl", style = MaterialTheme.typography.bodyMedium)
                    Text("WebView integration requires device testing", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            // App grid
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedApps.forEach { (category, apps) ->
                    item { Text(category, style = MaterialTheme.typography.titleMedium) }
                    items(apps) { app ->
                        app?.let { WebAppCard(it) { currentUrl = it.url } }
                    }
                }
            }
        }
    }
}

@Composable
private fun WebAppCard(webApp: WebApp, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Web, null, modifier = Modifier.padding(end = 12.dp))
            Column {
                Text(webApp.name, style = MaterialTheme.typography.titleMedium)
                Text(webApp.url, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
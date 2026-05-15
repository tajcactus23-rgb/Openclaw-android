package com.openhands.android.presentation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// SECTION 9: MCP / TOOL MANAGER
@Composable
fun ToolManagerScreen() {
    // Available tools - from OpenHands capabilities
    val availableTools = remember { listOf(
        "Bash" to true,
        "Read" to true,
        "Write" to true,
        "Glob" to true,
        "Grep" to true,
        "WebFetch" to true,
        "Browser" to false,
        "Git" to false
    ) }
    
    var enabledTools by remember { mutableStateOf(setOf("Bash", "Read", "Write")) }
    var mcpStatus by remember { mutableStateOf("No MCP servers configured") }

    // Test MCP connection
    fun testMcpConnection() {
        mcpStatus = "ADAPTER REQUIRED - MCP requires project adapter"
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Tool Manager", style = MaterialTheme.typography.headlineMedium)

        Text("MCP Configuration", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Column(Modifier.padding(8.dp)) {
                Text("MCP servers connect to OpenHands plugins", Modifier.padding(2.dp))
                Text("Status: $mcpStatus", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
            }
        }

        Text("Available Tools", style = MaterialTheme.typography.titleMedium)
        
        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Column {
                availableTools.forEach { (tool, available) ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Checkbox(
                            checked = enabledTools.contains(tool),
                            onCheckedChange = { checked ->
                                enabledTools = if (checked) enabledTools + tool else enabledTools - tool
                            },
                            enabled = available
                        )
                        Text(tool, Modifier.padding(start = 4.dp))
                        if (!available) {
                            Text(" (unavailable)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { testMcpConnection() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test MCP Connection")
        }

        Text("Enabled: ${enabledTools.joinToString()}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))

        Spacer(Modifier.padding(16.dp))
    }
}

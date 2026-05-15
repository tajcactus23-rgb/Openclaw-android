package com.openhands.android.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

enum class NodeType(val label: String, val color: Long) {
    PROMPT("Prompt", 0xFF2196F3),
    FILE("File", 0xFF4CAF50),
    SKILL("Skill", 0xFFFFC107),
    TOOL("Tool", 0xFF9C27B0),
    MODEL("Model", 0xFFE91E63),
    OUTPUT("Output", 0xFF00BCD4)
}

data class Node(
    val id: String,
    val type: NodeType,
    val label: String,
    val config: String = ""
)

@Composable
fun AgentCanvasScreen() {
    var nodes by remember { mutableStateOf(listOf<Node>()) }
    var workflowJson by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(NodeType.PROMPT) }
    var nodeLabel by remember { mutableStateOf("") }
    var graphValid by remember { mutableStateOf(true) }

    fun buildWorkflowJson(): String {
        val json = JSONObject()
        json.put("name", "Mobile Workflow")
        json.put("version", "1.0")
        val nodeArray = JSONArray()
        nodes.forEach { node ->
            val nodeJson = JSONObject()
            nodeJson.put("id", node.id)
            nodeJson.put("type", node.type.name)
            nodeJson.put("label", node.label)
            nodeArray.put(nodeJson)
        }
        json.put("nodes", nodeArray)
        val connections = JSONArray()
        for (i in 0 until nodes.size - 1) {
            val conn = JSONObject()
            conn.put("from", nodes[i].id)
            conn.put("to", nodes[i + 1].id)
            connections.put(conn)
        }
        json.put("connections", connections)
        return json.toString(2)
    }

    fun validateGraph(): Boolean = nodes.isEmpty() || nodes.distinctBy { it.type }.size == nodes.size

    fun addNode() {
        if (nodeLabel.isBlank()) return
        val newNode = Node("node_${nodes.size}_${System.currentTimeMillis()}", selectedType, nodeLabel)
        nodes = nodes + newNode
        nodeLabel = ""
        workflowJson = buildWorkflowJson()
        graphValid = validateGraph()
    }

    fun deleteNode(node: Node) {
        nodes = nodes - node
        workflowJson = buildWorkflowJson()
        graphValid = validateGraph()
    }

    fun clearWorkflow() {
        nodes = emptyList()
        nodeLabel = ""
        workflowJson = ""
        graphValid = true
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text("Agent Canvas", style = MaterialTheme.typography.headlineMedium)
        Text("Workflow Nodes", style = MaterialTheme.typography.titleMedium)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            NodeType.entries.forEach { type ->
                Box(
                    Modifier.clip(RoundedCornerShape(4.dp))
                    .background(if (selectedType == type) Color(type.color) else Color.Transparent)
                    .border(1.dp, Color(type.color), RoundedCornerShape(4.dp))
                    .clickable { selectedType = type }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(type.label, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = nodeLabel,
                onValueChange = { nodeLabel = it },
                label = { Text("Node label") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { addNode() }, enabled = nodeLabel.isNotBlank()) {
                Icon(Icons.Default.Add, null)
            }
        }

        if (nodes.isEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Text("No nodes. Add nodes above.", Modifier.padding(12.dp))
            }
        } else {
            nodes.forEach { node ->
                Card(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(Color(node.type.color)))
                        Text(node.label, Modifier.weight(1f).padding(start = 8.dp))
                        Text(node.type.label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        IconButton(onClick = { deleteNode(node) }) { Icon(Icons.Default.Delete, "Delete", Modifier.size(16.dp)) }
                    }
                }
            }
        }

        Text("Workflow JSON", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = workflowJson,
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            readOnly = true
        )

        Button(onClick = { clearWorkflow() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Delete, null)
            Text("Clear Workflow")
        }

        Text("Graph valid: $graphValid",
            style = MaterialTheme.typography.bodySmall,
            color = if (graphValid) Color(0xFF2E7D32) else Color(0xFFD32F2F))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            enabled = nodes.isNotEmpty()
        ) {
            Icon(Icons.Default.PlayArrow, null)
            Text("Run Workflow")
        }

        Text("Execution: ADAPTER REQUIRED",
            style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))

        Spacer(Modifier.padding(16.dp))
    }
}
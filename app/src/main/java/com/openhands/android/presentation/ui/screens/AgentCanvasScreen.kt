package com.openhands.android.presentation.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Upload
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.openhands.android.data.remote.WorkflowApi
import com.openhands.android.data.remote.WorkflowExecuteRequest
import com.openhands.android.data.remote.WorkflowExecutionStatus
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStreamReader

enum class NodeType(val label: String, val color: Long) {
    PROMPT("Prompt", 0xFF2196F3),
    FILE("File", 0xFF4CAF50),
    SKILL("Skill", 0xFFFFC107),
    TOOL("Tool", 0xFF9C27B0),
    MODEL("Model", 0xFFE91E63),
    OUTPUT("Output", 0xFF00BCD4)
}

data class Node(val id: String, val type: NodeType, val label: String, val config: String = "")
data class Edge(val fromNodeId: String, val toNodeId: String)

// SECTION 10: AGENT CANVAS - Full workflow builder with edges, import/export
@Composable
fun AgentCanvasScreen() {
    val context = LocalContext.current
    var nodes by remember { mutableStateOf(listOf<Node>()) }
    var edges by remember { mutableStateOf(listOf<Edge>()) }
    var workflowName by remember { mutableStateOf("workflow") }
    var workflowJson by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(NodeType.PROMPT) }
    var nodeLabel by remember { mutableStateOf("") }
    var nodeConfig by remember { mutableStateOf("") }
    var connectingFrom by remember { mutableStateOf<String?>(null) }
    var graphValid by remember { mutableStateOf(true) }
    var lastSaved by remember { mutableStateOf("") }
    var executionId by remember { mutableStateOf("") }
    var executionStatus by remember { mutableStateOf("") }
    var executionLogs by remember { mutableStateOf(listOf<String>()) }
    var executionHistory by remember { mutableStateOf(listOf<WorkflowExecutionStatus>()) }
    var isPolling by remember { mutableStateOf(false) }
    var executionError by remember { mutableStateOf("") }

    val coroutineScope = remember { CoroutineScope(Dispatchers.Main) }

    fun runWorkflow() {
        coroutineScope.launch {
            try {
                val nodesList = nodes.map { node ->
                    mapOf("id" to node.id, "type" to node.type.name.lowercase(), "name" to node.label, "config" to node.config)
                }
                val request = WorkflowExecuteRequest(workflowId = "canvas-" + System.currentTimeMillis(), name = workflowName, nodes = nodesList)
                
                executionStatus = "running"
                executionError = ""
                isPolling = true
                
                val result = withContext(Dispatchers.IO) {
                    val api = WorkflowApi(okhttp3.OkHttpClient(), com.squareup.moshi.Moshi.Builder().build(), "http://10.0.2.2:8000")
                    api.executeWorkflow(request)
                }
                
                result.onSuccess { response ->
                    executionId = response.executionId
                    executionStatus = response.status
                    
                    while (isPolling && executionStatus == "running") {
                        Thread.sleep(1000)
                        val statusResult = withContext(Dispatchers.IO) {
                            val api = WorkflowApi(okhttp3.OkHttpClient(), com.squareup.moshi.Moshi.Builder().build(), "http://10.0.2.2:8000")
                            api.getWorkflowExecution(executionId)
                        }
                        statusResult.onSuccess { status ->
                            executionStatus = status.status
                            if (status.status != "running") {
                                isPolling = false
                                val logsResult = withContext(Dispatchers.IO) {
                                    val api = WorkflowApi(okhttp3.OkHttpClient(), com.squareup.moshi.Moshi.Builder().build(), "http://10.0.2.2:8000")
                                    api.getWorkflowExecutionLogs(executionId)
                                }
                                logsResult.onSuccess { logs -> executionLogs = logs.map { "${it.level}: ${it.message}" } }
                            }
                        }
                    }
                }
                result.onFailure { error ->
                    executionError = error.message ?: "Execution failed"
                    executionStatus = "failed"
                    isPolling = false
                }
            } catch (e: Exception) {
                executionError = e.message ?: "Unknown error"
                executionStatus = "failed"
                isPolling = false
            }
        }
    }

    fun loadHistory() {
        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                val api = WorkflowApi(okhttp3.OkHttpClient(), com.squareup.moshi.Moshi.Builder().build(), "http://10.0.2.2:8000")
                api.listWorkflowExecutions()
            }
            result.onSuccess { list -> executionHistory = list }
        }
    }

    fun buildWorkflowJson(): String {
        val json = JSONObject()
        json.put("name", workflowName)
        json.put("version", "1.0")
        
        val nodeArray = JSONArray()
        nodes.forEach { node ->
            val nodeJson = JSONObject()
            nodeJson.put("id", node.id)
            nodeJson.put("type", node.type.name)
            nodeJson.put("label", node.label)
            nodeJson.put("config", node.config)
            nodeArray.put(nodeJson)
        }
        json.put("nodes", nodeArray)
        
        val edgeArray = JSONArray()
        edges.forEach { edge ->
            val edgeJson = JSONObject()
            edgeJson.put("from", edge.fromNodeId)
            edgeJson.put("to", edge.toNodeId)
            edgeArray.put(edgeJson)
        }
        json.put("edges", edgeArray)
        return json.toString(2)
    }

    fun parseWorkflowJson(jsonStr: String): Boolean {
        try {
            val json = JSONObject(jsonStr)
            workflowName = json.optString("name", "workflow")
            val nodeArray = json.getJSONArray("nodes")
            val parsedNodes = mutableListOf<Node>()
            for (i in 0 until nodeArray.length()) {
                val n = nodeArray.getJSONObject(i)
                val typeName = n.optString("type", "PROMPT")
                val nodeType = NodeType.entries.find { it.name == typeName } ?: NodeType.PROMPT
                parsedNodes.add(Node(n.optString("id"), nodeType, n.optString("label"), n.optString("config")))
            }
            nodes = parsedNodes
            
            val edgeArray = json.optJSONArray("edges") ?: JSONArray()
            val parsedEdges = mutableListOf<Edge>()
            for (i in 0 until edgeArray.length()) {
                val e = edgeArray.getJSONObject(i)
                parsedEdges.add(Edge(e.optString("from"), e.optString("to")))
            }
            edges = parsedEdges
            workflowJson = jsonStr
            return true
        } catch (e: Exception) { return false }
    }

    fun validateGraph(): Boolean {
        if (nodes.isEmpty()) return true
        if (nodes.size == 1) return true
        val connected_ids = mutableSetOf<String>()
        edges.forEach { connected_ids.add(it.fromNodeId); connected_ids.add(it.toNodeId) }
        return connected_ids.isNotEmpty()
    }

    fun addNode() {
        if (nodeLabel.isBlank()) return
        val newNode = Node("node_${nodes.size}_${System.currentTimeMillis()}", selectedType, nodeLabel, nodeConfig)
        nodes = nodes + newNode
        nodeLabel = ""
        nodeConfig = ""
        workflowJson = buildWorkflowJson()
        graphValid = validateGraph()
    }

    fun deleteNode(node: Node) {
        nodes = nodes - node
        edges = edges.filter { it.fromNodeId != node.id && it.toNodeId != node.id }
        connectingFrom = null
        workflowJson = buildWorkflowJson()
        graphValid = validateGraph()
    }

    fun startConnection(nodeId: String) {
        if (connectingFrom == null) {
            connectingFrom = nodeId
        } else if (connectingFrom != null && connectingFrom != nodeId) {
            val from = connectingFrom ?: return
            edges = edges + Edge(from, nodeId)
            connectingFrom = null
            workflowJson = buildWorkflowJson()
            graphValid = validateGraph()
        } else {
            connectingFrom = null
        }
    }

    fun removeEdge(edge: Edge) {
        edges = edges - edge
        workflowJson = buildWorkflowJson()
        graphValid = validateGraph()
    }

    fun clearWorkflow() {
        nodes = emptyList()
        edges = emptyList()
        nodeLabel = ""
        nodeConfig = ""
        workflowJson = ""
        connectingFrom = null
        graphValid = true
    }

    fun saveToFile() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val file = File(context.filesDir, "${workflowName}.json")
                withContext(Dispatchers.IO) { file.writeText(workflowJson) }
                lastSaved = "Saved"
            } catch (e: Exception) { lastSaved = "Error" }
        }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                val json = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
                if (json != null) { parseWorkflowJson(json); lastSaved = "Loaded" }
            } catch (e: Exception) { lastSaved = "Error" }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Agent Canvas", style = MaterialTheme.typography.headlineMedium)
        
        OutlinedTextField(value = workflowName, onValueChange = { workflowName = it; if (workflowJson.isNotBlank()) workflowJson = buildWorkflowJson() },
            label = { Text("Workflow name") }, modifier = Modifier.fillMaxWidth())

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            NodeType.entries.forEach { type ->
                Box(Modifier.clip(RoundedCornerShape(4.dp))
                    .background(if (selectedType == type) Color(type.color) else Color.Transparent)
                    .border(1.dp, Color(type.color), RoundedCornerShape(4.dp))
                    .clickable { selectedType = type }.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(type.label, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = nodeLabel, onValueChange = { nodeLabel = it }, label = { Text("Label") }, modifier = Modifier.weight(1f))
            Button(onClick = { addNode() }, enabled = nodeLabel.isNotBlank()) { Icon(Icons.Default.Add, null) }
        }
        
        OutlinedTextField(value = nodeConfig, onValueChange = { nodeConfig = it }, label = { Text("Config") }, modifier = Modifier.fillMaxWidth())

        if (connectingFrom != null) Text("Tap to connect", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))

        if (nodes.isEmpty()) { Card(Modifier.fillMaxWidth()) { Text("No nodes", Modifier.padding(12.dp)) } }
        else {
            nodes.forEach { node ->
                Card(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(Color(node.type.color)))
                        Column(Modifier.weight(1f).padding(start = 8.dp)) {
                            Text(node.label, style = MaterialTheme.typography.titleMedium)
                            if (node.config.isNotBlank()) Text(node.config, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Button(onClick = { startConnection(node.id) }, modifier = Modifier.width(80.dp)) { Text("Link", style = MaterialTheme.typography.bodySmall) }
                        IconButton(onClick = { deleteNode(node) }) { Icon(Icons.Default.Delete, "Delete", Modifier.size(16.dp)) }
                    }
                }
            }
        }

        if (edges.isNotEmpty()) {
            Text("Connections", style = MaterialTheme.typography.titleMedium)
            edges.forEach { edge ->
                val fromLabel = nodes.find { it.id == edge.fromNodeId }?.label ?: edge.fromNodeId
                val toLabel = nodes.find { it.id == edge.toNodeId }?.label ?: edge.toNodeId
                Card(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$fromLabel → $toLabel", style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = { removeEdge(edge) }) { Icon(Icons.Default.Delete, "Remove", Modifier.size(16.dp)) }
                    }
                }
            }
        }

        Text("Workflow JSON", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = workflowJson, onValueChange = { workflowJson = it }, modifier = Modifier.fillMaxWidth(), minLines = 4)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { clearWorkflow() }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Delete, null); Text("Clear") }
            Button(onClick = { saveToFile() }, modifier = Modifier.weight(1f), enabled = workflowJson.isNotBlank()) { Icon(Icons.Default.Save, null); Text("Save") }
            Button(onClick = { filePicker.launch(arrayOf("application/json")) }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Upload, null); Text("Load") }
        }

        Text(lastSaved, style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
        Text("Graph valid: $graphValid", style = MaterialTheme.typography.bodySmall, color = if (graphValid) Color(0xFF2E7D32) else Color(0xFFD32F2F))

        Button(onClick = { runWorkflow() }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), enabled = nodes.isNotEmpty() && graphValid) {
            Icon(Icons.Default.PlayArrow, null); Text("Run Workflow")
        }
        
        // Execution status display
        if (executionId.isNotBlank()) {
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(8.dp)) {
                    Text("Execution: $executionStatus", style = MaterialTheme.typography.titleSmall)
                    Text("ID: $executionId", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    if (executionLogs.isNotEmpty()) {
                        Text("Logs:", style = MaterialTheme.typography.labelSmall)
                        executionLogs.take(5).forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                    if (executionError.isNotBlank()) {
                        Text("Error: $executionError", style = MaterialTheme.typography.bodySmall, color = Color.Red)
                    }
                    if (executionStatus == "running") {
                        Button(onClick = { isPolling = false }, modifier = Modifier.padding(top = 4.dp)) { Text("Cancel") }
                    }
                }
            }
        } else {
            Text("Execution: ADAPTER_REQUIRED - Connect relay at http://10.0.2.2:8000", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
        }
        Spacer(Modifier.padding(16.dp))
    }
}
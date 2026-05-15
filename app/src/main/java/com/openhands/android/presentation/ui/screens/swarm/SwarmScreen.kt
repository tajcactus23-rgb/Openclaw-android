package com.openhands.android.presentation.ui.screens.swarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openhands.android.domain.model.InstanceStatus
import com.openhands.android.domain.model.SwarmInstance
import com.openhands.android.domain.model.SwarmTask
import com.openhands.android.domain.model.SwarmTaskStatus

@Composable
fun SwarmScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tasks", "Instances", "Run")

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { /* Create task */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Create Task")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> SwarmTasksTab()
                1 -> SwarmInstancesTab()
                2 -> SwarmRunTab()
            }
        }
    }
}

@Composable
private fun SwarmTasksTab() {
    // Mock data - would come from ViewModel
    val tasks = remember {
        listOf(
            SwarmTask(
                id = "1",
                title = "Process repository",
                description = "Analyze and refactor the codebase",
                targetInstanceCount = 3,
                status = SwarmTaskStatus.RUNNING,
                instanceIds = listOf("i1", "i2")
            ),
            SwarmTask(
                id = "2",
                title = "Run tests",
                description = "Execute full test suite",
                targetInstanceCount = 5,
                status = SwarmTaskStatus.PENDING
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tasks) { task ->
            TaskCard(task = task)
        }
    }
}

@Composable
private fun TaskCard(task: SwarmTask) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (task.status) {
                SwarmTaskStatus.RUNNING -> MaterialTheme.colorScheme.primaryContainer
                SwarmTaskStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                SwarmTaskStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = task.status.name,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${task.instanceIds.size}/${task.targetInstanceCount} instances",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            if (task.status == SwarmTaskStatus.RUNNING) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    progress = { task.instanceIds.size.toFloat() / task.targetInstanceCount }
                )
            }
        }
    }
}

@Composable
private fun SwarmInstancesTab() {
    val instances = remember {
        listOf(
            SwarmInstance(id = "i1", name = "Agent-1", status = InstanceStatus.BUSY, progress = 65),
            SwarmInstance(id = "i2", name = "Agent-2", status = InstanceStatus.IDLE),
            SwarmInstance(id = "i3", name = "Agent-3", status = InstanceStatus.RUNNING, progress = 30),
            SwarmInstance(id = "i4", name = "Agent-4", status = InstanceStatus.ERROR),
            SwarmInstance(id = "i5", name = "Agent-5", status = InstanceStatus.OFFLINE)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(instances) { instance ->
            InstanceCard(instance = instance)
        }
    }
}

@Composable
private fun InstanceCard(instance: SwarmInstance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (instance.status) {
                InstanceStatus.BUSY, InstanceStatus.RUNNING -> MaterialTheme.colorScheme.primaryContainer
                InstanceStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                InstanceStatus.OFFLINE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = instance.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = instance.status.name,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (instance.progress > 0) {
                LinearProgressIndicator(
                    progress = { instance.progress / 100f },
                    modifier = Modifier.width(100.dp)
                )
            }
        }
    }
}

@Composable
private fun SwarmRunTab() {
    var taskDescription by remember { mutableStateOf("") }
    var instanceCount by remember { mutableStateOf(1f) }
    var running by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Swarm Mode",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Deploy multiple OpenHands instances to work on a common goal. Each instance can work on different parts of the task in parallel.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = taskDescription,
            onValueChange = { taskDescription = it },
            label = { Text("Task Description") },
            placeholder = { Text("What should the swarm work on?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Text(
            text = "Instance Count: ${instanceCount.toInt()}",
            style = MaterialTheme.typography.titleMedium
        )

        Slider(
            value = instanceCount,
            onValueChange = { instanceCount = it },
            valueRange = 1f..10f,
            steps = 8
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { running = !running },
                modifier = Modifier.weight(1f),
                enabled = taskDescription.isNotBlank()
            ) {
                Icon(
                    if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (running) "Stop Swarm" else "Start Swarm")
            }
        }

        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = "Swarm Mode",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Each instance works independently on subtasks. Results are aggregated when all instances complete.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
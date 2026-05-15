package com.openhands.android.presentation.ui.screens.connectionsuite

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openhands.android.domain.model.ConnectionType
import com.openhands.android.domain.model.ConnectionTypeEnum
import com.openhands.android.domain.model.ConnectionTypeStatus

@Composable
fun ConnectionSuiteScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Available", "Connected", "All")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Connection Suite",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Manage OpenHands connections and integrations",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = selectedTab == 0, onClick = { selectedTab = 0 }, label = { Text("Available") })
            FilterChip(selected = selectedTab == 1, onClick = { selectedTab = 1 }, label = { Text("Connected") })
            FilterChip(selected = selectedTab == 2, onClick = { selectedTab = 2 }, label = { Text("All") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> AvailableConnectionsTab()
            1 -> ConnectedTab()
            2 -> AllConnectionsTab()
        }
    }
}

@Composable
private fun AvailableConnectionsTab() {
    val available = remember {
        listOf(
            ConnectionType("oh-cloud", "OpenHands Cloud", ConnectionTypeEnum.OPENHANDS, ConnectionTypeStatus.AVAILABLE),
            ConnectionType("oh-selfhost", "Self-Hosted OpenHands", ConnectionTypeEnum.OPENHANDS, ConnectionTypeStatus.AVAILABLE),
            ConnectionType("gh-api", "GitHub API", ConnectionTypeEnum.CUSTOM, ConnectionTypeStatus.AVAILABLE),
            ConnectionType("gl-api", "GitLab API", ConnectionTypeEnum.CUSTOM, ConnectionTypeStatus.AVAILABLE),
            ConnectionType("bb-api", "Bitbucket API", ConnectionTypeEnum.CUSTOM, ConnectionTypeStatus.AVAILABLE)
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(available) { conn -> ConnectionTypeCard(connection = conn) }
    }
}

@Composable
private fun ConnectedTab() {
    val connected = remember {
        listOf(
            ConnectionType("oh-cloud-1", "My OpenHands", ConnectionTypeEnum.OPENHANDS, ConnectionTypeStatus.CONNECTED),
            ConnectionType("gh-1", "Work Repo", ConnectionTypeEnum.CUSTOM, ConnectionTypeStatus.CONNECTED)
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(connected) { conn -> ConnectionTypeCard(connection = conn) }
    }
}

@Composable
private fun AllConnectionsTab() {
    val all = remember {
        listOf(
            ConnectionType("oh-cloud-1", "My OpenHands", ConnectionTypeEnum.OPENHANDS, ConnectionTypeStatus.CONNECTED),
            ConnectionType("oh-cloud", "OpenHands Cloud", ConnectionTypeEnum.OPENHANDS, ConnectionTypeStatus.AVAILABLE),
            ConnectionType("gh-1", "Work Repo", ConnectionTypeEnum.CUSTOM, ConnectionTypeStatus.CONNECTED),
            ConnectionType("gh-api", "GitHub API", ConnectionTypeEnum.CUSTOM, ConnectionTypeStatus.AVAILABLE)
        )
    }

    Column {
        Button(onClick = { /* Add dialog */ }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Connection")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(all) { conn -> ConnectionTypeCard(connection = conn) }
        }
    }
}

@Composable
private fun ConnectionTypeCard(connection: ConnectionType) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (connection.status) {
                ConnectionTypeStatus.CONNECTED -> MaterialTheme.colorScheme.primaryContainer
                ConnectionTypeStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    when (connection.status) {
                        ConnectionTypeStatus.CONNECTED -> Icons.Default.Link
                        ConnectionTypeStatus.ERROR -> Icons.Default.CloudOff
                        else -> Icons.Default.Cloud
                    },
                    contentDescription = null,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(text = connection.name, style = MaterialTheme.typography.titleMedium)
                    Text(text = "${connection.type.name} • ${connection.status.name}", style = MaterialTheme.typography.bodySmall)
                }
            }
            when (connection.status) {
                ConnectionTypeStatus.AVAILABLE -> Button(onClick = { }) { Text("Connect") }
                ConnectionTypeStatus.CONNECTED -> TextButton(onClick = { }) { Text("Disconnect") }
                else -> { }
            }
        }
    }
}
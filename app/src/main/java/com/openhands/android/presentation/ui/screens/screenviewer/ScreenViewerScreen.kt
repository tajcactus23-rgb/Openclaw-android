package com.openhands.android.presentation.ui.screens.screenviewer

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
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openhands.android.domain.model.ScreenShareRequest
import com.openhands.android.domain.model.ScreenShareStatus

@Composable
fun ScreenViewerScreen() {
    var mode by remember { mutableStateOf<ScreenViewerMode>(ScreenViewerMode.LIST) }
    var instanceId by remember { mutableStateOf("") }
    var authToken by remember { mutableStateOf("") }

    when (mode) {
        ScreenViewerMode.LIST -> InstanceListView(
            onRequestView = { mode = ScreenViewerMode.REQUEST }
        )
        ScreenViewerMode.REQUEST -> ScreenRequestView(
            instanceId = instanceId,
            onInstanceIdChange = { instanceId = it },
            authToken = authToken,
            onAuthTokenChange = { authToken = it },
            onRequest = {
                mode = ScreenViewerMode.VIEWING
            },
            onBack = { mode = ScreenViewerMode.LIST }
        )
        ScreenViewerMode.VIEWING -> ScreenViewingView(
            instanceId = instanceId,
            onStop = { mode = ScreenViewerMode.LIST }
        )
    }
}

@Composable
private fun InstanceListView(onRequestView: () -> Unit) {
    val instances = remember {
        listOf(
            InstanceInfo("i1", "Dev Agent", true),
            InstanceInfo("i2", "Test Agent", true),
            InstanceInfo("i3", "Prod Agent", false)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Screen Viewer",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "View live screen content from remote OpenHands instances via relay. Requires authorization from the instance owner.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                    Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = "How it works",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Screens are relayed via encrypted message passing. No direct VNC or screen sharing required.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Available Instances",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(instances) { instance ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (instance.allowsView)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
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
                                text = if (instance.allowsView) "Viewing allowed" else "Viewing not allowed",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (instance.allowsView) {
                            OutlinedButton(onClick = onRequestView) {
                                Icon(Icons.Default.Screenshot, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View Screen")
                            }
                        } else {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenRequestView(
    instanceId: String,
    onInstanceIdChange: (String) -> Unit,
    authToken: String,
    onAuthTokenChange: (String) -> Unit,
    onRequest: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Request Screen View",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = instanceId,
            onValueChange = onInstanceIdChange,
            label = { Text("Instance ID") },
            placeholder = { Text("Enter instance ID to view") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = authToken,
            onValueChange = onAuthTokenChange,
            label = { Text("Authorization Token") },
            placeholder = { Text("Optional: specific token from owner") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRequest,
            enabled = instanceId.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Visibility, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Request to View Screen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Screen viewing requires explicit authorization from the instance owner.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ScreenViewingView(
    instanceId: String,
    onStop: () -> Unit
) {
    var isAuthorized by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Viewing: $instanceId",
                style = MaterialTheme.typography.titleMedium
            )
            Button(onClick = onStop) {
                Text("Stop Viewing")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isAuthorized) {
            // Screen preview area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.DesktopWindows,
                            contentDescription = null,
                            modifier = Modifier.height(64.dp).width(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Screen Content",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Live relay from instance",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Waiting for authorization...")
                }
            }
        }
    }
}

private data class InstanceInfo(
    val id: String,
    val name: String,
    val allowsView: Boolean
)

private enum class ScreenViewerMode {
    LIST, REQUEST, VIEWING
}
package com.openhands.android.domain.model

// Swarm Mode - Multiple agents working on common goals
data class SwarmTask(
    val id: String,
    val title: String,
    val description: String,
    val targetInstanceCount: Int = 1,
    val status: SwarmTaskStatus = SwarmTaskStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val instanceIds: List<String> = emptyList(),
    val results: Map<String, String> = emptyMap()
)

enum class SwarmTaskStatus {
    PENDING, RUNNING, COMPLETED, FAILED, PARTIAL
}

data class SwarmInstance(
    val id: String,
    val name: String,
    val status: InstanceStatus = InstanceStatus.IDLE,
    val currentTaskId: String? = null,
    val progress: Int = 0,
    val lastHeartbeat: Long = System.currentTimeMillis()
)

enum class InstanceStatus {
    IDLE, BUSY, RUNNING, COMPLETED, ERROR, OFFLINE
}

// Connection Suite
data class ConnectionSuite(
    val id: String,
    val name: String,
    val connections: List<ConnectionProfile>,
    val isDefault: Boolean = false
)

data class ConnectionType(
    val id: String,
    val name: String,
    val type: ConnectionTypeEnum,
    val status: ConnectionTypeStatus = ConnectionTypeStatus.AVAILABLE
)

enum class ConnectionTypeEnum {
    OPENHANDS, OPENCLAW, CUSTOM, WEBHOOK
}

enum class ConnectionTypeStatus {
    AVAILABLE, CONNECTED, ERROR, DISABLED
}

// WebApp Preview
data class WebApp(
    val id: String,
    val name: String,
    val url: String,
    val iconUrl: String? = null,
    val category: String = "General"
)

// Terminal Session
data class TerminalSession(
    val id: String,
    val name: String,
    val instanceId: String? = null,
    val history: List<TerminalCommand> = emptyList(),
    val isActive: Boolean = false
)

data class TerminalCommand(
    val id: String,
    val command: String,
    val output: String,
    val exitCode: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// Screen Viewer - Relay based screen sharing
data class ScreenShareRequest(
    val id: String,
    val instanceId: String,
    val requesterId: String,
    val status: ScreenShareStatus = ScreenShareStatus.PENDING,
    val authorizedAt: Long? = null,
    val frameData: String? = null // Base64 encoded frames
)

enum class ScreenShareStatus {
    PENDING, AUTHORIZED, ACTIVE, DENIED, ENDED
}

// OpenClaw Connection (OpenHands compatible)
data class OpenClawProfile(
    val id: String,
    val name: String,
    val serverUrl: String,
    val apiKey: String,
    val isCompatible: Boolean = true
)
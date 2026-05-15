package com.openhands.android.domain.model

import java.util.UUID

enum class ProfileType {
    DIRECT,  // OpenHands Cloud
    RELAY    // Local/remote relay server
}

data class ConnectionProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val serverUrl: String,
    val apiKey: String,
    val profileType: ProfileType = ProfileType.DIRECT,
    val isDefault: Boolean = false
)

data class ConnectionStatus(
    val isConnected: Boolean,
    val serverUrl: String = "",
    val userEmail: String? = null,
    val userName: String? = null,
    val lastChecked: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

data class User(
    val id: String,
    val email: String,
    val name: String
)
// Session related models
data class Session(
    val id: String,
    val name: String,
    val status: String,
    val model: String? = null,
    val workspace: String? = null,
    val startedAt: String,
    val endedAt: String? = null,
    val lastActivity: String? = null
)

data class SessionMessage(
    val content: String,
    val attachments: List<String>? = null
)

data class SessionLogs(
    val timestamp: String,
    val level: String,
    val message: String,
    val source: String? = null
)

// Skill models
data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val trigger: String,
    val content: String,
    val source: String = "local"
)

// MCP tool models
data class MCPTool(
    val id: String,
    val name: String,
    val description: String,
    val server: String,
    val enabled: Boolean = true
)

// Capability models
data class Capability(
    val name: String,
    val status: String,
    val description: String,
    val backend: String? = null
)

enum class CapabilityStatus {
    AVAILABLE,
    ADAPTER_REQUIRED,
    LOCAL_ONLY,
    NOT_CONFIGURED
}

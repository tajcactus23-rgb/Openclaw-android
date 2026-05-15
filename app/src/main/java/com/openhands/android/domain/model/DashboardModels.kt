package com.openhands.android.domain.model

// Capability states for capability-first UI
enum class CapabilityState {
    SUPPORTED,
    PARTIALLY_SUPPORTED,
    UNSUPPORTED,
    ADAPTER_REQUIRED,
    UNAVAILABLE,
    DISCONNECTED
}

// SECTION 4: Dashboard domain models
data class RuntimeStatus(
    val isConnected: Boolean = false,
    val isRunning: Boolean = false,
    val serverUrl: String? = null,
    val activeSandboxes: Int? = null,
    val uptime: String? = null,
    val version: String? = null,
    val memoryUsage: Float? = null,
    val cpuUsage: Float? = null
)

data class AgentSession(
    val id: String,
    val name: String,
    val status: String, // "active", "running", "idle", "completed"
    val workspace: String? = null,
    val startedAt: String,
    val lastActivity: String? = null,
    val agentType: String? = null
)

data class TaskSummary(
    val id: String,
    val title: String,
    val description: String,
    val status: String, // "pending", "running", "completed", "failed"
    val createdAt: String,
    val completedAt: String? = null
)

data class LogEntry(
    val id: String,
    val timestamp: String,
    val level: String, // "ERROR", "WARN", "INFO", "DEBUG"
    val message: String,
    val source: String? = null
)

data class WorkspaceInfo(
    val id: String,
    val name: String,
    val path: String,
    val fileCount: Int = 0,
    val lastModified: String
)
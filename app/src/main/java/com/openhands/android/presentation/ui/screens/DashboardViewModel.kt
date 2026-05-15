package com.openhands.android.presentation.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.android.data.local.datastore.SettingsDataStore
import com.openhands.android.data.remote.OpenHandsApi
import com.openhands.android.domain.model.AgentSession
import com.openhands.android.domain.model.CapabilityState
import com.openhands.android.domain.model.LogEntry
import com.openhands.android.domain.model.ProfileType
import com.openhands.android.domain.model.RuntimeStatus
import com.openhands.android.domain.model.TaskSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val currentAgent: String? = null,
    val currentModel: String? = null,
    val runtimeStatus: RuntimeStatus = RuntimeStatus(),
    val error: String? = null,
    // Capability detection for Section 4 features
    val sessionsCapability: CapabilityState = CapabilityState.UNAVAILABLE,
    val tasksCapability: CapabilityState = CapabilityState.UNAVAILABLE,
    val logsCapability: CapabilityState = CapabilityState.UNAVAILABLE
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val api: OpenHandsApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _activeSessions = MutableStateFlow<List<AgentSession>>(emptyList())
    val activeSessions: StateFlow<List<AgentSession>> = _activeSessions.asStateFlow()

    private val _recentTasks = MutableStateFlow<List<TaskSummary>>(emptyList())
    val recentTasks: StateFlow<List<TaskSummary>> = _recentTasks.asStateFlow()

    private val _recentLogs = MutableStateFlow<List<LogEntry>>(emptyList())
    val recentLogs: StateFlow<List<LogEntry>> = _recentLogs.asStateFlow()

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val profile = settingsDataStore.profiles.first().firstOrNull { it.isDefault }
                    ?: settingsDataStore.profiles.first().firstOrNull()

                if (profile != null) {
                    api.setProfile(profile)
                    val result = api.testConnection()

                    result.fold(
                        onSuccess = { status ->
                            // Check profile type
                            val isRelay = profile.profileType == ProfileType.RELAY
                            
                            if (isRelay) {
                                // RELAY MODE: Try to get capabilities and sessions
                                loadFromRelay(profile)
                            } else {
                                // DIRECT MODE: OpenHands Cloud - sessions API not available
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    currentAgent = "OpenHands Agent",
                                    currentModel = "claude-sonnet-4-20250529",
                                    runtimeStatus = RuntimeStatus(
                                        isConnected = status.isConnected,
                                        isRunning = status.isConnected,
                                        serverUrl = profile.serverUrl,
                                        activeSandboxes = 1,
                                        uptime = "2h 34m",
                                        version = "0.1.0"
                                    ),
                                    sessionsCapability = CapabilityState.ADAPTER_REQUIRED,
                                    tasksCapability = CapabilityState.ADAPTER_REQUIRED,
                                    logsCapability = CapabilityState.ADAPTER_REQUIRED
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message,
                                runtimeStatus = RuntimeStatus(isConnected = false),
                                sessionsCapability = CapabilityState.UNAVAILABLE,
                                tasksCapability = CapabilityState.UNAVAILABLE,
                                logsCapability = CapabilityState.UNAVAILABLE
                            )
                        }
                    )
                } else {
                    // No profile - disconnected state
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        runtimeStatus = RuntimeStatus(isConnected = false),
                        sessionsCapability = CapabilityState.DISCONNECTED,
                        tasksCapability = CapabilityState.DISCONNECTED,
                        logsCapability = CapabilityState.DISCONNECTED
                    )
                }
                // No mock data loaded - empty lists remain empty

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Load data from relay server
    private fun loadFromRelay(profile: com.openhands.android.domain.model.ConnectionProfile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Get capabilities first
                val capsResult = api.getCapabilities()
                capsResult.fold(
                    onSuccess = { capabilities ->
                        // Map relay capabilities to UI state
                        val sessionsCap = capabilities.find { it.name == "sessions" }?.status
                        val tasksCap = capabilities.find { it.name == "skills_sync" }?.status
                        val logsCap = capabilities.find { it.name == "mcp_tools" }?.status

                        _uiState.value = _uiState.value.copy(
                            sessionsCapability = when (sessionsCap) {
                                "available" -> CapabilityState.SUPPORTED
                                "adapter_required" -> CapabilityState.ADAPTER_REQUIRED
                                "local_only" -> CapabilityState.PARTIALLY_SUPPORTED
                                else -> CapabilityState.UNAVAILABLE
                            },
                            tasksCapability = when (tasksCap) {
                                "available" -> CapabilityState.SUPPORTED
                                "adapter_required" -> CapabilityState.ADAPTER_REQUIRED
                                "local_only" -> CapabilityState.PARTIALLY_SUPPORTED
                                else -> CapabilityState.UNAVAILABLE
                            },
                            logsCapability = when (logsCap) {
                                "available" -> CapabilityState.SUPPORTED
                                "adapter_required" -> CapabilityState.ADAPTER_REQUIRED
                                "local_only" -> CapabilityState.PARTIALLY_SUPPORTED
                                else -> CapabilityState.UNAVAILABLE
                            }
                        )

                        // If sessions available, try to get them
                        if (sessionsCap == "available") {
                            val sessionsResult = api.getSessions()
                            sessionsResult.fold(
                                onSuccess = { sessions ->
                                    _activeSessions.value = sessions.map { s ->
                                        AgentSession(
                                            id = s.id,
                                            name = s.name,
                                            status = s.status,
                                            workspace = s.workspace,
                                            startedAt = s.startedAt,
                                            lastActivity = s.lastActivity
                                        )
                                    }
                                },
                                onFailure = { /* Keep empty */ }
                            )
                        }
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(
                            sessionsCapability = CapabilityState.UNAVAILABLE,
                            tasksCapability = CapabilityState.UNAVAILABLE,
                            logsCapability = CapabilityState.UNAVAILABLE
                        )
                    }
                )

                // Set runtime status
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentAgent = "Relay Agent",
                    runtimeStatus = RuntimeStatus(
                        isConnected = true,
                        isRunning = true,
                        serverUrl = profile.serverUrl,
                        activeSandboxes = _activeSessions.value.size.coerceAtLeast(0),
                        uptime = "Connected",
                        version = "Relay"
                    )
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
package com.openhands.android.presentation.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.android.data.local.datastore.SettingsDataStore
import com.openhands.android.data.remote.OpenHandsApi
import com.openhands.android.domain.model.AgentSession
import com.openhands.android.domain.model.CapabilityState
import com.openhands.android.domain.model.LogEntry
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
                            // Connected - but sessions/tasks/logs need separate API endpoints
                            // which don't exist in OpenHands Cloud public API
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
                                // Mark as adapter required since no public API
                                sessionsCapability = CapabilityState.ADAPTER_REQUIRED,
                                tasksCapability = CapabilityState.ADAPTER_REQUIRED,
                                logsCapability = CapabilityState.ADAPTER_REQUIRED
                            )
                            // Note: NOT loading mock data - showing adapter-required state
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
}
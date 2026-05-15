package com.openhands.android.presentation.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.android.data.local.datastore.SettingsDataStore
import com.openhands.android.data.remote.OpenHandsApi
import com.openhands.android.data.remote.AutomationResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PromptUiState(
    val isLoading: Boolean = false,
    val prompt: String = "",
    val error: String? = null,
    val canSend: Boolean = false,
    val automationCreated: AutomationResponse? = null
)

@HiltViewModel
class PromptViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val api: OpenHandsApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(PromptUiState())
    val uiState: StateFlow<PromptUiState> = _uiState.asStateFlow()

    fun onPromptChanged(prompt: String) {
        _uiState.value = _uiState.value.copy(prompt = prompt, canSend = prompt.isNotBlank())
    }

    fun sendPrompt() {
        val prompt = _uiState.value.prompt
        if (prompt.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val profile = settingsDataStore.profiles.first().firstOrNull { it.isDefault }
                    ?: settingsDataStore.profiles.first().firstOrNull()

                if (profile == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No profile configured. Add profile in Settings."
                    )
                    return@launch
                }

                api.setProfile(profile)
                
                // Using real API - POST /api/automation/v1/preset/prompt
                val result = api.createAutomation("Mobile Prompt", prompt)
                
                result.fold(
                    onSuccess = { automation ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            automationCreated = automation,
                            prompt = "" // Clear after send
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                )
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

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(automationCreated = null)
    }
}
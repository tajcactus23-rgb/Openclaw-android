package com.openhands.android.presentation.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.android.data.repository.ConnectionRepository
import com.openhands.android.domain.model.ConnectionProfile
import com.openhands.android.domain.model.ConnectionStatus
import com.openhands.android.domain.model.ProfileType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConnectionUiState(
    val isLoading: Boolean = false,
    val serverUrl: String = "https://app.all-hands.dev",
    val apiKey: String = "",
    val profileName: String = "",
    val profileType: ProfileType = ProfileType.DIRECT,  // NEW: relay support
    val status: ConnectionStatus? = null,
    val selectedProfile: ConnectionProfile? = null,
    val isTesting: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val repository: ConnectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()

    val profiles: StateFlow<List<ConnectionProfile>> = repository.profiles
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadActiveProfile()
    }

    private fun loadActiveProfile() {
        viewModelScope.launch {
            val profile = repository.getActiveProfile()
            if (profile != null) {
                _uiState.value = _uiState.value.copy(
                    serverUrl = profile.serverUrl,
                    apiKey = profile.apiKey,
                    profileName = profile.name,
                    selectedProfile = profile
                )
                testConnection()
            }
        }
    }

    fun updateServerUrl(url: String) {
        _uiState.value = _uiState.value.copy(serverUrl = url)
    }

    fun updateApiKey(key: String) {
        _uiState.value = _uiState.value.copy(apiKey = key)
    }

    fun updateProfileName(name: String) {
        _uiState.value = _uiState.value.copy(profileName = name)
    }

    fun updateProfileType(type: ProfileType) {
        _uiState.value = _uiState.value.copy(profileType = type)
    }

    fun testConnection() {
        val state = _uiState.value
        // For relay mode, allow empty API key
        if (state.serverUrl.isBlank()) {
            _uiState.value = state.copy(error = "Server URL is required")
            return
        }
        if (state.profileType == ProfileType.DIRECT && state.apiKey.isBlank()) {
            _uiState.value = state.copy(error = "API Key is required for Direct mode")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isTesting = true, error = null)

            val profile = ConnectionProfile(
                name = state.profileName.ifBlank { "Default" },
                serverUrl = state.serverUrl,
                apiKey = state.apiKey,
                profileType = state.profileType
            )

            val result = repository.connectProfile(profile)
            result.fold(
                onSuccess = { status ->
                    _uiState.value = _uiState.value.copy(
                        isTesting = false,
                        status = status,
                        selectedProfile = if (status.isConnected) profile else null,
                        error = if (!status.isConnected) status.errorMessage else null
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isTesting = false,
                        error = e.message ?: "Connection failed"
                    )
                }
            )
        }
    }

    fun saveProfile() {
        val state = _uiState.value
        if (state.serverUrl.isBlank() || state.apiKey.isBlank()) {
            _uiState.value = state.copy(error = "Server URL and API Key are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)

            try {
                val profile = ConnectionProfile(
                    name = state.profileName.ifBlank { "Default" },
                    serverUrl = state.serverUrl,
                    apiKey = state.apiKey,
                    isDefault = profiles.value.isEmpty()
                )
                repository.saveProfile(profile)
                repository.setActiveProfile(profile.id)

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    selectedProfile = profile
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save"
                )
            }
        }
    }

    fun selectProfile(profile: ConnectionProfile) {
        viewModelScope.launch {
            repository.setActiveProfile(profile.id)
            repository.connectProfile(profile)
            _uiState.value = _uiState.value.copy(
                serverUrl = profile.serverUrl,
                apiKey = profile.apiKey,
                profileName = profile.name,
                selectedProfile = profile
            )
            testConnection()
        }
    }

    fun deleteProfile(profile: ConnectionProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile.id)
            if (_uiState.value.selectedProfile?.id == profile.id) {
                _uiState.value = _uiState.value.copy(
                    serverUrl = "",
                    apiKey = "",
                    profileName = "",
                    selectedProfile = null
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
package com.openhands.android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.android.data.remote.QueueItem
import com.openhands.android.data.remote.RuntimeApi
import com.openhands.android.data.remote.RuntimeEvent
import com.openhands.android.data.remote.RuntimeSSEClient
import com.openhands.android.data.remote.QueueStatusResponse
import com.openhands.android.data.remote.RuntimeSessionResponse
import com.openhands.android.data.remote.StreamState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * Runtime monitor state.
 */
data class RuntimeState(
    val sessions: List<RuntimeSessionResponse> = emptyList(),
    val queueStatus: QueueStatusResponse? = null,
    val streamState: StreamState = StreamState.DISCONNECTED,
    val connectionQuality: Int = 0,
    val events: List<RuntimeEvent> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * RuntimeMonitor ViewModel with SSE streaming.
 */
@HiltViewModel
class RuntimeMonitorViewModel @Inject constructor(
    private val runtimeApi: RuntimeApi,
    private val okHttpClient: OkHttpClient,
    private val baseUrl: String
) : ViewModel() {
    
    private val _state = MutableStateFlow(RuntimeState())
    val state: StateFlow<RuntimeState> = _state.asStateFlow()
    
    private var sseClient: RuntimeSSEClient? = null
    
    init {
        // Start SSE connection
        connectStream()
    }
    
    /**
     * Connect to SSE stream.
     */
    fun connectStream() {
        if (sseClient == null) {
            sseClient = RuntimeSSEClient(okHttpClient, baseUrl)
        }
        
        // Observe stream state
        viewModelScope.launch {
            sseClient?.state?.collect { streamState ->
                _state.value = _state.value.copy(
                    streamState = streamState,
                    connectionQuality = sseClient?.getConnectionQuality() ?: 0
                )
            }
        }
        
        // Observe events
        viewModelScope.launch {
            sseClient?.events?.collect { events ->
                _state.value = _state.value.copy(events = events)
            }
        }
        
        // Observe queue status
        viewModelScope.launch {
            sseClient?.queueStatus?.collect { queueStatus ->
                // Convert RuntimeSSEClient.QueueStatus to QueueStatusResponse
                queueStatus?.let { qs ->
                    _state.value = _state.value.copy(
                        queueStatus = QueueStatusResponse(
                            queued = qs.queued,
                            items = qs.items.map { item ->
                                QueueItem(
                                    execution_id = item.executionId,
                                    workflow_id = item.workflowId,
                                    status = item.status
                                )
                            }
                        )
                    )
                }
            }
        }
        
        // Connect
        sseClient?.connect()
        
        // Initial poll for sessions and queue
        refresh()
    }
    
    /**
     * Disconnect from SSE stream.
     */
    fun disconnectStream() {
        sseClient?.disconnect()
        sseClient = null
    }
    
    /**
     * Refresh sessions and queue (polling fallback).
     */
    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            // Poll sessions
            val sessionsResult = runtimeApi.listSessions()
            sessionsResult.onSuccess { sessions ->
                _state.value = _state.value.copy(sessions = sessions)
            }
            
            // Poll queue status
            val queueResult = runtimeApi.getQueueStatus()
            queueResult.onSuccess { queue ->
                _state.value = _state.value.copy(queueStatus = queue)
            }
            
            _state.value = _state.value.copy(isLoading = false)
        }
    }
    
    /**
     * Create session.
     */
    fun createSession(runtimeType: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = runtimeApi.createSession(runtimeType)
            result.onFailure { error ->
                _state.value = _state.value.copy(error = error.message)
            }
            refresh()
        }
    }
    
    /**
     * End session.
     */
    fun endSession(sessionId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = runtimeApi.endSession(sessionId)
            result.onFailure { error ->
                _state.value = _state.value.copy(error = error.message)
            }
            refresh()
        }
    }
    
    /**
     * Queue execution.
     */
    fun queueExecution(workflowId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = runtimeApi.queueExecution(workflowId)
            result.onFailure { error ->
                _state.value = _state.value.copy(error = error.message)
            }
            refresh()
        }
    }
    
    /**
     * Clear error.
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        disconnectStream()
    }
}
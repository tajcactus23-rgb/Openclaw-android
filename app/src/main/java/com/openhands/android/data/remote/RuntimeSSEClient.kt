package com.openhands.android.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * SSE event types for runtime streaming.
 */
sealed class RuntimeEvent {
    data class SessionStart(val sessionId: String, val runtimeType: String) : RuntimeEvent()
    data class SessionEnd(val sessionId: String, val status: String) : RuntimeEvent()
    data class QueueChange(val queued: Int, val items: List<SSEQueueItem>) : RuntimeEvent()
    data class ExecutionLog(val executionId: String, val message: String) : RuntimeEvent()
    data class ExecutionComplete(val executionId: String, val status: String) : RuntimeEvent()
    data class Error(val message: String) : RuntimeEvent()
    object KeepAlive : RuntimeEvent()
    object Disconnected : RuntimeEvent()
    object Connecting : RuntimeEvent()
    object Reconnecting : RuntimeEvent()
}

data class SSEQueueItem(val executionId: String, val workflowId: String, val status: String)

/**
 * Connection state for SSE stream.
 */
enum class StreamState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ERROR
}

/**
 * Android SSE client for runtime events.
 * 
 * Features:
 * - Automatic reconnection with exponential backoff
 * - Event parsing from SSE format
 * - Lifecycle-safe stream handling
 * - Connection quality monitoring
 * 
 * Note: Shows "STREAM_ADAPTER_REQUIRED" when relay is unavailable.
 */
class RuntimeSSEClient(
    private val client: OkHttpClient,
    private val baseUrl: String
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    
    private val _events = MutableStateFlow<List<RuntimeEvent>>(emptyList())
    val events: StateFlow<List<RuntimeEvent>> = _events
    
    private val _state = MutableStateFlow(StreamState.DISCONNECTED)
    val state: StateFlow<StreamState> = _state
    
    private val _queueStatus = MutableStateFlow<QueueStatus?>(null)
    val queueStatus: StateFlow<QueueStatus?> = _queueStatus
    
    private val _sessions = MutableStateFlow<List<SessionInfo>>(emptyList())
    val sessions: StateFlow<List<SessionInfo>> = _sessions
    
    private var streamJob: Job? = null
    private var currentReader: BufferedReader? = null
    private var reconnectAttempts = 0
    
    companion object {
        private const val MAX_RECONNECT_ATTEMPTS = 5
        private const val INITIAL_RECONNECT_DELAY_MS = 1000L
        private const val MAX_RECONNECT_DELAY_MS = 30000L
    }
    
    data class QueueStatus(val queued: Int, val items: List<SSEQueueItem>)
    data class SessionInfo(val id: String, val runtimeType: String, val status: String)
    
    /**
     * Connect to SSE stream.
     */
    fun connect() {
        if (_state.value == StreamState.CONNECTING || _state.value == StreamState.CONNECTED) {
            return
        }
        
        _state.value = StreamState.CONNECTING
        streamJob = scope.launch {
            connectStream()
        }
    }
    
    /**
     * Disconnect from SSE stream.
     */
    fun disconnect() {
        streamJob?.cancel()
        streamJob = null
        currentReader?.close()
        currentReader = null
        _state.value = StreamState.DISCONNECTED
        addEvent(RuntimeEvent.Disconnected)
    }
    
    private suspend fun connectStream() {
        while (scope.isActive) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/api/v1/runtime/events")
                    .build()
                
                val call = client.newCall(request)
                val response = call.execute()
                
                if (!response.isSuccessful) {
                    handleError("HTTP ${response.code}")
                    return
                }
                
                val body = response.body?.byteStream() ?: throw Exception("No body")
                currentReader = BufferedReader(InputStreamReader(body))
                
                _state.value = StreamState.CONNECTED
                reconnectAttempts = 0
                addEvent(RuntimeEvent.Connecting)
                
                // Read SSE events
                readStream()
                
            } catch (e: Exception) {
                if (scope.isActive) {
                    handleError(e.message ?: "Connection failed")
                }
            }
            
            // Check if we should reconnect
            if (scope.isActive && _state.value != StreamState.DISCONNECTED) {
                if (!attemptReconnect()) {
                    break
                }
            }
        }
    }
    
    private suspend fun readStream() {
        val reader = currentReader ?: return
        var line: String?
        
        while (scope.isActive) {
            line = withContext(Dispatchers.IO) {
                try {
                    reader.readLine()
                } catch (e: Exception) {
                    null
                }
            }
            
            if (line == null) break
            
            // Parse SSE event
            if (line.startsWith("data: ")) {
                val data = line.removePrefix("data: ")
                parseEvent(data)
            }
        }
    }
    
    private fun parseEvent(data: String) {
        try {
            if (data == "{}" || data.isEmpty()) {
                addEvent(RuntimeEvent.KeepAlive)
                return
            }
            
            val json = parseJson(data)
            val type = json["type"] as? String ?: return
            
            when (type) {
                "keepalive" -> addEvent(RuntimeEvent.KeepAlive)
                "disconnect" -> {
                    addEvent(RuntimeEvent.Disconnected)
                    _state.value = StreamState.DISCONNECTED
                }
                "session_start" -> {
                    val sessionId = json["session_id"] as? String ?: return
                    val runtimeType = json["runtime_type"] as? String ?: return
                    addEvent(RuntimeEvent.SessionStart(sessionId, runtimeType))
                    updateSessions()
                }
                "session_end" -> {
                    val sessionId = json["session_id"] as? String ?: return
                    val status = json["status"] as? String ?: return
                    addEvent(RuntimeEvent.SessionEnd(sessionId, status))
                    updateSessions()
                }
                "queue_change" -> {
                    val queued = json["queued"] as? Int ?: 0
                    val items = (json["items"] as? List<*>)?.mapNotNull { item ->
                        val map = item as? Map<*, *>
                        if (map != null) {
                            SSEQueueItem(
                                map["execution_id"] as? String ?: "",
                                map["workflow_id"] as? String ?: "",
                                map["status"] as? String ?: ""
                            )
                        } else null
                    } ?: emptyList()
                    _queueStatus.value = QueueStatus(queued, items)
                    addEvent(RuntimeEvent.QueueChange(queued, items))
                }
                "execution_log" -> {
                    val executionId = json["execution_id"] as? String ?: ""
                    val message = json["message"] as? String ?: ""
                    addEvent(RuntimeEvent.ExecutionLog(executionId, message))
                }
                "execution_complete" -> {
                    val executionId = json["execution_id"] as? String ?: ""
                    val status = json["status"] as? String ?: ""
                    addEvent(RuntimeEvent.ExecutionComplete(executionId, status))
                }
                "error" -> {
                    val message = json["message"] as? String ?: "Unknown error"
                    addEvent(RuntimeEvent.Error(message))
                }
            }
        } catch (e: Exception) {
            // Invalid JSON - ignore
        }
    }
    
    private fun parseJson(json: String): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        // Simple JSON parser for SSE data
        // Format: {"type": "event", "data": "value"}
        var cleaned = json.trim()
        if (cleaned.startsWith("{") && cleaned.endsWith("}")) {
            cleaned = cleaned.removeSurrounding("{", "}")
            cleaned.split(",").forEach { pair ->
                val (key, value) = pair.split(":", limit = 2)
                val k = key.trim().removeSurrounding("\"", "\"")
                val v = value.trim().removeSurrounding("\"", "\"")
                result[k] = v
            }
        }
        return result
    }
    
    private fun handleError(message: String) {
        addEvent(RuntimeEvent.Error(message))
        _state.value = StreamState.ERROR
    }
    
    private suspend fun attemptReconnect(): Boolean {
        reconnectAttempts++
        
        if (reconnectAttempts > MAX_RECONNECT_ATTEMPTS) {
            _state.value = StreamState.ERROR
            return false
        }
        
        _state.value = StreamState.RECONNECTING
        addEvent(RuntimeEvent.Reconnecting)
        
        // Exponential backoff
        val delayMs = minOf(
            INITIAL_RECONNECT_DELAY_MS * (1 shl (reconnectAttempts - 1)),
            MAX_RECONNECT_DELAY_MS
        )
        delay(delayMs)
        
        return true
    }
    
    private fun addEvent(event: RuntimeEvent) {
        val current = _events.value.toMutableList()
        current.add(event)
        // Keep last 100 events
        if (current.size > 100) {
            current.removeAt(0)
        }
        _events.value = current
    }
    
    private fun updateSessions() {
        // Sessions are updated via SSE events - no need to poll separately
        // Just emit a refresh event if needed
    }
    
    /**
     * Get connection quality (0-100).
     */
    fun getConnectionQuality(): Int {
        return when (_state.value) {
            StreamState.CONNECTED -> 100 - (reconnectAttempts * 10).coerceAtMost(50)
            StreamState.RECONNECTING -> 50 - (reconnectAttempts * 10)
            else -> 0
        }
    }
}
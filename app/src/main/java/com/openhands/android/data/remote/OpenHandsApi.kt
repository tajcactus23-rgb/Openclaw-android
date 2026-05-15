package com.openhands.android.data.remote

import com.openhands.android.domain.model.ConnectionProfile
import com.openhands.android.domain.model.ConnectionStatus
import com.openhands.android.domain.model.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class OpenHandsApi @Inject constructor(
    private val client: OkHttpClient,
    private val moshi: Moshi
) {
    companion object {
        private const val DEFAULT_BASE_URL = "https://app.all-hands.dev"
    }
    
    private var currentProfile: ConnectionProfile? = null

    fun setProfile(profile: ConnectionProfile) {
        currentProfile = profile
    }

    fun getCurrentProfile(): ConnectionProfile? = currentProfile

    fun getBaseUrl(): String = currentProfile?.serverUrl ?: DEFAULT_BASE_URL

    private fun createRequestBuilder(): Request.Builder {
        val profile = currentProfile ?: throw IllegalStateException("No profile set")
        return Request.Builder()
            .addHeader("Authorization", "Bearer ${profile.apiKey}")
    }

    suspend fun testConnection(): Result<ConnectionStatus> = withContext(Dispatchers.IO) {
        try {
            val request = createRequestBuilder()
                .url("${getBaseUrl()}/api/v1/users/me")
                .get()
                .build()

            suspendCoroutine { continuation ->
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        continuation.resume(Result.failure(e))
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        if (response.isSuccessful) {
                            val body = response.body?.string()
                            val userResponse = body?.let {
                                try {
                                    moshi.adapter(UserResponse::class.java).fromJson(it)
                                } catch (e: Exception) { null }
                            }
                            continuation.resume(
                                Result.success(
                                    ConnectionStatus(
                                        isConnected = true,
                                        serverUrl = getBaseUrl(),
                                        userEmail = userResponse?.email,
                                        userName = userResponse?.name,
                                        lastChecked = System.currentTimeMillis()
                                    )
                                )
                            )
                        } else {
                            continuation.resume(
                                Result.success(
                                    ConnectionStatus(
                                        isConnected = false,
                                        serverUrl = getBaseUrl(),
                                        errorMessage = "HTTP ${response.code}",
                                        lastChecked = System.currentTimeMillis()
                                    )
                                )
                            )
                        }
                    }
                })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val request = createRequestBuilder()
                .url("${getBaseUrl()}/api/v1/users/me")
                .get()
                .build()

            suspendCoroutine { continuation ->
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        continuation.resume(Result.failure(e))
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        if (response.isSuccessful) {
                            val body = response.body?.string()
                            val userResponse = body?.let {
                                try {
                                    moshi.adapter(UserResponse::class.java).fromJson(it)
                                } catch (e: Exception) { null }
                            }
                            if (userResponse != null) {
                                continuation.resume(Result.success(User(userResponse.id, userResponse.email, userResponse.name)))
                            } else {
                                continuation.resume(Result.failure(Exception("Failed to parse user")))
                            }
                        } else {
                            continuation.resume(Result.failure(Exception("HTTP ${response.code}")))
                        }
                    }
                })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Create automation via preset/prompt - real API call
    suspend fun createAutomation(name: String, prompt: String): Result<AutomationResponse> = withContext(Dispatchers.IO) {
        try {
            val requestBody = AutomationRequest(name, prompt, mapOf("type" to "manual"))
            val json = moshi.adapter(AutomationRequest::class.java).toJson(requestBody)
            
            val request = createRequestBuilder()
                .url("${getBaseUrl()}/api/automation/v1/preset/prompt")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()
            
            suspendCoroutine { continuation ->
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        continuation.resume(Result.failure(e))
                    }
                    
                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        if (response.isSuccessful) {
                            val body = response.body?.string()
                            val autoResponse = body?.let {
                                try {
                                    moshi.adapter(AutomationResponse::class.java).fromJson(it)
                                } catch (e: Exception) { null }
                            }
                            if (autoResponse != null) {
                                continuation.resume(Result.success(autoResponse))
                            } else {
                                continuation.resume(Result.failure(Exception("Failed to parse response")))
                            }
                        } else {
                            continuation.resume(Result.failure(Exception("HTTP ${response.code}")))
                        }
                    }
                })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ===== Relay-specific API methods =====
    
    // Get capabilities from relay
    suspend fun getCapabilities(): Result<List<CapabilityResponse>> = withContext(Dispatchers.IO) {
        try {
            val request = createRequestBuilder()
                .url("${getBaseUrl()}/api/v1/capabilities")
                .get()
                .build()
            
            suspendCoroutine { continuation ->
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        continuation.resume(Result.failure(e))
                    }
                    
                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        if (response.isSuccessful) {
                            val body = response.body?.string()
                            val capabilities = body?.let {
                                try {
                                    moshi.adapter<List<CapabilityResponse>>(
                                        com.squareup.moshi.Types.newParameterizedType(List::class.java, CapabilityResponse::class.java)
                                    ).fromJson(it)
                                } catch (e: Exception) { null }
                            }
                            if (capabilities != null) {
                                continuation.resume(Result.success(capabilities))
                            } else {
                                continuation.resume(Result.failure(Exception("Failed to parse capabilities")))
                            }
                        } else {
                            continuation.resume(Result.failure(Exception("HTTP ${response.code}")))
                        }
                    }
                })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get sessions from relay
    suspend fun getSessions(): Result<List<SessionResponse>> = withContext(Dispatchers.IO) {
        try {
            val request = createRequestBuilder()
                .url("${getBaseUrl()}/api/v1/sessions")
                .get()
                .build()
            
            suspendCoroutine { continuation ->
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        continuation.resume(Result.failure(e))
                    }
                    
                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        if (response.isSuccessful) {
                            val body = response.body?.string()
                            val sessions = body?.let {
                                try {
                                    moshi.adapter<List<SessionResponse>>(
                                        com.squareup.moshi.Types.newParameterizedType(List::class.java, SessionResponse::class.java)
                                    ).fromJson(it)
                                } catch (e: Exception) { null }
                            }
                            if (sessions != null) {
                                continuation.resume(Result.success(sessions))
                            } else {
                                continuation.resume(Result.success(emptyList()))  // Empty is valid
                            }
                        } else {
                            continuation.resume(Result.failure(Exception("HTTP ${response.code}")))
                        }
                    }
                })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get session logs from relay
    suspend fun getSessionLogs(sessionId: String, limit: Int = 100): Result<List<LogEntryResponse>> = withContext(Dispatchers.IO) {
        try {
            val request = createRequestBuilder()
                .url("${getBaseUrl()}/api/v1/sessions/$sessionId/logs?limit=$limit")
                .get()
                .build()
            
            suspendCoroutine { continuation ->
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        continuation.resume(Result.failure(e))
                    }
                    
                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        if (response.isSuccessful) {
                            val body = response.body?.string()
                            val logs = body?.let {
                                try {
                                    moshi.adapter<List<LogEntryResponse>>(
                                        com.squareup.moshi.Types.newParameterizedType(List::class.java, LogEntryResponse::class.java)
                                    ).fromJson(it)
                                } catch (e: Exception) { null }
                            }
                            if (logs != null) {
                                continuation.resume(Result.success(logs))
                            } else {
                                continuation.resume(Result.success(emptyList()))
                            }
                        } else {
                            continuation.resume(Result.failure(Exception("HTTP ${response.code}")))
                        }
                    }
                })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get skills from relay
    suspend fun getSkills(): Result<List<SkillResponse>> = withContext(Dispatchers.IO) {
        try {
            val request = createRequestBuilder()
                .url("${getBaseUrl()}/api/v1/skills")
                .get()
                .build()
            
            suspendCoroutine { continuation ->
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        continuation.resume(Result.failure(e))
                    }
                    
                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        if (response.isSuccessful) {
                            val body = response.body?.string()
                            val skills = body?.let {
                                try {
                                    moshi.adapter<List<SkillResponse>>(
                                        com.squareup.moshi.Types.newParameterizedType(List::class.java, SkillResponse::class.java)
                                    ).fromJson(it)
                                } catch (e: Exception) { null }
                            }
                            if (skills != null) {
                                continuation.resume(Result.success(skills))
                            } else {
                                continuation.resume(Result.success(emptyList()))
                            }
                        } else {
                            continuation.resume(Result.failure(Exception("HTTP ${response.code}")))
                        }
                    }
                })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get MCP tools from relay
    suspend fun getMCPTools(): Result<List<MCPToolResponse>> = withContext(Dispatchers.IO) {
        try {
            val request = createRequestBuilder()
                .url("${getBaseUrl()}/api/v1/mcp/tools")
                .get()
                .build()
            
            suspendCoroutine { continuation ->
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        continuation.resume(Result.failure(e))
                    }
                    
                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        if (response.isSuccessful) {
                            val body = response.body?.string()
                            val tools = body?.let {
                                try {
                                    moshi.adapter<List<MCPToolResponse>>(
                                        com.squareup.moshi.Types.newParameterizedType(List::class.java, MCPToolResponse::class.java)
                                    ).fromJson(it)
                                } catch (e: Exception) { null }
                            }
                            if (tools != null) {
                                continuation.resume(Result.success(tools))
                            } else {
                                continuation.resume(Result.success(emptyList()))
                            }
                        } else {
                            continuation.resume(Result.failure(Exception("HTTP ${response.code}")))
                        }
                    }
                })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@JsonClass(generateAdapter = true)
data class UserResponse(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class AutomationRequest(
    @Json(name = "name") val name: String,
    @Json(name = "prompt") val prompt: String,
    @Json(name = "trigger") val trigger: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class AutomationResponse(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "created_at") val createdAt: String?
)

// Session models for relay
@JsonClass(generateAdapter = true)
data class SessionResponse(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "status") val status: String,
    @Json(name = "model") val model: String? = null,
    @Json(name = "workspace") val workspace: String? = null,
    @Json(name = "started_at") val startedAt: String,
    @Json(name = "ended_at") val endedAt: String? = null,
    @Json(name = "last_activity") val lastActivity: String? = null
)

// Skill model for relay
@JsonClass(generateAdapter = true)
data class SkillResponse(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "trigger") val trigger: String,
    @Json(name = "content") val content: String,
    @Json(name = "source") val source: String = "local"
)

// MCP tool model for relay
@JsonClass(generateAdapter = true)
data class MCPToolResponse(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "server") val server: String,
    @Json(name = "enabled") val enabled: Boolean = true
)

// Capability model for relay
@JsonClass(generateAdapter = true)
data class CapabilityResponse(
    @Json(name = "name") val name: String,
    @Json(name = "status") val status: String,
    @Json(name = "description") val description: String,
    @Json(name = "backend") val backend: String? = null
)

// Log entry for relay
@JsonClass(generateAdapter = true)
data class LogEntryResponse(
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "level") val level: String,
    @Json(name = "message") val message: String,
    @Json(name = "source") val source: String? = null
)
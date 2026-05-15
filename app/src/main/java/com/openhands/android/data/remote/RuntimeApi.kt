package com.openhands.android.data.remote

import com.squareup.moshi.JsonClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Runtime API client for session management and execution queue.
 * 
 * Endpoints:
 * - POST /api/v1/runtime/sessions - Create session
 * - GET /api/v1/runtime/sessions - List sessions  
 * - GET /api/v1/runtime/sessions/{id} - Get session
 * - POST /api/v1/runtime/sessions/{id}/end - End session
 * - GET /api/v1/runtime/queue - Get queue status
 * - POST /api/v1/runtime/queue - Queue execution
 * - POST /api/v1/runtime/queue/next - Get next from queue
 */

// DTOs
@JsonClass(generateAdapter = true)
data class CreateSessionRequest(val runtime_type: String)

@JsonClass(generateAdapter = true)
data class CreateSessionResponse(
    val session_id: String?,
    val runtime_type: String?,
    val status: String?,
    val error: String?,
    val message: String?
)

@JsonClass(generateAdapter = true)
data class RuntimeSessionResponse(
    val id: String,
    val runtime_type: String,
    val status: String,
    val created_at: String,
    val ended_at: String?
)

@JsonClass(generateAdapter = true)
data class EndSessionResponse(val session_id: String, val status: String)

@JsonClass(generateAdapter = true)
data class QueueStatusResponse(val queued: Int, val items: List<QueueItem>)

@JsonClass(generateAdapter = true)
data class QueueItem(val execution_id: String, val workflow_id: String, val status: String)

@JsonClass(generateAdapter = true)
data class QueueExecutionResponse(val execution_id: String, val status: String)

@JsonClass(generateAdapter = true)
data class QueueNextResponse(
    val execution_id: String?,
    val workflow_id: String?,
    val error: String?
)

// API Client
class RuntimeApi(
    private val client: OkHttpClient,
    private val moshi: com.squareup.moshi.Moshi,
    private val baseUrl: String
) {
    private val base = baseUrl.removeSuffix("/")
    
    private fun <T> fromJson(json: String, clazz: Class<T>): T? = 
        moshi.adapter(clazz).fromJson(json)
    
    /** Create session - returns ADAPTER_REQUIRED for terminal/browser */
    suspend fun createSession(runtimeType: String): Result<CreateSessionResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CreateSessionRequest(runtimeType)
            val json = moshi.adapter(CreateSessionRequest::class.java).toJson(request)
            val req = Request.Builder()
                .url("$base/api/v1/runtime/sessions")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: return@withContext Result.failure(Exception("Empty"))
                val result = fromJson(body, CreateSessionResponse::class.java)
                if (result != null) Result.success(result) else Result.failure(Exception("Parse"))
            } else Result.failure(Exception("HTTP ${resp.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }
    
    /** List sessions */
    suspend fun listSessions(status: String? = null): Result<List<RuntimeSessionResponse>> = withContext(Dispatchers.IO) {
        try {
            val url = if (status != null) "$base/api/v1/runtime/sessions?status=$status" else "$base/api/v1/runtime/sessions"
            val req = Request.Builder().url(url).get().build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: return@withContext Result.success(emptyList())
                val type = com.squareup.moshi.Types.newParameterizedType(List::class.java, RuntimeSessionResponse::class.java)
                val result = moshi.adapter<List<RuntimeSessionResponse>>(type).fromJson(body)
                Result.success(result ?: emptyList())
            } else Result.failure(Exception("HTTP ${resp.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }
    
    /** Get single session */
    suspend fun getSession(id: String): Result<RuntimeSessionResponse> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$base/api/v1/runtime/sessions/$id").get().build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: return@withContext Result.failure(Exception("Empty"))
                val result = fromJson(body, RuntimeSessionResponse::class.java)
                if (result != null) Result.success(result) else Result.failure(Exception("Parse"))
            } else Result.failure(Exception("HTTP ${resp.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }
    
    /** End session */
    suspend fun endSession(id: String, status: String = "terminated"): Result<EndSessionResponse> = withContext(Dispatchers.IO) {
        try {
            val json = """{"status":"$status"}"""
            val req = Request.Builder()
                .url("$base/api/v1/runtime/sessions/$id/end")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: return@withContext Result.failure(Exception("Empty"))
                val result = fromJson(body, EndSessionResponse::class.java)
                if (result != null) Result.success(result) else Result.failure(Exception("Parse"))
            } else Result.failure(Exception("HTTP ${resp.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }
    
    /** Get queue status */
    suspend fun getQueueStatus(): Result<QueueStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$base/api/v1/runtime/queue").get().build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: return@withContext Result.failure(Exception("Empty"))
                val result = fromJson(body, QueueStatusResponse::class.java)
                if (result != null) Result.success(result) else Result.failure(Exception("Parse"))
            } else Result.failure(Exception("HTTP ${resp.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }
    
    /** Queue execution */
    suspend fun queueExecution(workflowId: String): Result<QueueExecutionResponse> = withContext(Dispatchers.IO) {
        try {
            val json = """{"workflow_id":"$workflowId","nodes":[]}"""
            val req = Request.Builder()
                .url("$base/api/v1/runtime/queue")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: return@withContext Result.failure(Exception("Empty"))
                val result = fromJson(body, QueueExecutionResponse::class.java)
                if (result != null) Result.success(result) else Result.failure(Exception("Parse"))
            } else Result.failure(Exception("HTTP ${resp.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }
    
    /** Get next from queue */
    suspend fun getNext(): Result<QueueNextResponse> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("$base/api/v1/runtime/queue/next")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: return@withContext Result.failure(Exception("Empty"))
                val result = fromJson(body, QueueNextResponse::class.java)
                if (result != null) Result.success(result) else Result.failure(Exception("Parse"))
            } else Result.failure(Exception("HTTP ${resp.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
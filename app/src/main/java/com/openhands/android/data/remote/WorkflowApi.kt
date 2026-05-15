package com.openhands.android.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@JsonClass(generateAdapter = true)
data class WorkflowExecuteRequest(
    @Json(name = "workflow_id") val workflowId: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "nodes") val nodes: List<Map<String, Any>> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WorkflowExecutionResponse(
    @Json(name = "execution_id") val executionId: String,
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String? = null,
    @Json(name = "workflow_id") val workflowId: String,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class WorkflowExecutionStatus(
    @Json(name = "id") val id: String,
    @Json(name = "workflow_id") val workflowId: String,
    @Json(name = "name") val name: String,
    @Json(name = "status") val status: String,
    @Json(name = "current_node") val currentNode: String? = null,
    @Json(name = "progress") val progress: Double = 0.0,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "completed_at") val completedAt: String? = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "result") val result: Map<String, Any>? = null
)

class WorkflowApi(
    private val client: OkHttpClient,
    private val moshi: com.squareup.moshi.Moshi,
    private val baseUrl: String
) {
    private val base = baseUrl.removeSuffix("/")
    
    suspend fun executeWorkflow(request: WorkflowExecuteRequest): Result<WorkflowExecutionResponse> = withContext(Dispatchers.IO) {
        try {
            val json = moshi.adapter(WorkflowExecuteRequest::class.java).toJson(request)
            val body = json.toRequestBody("application/json".toMediaType())
            val req = Request.Builder().url("$base/api/v1/workflows/execute").post(body).build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val bodyStr = resp.body?.string()
                if (bodyStr != null) {
                    val result = moshi.adapter(WorkflowExecutionResponse::class.java).fromJson(bodyStr)
                    if (result != null) Result.success(result) else Result.failure(Exception("Parse error"))
                } else Result.failure(Exception("Empty response"))
            } else Result.failure(Exception("HTTP ${resp.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }
    
    suspend fun getWorkflowExecution(id: String): Result<WorkflowExecutionStatus> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$base/api/v1/workflows/executions/$id").get().build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val bodyStr = resp.body?.string()
                if (bodyStr != null) {
                    val result = moshi.adapter(WorkflowExecutionStatus::class.java).fromJson(bodyStr)
                    if (result != null) Result.success(result) else Result.failure(Exception("Parse error"))
                } else Result.failure(Exception("Empty response"))
            } else Result.failure(Exception("HTTP ${resp.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }
    
    suspend fun getWorkflowExecutionLogs(id: String): Result<List<LogEntryResponse>> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$base/api/v1/workflows/executions/$id/logs").get().build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val bodyStr = resp.body?.string()
                if (bodyStr != null) {
                    val type = moshi.adapter<List<LogEntryResponse>>(
                        com.squareup.moshi.Types.newParameterizedType(List::class.java, LogEntryResponse::class.java)
                    )
                    val result = type.fromJson(bodyStr)
                    if (result != null) Result.success(result) else Result.failure(Exception("Parse error"))
                } else Result.failure(Exception("Empty response"))
            } else Result.failure(Exception("HTTP ${resp.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }
    
    suspend fun listWorkflowExecutions(): Result<List<WorkflowExecutionStatus>> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$base/api/v1/workflows/executions").get().build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val bodyStr = resp.body?.string()
                if (bodyStr != null) {
                    val type = moshi.adapter<List<WorkflowExecutionStatus>>(
                        com.squareup.moshi.Types.newParameterizedType(List::class.java, WorkflowExecutionStatus::class.java)
                    )
                    val result = type.fromJson(bodyStr)
                    if (result != null) Result.success(result) else Result.failure(Exception("Parse error"))
                } else Result.failure(Exception("Empty response"))
            } else Result.failure(Exception("HTTP ${resp.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
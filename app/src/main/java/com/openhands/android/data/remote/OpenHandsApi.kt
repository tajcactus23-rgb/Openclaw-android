package com.openhands.android.data.remote

import com.openhands.android.domain.model.ConnectionProfile
import com.openhands.android.domain.model.ConnectionStatus
import com.openhands.android.domain.model.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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
}

@JsonClass(generateAdapter = true)
data class UserResponse(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String,
    @Json(name = "name") val name: String
)
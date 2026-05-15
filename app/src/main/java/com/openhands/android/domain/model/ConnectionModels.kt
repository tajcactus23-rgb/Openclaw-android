package com.openhands.android.domain.model

import java.util.UUID

data class ConnectionProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val serverUrl: String,
    val apiKey: String,
    val isDefault: Boolean = false
)

data class ConnectionStatus(
    val isConnected: Boolean,
    val serverUrl: String = "",
    val userEmail: String? = null,
    val userName: String? = null,
    val lastChecked: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

data class User(
    val id: String,
    val email: String,
    val name: String
)
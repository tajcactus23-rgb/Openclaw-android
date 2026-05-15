package com.openhands.android

import org.junit.Test
import org.junit.Assert.*
import com.openhands.android.domain.model.ConnectionProfile
import com.openhands.android.domain.model.ConnectionStatus

class ConnectionModelsTest {

    @Test
    fun testConnectionProfile_creation() {
        val profile = ConnectionProfile(
            name = "TestProfile",
            serverUrl = "https://app.all-hands.dev",
            apiKey = "test-key-123"
        )
        
        assertEquals("TestProfile", profile.name)
        assertEquals("https://app.all-hands.dev", profile.serverUrl)
        assertEquals("test-key-123", profile.apiKey)
    }

    @Test
    fun testConnectionProfile_hasId() {
        val profile = ConnectionProfile(
            name = "Default",
            serverUrl = "https://example.com",
            apiKey = "key"
        )
        
        assertTrue(profile.id.isNotBlank()) // UUID generated
    }

    @Test
    fun testConnectionStatus_connected() {
        val status = ConnectionStatus(
            isConnected = true,
            serverUrl = "https://app.all-hands.dev",
            userEmail = "test@example.com",
            userName = "Test User",
            lastChecked = System.currentTimeMillis()
        )
        
        assertTrue(status.isConnected)
        assertEquals("test@example.com", status.userEmail)
    }

    @Test
    fun testConnectionStatus_disconnected() {
        val status = ConnectionStatus(
            isConnected = false,
            serverUrl = "https://app.all-hands.dev",
            errorMessage = "HTTP 401",
            lastChecked = System.currentTimeMillis()
        )
        
        assertFalse(status.isConnected)
        assertEquals("HTTP 401", status.errorMessage)
    }
}
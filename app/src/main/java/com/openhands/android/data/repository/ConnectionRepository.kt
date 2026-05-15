package com.openhands.android.data.repository

import com.openhands.android.data.local.datastore.SettingsDataStore
import com.openhands.android.data.remote.OpenHandsApi
import com.openhands.android.domain.model.ConnectionProfile
import com.openhands.android.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionRepository @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val api: OpenHandsApi
) {
    val profiles: Flow<List<ConnectionProfile>> = settingsDataStore.profiles

    suspend fun saveProfile(profile: ConnectionProfile) {
        settingsDataStore.saveProfile(profile)
    }

    suspend fun deleteProfile(profileId: String) {
        settingsDataStore.deleteProfile(profileId)
    }

    suspend fun setActiveProfile(profileId: String) {
        settingsDataStore.setActiveProfile(profileId)
    }

    val activeProfile: Flow<ConnectionProfile?> = settingsDataStore.profiles.combine(settingsDataStore.activeProfileId) { profiles, activeId ->
        profiles.find { it.id == activeId } ?: profiles.firstOrNull { it.isDefault }
    }

    suspend fun connectProfile(profile: ConnectionProfile): Result<ConnectionStatus> {
        api.setProfile(profile)
        return api.testConnection()
    }

    suspend fun testCurrentConnection(): Result<ConnectionStatus> {
        return api.testConnection()
    }

    suspend fun getActiveProfile(): ConnectionProfile? {
        return settingsDataStore.profiles.first()
            .let { profiles ->
                val activeId = settingsDataStore.activeProfileId.first()
                profiles.find { it.id == activeId } ?: profiles.firstOrNull { it.isDefault }
            }
    }
}
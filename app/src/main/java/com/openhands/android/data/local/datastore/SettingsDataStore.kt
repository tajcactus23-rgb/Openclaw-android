package com.openhands.android.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.openhands.android.domain.model.ConnectionProfile
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    private val context: Context,
    private val moshi: Moshi
) {
    companion object {
        private val PROFILES_KEY = stringPreferencesKey("connection_profiles")
        private val ACTIVE_PROFILE_ID_KEY = stringPreferencesKey("active_profile_id")
        private val SELECTED_MODEL_KEY = stringPreferencesKey("selected_model")
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    }

    private val profileListType = Types.newParameterizedType(List::class.java, ConnectionProfile::class.java)
    private val profileAdapter = moshi.adapter<List<ConnectionProfile>>(profileListType)

    val profiles: Flow<List<ConnectionProfile>> = context.dataStore.data.map { prefs ->
        val json = prefs[PROFILES_KEY] ?: "[]"
        profileAdapter.fromJson(json) ?: emptyList()
    }

    val activeProfileId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[ACTIVE_PROFILE_ID_KEY]
    }

    suspend fun saveProfile(profile: ConnectionProfile) {
        context.dataStore.edit { prefs ->
            val current = prefs[PROFILES_KEY] ?: "[]"
            val list = profileAdapter.fromJson(current)?.toMutableList() ?: mutableListOf()
            
            val existing = list.indexOfFirst { it.id == profile.id }
            if (existing >= 0) {
                list[existing] = profile
            } else {
                list.add(profile)
            }
            
            // If this is default, clear other defaults
            if (profile.isDefault) {
                list.forEachIndexed { index, p ->
                    if (p.id != profile.id && p.isDefault) {
                        list[index] = p.copy(isDefault = false)
                    }
                }
            }
            
            prefs[PROFILES_KEY] = profileAdapter.toJson(list)
        }
    }

    suspend fun deleteProfile(profileId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[PROFILES_KEY] ?: "[]"
            val list = profileAdapter.fromJson(current)?.toMutableList() ?: mutableListOf()
            list.removeAll { it.id == profileId }
            prefs[PROFILES_KEY] = profileAdapter.toJson(list)
            
            if (prefs[ACTIVE_PROFILE_ID_KEY] == profileId) {
                prefs.remove(ACTIVE_PROFILE_ID_KEY)
            }
        }
    }

    suspend fun setActiveProfile(profileId: String) {
        context.dataStore.edit { prefs ->
            prefs[ACTIVE_PROFILE_ID_KEY] = profileId
        }
    }

    val darkTheme: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_THEME_KEY] ?: false
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_THEME_KEY] = enabled
        }
    }

    val selectedModel: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_MODEL_KEY] ?: "anthropic/claude-sonnet-4-20250529"
    }

    suspend fun setSelectedModel(model: String) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_MODEL_KEY] = model
        }
    }
}
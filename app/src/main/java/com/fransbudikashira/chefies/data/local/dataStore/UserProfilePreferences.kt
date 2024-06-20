package com.fransbudikashira.chefies.data.local.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userProfileDataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class UserProfilePreferences private constructor(
    private val userProfileDataStore: DataStore<Preferences>
) {
    private val usernamePreferences = stringPreferencesKey("username")
    private val avatarPreferences = stringPreferencesKey("avatar")
    private val themePreferences = booleanPreferencesKey("theme_settings")

    fun getUsername(): Flow<String> {
        return userProfileDataStore.data.map { preferences ->
            preferences[usernamePreferences] ?: ""
        }
    }

    fun getAvatar(): Flow<String> {
        return userProfileDataStore.data.map { preferences ->
            preferences[avatarPreferences] ?: ""
        }
    }

    fun getThemeSetting(): Flow<Boolean> {
        return userProfileDataStore.data.map { preferences ->
            preferences[themePreferences] ?: false
        }
    }

    suspend fun saveUsername(username: String) {
        userProfileDataStore.edit { preferences ->
            preferences[usernamePreferences] = username
        }
    }

    suspend fun saveAvatar(avatar: String) {
        userProfileDataStore.edit { preferences ->
            preferences[avatarPreferences] = avatar
        }
    }

    suspend fun saveThemeSetting(isDarkModeActive: Boolean) {
        userProfileDataStore.edit { preferences ->
            preferences[themePreferences] = isDarkModeActive
        }
    }

    companion object {
        @Volatile
        private var instance: UserProfilePreferences? = null
        fun getInstance(
            dataStore: DataStore<Preferences>
        ): UserProfilePreferences =
            instance ?: synchronized(this) {
                instance ?: UserProfilePreferences(dataStore)
            }.also { instance = it }
    }
}
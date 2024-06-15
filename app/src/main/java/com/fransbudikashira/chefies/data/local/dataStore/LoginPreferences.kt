package com.fransbudikashira.chefies.data.local.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.loginDataStore: DataStore<Preferences> by preferencesDataStore(name = "login")

class LoginPreferences private constructor(
    private val loginDataStore: DataStore<Preferences>
) {
    private val emailPreferences = stringPreferencesKey("email")
    private val passwordPreferences = stringPreferencesKey("password")

    fun getEmail(): Flow<String> {
        return loginDataStore.data.map { preferences ->
            preferences[emailPreferences] ?: ""
        }
    }

    fun getPassword(): Flow<String> {
        return loginDataStore.data.map { preferences ->
            preferences[passwordPreferences] ?: ""
        }
    }

    suspend fun saveEmail(email: String) {
        loginDataStore.edit { preferences ->
            preferences[emailPreferences] = email
        }
    }

    suspend fun savePassword(password: String) {
        loginDataStore.edit { preferences ->
            preferences[passwordPreferences] = password
        }
    }

    companion object {
        @Volatile
        private var instance: LoginPreferences? = null
        fun getInstance(
            dataStore: DataStore<Preferences>
        ): LoginPreferences =
            instance ?: synchronized(this) {
                instance ?: LoginPreferences(dataStore)
            }.also { instance = it }
    }
}
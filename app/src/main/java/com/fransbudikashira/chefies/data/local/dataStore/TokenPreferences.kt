package com.fransbudikashira.chefies.data.local.dataStore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")

class TokenPreferences private constructor(
    private val datastore: DataStore<Preferences>
) {
    private val tokenPreferences = stringPreferencesKey("token")

    fun getToken(): Flow<String> {
        return datastore.data.map { preferences ->
            preferences[tokenPreferences] ?: ""
        }
    }

    suspend fun saveToken(token: String) {
        datastore.edit { preferences ->
            preferences[tokenPreferences] = token
        }
    }

    suspend fun deleteToken() {
        datastore.edit { preferences ->
            preferences.remove(tokenPreferences)
        }
    }

    companion object {
        @Volatile
        private var instance: TokenPreferences? = null
        fun getInstance(
            dataStore: DataStore<Preferences>
        ): TokenPreferences =
            instance ?: synchronized(this) {
                instance ?: TokenPreferences(dataStore)
            }.also { instance = it }

        const val TAG = "TokenPreferences"
    }
}
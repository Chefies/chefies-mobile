package com.fransbudikashira.chefies.di

import android.content.Context
import com.fransbudikashira.chefies.data.local.dataStore.TokenPreferences
import com.fransbudikashira.chefies.data.local.dataStore.dataStore
import com.fransbudikashira.chefies.data.local.room.RecipeDao
import com.fransbudikashira.chefies.data.remote.retrofit.ApiConfig
import com.fransbudikashira.chefies.data.repository.RecipeRepository
import com.fransbudikashira.chefies.data.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {

    fun provideUserRepository(context: Context): UserRepository {
        val tokenPreferences = TokenPreferences.getInstance(context.dataStore)
        val user = runBlocking { tokenPreferences.getToken().first() }
        val apiService = ApiConfig.getApiService(user)

        return UserRepository.getInstance(apiService, tokenPreferences)
    }
}
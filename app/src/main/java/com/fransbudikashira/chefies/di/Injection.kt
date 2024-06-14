package com.fransbudikashira.chefies.di

import android.content.Context
import com.fransbudikashira.chefies.data.local.dataStore.TokenPreferences
import com.fransbudikashira.chefies.data.local.dataStore.dataStore
import com.fransbudikashira.chefies.data.remote.retrofit.ApiConfig
import com.fransbudikashira.chefies.data.repository.MainRepository
import com.fransbudikashira.chefies.data.repository.UserRepository
object Injection {

    fun provideUserRepository(context: Context): UserRepository {
        val tokenPreferences = TokenPreferences.getInstance(context.dataStore)
//        val user = runBlocking { tokenPreferences.getToken().first() }
        val apiService = ApiConfig.getApiService(context)
        return UserRepository.getInstance(apiService, tokenPreferences)
    }

    fun provideMainRepository(context: Context): MainRepository {
        val tokenPreferences = TokenPreferences.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService(context)
        return MainRepository.getInstance(apiService, tokenPreferences)
    }
}
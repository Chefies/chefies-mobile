package com.fransbudikashira.chefies.di

import android.content.Context
import com.fransbudikashira.chefies.data.local.dataStore.LoginPreferences
import com.fransbudikashira.chefies.data.local.dataStore.TokenPreferences
import com.fransbudikashira.chefies.data.local.dataStore.UserProfilePreferences
import com.fransbudikashira.chefies.data.local.dataStore.dataStore
import com.fransbudikashira.chefies.data.local.dataStore.loginDataStore
import com.fransbudikashira.chefies.data.local.dataStore.userProfileDataStore
import com.fransbudikashira.chefies.data.local.room.ChefiesDatabase
import com.fransbudikashira.chefies.data.remote.retrofit.ApiConfig
import com.fransbudikashira.chefies.data.repository.MainRepository
import com.fransbudikashira.chefies.data.repository.RecipeRepository
import com.fransbudikashira.chefies.data.repository.UserRepository
object Injection {

    fun provideUserRepository(context: Context): UserRepository {
        val tokenPreferences = TokenPreferences.getInstance(context.dataStore)
        val loginPreferences = LoginPreferences.getInstance(context.loginDataStore)
        val userProfilePreferences = UserProfilePreferences.getInstance(context.userProfileDataStore)
        val apiService = ApiConfig.getApiService(context)

        return UserRepository.getInstance(apiService, tokenPreferences, loginPreferences, userProfilePreferences)
    }

    fun provideMainRepository(context: Context): MainRepository {
        val apiService = ApiConfig.getApiService(context)
        return MainRepository.getInstance(apiService)
    }

    fun provideRecipeRepository(context: Context): RecipeRepository {
        val database = ChefiesDatabase.getDatabase(context)
        return RecipeRepository.getInstance(database.recipeDao(), database.historyDao())
    }
}
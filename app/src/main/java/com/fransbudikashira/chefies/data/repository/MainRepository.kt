package com.fransbudikashira.chefies.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.fransbudikashira.chefies.data.remote.request.IngredientsRequest
import com.fransbudikashira.chefies.data.remote.response.RecipeResponse
import com.fransbudikashira.chefies.data.remote.response.RegisterResponse
import com.fransbudikashira.chefies.data.remote.retrofit.ApiService
import com.fransbudikashira.chefies.helper.Result
import com.google.gson.Gson

class MainRepository(
    private val apiService: ApiService
) {

    fun getRecipes(ingredients: List<String>): LiveData<Result<RecipeResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getRecipes(IngredientsRequest(ingredients))
            if (response.isSuccessful) {
                if (response.body() != null) {
                    emit(Result.Success(response.body()!!))
                    Log.d(TAG, "Get Recipes Response: ${response.body()}")
                }
                if (response.body()!!.recipes.isEmpty()) {
                    emit(Result.Error("Recipe not available for this ingredients"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, RegisterResponse::class.java)
                Log.e(TAG, "Get Recipes Error: ${errorResponse.detail}")
                emit(Result.Error(errorResponse.detail))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get Recipes Exception: ${e.message}")
            emit(Result.Error(e.message ?: "Error, something went wrong"))
        }
    }

    companion object {
        @Volatile
        private var instance: MainRepository? = null
        fun getInstance(
            apiService: ApiService
        ): MainRepository =
            instance ?: synchronized(this) {
                instance ?: MainRepository(apiService)
            }.also { instance = it }

        private const val TAG = "MainRepository"
    }
}
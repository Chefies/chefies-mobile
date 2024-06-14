package com.fransbudikashira.chefies.data.remote.retrofit

import com.fransbudikashira.chefies.data.remote.request.IngredientsRequest
import com.fransbudikashira.chefies.data.remote.request.LoginRequest
import com.fransbudikashira.chefies.data.remote.request.RegisterRequest
import com.fransbudikashira.chefies.data.remote.response.GetSuggestionResponse
import com.fransbudikashira.chefies.data.remote.response.LoginResponse
import com.fransbudikashira.chefies.data.remote.response.RecipeResponse
import com.fransbudikashira.chefies.data.remote.response.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    @POST("auth/register")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @POST("generate/recipes")
    suspend fun getRecipes(
        @Body ingredientsRequest: IngredientsRequest
    ): Response<RecipeResponse>
}
package com.fransbudikashira.chefies.data.remote.retrofit

import com.fransbudikashira.chefies.data.remote.request.IngredientsRequest
import com.fransbudikashira.chefies.data.remote.request.LoginRequest
import com.fransbudikashira.chefies.data.remote.request.PasswordUpdateRequest
import com.fransbudikashira.chefies.data.remote.request.RegisterRequest
import com.fransbudikashira.chefies.data.remote.response.GetProfileResponse
import com.fransbudikashira.chefies.data.remote.response.LoginResponse
import com.fransbudikashira.chefies.data.remote.response.RecipeResponse
import com.fransbudikashira.chefies.data.remote.response.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface ApiService {
    @POST("auth/register")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @GET("profile/")
    suspend fun getProfile(): Response<GetProfileResponse>

    @Multipart
    @PUT("profile/")
    suspend fun updateProfile(
        @Part("name") name: RequestBody,
        @Part avatar: MultipartBody.Part
    ): Response<RegisterResponse>

    @PATCH("profile/password")
    suspend fun updatePassword(
        @Body passwordUpdateRequest: PasswordUpdateRequest
    ): Response<RegisterResponse>

    @POST("generate/recipes")
    suspend fun getRecipes(
        @Body ingredientsRequest: IngredientsRequest
    ): Response<RecipeResponse>
}
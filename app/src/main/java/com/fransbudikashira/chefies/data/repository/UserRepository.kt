package com.fransbudikashira.chefies.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.fransbudikashira.chefies.data.local.dataStore.LoginPreferences
import com.fransbudikashira.chefies.data.local.dataStore.TokenPreferences
import com.fransbudikashira.chefies.data.local.dataStore.UserProfilePreferences
import com.fransbudikashira.chefies.data.remote.request.LoginRequest
import com.fransbudikashira.chefies.data.remote.request.PasswordUpdateRequest
import com.fransbudikashira.chefies.data.remote.request.RegisterRequest
import com.fransbudikashira.chefies.data.remote.response.GetProfileResponse
import com.fransbudikashira.chefies.data.remote.response.LoginResponse
import com.fransbudikashira.chefies.data.remote.response.RegisterResponse
import com.fransbudikashira.chefies.data.remote.retrofit.ApiService
import com.fransbudikashira.chefies.helper.Result
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UserRepository(
    private val apiService: ApiService,
    private val tokenPreferences: TokenPreferences,
    private val loginPreferences: LoginPreferences,
    private val userProfilePreferences: UserProfilePreferences
) {

    fun userRegister(
        name: String,
        email: String,
        password: String
    ): LiveData<Result<RegisterResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.register(RegisterRequest(name, email, password))
            if (response.isSuccessful) {
                emit(Result.Success(response.body()!!))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, RegisterResponse::class.java)
                Log.d(TAG, "userRegister: ${errorResponse.detail}")
                emit(Result.Error(errorResponse.detail))
            }
        } catch (e: Exception) {
            Log.d(TAG, "userRegister: ${e.message}")
            emit(Result.Error(e.message.toString()))
        }
    }

    fun userLogin(
        email: String,
        password: String
    ): LiveData<Result<LoginResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { data ->
                    tokenPreferences.saveToken(data.token)
                    loginPreferences.saveEmail(email)
                    loginPreferences.savePassword(password)
                    emit(Result.Success(data))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, RegisterResponse::class.java)
                Log.d(TAG, "userLogin: ${errorResponse.detail}")
                emit(Result.Error(errorResponse.detail))
            }
        } catch (e: Exception) {
            Log.d(TAG, "userLogin: ${e.message}")
            emit(Result.Error(e.message.toString()))
        }
    }

    fun getProfile(): LiveData<Result<GetProfileResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getProfile()
            if (response.isSuccessful) {
                response.body()?.let { data ->
                    userProfilePreferences.saveUsername(data.name)
                    userProfilePreferences.saveAvatar(data.avatar)
                    emit(Result.Success(data))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, RegisterResponse::class.java)
                Log.d(TAG, "getProfile: ${errorResponse.detail}")
                emit(Result.Error(errorResponse.detail))
            }
        } catch (e: Exception) {
            Log.d(TAG, "getProfile: ${e.message}")
            emit(Result.Error(e.message.toString()))
        }
    }

    fun updateProfile(name: String, avatarFile: File): LiveData<Result<RegisterResponse>> =
        liveData {
            emit(Result.Loading)
            val nameRequestBody = name.toRequestBody("text/plain".toMediaType())
            val avatarRequestBody = avatarFile.asRequestBody("image/*".toMediaType())
            val avatarPart = MultipartBody.Part.createFormData(
                "avatar",
                avatarFile.name,
                avatarRequestBody
            )
            try {
                val response = apiService.updateProfile(nameRequestBody, avatarPart)
                if (response.isSuccessful) {
//                    userProfilePreferences.saveUsername(name)
//                    userProfilePreferences.saveAvatar(avatarFile.toString())
                    emit(Result.Success(response.body()!!))
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, RegisterResponse::class.java)
                    Log.d(TAG, "updateProfile: ${errorResponse.detail}")
                    emit(Result.Error(errorResponse.detail))
                }
            } catch (e: Exception) {
                Log.d(TAG, "updateProfile: ${e.message}")
                emit(Result.Error(e.message.toString()))
            }
        }

    fun updatePassword(
        newPassword: String,
        oldPassword: String
    ): LiveData<Result<RegisterResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response =
                apiService.updatePassword(PasswordUpdateRequest(newPassword, oldPassword))
            if (response.isSuccessful) {
                loginPreferences.savePassword(newPassword)
                emit(Result.Success(response.body()!!))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, RegisterResponse::class.java)
                Log.d(TAG, "updatePassword: ${errorResponse.detail}")
                emit(Result.Error(errorResponse.detail))
            }
        } catch (e: Exception) {
            Log.d(TAG, "updatePassword: ${e.message}")
            emit(Result.Error(e.message.toString()))
        }
    }

    fun getToken(): String = runBlocking { tokenPreferences.getToken().first() }

    fun deleteToken() = CoroutineScope(Dispatchers.IO).launch {
        tokenPreferences.deleteToken()
    }

    fun getEmail(): String = runBlocking { loginPreferences.getEmail().first() }

    fun getPassword(): String = runBlocking { loginPreferences.getPassword().first() }

    fun getUsername(): String = runBlocking { userProfilePreferences.getUsername().first() }

    fun getAvatar(): String = runBlocking { userProfilePreferences.getAvatar().first() }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            apiService: ApiService,
            tokenPreferences: TokenPreferences,
            loginPreferences: LoginPreferences,
            userProfilePreferences: UserProfilePreferences
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(
                    apiService,
                    tokenPreferences,
                    loginPreferences,
                    userProfilePreferences
                )
            }.also { instance = it }

        private const val TAG = "UserRepository"
    }
}
package com.fransbudikashira.chefies.ui.splash

import androidx.lifecycle.ViewModel
import com.fransbudikashira.chefies.data.repository.UserRepository

class SplashViewModel(private val userRepository: UserRepository): ViewModel() {

    fun getToken() = userRepository.getToken()

    fun getProfile() = userRepository.getProfile()

    fun getEmail() = userRepository.getEmail()

    fun getPassword() = userRepository.getPassword()

    fun userLogin(
        email: String,
        password: String
    ) = userRepository.userLogin(email, password)
}
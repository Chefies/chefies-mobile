package com.fransbudikashira.chefies.ui.main

import androidx.lifecycle.ViewModel
import com.fransbudikashira.chefies.data.repository.UserRepository

class MainViewModel(private val userRepository: UserRepository): ViewModel() {

    fun deleteToken() = userRepository.deleteToken()

    fun getProfile() = userRepository.getProfile()

    fun getEmail() = userRepository.getEmail()

    fun getPassword() = userRepository.getPassword()

    fun userLogin(
        email: String,
        password: String
    ) = userRepository.userLogin(email, password)
}
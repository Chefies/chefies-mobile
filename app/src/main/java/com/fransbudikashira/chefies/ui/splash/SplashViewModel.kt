package com.fransbudikashira.chefies.ui.splash

import androidx.lifecycle.ViewModel
import com.fransbudikashira.chefies.data.repository.UserRepository

class SplashViewModel(private val userRepository: UserRepository): ViewModel() {

    fun getToken() = userRepository.getToken()
}
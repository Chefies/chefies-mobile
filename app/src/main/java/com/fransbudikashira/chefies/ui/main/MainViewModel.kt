package com.fransbudikashira.chefies.ui.main

import androidx.lifecycle.ViewModel
import com.fransbudikashira.chefies.data.repository.UserRepository

class MainViewModel(private val userRepository: UserRepository): ViewModel() {

    fun deleteToken() = userRepository.deleteToken()
}
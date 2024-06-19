package com.fransbudikashira.chefies.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fransbudikashira.chefies.data.repository.RecipeRepository
import com.fransbudikashira.chefies.data.repository.UserRepository

class MainViewModel(
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository
): ViewModel() {

    private val _isSettingFragment = MutableLiveData<Boolean>()
    val isSettingFragment : LiveData<Boolean> = _isSettingFragment

    init {
        _isSettingFragment.value = false
    }

    fun setSettingFragment(isSettingFragment: Boolean) {
        _isSettingFragment.value = isSettingFragment
    }

    fun deleteToken() = userRepository.deleteToken()

    fun getProfile() = userRepository.getProfile()

    fun getEmail() = userRepository.getEmail()

    fun getPassword() = userRepository.getPassword()

    fun getHistory() = recipeRepository.getHistories()

    fun userLogin(
        email: String,
        password: String
    ) = userRepository.userLogin(email, password)
}
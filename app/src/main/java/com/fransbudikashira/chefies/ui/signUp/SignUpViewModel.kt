package com.fransbudikashira.chefies.ui.signUp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fransbudikashira.chefies.data.repository.UserRepository
import com.fransbudikashira.chefies.helper.Event

class SignUpViewModel(
    private val userRepository: UserRepository
): ViewModel() {

    private val _isEnableButton = MutableLiveData<Event<Boolean>>()
    val isEnableButton: LiveData<Event<Boolean>> = _isEnableButton

    init {
        setEnabledButton(false)
    }

    fun setEnabledButton(isEnabled: Boolean) {
        _isEnableButton.value = Event(isEnabled)
    }

    fun userRegister(
        name: String,
        email: String,
        password: String
    ) = userRepository.userRegister(name, email, password)
}
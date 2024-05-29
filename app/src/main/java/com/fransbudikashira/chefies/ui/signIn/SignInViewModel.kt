package com.fransbudikashira.chefies.ui.signIn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fransbudikashira.chefies.util.Event

class SignInViewModel: ViewModel() {
    private val _isEnableButton = MutableLiveData<Event<Boolean>>()
    val isEnableButton: LiveData<Event<Boolean>> = _isEnableButton

    init {
        setEnabledButton(false)
    }

    fun setEnabledButton(isEnabled: Boolean) {
        _isEnableButton.value = Event(isEnabled)
    }
}
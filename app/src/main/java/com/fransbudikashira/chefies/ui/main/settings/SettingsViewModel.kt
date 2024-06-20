package com.fransbudikashira.chefies.ui.main.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.viewModelScope
import com.fransbudikashira.chefies.data.repository.UserRepository
import com.fransbudikashira.chefies.helper.Event
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(private val userRepository: UserRepository): ViewModel() {

    private val _isEnableButton = MutableLiveData<Event<Boolean>>()
    val isEnableButton: LiveData<Event<Boolean>> = _isEnableButton

    private val _username = MutableLiveData<String>()
    val username : LiveData<String> = _username

    private val _avatarUrl = MutableLiveData<String>()
    val avatarUrl : LiveData<String> = _avatarUrl

    init {
        setEnabledButton(false)
        _username.value = userRepository.getUsername()
        _avatarUrl.value = userRepository.getAvatar()
    }

    fun saveThemeSetting(isDarkModeActive: Boolean) {
        userRepository.saveThemeSetting(isDarkModeActive)
    }

    fun getThemeSetting() = userRepository.getThemeSetting()

    fun setUsername (username: String) {
        _username.value = username
    }

    fun setAvatarUrl (avatarUrl: String) {
        _avatarUrl.value = avatarUrl
    }

    fun setEnabledButton(isEnabled: Boolean) {
        _isEnableButton.value = Event(isEnabled)
    }

    fun updateProfile(name: String, avatarFile: File) = userRepository.updateProfile(name, avatarFile)

    fun getProfile() = userRepository.getProfile()

    fun getUsername() = userRepository.getUsername()

    fun getAvatar() = userRepository.getAvatar()

    fun updatePassword(
        newPassword: String,
        oldPassword: String
    ) = userRepository.updatePassword(newPassword, oldPassword)
}
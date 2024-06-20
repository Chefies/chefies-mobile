package com.fransbudikashira.chefies.ui.changePassword

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.data.factory.AuthViewModelFactory
import com.fransbudikashira.chefies.databinding.ActivityChangePasswordBinding
import com.fransbudikashira.chefies.helper.Result
import com.fransbudikashira.chefies.ui.successfulCP.SuccessfulCPActivity
import androidx.lifecycle.lifecycleScope
import com.fransbudikashira.chefies.ui.main.MainViewModel
import com.fransbudikashira.chefies.ui.main.settings.SettingsViewModel
import com.fransbudikashira.chefies.ui.signIn.SignInActivity
import com.fransbudikashira.chefies.util.moveActivityTo
import com.fransbudikashira.chefies.util.showToast
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private val settingsViewModel: SettingsViewModel by viewModels {
        AuthViewModelFactory.getInstance(this)
    }

    private val mainViewModel: MainViewModel by viewModels {
        AuthViewModelFactory.getInstance(this)
    }

    private lateinit var newPassword: String
    private lateinit var oldPassword: String
    private var checkNewPassword: Boolean = false
    private var checkOldPassword: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set the status bar and navigation bar colors
        window.statusBarColor = getColor(R.color.md_theme_background)
        window.navigationBarColor = getColor(R.color.md_theme_background)

        // BackButton
        binding.toAppBar.setNavigationOnClickListener {
            finish()
        }

        // Handle Enabled Button
        settingsViewModel.isEnableButton.observe(this) {
            it.getContentIfNotHandled()?.let { isEnabled ->
                binding.btnChangePassword.isEnabled = isEnabled
            }
        }

        setupEditText(settingsViewModel)

        binding.btnChangePassword.setOnClickListener {
            tokenValidationMechanism()
        }

    }

    private fun updatePassword() {
        lifecycleScope.launch {
            settingsViewModel.updatePassword(newPassword, oldPassword).observe(this@ChangePasswordActivity) { result ->
                if (result != null) {
                    when (result) {
                        is Result.Loading -> {
                            showLoading(true)
                        }
                        is Result.Success -> {
                            showLoading(false)
                            showToast(getString(R.string.success_change_password))
                            moveActivityTo(this@ChangePasswordActivity, SuccessfulCPActivity::class.java, true)
                        }
                        is Result.Error -> {
                            showLoading(false)
                            showToast(result.error)
                        }
                    }
                }
            }
        }
    }

    private fun tokenValidationMechanism() {
        lifecycleScope.launch {
            // Check Valid Token
            if (checkValidToken()) {
                updatePassword()
                // - - -
                Log.d(TAG, "VALID TOKEN")
            } else {
                val email = mainViewModel.getEmail()
                val password = mainViewModel.getPassword()
                // Check Email & Password Available
                if (email.isEmpty() || password.isEmpty()) {
                    moveActivityTo(this@ChangePasswordActivity, SignInActivity::class.java, true)
                    // - - -
                    Log.d(TAG, "EMAIL & PASSWORD UN-AVAILABLE")
                } else {
                    // Do Login
                    doLogin(email, password)
                    // - - -
                    Log.d(TAG, "EMAIL & PASSWORD AVAILABLE")
                }
                // - - -
                Log.d(TAG, "INVALID TOKEN")
            }
            // - - -
            Log.d(TAG, "TOKEN AVAILABLE")
        }
    }

    private fun doLogin(email: String, password: String) {
        mainViewModel.userLogin(email, password).observe(this@ChangePasswordActivity) { userLoginResult ->
            when (userLoginResult) {
                is Result.Loading -> {}
                is Result.Success -> {
                    updatePassword()
                    // - - -
                    Log.d(TAG, "LOGIN SUCCESS -> MOVE TO MAIN")
                }
                is Result.Error -> {
                    moveActivityTo(this@ChangePasswordActivity, SignInActivity::class.java, true)
                    // - - -
                    Log.d(TAG, "LOGIN FAILED -> MOVE TO SIGN-IN")
                }
            }
        }
    }

    private suspend fun checkValidToken(): Boolean {
        return suspendCoroutine { continuation ->
            mainViewModel.getProfile().observe(this@ChangePasswordActivity) { getProfileResult ->
                when (getProfileResult) {
                    is Result.Loading -> {}
                    is Result.Success -> {
                        continuation.resume(true)
                    }
                    is Result.Error -> {
                        continuation.resume(false)
                    }
                }
            }
        }
    }


    private fun setupEditText(viewModel: SettingsViewModel) {
        // New Password
        binding.etNewPassword.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length < 8) {
                    binding.etLayoutNewPassword.error = getString(R.string.invalid_password)

                    checkNewPassword = false
                    isEnabledButton(viewModel)
                } else {
                    binding.etLayoutNewPassword.error = null
                    binding.etLayoutNewPassword.isErrorEnabled = false

                    newPassword = s.toString()
                    checkNewPassword = true
                    isEnabledButton(viewModel)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        // New Password
        binding.etOldPassword.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length < 8) {
                    binding.etLayoutOldPassword.error = getString(R.string.invalid_password)

                    checkOldPassword = false
                    isEnabledButton(viewModel)
                } else {
                    binding.etLayoutOldPassword.error = null
                    binding.etLayoutOldPassword.isErrorEnabled = false

                    oldPassword = s.toString()
                    checkOldPassword = true
                    isEnabledButton(viewModel)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun isEnabledButton(viewModel: SettingsViewModel) {
        if (checkNewPassword && checkOldPassword)
            viewModel.setEnabledButton(true)
        else
            viewModel.setEnabledButton(false)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnChangePassword.text = if (isLoading) "" else getString(R.string.change_password_text)
        }
    }

    companion object {
        const val TAG = "ChangePasswordActivity"
    }
}
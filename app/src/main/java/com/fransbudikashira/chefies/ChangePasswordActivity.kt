package com.fransbudikashira.chefies

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fransbudikashira.chefies.data.factory.AuthViewModelFactory
import com.fransbudikashira.chefies.databinding.ActivityChangePasswordBinding
import com.fransbudikashira.chefies.helper.Result
import com.fransbudikashira.chefies.ui.main.MainActivity
import com.fransbudikashira.chefies.ui.main.settings.SettingsViewModel

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private val settingsViewModel: SettingsViewModel by viewModels {
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
        window.statusBarColor = getColor(R.color.white)
        window.navigationBarColor = getColor(R.color.white)

        // BackButton
        binding.toAppBar.setNavigationOnClickListener {
            moveToMain()
        }

        // Handle Enabled Button
        settingsViewModel.isEnableButton.observe(this) {
            it.getContentIfNotHandled()?.let { isEnabled ->
                binding.btnChangePassword.isEnabled = isEnabled
            }
        }

        setupEditText(settingsViewModel)

        binding.btnChangePassword.setOnClickListener {
            settingsViewModel.updatePassword(newPassword, oldPassword).observe(this) { result ->
                if (result != null) {
                    when (result) {
                        is Result.Loading -> {
                            showLoading(true)
                        }
                        is Result.Success -> {
                            showLoading(false)
                            showToast(getString(R.string.success_change_password))
                            moveToSuccesCP()
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

    private fun moveToSuccesCP() {
        val intent = Intent(this@ChangePasswordActivity, SuccessfulCPActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun moveToMain() {
        val intent = Intent(this@ChangePasswordActivity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
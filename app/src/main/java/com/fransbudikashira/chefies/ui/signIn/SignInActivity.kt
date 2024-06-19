package com.fransbudikashira.chefies.ui.signIn

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.data.factory.AuthViewModelFactory
import com.fransbudikashira.chefies.databinding.ActivitySignInBinding
import com.fransbudikashira.chefies.helper.Result
import com.fransbudikashira.chefies.ui.main.MainActivity
import com.fransbudikashira.chefies.ui.signUp.SignUpActivity
import com.fransbudikashira.chefies.util.isValidEmail
import com.fransbudikashira.chefies.util.moveTo
import com.google.android.material.button.MaterialButton

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var viewModel: SignInViewModel

    private lateinit var email: String
    private lateinit var password: String
    private var checkEmail:Boolean = false
    private var checkPassword:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set the status bar and navigation bar colors
        window.statusBarColor = getColor(R.color.md_theme_primary)
        window.navigationBarColor = getColor(R.color.md_theme_background)

        // set ViewModel
        viewModel = obtainViewModel(this@SignInActivity)

        with(binding) {
            moveToSignUp.setOnClickListener{
                moveTo(SignUpActivity::class.java, true)
            }
            btnLogin.setOnClickListener{
                viewModel.userLogin(email.trim(), password.trim()).observe(this@SignInActivity) { result ->
                    if (result != null) {
                        when (result) {
                            is Result.Loading -> {
                                showLoading(true)
                            }
                            is Result.Success -> {
                                showLoading(false)
                                successDialog(getString(R.string.success_login))
                            }
                            is Result.Error -> {
                                showLoading(false)
                                edtPassword.text?.clear()
                                errorDialog(result.error)
                            }
                        }
                    }
                }
            }
        }
        // Handle Enabled Button
        viewModel.isEnableButton.observe(this) {
            it.getContentIfNotHandled()?.let { isEnabled ->
                binding.btnLogin.isEnabled = isEnabled
            }
        }

        setupEditText(viewModel)
        playAnimation()
    }

    private fun errorDialog(message: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.custom_dialog_error_auth)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.5f)

        val tvMessage: TextView = dialog.findViewById(R.id.message)
        tvMessage.text = message

        val btnTryAgain: MaterialButton = dialog.findViewById(R.id.btnTryAgain)
        btnTryAgain.setOnClickListener { dialog.dismiss() }

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    private fun successDialog(message: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.custom_dialog_success_auth)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.5f)

        val tvMessage: TextView = dialog.findViewById(R.id.message)
        tvMessage.text = message

        val btnAction: MaterialButton = dialog.findViewById(R.id.btnAction)
        btnAction.text = getString(R.string.next)
        btnAction.setOnClickListener {
            dialog.dismiss()
            moveTo(MainActivity::class.java, true)
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnLogin.text = if (isLoading) "" else getString(R.string.sign_up)
        }
    }

    private fun obtainViewModel(activity: AppCompatActivity): SignInViewModel {
        val factory: AuthViewModelFactory = AuthViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory)[SignInViewModel::class.java]
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.illustration, View.TRANSLATION_Y, -20f, 20f).apply {
            duration = 3000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = DecelerateInterpolator()
        }.start()

        val title = ObjectAnimator.ofFloat(binding.introTitle, View.ALPHA, 1f).apply {
            duration = 200
            startDelay = 300
        }
        val subTitle = ObjectAnimator.ofFloat(binding.introSubTitle, View.ALPHA, 1f).setDuration(200)
        val illustration = ObjectAnimator.ofFloat(binding.illustration, View.ALPHA, 1f).setDuration(200)
        val edtEmail = ObjectAnimator.ofFloat(binding.edtEmailLayout, View.ALPHA, 1f).setDuration(200)
        val edtPassword = ObjectAnimator.ofFloat(binding.edtPasswordLayout, View.ALPHA, 1f).setDuration(200)
        val button = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(200)
        val bottomText = ObjectAnimator.ofFloat(binding.bottomText, View.ALPHA, 1f).setDuration(200)
        val bottomAction = ObjectAnimator.ofFloat(binding.moveToSignUp, View.ALPHA, 1f).setDuration(200)

        val bottom = AnimatorSet().apply { playTogether(bottomText, bottomAction) }

        AnimatorSet().apply {
            playSequentially(title, subTitle, illustration, edtEmail, edtPassword, button, bottom)
            start()
        }
    }

    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false  // Change to false if you want light content (white icons) on the status bar
            isAppearanceLightNavigationBars = false  // Change to false if you want light content (white icons) on the navigation bar
        }
    }

    private fun setupEditText(viewModel: SignInViewModel) {
        // Email
        binding.edtEmail.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isValidEmail(s.toString())) {
                    binding.edtEmailLayout.error = getString(R.string.invalid_email)

                    checkEmail = false
                    isEnabledButton(viewModel)
                } else {
                    binding.edtEmailLayout.error = null
                    binding.edtEmailLayout.isErrorEnabled = false

                    email = s.toString()
                    checkEmail = true
                    isEnabledButton(viewModel)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        // Password
        binding.edtPassword.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length < 8) {
                    binding.edtPasswordLayout.error = getString(R.string.invalid_password)

                    checkPassword = false
                    isEnabledButton(viewModel)
                } else {
                    binding.edtPasswordLayout.error = null
                    binding.edtPasswordLayout.isErrorEnabled = false

                    password = s.toString()
                    checkPassword = true
                    isEnabledButton(viewModel)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun isEnabledButton(viewModel: SignInViewModel) {
        if (checkEmail && checkPassword)
            viewModel.setEnabledButton(true)
        else
            viewModel.setEnabledButton(false)
    }
}
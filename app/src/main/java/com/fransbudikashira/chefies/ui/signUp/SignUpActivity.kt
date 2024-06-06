package com.fransbudikashira.chefies.ui.signUp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.databinding.ActivitySignUpBinding
import com.fransbudikashira.chefies.ui.signIn.SignInActivity
import com.fransbudikashira.chefies.util.isValidEmail

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    private lateinit var name: String
    private lateinit var email: String
    private lateinit var password: String
    private var checkName:Boolean = false
    private var checkEmail:Boolean = false
    private var checkPassword:Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val viewModel = SignUpViewModel()

        // Set the status bar and navigation bar colors
        window.statusBarColor = getColor(R.color.primary)
        window.navigationBarColor = getColor(R.color.white)

        binding.moveToSignIn.setOnClickListener{
            val intent = Intent(this@SignUpActivity, SignInActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
        // Handle Enabled Button
        viewModel.isEnableButton.observe(this) {
            it.getContentIfNotHandled()?.let { isEnabled ->
                binding.button.isEnabled = isEnabled
            }
        }

        setupEditText(viewModel)
        playAnimation()
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
        val edtName = ObjectAnimator.ofFloat(binding.edtNameLayout, View.ALPHA, 1f).setDuration(200)
        val edtEmail = ObjectAnimator.ofFloat(binding.edtEmailLayout, View.ALPHA, 1f).setDuration(200)
        val edtPassword = ObjectAnimator.ofFloat(binding.edtPasswordLayout, View.ALPHA, 1f).setDuration(200)
        val button = ObjectAnimator.ofFloat(binding.button, View.ALPHA, 1f).setDuration(200)
        val bottomText = ObjectAnimator.ofFloat(binding.bottomText, View.ALPHA, 1f).setDuration(200)
        val bottomAction = ObjectAnimator.ofFloat(binding.moveToSignIn, View.ALPHA, 1f).setDuration(200)

        val bottom = AnimatorSet().apply { playTogether(bottomText, bottomAction) }

        AnimatorSet().apply {
            playSequentially(title, subTitle, illustration, edtName, edtEmail, edtPassword, button, bottom)
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

    private fun setupEditText(viewModel: SignUpViewModel) {
        // Name
        binding.edtName.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length <= 3) {
                    binding.edtNameLayout.error = getString(R.string.invalid_name)

                    checkName = false
                    isEnabledButton(viewModel)
                } else {
                    binding.edtNameLayout.error = null
                    binding.edtNameLayout.isErrorEnabled = false

                    name = s.toString()
                    checkName = true
                    isEnabledButton(viewModel)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
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

    private fun isEnabledButton(viewModel: SignUpViewModel) {
        if (checkName && checkEmail && checkPassword)
            viewModel.setEnabledButton(true)
        else
            viewModel.setEnabledButton(false)
    }
}
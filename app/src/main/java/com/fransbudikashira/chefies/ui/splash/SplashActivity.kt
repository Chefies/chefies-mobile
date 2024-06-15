package com.fransbudikashira.chefies.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.data.factory.AuthViewModelFactory
import com.fransbudikashira.chefies.data.repository.UserRepository
import com.fransbudikashira.chefies.helper.Result
import com.fransbudikashira.chefies.ui.main.MainActivity
import com.fransbudikashira.chefies.ui.signIn.SignInActivity
import com.fransbudikashira.chefies.util.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val viewModel: SplashViewModel by viewModels {
        AuthViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.navigationBarColor = getColor(R.color.md_theme_primary)

        lifecycleScope.launch {
            delay(100L)
        }

        // Check Token Available
        if (checkTokenAvailable()) {
            // Run Token Validation Mechanism
            tokenValidationMechanism()
        } else {
            moveActivityTo(SignInActivity::class.java)
            // - - -
            Log.d(TAG, "TOKEN UN-AVAILABLE")
        }
    }

    private fun tokenValidationMechanism() {
        lifecycleScope.launch {
            // Check Valid Token
            if (checkValidToken()) {
                moveActivityTo(MainActivity::class.java)
                // - - -
                Log.d(TAG, "VALID TOKEN")
            } else {
                val email = viewModel.getEmail()
                val password = viewModel.getPassword()
                // Check Email & Password Available
                if (email.isEmpty() || password.isEmpty()) {
                    moveActivityTo(SignInActivity::class.java)
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
        viewModel.userLogin(email, password).observe(this@SplashActivity) { userLoginResult ->
            when (userLoginResult) {
                is Result.Loading -> {}
                is Result.Success -> {
                    moveActivityTo(MainActivity::class.java)
                    // - - -
                    Log.d(TAG, "LOGIN SUCCESS -> MOVE TO MAIN")
                }
                is Result.Error -> {
                    moveActivityTo(SignInActivity::class.java)
                    // - - -
                    Log.d(TAG, "LOGIN FAILED -> MOVE TO SIGN-IN")
                }
            }
        }
    }

    private suspend fun checkValidToken(): Boolean {
        return suspendCoroutine { continuation ->
            viewModel.getProfile().observe(this@SplashActivity) { getProfileResult ->
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

    private fun checkTokenAvailable(): Boolean {
        val token = viewModel.getToken()
        return token.isNotEmpty()
    }

    private fun <T> moveActivityTo(activity: Class<T>) {
        val intent = Intent(this, activity)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "SplashActivity"
    }
}
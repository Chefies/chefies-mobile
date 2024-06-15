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

        lifecycleScope.launch {
//            delay(1000L)
            val token = viewModel.getToken()
            // Check Token Available
            if (token.isNotEmpty()) {
                // Check Valid Token
                viewModel.getProfile().observe(this@SplashActivity) { getProfileResult ->
                    when (getProfileResult) {
                        is Result.Loading -> {}
                        is Result.Success -> {
                            moveActivityTo(MainActivity::class.java)
                            // - - -
                            Log.d(TAG, "VALID TOKEN")
                        }
                        is Result.Error -> {
                            val email = viewModel.getEmail()
                            val password = viewModel.getPassword()
                            // Check Email & Password Available
                            if (email.isEmpty() || password.isEmpty()) {
                                moveActivityTo(SignInActivity::class.java)
                                // - - -
                                Log.d(TAG, "EMAIL & PASSWORD UN-AVAILABLE")
                            } else {
                                // Do Login
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
                                // - - -
                                Log.d(TAG, "EMAIL & PASSWORD AVAILABLE")
                            }
                            // - - -
                            Log.d(TAG, "INVALID TOKEN")
                        }
                    }
                }
                // - - -
                Log.d(TAG, "TOKEN AVAILABLE")
            } else {
                moveActivityTo(SignInActivity::class.java)
                // - - -
                Log.d(TAG, "TOKEN UN-AVAILABLE")
            }
        }
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
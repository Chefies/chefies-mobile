package com.fransbudikashira.chefies.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.data.factory.AuthViewModelFactory
import com.fransbudikashira.chefies.ui.main.MainActivity
import com.fransbudikashira.chefies.ui.signIn.SignInActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
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

        val factory: AuthViewModelFactory = AuthViewModelFactory.getInstance(this)
        val viewModel: SplashViewModel by viewModels { factory }

        lifecycleScope.launch {
            delay(1000L)
            viewModel.getToken().observe(this@SplashActivity) { token ->
                if (!token.isNullOrEmpty()) {
                    moveActivityTo(MainActivity::class.java)
                } else {
                    moveActivityTo(SignInActivity::class.java)
                }
            }
        }
    }

    private fun <T> moveActivityTo(activity: Class<T>) {
        val intent = Intent(this, activity)
        startActivity(intent)
        finish()
    }
}
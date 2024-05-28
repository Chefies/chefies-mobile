package com.fransbudikashira.chefies.ui.Result

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.databinding.ActivityResultBinding
import com.google.android.material.internal.ViewUtils.hideKeyboard

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.primary)
        window.navigationBarColor = getColor(R.color.white)

        binding.main.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val focusedView = currentFocus
                if (focusedView is EditText) {
                    focusedView.clearFocus()
                    hideKeyboard(focusedView)
                }
            }
            false
        }
    }

    private fun enableEdgeToEdge() {
        // Enable edge-to-edge mode and make system bars transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false  // Change to false if you want light content (white icons) on the status bar
            isAppearanceLightNavigationBars = false  // Change to false if you want light content (white icons) on the navigation bar
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
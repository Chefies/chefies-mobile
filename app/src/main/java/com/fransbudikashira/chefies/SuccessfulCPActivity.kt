package com.fransbudikashira.chefies

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fransbudikashira.chefies.databinding.ActivitySuccessfulCpactivityBinding
import com.fransbudikashira.chefies.ui.main.MainActivity
import com.fransbudikashira.chefies.util.moveActivityTo

class SuccessfulCPActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySuccessfulCpactivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySuccessfulCpactivityBinding.inflate(layoutInflater)
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
            finish()
        }

        // handle button
        binding.btnOk.setOnClickListener {
            moveActivityTo(this, MainActivity::class.java, true)
        }
    }
}
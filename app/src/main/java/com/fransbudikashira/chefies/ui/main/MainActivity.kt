package com.fransbudikashira.chefies.ui.main

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.databinding.ActivityMainBinding
import com.fransbudikashira.chefies.helper.Constants.LABELS_PATH
import com.fransbudikashira.chefies.helper.Constants.MODEL_PATH
import com.fransbudikashira.chefies.helper.ObjectDetectorHelper
import org.tensorflow.lite.task.vision.detector.Detection


class MainActivity : AppCompatActivity(), ObjectDetectorHelper.DetectorListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var objectDetectorHelper: ObjectDetectorHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        objectDetectorHelper = ObjectDetectorHelper(baseContext, MODEL_PATH, LABELS_PATH, this)
        objectDetectorHelper.setupObjectDetector()

        window.statusBarColor = getColor(R.color.md_theme_primary)
        window.navigationBarColor = getColor(R.color.md_theme_primary)

        //
        binding.bottomNavigation.background = null
        binding.bottomNavigation.menu.getItem(1).isEnabled = false

        // navigation bottom & controller configuration
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

//        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.settingsFragment
            )
        )
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration)

        binding.bottomNavigation.setupWithNavController(navController)
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        binding.bottomNavigation.setupWithNavController(navController)

        binding.fabButton.setOnClickListener {
            Toast.makeText(this, "Hello this is Fab",  Toast.LENGTH_SHORT).show()
            startGallery()
        }
    }

    private fun startGallery() {
        launcherGallery.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            Log.d("photo picker", "$uri")
            analyzeImage(uri)
        } else {
            Log.d("photo picker", "No media selected")
        }
    }

    private fun analyzeImage(uri: Uri) {
        objectDetectorHelper.detectObject(uri)
    }

    override fun onError(error: String) {
        runOnUiThread{
            Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResult(results: List<String>?) {
        runOnUiThread {
            results?.let {
                if (it.isNotEmpty()) {
                    Log.d(TAG, it.toString())
                } else {
                    Log.d(TAG, "No result")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        objectDetectorHelper.clear()
    }

    private fun enableEdgeToEdge() {
        // Enable edge-to-edge mode and make system bars transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false  // Change to false if you want light content (white icons) on the status bar
            isAppearanceLightNavigationBars = true  // Change to false if you want light content (white icons) on the navigation bar
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
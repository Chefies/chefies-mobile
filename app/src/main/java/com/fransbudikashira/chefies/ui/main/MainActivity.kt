package com.fransbudikashira.chefies.ui.main

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.databinding.ActivityMainBinding
import com.fransbudikashira.chefies.ui.home.HomeFragment
import com.fransbudikashira.chefies.ui.result.ResultActivity
import com.fransbudikashira.chefies.ui.settings.SettingsFragment
import com.fransbudikashira.chefies.util.getImageUri
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.yalantis.ucrop.UCrop

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentImageUri: Uri? = null
    private var requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast("Permission request granted")
            } else {
                showToast("Permission request denied")
            }
        }
    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        // enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = getColor(R.color.md_theme_primary)
        window.navigationBarColor = getColor(R.color.md_theme_primary)

        binding.bottomNavigation.background = null // ensure bottomNav background doesn't appear
        binding.bottomNavigation.menu.getItem(1).isEnabled = false // & hide item menu index 1 (space for FAB)

        // navigation bottom & controller configuration
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.settingsFragment
            )
        )
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)

        // Fab action
        binding.fabButton.setOnClickListener {
            showCustomDialogBox()
        }
    }

    // Dialog Box get image options
    private fun showCustomDialogBox() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog_get_image)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCamera: ConstraintLayout = dialog.findViewById(R.id.btn_open_cam)
        val btnGalery: ConstraintLayout = dialog.findViewById(R.id.btn_open_galery)

        btnCamera.setOnClickListener {
            startCamera()
            dialog.dismiss()
        }

        btnGalery.setOnClickListener {
            startGallery()
            dialog.dismiss()
        }

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        currentImageUri?.let {
            launcherIntentCamera.launch(it)
        } ?: run {
            showToast("Failed to get image URI")
        }
    }
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            currentImageUri?.let {
                cropImage(it)
                // analyzeImage()   penemaptan nya disini ? atau di onActivityResult()?
                // moveToResult()   penemaptan nya disini ? atau di onActivityResult()?
            }
        } else {
            showToast("Failed to take picture")
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            cropImage(uri)
            // analyzeImage()   penemaptan nya disini ? atau di onActivityResult()?
            // moveToResult()   penemaptan nya disini ? atau di onActivityResult()?
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun cropImage(uri: Uri) {
        UCrop.of(uri, Uri.fromFile(cacheDir.resolve("${System.currentTimeMillis()}.jpg")))
            .start(this)
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult")
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            currentImageUri = UCrop.getOutput(data!!)
            // analyzeImage()
            moveToResult()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val errorMessage = UCrop.getError(data!!)?.message.toString()
            showToast(errorMessage)
            Log.e(TAG, errorMessage)
        }
    }

    private fun moveToResult() {
        val intent = Intent(this, ResultActivity::class.java)
        if (currentImageUri != null) {
            intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
            // intent.putExtra(ResultActivity.EXTRA_RESULT, result)
            startActivity(intent)
        } else {
            showToast("No Image Selected")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }


    private fun enableEdgeToEdge() {
        // Enable edge-to-edge mode and make system bars transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false  // Change to false if you want light content (white icons) on the status bar
            isAppearanceLightNavigationBars = true  // Change to false if you want light content (white icons) on the navigation bar
        }
    }
}
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
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.data.model.MLResultModel
import com.fransbudikashira.chefies.databinding.ActivityMainBinding
import com.fransbudikashira.chefies.util.getImageUri
import com.yalantis.ucrop.UCrop
import com.fransbudikashira.chefies.helper.Constants.LABELS_PATH
import com.fransbudikashira.chefies.helper.Constants.MODEL_PATH
import com.fransbudikashira.chefies.helper.ObjectDetectorHelper
import com.fransbudikashira.chefies.ui.mlResult.MLResultActivity

class MainActivity : AppCompatActivity(), ObjectDetectorHelper.DetectorListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var objectDetectorHelper: ObjectDetectorHelper

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

        objectDetectorHelper = ObjectDetectorHelper(baseContext, MODEL_PATH, LABELS_PATH, this)
        objectDetectorHelper.setupObjectDetector()

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
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_get_image)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.5f)

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

    private fun startGallery() {
        launcherGallery.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            currentImageUri?.let {
                cropImage(it)
            }
        } else {
            showToast("Failed to take picture")
        }
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            cropImage(uri)
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
            currentImageUri?.let {
                analyzeImage(it)
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val errorMessage = UCrop.getError(data!!)?.message.toString()
            showToast(errorMessage)
            Log.e(TAG, errorMessage)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
                    moveToMLResult(it.distinct())
                } else {
                    Log.d(TAG, "No result")
                    showToast("No result")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        objectDetectorHelper.clear()
    }

    private fun moveToMLResult(ingredients: List<String>) {
        val intent = Intent(this, MLResultActivity::class.java)
        if (currentImageUri != null) {
            val result = MLResultModel(
                photoUrl = currentImageUri!!,
                ingredients = ingredients
            )
            intent.putExtra(MLResultActivity.EXTRA_RESULT, result)
            startActivity(intent)
        } else {
            showToast("No Image Selected")
        }
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
        private const val TAG = "MainActivity"
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
        private const val EXTRA_IMAGE = "EXTRA_IMAGE"
    }
}
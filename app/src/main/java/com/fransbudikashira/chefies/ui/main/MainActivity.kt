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
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.data.factory.RecipeViewModelFactory
import com.fransbudikashira.chefies.data.model.MLResultIngredients
import com.fransbudikashira.chefies.databinding.ActivityMainBinding
import com.fransbudikashira.chefies.util.getImageUri
import com.yalantis.ucrop.UCrop
import com.fransbudikashira.chefies.helper.Constants.LABELS_PATH
import com.fransbudikashira.chefies.helper.Constants.MODEL_PATH
import com.fransbudikashira.chefies.helper.ObjectDetectorHelper
import com.fransbudikashira.chefies.ui.main.history.HistoryFragment
import com.fransbudikashira.chefies.ui.main.home.HomeFragment
import com.fransbudikashira.chefies.ui.main.settings.SettingsFragment
import com.fransbudikashira.chefies.ui.mlResult.MLResultActivity
import com.fransbudikashira.chefies.util.await
import com.fransbudikashira.chefies.util.moveActivityTo
import com.fransbudikashira.chefies.util.showToast
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity(), ObjectDetectorHelper.DetectorListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var objectDetectorHelper: ObjectDetectorHelper

    private val viewModel: MainViewModel by viewModels {
        RecipeViewModelFactory.getInstance(this)
    }

    // animation properties
    private var clicked = false
    private val rotateopen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_open_anim
        )
    }
    private val rotateclose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.to_bottom_anim
        )
    }

    private var currentImageUri: Uri? = null

    private var requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast(getString(R.string.permission_request_granted))
            } else {
                showToast(getString(R.string.permission_request_denied))
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d(TAG, "onCreate: ${Locale.getDefault().language}")

        objectDetectorHelper = ObjectDetectorHelper(baseContext, MODEL_PATH, LABELS_PATH, this)
        objectDetectorHelper.setupObjectDetector()

        window.statusBarColor = getColor(R.color.md_theme_primary)
        window.navigationBarColor = getColor(R.color.md_theme_primary)

        binding.bottomNavigation.background = null // ensure bottomNav background doesn't appear
        binding.bottomNavigation.menu.getItem(1).isEnabled = false // & hide item menu index 1 (space for FAB)
        binding.bottomNavigation.menu.getItem(3).isVisible = false

        // navigation bottom & controller configuration
//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val navController = navHostFragment.navController
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.homeFragment, R.id.historyFragment, R.id.settingsFragment
//            )
//        )
//        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration)
//        binding.bottomNavigation.setupWithNavController(navController)


        lifecycleScope.launch {
            val defaultFragment: Fragment = if (isHistoriesEmpty()) HomeFragment() else HistoryFragment()

            viewModel.isSettingFragment.observe(this@MainActivity) { isSettingFragment ->
                Log.d(TAG, "isSettingFragment: $isSettingFragment")
                val defaultSetFragment = if (isSettingFragment) SettingsFragment() else defaultFragment
                loadFragment(defaultSetFragment)
            }

            binding.bottomNavigation.setOnItemSelectedListener {
                when(it.itemId) {
                    R.id.homeFragment -> {
                        loadFragment(defaultFragment)
                        true
                    }
                    R.id.settingsFragment -> {
                        loadFragment(SettingsFragment())
                        true
                    }
                    R.id.historyFragment -> {
                        loadFragment(HistoryFragment())
                        true
                    }
                    else -> false
                }
            }
        }

        // Main Fab action
        binding.fabButton.setOnClickListener { onFabButtonClicked() }
        binding.fabCam.setOnClickListener {
            startCamera()
            onFabButtonClicked()
        }
        binding.fabGallery.setOnClickListener {
            startGallery()
            onFabButtonClicked()
        }

    } // ------ end of onCreate --------

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

    private suspend fun isHistoriesEmpty(): Boolean {
        val listHistory = viewModel.getHistory().await()
        return listHistory.isNullOrEmpty()
    }

    private  fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.commit()
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        currentImageUri?.let {
            launcherIntentCamera.launch(it)
        } ?: run {
            showToast(getString(R.string.failed_to_get_image_uri))
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
            showToast(getString(R.string.failed_to_take_picture))
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

    private fun analyzeImage(uri: Uri) {
        objectDetectorHelper.detectObject(uri)
    }

    override fun onError(error: String) {
        runOnUiThread {
            showToast(error)
        }
    }

    override fun onResult(results: List<String>?) {
        runOnUiThread {
            results?.let {
                if (it.isNotEmpty()) {
                    Log.d(TAG, it.toString())
                    moveToMLResult(it.distinct())
                } else {
                    Log.d(TAG, "No ingredients detected")
                    showFailedDialog()
                    moveActivityTo(this@MainActivity, MLResultActivity::class.java, true)
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
            val resultWithIngredients = MLResultIngredients(currentImageUri!!, ingredients)
            intent.putExtra(MLResultActivity.EXTRA_RESULT, resultWithIngredients)
            startActivity(intent)
        } else {
            showToast(getString(R.string.no_image_selected))
        }
    }

    // Dialog box failed to get ingredients
    private fun showFailedDialog() {
        val dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_failed_detect_ingredients)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.5f)

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    // connfiguration when fab is clicked
    private fun onFabButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked=!clicked
    }

    // set visibility of fab & label
    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            binding.labelFabCam.visibility = View.VISIBLE
            binding.labelFabGallery.visibility = View.VISIBLE
            binding.fabCam.visibility = View.VISIBLE
            binding.fabGallery.visibility = View.VISIBLE
        }else{
            binding.fabCam.visibility = View.INVISIBLE
            binding.fabGallery.visibility = View.INVISIBLE
            binding.labelFabCam.visibility = View.INVISIBLE
            binding.labelFabGallery.visibility = View.INVISIBLE
        }
    }

    // set animation
    private fun setAnimation(clicked: Boolean) {
        if(!clicked){
            binding.fabButton.startAnimation(rotateopen)
            binding.labelFabCam.startAnimation(fromBottom)
            binding.labelFabGallery.startAnimation(fromBottom)
            binding.fabCam.startAnimation(fromBottom)
            binding.fabGallery.startAnimation(fromBottom)
        }
        else{
            binding.fabButton.startAnimation(rotateclose)
            binding.fabCam.startAnimation(toBottom)
            binding.fabGallery.startAnimation(toBottom)
            binding.labelFabCam.startAnimation(toBottom)
            binding.labelFabGallery.startAnimation(toBottom)
        }
    }

    // set state of Fab
    private fun setClickable(clicked: Boolean){
        if(!clicked){
            binding.fabCam.isClickable=true
            binding.fabGallery.isClickable=true
        }
        else{
            binding.fabCam.isClickable=false
            binding.fabGallery.isClickable=false
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}
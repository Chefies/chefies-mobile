package com.fransbudikashira.chefies.ui.changeProfile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.data.factory.AuthViewModelFactory
import com.fransbudikashira.chefies.databinding.ActivityChangeProfileBinding
import com.fransbudikashira.chefies.helper.Result
import com.fransbudikashira.chefies.ui.main.MainViewModel
import com.fransbudikashira.chefies.ui.main.settings.SettingsViewModel
import com.fransbudikashira.chefies.ui.signIn.SignInActivity
import com.fransbudikashira.chefies.util.loadImageProfile
import com.fransbudikashira.chefies.util.moveActivityTo
import com.fransbudikashira.chefies.util.reduceFileImage
import com.fransbudikashira.chefies.util.showToast
import com.fransbudikashira.chefies.util.uriToFile
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChangeProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeProfileBinding

    private val settingsViewModel: SettingsViewModel by viewModels {
        AuthViewModelFactory.getInstance(this)
    }
    private val mainViewModel: MainViewModel by viewModels {
        AuthViewModelFactory.getInstance(this)
    }

    private var currentImageUri: Uri? = null
    private var checkNewName: Boolean = false
    private var checkImageUri: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChangeProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set the status bar and navigation bar colors
        window.statusBarColor = getColor(R.color.md_theme_background)
        window.navigationBarColor = getColor(R.color.md_theme_background)

        // BackButton
        binding.toAppBar.setNavigationOnClickListener {
            finish()
        }

        // Handle Enabled Button
        settingsViewModel.isEnableButton.observe(this) {
            it.getContentIfNotHandled()?.let { isEnabled ->
                binding.btnChangeProfile.isEnabled = isEnabled
            }
        }

        setupView()

        binding.btnUploadPhoto.setOnClickListener {
            startGallery()
        }

        binding.btnChangeProfile.setOnClickListener {
            tokenValidationMechanism()
        }
    }

    private fun tokenValidationMechanism() {
        lifecycleScope.launch {
            // Check Valid Token
            if (checkValidToken()) {
                updateProfile()
                // - - -
                Log.d(TAG, "VALID TOKEN")
            } else {
                val email = mainViewModel.getEmail()
                val password = mainViewModel.getPassword()
                // Check Email & Password Available
                if (email.isEmpty() || password.isEmpty()) {
                    moveActivityTo(this@ChangeProfileActivity, SignInActivity::class.java, true)
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
        mainViewModel.userLogin(email, password).observe(this@ChangeProfileActivity) { userLoginResult ->
            when (userLoginResult) {
                is Result.Loading -> {}
                is Result.Success -> {
                    updateProfile()
                    // - - -
                    Log.d(TAG, "LOGIN SUCCESS -> MOVE TO MAIN")
                }
                is Result.Error -> {
                    moveActivityTo(this@ChangeProfileActivity, SignInActivity::class.java, true)
                    // - - -
                    Log.d(TAG, "LOGIN FAILED -> MOVE TO SIGN-IN")
                }
            }
        }
    }

    private suspend fun checkValidToken(): Boolean {
        return suspendCoroutine { continuation ->
            mainViewModel.getProfile().observe(this@ChangeProfileActivity) { getProfileResult ->
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

    private fun updateProfile() {
        lifecycleScope.launch {
            val name = binding.etName.text.toString()
            val avatarFile: File = uriToFile(currentImageUri!!, this@ChangeProfileActivity).reduceFileImage()

            settingsViewModel.updateProfile(name, avatarFile).observe(this@ChangeProfileActivity) { result ->
                when (result) {
                    is Result.Loading -> showLoading(true)
                    is Result.Success -> handleSuccess(getString(R.string.success_update_profile))
                    is Result.Error -> handleError(result.error)
                }
            }
        }
    }

    private fun handleSuccess(message: String) {
        showLoading(false)
        showToast(message)

        val intent = Intent()
        intent.putExtra(EXTRA_PROFILE_CHANGED, true)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun handleError(error: String) {
        showLoading(false)
        showToast(error)
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.ivProfile.setImageURI(it)

            checkImageUri = true
            isEnabledButton()
        }
    }

    private fun setupView() {
        //
        lifecycleScope.launch {
            binding.etName.setText(settingsViewModel.getUsername())
        }
        // New name
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length <= 3) {
                    binding.etLayoutName.error = getString(R.string.invalid_name)

                    checkNewName = false
                    isEnabledButton()
                } else if (s.toString() == settingsViewModel.getUsername()) {
                    checkNewName = false
                    isEnabledButton()
                } else {
                    binding.etLayoutName.error = null
                    binding.etLayoutName.isErrorEnabled = false

                    checkNewName = true
                    isEnabledButton()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        // Image uri
        val avatarDb = settingsViewModel.getAvatar()
        binding.ivProfile.loadImageProfile(avatarDb)
    }

    private fun isEnabledButton() {
        if (checkNewName && checkImageUri)
            settingsViewModel.setEnabledButton(true)
        else
            settingsViewModel.setEnabledButton(false)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnChangeProfile.text = if (isLoading) "" else getString(R.string.change_profile_txt)
        }
    }

    companion object {
        const val TAG = "ChangeProfileActivity"
        const val EXTRA_PROFILE_CHANGED = "EXTRA_PROFILE_CHANGED"
    }
}
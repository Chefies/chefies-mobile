package com.fransbudikashira.chefies

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fransbudikashira.chefies.data.factory.AuthViewModelFactory
import com.fransbudikashira.chefies.databinding.ActivityChangeProfileBinding
import com.fransbudikashira.chefies.helper.Result
import com.fransbudikashira.chefies.ui.main.MainActivity
import com.fransbudikashira.chefies.ui.main.settings.SettingsViewModel
import com.fransbudikashira.chefies.util.loadImage
import com.fransbudikashira.chefies.util.loadImageProfile
import com.fransbudikashira.chefies.util.moveActivityTo
import com.fransbudikashira.chefies.util.reduceFileImage
import com.fransbudikashira.chefies.util.uriToFile
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ChangeProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeProfileBinding
    private val settingsViewModel: SettingsViewModel by viewModels {
        AuthViewModelFactory.getInstance(this)
    }

    private lateinit var newName: String
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
        window.statusBarColor = getColor(R.color.white)
        window.navigationBarColor = getColor(R.color.white)

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

        setupView(settingsViewModel)

        binding.btnUploadPhoto.setOnClickListener {
            startGallery()
        }

        binding.btnChangeProfile.setOnClickListener {
            val name = binding.etName.text.toString()
            updateProfile(name)
//            lifecycleScope.launch {
//                val avatarDb = settingsViewModel.getAvatar().first()
//                val avatarFile: File? = if (currentImageUri != null) {
//
//                }
//
//                currentImageUri?.let { uri ->
//                    val avatarFile = uriToFile(uri, this@ChangeProfileActivity).reduceFileImage()
//                    Log.d("Image File", "showImage: ${avatarFile.path}")
//                    val name = binding.etName.text.toString()
//
//                    settingsViewModel.updateProfile(name, avatarFile).observe(this@ChangeProfileActivity) { result ->
//                        if (result != null) {
//                            when (result) {
//                                is Result.Loading -> showLoading(true)
//                                is Result.Success -> handleSuccess(result.data.message)
//                                is Result.Error -> handleError(result.error)
//                            }
//                        }
//                    }
//                }
//            }
        }
    }

    private fun updateProfile(name: String) {
        lifecycleScope.launch {
            val avatarUrl = settingsViewModel.getAvatar()
            val avatarFile: File? = if (currentImageUri != null) {
                // If the user changes avatar, use the new URI
                uriToFile(currentImageUri!!, this@ChangeProfileActivity).reduceFileImage()
            } else if (avatarUrl.isNotEmpty()) {
                // If not, download the avatar from the saved avatar URL
                downloadImageToFile(this@ChangeProfileActivity, avatarUrl)
            } else {
                null
            }

            if (avatarFile != null) {
                settingsViewModel.updateProfile(name, avatarFile).observe(this@ChangeProfileActivity) { result ->
                    when (result) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> handleSuccess(result.data.message)
                        is Result.Error -> handleError(result.error)
                    }
                }
            } else {
                showToast("Avatar not found or unable to download avatar")
            }
        }
    }

    private fun downloadImageToFile(context: Context, imageUrl: String): File? {
        return try {
            val url = URL(imageUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()

            val inputStream: InputStream = connection.inputStream
            val file = File(context.cacheDir, "avatar.jpg")
            val outputStream = FileOutputStream(file)

            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
            }

            outputStream.close()
            inputStream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun handleSuccess(message: String) {
        showLoading(false)
        showToast(message)
        moveActivityTo(this, MainActivity::class.java, true)
    }

    private fun handleError(error: String?) {
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
            showImage(settingsViewModel)
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showImage(viewModel: SettingsViewModel) {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.ivProfile.setImageURI(it)

            checkImageUri = true
            isEnabledButton()
        }
    }

    private fun setupView(viewModel: SettingsViewModel) {
        //
        lifecycleScope.launch {
            binding.etName.setText(settingsViewModel.getUsername())
        }
        // New name
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length <= 3) {
                    binding.etLayoutName.error = getString(R.string.invalid_name)

                    checkNewName = false
                    isEnabledButton()
                } else {
                    binding.etLayoutName.error = null
                    binding.etLayoutName.isErrorEnabled = false

                    newName = s.toString()
                    checkNewName = true
                    isEnabledButton()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        // Image uri
        val avatarDb = settingsViewModel.getAvatar()
        binding.ivProfile.loadImageProfile(avatarDb)
        if (avatarDb.isNotEmpty()) {
            checkImageUri = true
            isEnabledButton()
        } else {
            checkImageUri = false
            isEnabledButton()
        }
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

    private fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
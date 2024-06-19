package com.fransbudikashira.chefies.ui.main.settings

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fransbudikashira.chefies.ChangePasswordActivity
import com.fransbudikashira.chefies.ChangeProfileActivity
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.data.factory.AuthViewModelFactory
import com.fransbudikashira.chefies.databinding.FragmentSettingsBinding
import com.fransbudikashira.chefies.helper.Result
import com.fransbudikashira.chefies.ui.main.MainViewModel
import com.fransbudikashira.chefies.ui.signIn.SignInActivity
import com.fransbudikashira.chefies.util.moveActivityTo
import com.fransbudikashira.chefies.util.loadImageProfile
import com.fransbudikashira.chefies.util.moveTo
import com.fransbudikashira.chefies.util.showToast
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val settingsViewModel: SettingsViewModel by viewModels {
        AuthViewModelFactory.getInstance(requireContext())
    }

    private val mainViewModel: MainViewModel by viewModels {
        AuthViewModelFactory.getInstance(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        tokenValidationMechanism()

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // observe user profile
        observeViewModel()

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.changePasswordSetting.setOnClickListener {
            moveActivityTo(requireActivity(), ChangePasswordActivity::class.java)
        }

        binding.changeProfileSetting.setOnClickListener {
            moveActivityTo(requireActivity(), ChangeProfileActivity::class.java)
        }

        binding.languageSetting.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }

        binding.switchTheme.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            settingsViewModel.saveThemeSetting(isChecked)
        }
    }

    private fun tokenValidationMechanism() {
        lifecycleScope.launch {
            // Check Valid Token
            if (checkValidToken()) {
                getUserProfile()
                // - - -
                Log.d(TAG, "VALID TOKEN")
            } else {
                val email = mainViewModel.getEmail()
                val password = mainViewModel.getPassword()
                // Check Email & Password Available
                if (email.isEmpty() || password.isEmpty()) {
                    moveActivityTo(requireActivity(), SignInActivity::class.java, true)
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
        mainViewModel.userLogin(email, password).observe(viewLifecycleOwner) { userLoginResult ->
            when (userLoginResult) {
                is Result.Loading -> {}
                is Result.Success -> {
                    getUserProfile()
                    // - - -
                    Log.d(TAG, "LOGIN SUCCESS -> MOVE TO MAIN")
                }
                is Result.Error -> {
                    moveActivityTo(requireActivity(), SignInActivity::class.java, true)
                    // - - -
                    Log.d(TAG, "LOGIN FAILED -> MOVE TO SIGN-IN")
                }
            }
        }
    }

    private suspend fun checkValidToken(): Boolean {
        return suspendCoroutine { continuation ->
            mainViewModel.getProfile().observe(viewLifecycleOwner) { getProfileResult ->
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

    private fun getUserProfile() {
        lifecycleScope.launch {
            settingsViewModel.getProfile().observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Result.Loading -> {}
                    is Result.Success -> {
                        handleSuccess()
                    }
                    is Result.Error -> {}
                }
            }
        }
    }

    private fun handleSuccess() {
        settingsViewModel.setUsername(settingsViewModel.getUsername())
        settingsViewModel.setAvatarUrl(settingsViewModel.getAvatar())
    }

    // Dialog box warn logout
    private fun showLogoutDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog_logout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.5f)

        val btnYes: MaterialButton = dialog.findViewById(R.id.btn_yes)
        val btnNo: MaterialButton = dialog.findViewById(R.id.btn_no)

        btnYes.setOnClickListener {
            mainViewModel.deleteToken()
            moveTo(SignInActivity::class.java, true)
            showToast(getString(R.string.success_logout))
            // moveActivityTo(requireActivity(), SignInActivity::class.java, true)
        }

        btnNo.setOnClickListener { dialog.dismiss() }

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    private fun observeViewModel() {
        // observe username
        settingsViewModel.username.observe(viewLifecycleOwner) {
            binding.tvUsername.text = it
        }
        // observe avatar
        settingsViewModel.avatarUrl.observe(viewLifecycleOwner) {
            binding.ivProfile.loadImageProfile(it)
        }
        // observe theme settings
        settingsViewModel.themeSetting.observe(viewLifecycleOwner) { isDarkModeActive ->
            if (isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                binding.switchTheme.isChecked = true
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                binding.switchTheme.isChecked = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SettingsFragment"
    }
}
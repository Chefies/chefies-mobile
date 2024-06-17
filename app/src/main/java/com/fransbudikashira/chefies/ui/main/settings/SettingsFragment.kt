package com.fransbudikashira.chefies.ui.main.settings

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.fransbudikashira.chefies.ChangePasswordActivity
import com.fransbudikashira.chefies.ChangeProfileActivity
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.data.factory.AuthViewModelFactory
import com.fransbudikashira.chefies.databinding.FragmentSettingsBinding
import com.fransbudikashira.chefies.helper.Result
import com.fransbudikashira.chefies.ui.main.MainViewModel
import com.fransbudikashira.chefies.ui.signIn.SignInActivity
import com.fransbudikashira.chefies.util.loadImage
import com.fransbudikashira.chefies.util.moveActivityTo
import com.fransbudikashira.chefies.util.loadImageProfile
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel

    private val settingsViewModel: SettingsViewModel by viewModels {
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
        getUserProfile()

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // observe user profile
        observeViewModel()

        // set ViewModel
        viewModel = obtainViewModel(requireActivity() as AppCompatActivity)

        binding.btnLogout.setOnClickListener {
            showCustomDialogBox()
        }

        binding.changePasswordSetting.setOnClickListener {
            moveActivityTo(requireActivity(), ChangeProfileActivity::class.java)
        }

        binding.changeProfileSetting.setOnClickListener {
            moveActivityTo(requireActivity(), ChangeProfileActivity::class.java)
        }

        binding.languageSetting.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
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
    private fun showCustomDialogBox() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog_logout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.5f)

        val btnYes: MaterialButton = dialog.findViewById(R.id.btn_yes)
        val btnNo: MaterialButton = dialog.findViewById(R.id.btn_no)

        btnYes.setOnClickListener {
            viewModel.deleteToken()
            moveActivityTo(requireActivity(), SignInActivity::class.java, true)
        }

        btnNo.setOnClickListener { dialog.dismiss() }

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    private fun obtainViewModel(activity: AppCompatActivity): MainViewModel {
        val factory: AuthViewModelFactory = AuthViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory)[MainViewModel::class.java]
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
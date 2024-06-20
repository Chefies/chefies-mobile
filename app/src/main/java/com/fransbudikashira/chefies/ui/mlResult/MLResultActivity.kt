package com.fransbudikashira.chefies.ui.mlResult

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.data.factory.AuthViewModelFactory
import com.fransbudikashira.chefies.data.factory.MainViewModelFactory
import com.fransbudikashira.chefies.data.local.entity.HistoryEntity
import com.fransbudikashira.chefies.data.local.entity.RecipeBahasaEntity
import com.fransbudikashira.chefies.data.local.entity.RecipeEnglishEntity
import com.fransbudikashira.chefies.data.model.MLResultIngredients
import com.fransbudikashira.chefies.data.model.MLResultModel
import com.fransbudikashira.chefies.data.remote.response.RecipeResponse
import com.fransbudikashira.chefies.databinding.ActivityMlresultBinding
import com.fransbudikashira.chefies.helper.Result
import com.fransbudikashira.chefies.ui.adapter.IngredientItemAdapter
import com.fransbudikashira.chefies.ui.main.MainViewModel
import com.fransbudikashira.chefies.ui.result.ResultActivity
import com.fransbudikashira.chefies.ui.signIn.SignInActivity
import com.fransbudikashira.chefies.util.moveActivityTo
import com.fransbudikashira.chefies.util.prettierIngredientResult
import com.fransbudikashira.chefies.util.showToast
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MLResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMlresultBinding

    private var result: MLResultIngredients? = null
    private var isDetected: Boolean = false
    private lateinit var adapter: IngredientItemAdapter
    private val ingredients = mutableListOf<String>()

    private val viewModel: MLResultViewModel by viewModels {
        MainViewModelFactory.getInstance(this)
    }

    private val mainViewModel: MainViewModel by viewModels {
        AuthViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMlresultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.md_theme_primary)
        window.navigationBarColor = getColor(R.color.md_theme_background)

        with(binding) {
            // BackButton
            toAppBar.setNavigationOnClickListener { finish() }

            // Handle Add Ingredient Button
            btnAddIngredient.setOnClickListener {
                inputAddIngredient()
            }
            // Handle Add Ingredient when Enter Key Pressed
            edtIngredient.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    inputAddIngredient()
                    true
                } else { false }
            }
            // Handle Get Suggestions Button
            btnGetSuggestions.setOnClickListener {
                // Run Token Validation Mechanism
                tokenValidationMechanism()
            }
        }

        // setup
        setUp()
    }

    private fun tokenValidationMechanism() {
        lifecycleScope.launch {
            // Check Valid Token
            if (checkValidToken()) {
                getSuggestions()
                // - - -
                Log.d(TAG, "VALID TOKEN")
            } else {
                val email = mainViewModel.getEmail()
                val password = mainViewModel.getPassword()
                // Check Email & Password Available
                if (email.isEmpty() || password.isEmpty()) {
                    moveActivityTo(this@MLResultActivity, SignInActivity::class.java, true)
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
        mainViewModel.userLogin(email, password).observe(this@MLResultActivity) { userLoginResult ->
            when (userLoginResult) {
                is Result.Loading -> {}
                is Result.Success -> {
                    getSuggestions()
                    // - - -
                    Log.d(TAG, "LOGIN SUCCESS -> MOVE TO MAIN")
                }
                is Result.Error -> {
                    moveActivityTo(this@MLResultActivity, SignInActivity::class.java, true)
                    // - - -
                    Log.d(TAG, "LOGIN FAILED -> MOVE TO SIGN-IN")
                }
            }
        }
    }

    private suspend fun checkValidToken(): Boolean {
        return suspendCoroutine { continuation ->
            mainViewModel.getProfile().observe(this@MLResultActivity) { getProfileResult ->
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

    private fun getSuggestions() {

        lifecycleScope.launch {
            viewModel.getRecipes(ingredients).observe(this@MLResultActivity) { result ->
                when (result) {
                    is Result.Loading -> isLoading(true)
                    is Result.Success -> handleSuccess(result.data)
                    is Result.Error -> handleError(result.error)
                }
            }
        }
    }

    // handle success result get recipes from API
    private fun handleSuccess(data: RecipeResponse) {
        isLoading(false)
        val recipeBahasa = data.recipes[0]
        val recipeEnglish = data.recipes[1]
        val photoUrl = result?.photoUrl

        val historyEntity = HistoryEntity(
            title = "",
            photoUrl = photoUrl,
        )
        val recipeBahasaEntity = RecipeBahasaEntity(
            title = recipeBahasa.name,
            ingredients = recipeBahasa.ingredients,
            steps = recipeBahasa.steps,
            facts = recipeBahasa.facts
        )
        val recipeEnglishEntity = RecipeEnglishEntity(
            id = null,
            title = recipeEnglish.name,
            ingredients = recipeEnglish.ingredients,
            steps = recipeEnglish.steps,
            facts = recipeEnglish.facts
        )

        moveToResult(MLResultModel(historyEntity, listOf(recipeBahasaEntity), listOf(recipeEnglishEntity)))
    }

    // Dialog box failed to get photo
    private fun showFailedDialog() {
        val dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_failed_generate_recipe)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.5f)

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    // handle error result get recipes from API
    private fun handleError(error: String) {
        isLoading(false)
        Log.e("MLResultActivity", "Recipes Error: $error")
        showFailedDialog()
    }

    private fun setUp() {
        // Initial for Recycle View Layout
        val layoutManager = LinearLayoutManager(this)
        binding.rcIngredient.layoutManager = layoutManager
        // Get Intent Data
        @Suppress("DEPRECATION")
        result = intent.getParcelableExtra(EXTRA_RESULT)
        isDetected = intent.getBooleanExtra(EXTRA_DETECTED, false)

        if (isDetected) {
            val resultIngredients = result?.listIngredient!!
            resultIngredients.let {
                for (ingredient in it) {
                    ingredients.add(ingredient.prettierIngredientResult(this))
                }
            }
        } else {
            showFailedDetectIngredientsDialog()
        }

        // Set Content Item
        adapter = IngredientItemAdapter(
            updateCallback = { position, item -> updateIngredient(position, item) },
            deleteCallback = { position -> deleteIngredient(position) },
        )
        adapter.submitList(ingredients)
        binding.rcIngredient.adapter = adapter

        // Handle Button Enabled
        checkEnabledButton()
    }

    private fun updateIngredient(position: Int, item: String) {
        // Update Item
        ingredients[position] = item
        adapter.submitList(ingredients)
        binding.rcIngredient.adapter = adapter
    }

    private fun deleteIngredient(position: Int) {
        // Remote Item
        ingredients.removeAt(position)
        adapter.submitList(ingredients)
        binding.rcIngredient.adapter = adapter

        // Handle Button Enabled
        checkEnabledButton()
    }

    private fun inputAddIngredient() {
        val input = binding.edtIngredient.text?.trim().toString()
        if (input.length < 3) {
            binding.edtIngredientLayout.error = getString(R.string.invalid_min3_characters)
            return
        } else if (ingredients.contains(input)) {
            binding.edtIngredientLayout.error =
                getString(R.string.this_ingredient_already_added_txt)
            return
        } else {
            binding.edtIngredientLayout.error = null
            binding.edtIngredientLayout.isErrorEnabled = false
        }
        addItem(input)
    }

    private fun addItem(item: String) {
        // Clear Edit Text
        binding.edtIngredient.text?.clear()
        // Add Item
        ingredients.add(item)
        adapter.submitList(ingredients)
        binding.rcIngredient.adapter = adapter

        // Handle Button Enabled
        checkEnabledButton()
    }

    // Dialog box failed to detect ingredients
    private fun showFailedDetectIngredientsDialog() {
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

    private fun moveToResult(result: MLResultModel?) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_RESULT, result)
        startActivity(intent)
        finish()
    }

    private fun checkEnabledButton() {
        binding.btnGetSuggestions.isEnabled = ingredients.isNotEmpty()
    }

    private fun isLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnGetSuggestions.text = if (isLoading) "" else getString(R.string.get_suggestions)
        }
    }

    private fun enableEdgeToEdge() {
        // Enable edge-to-edge mode and make system bars transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars =
                false  // Change to false if you want light content (white icons) on the status bar
            isAppearanceLightNavigationBars =
                false  // Change to false if you want light content (white icons) on the navigation bar
        }
    }

    companion object {
        const val EXTRA_RESULT = "EXTRA_RESULT"
        const val EXTRA_DETECTED = "EXTRA_DETECTED"
        const val TAG = "MLResultActivity"
    }

}
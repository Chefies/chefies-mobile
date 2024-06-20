package com.fransbudikashira.chefies.ui.result

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.data.factory.AuthViewModelFactory
import com.fransbudikashira.chefies.data.factory.RecipeViewModelFactory
import com.fransbudikashira.chefies.data.local.entity.HistoryEntity
import com.fransbudikashira.chefies.data.model.MLResultModel
import com.fransbudikashira.chefies.data.local.entity.RecipeBahasaEntity
import com.fransbudikashira.chefies.data.local.entity.RecipeEnglishEntity
import com.fransbudikashira.chefies.data.remote.response.RecipeResponse
import com.fransbudikashira.chefies.databinding.ActivityResultBinding
import com.fransbudikashira.chefies.helper.Result
import com.fransbudikashira.chefies.ui.main.MainActivity
import com.fransbudikashira.chefies.ui.signIn.SignInActivity
import com.fransbudikashira.chefies.ui.splash.SplashViewModel
import com.fransbudikashira.chefies.util.getDefaultLanguage
import com.fransbudikashira.chefies.util.moveActivityTo
import com.fransbudikashira.chefies.util.moveTo
import com.fransbudikashira.chefies.util.showToast
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val viewModel: ResultViewModel by viewModels {
        RecipeViewModelFactory.getInstance(this)
    }
    private val splashViewModel: SplashViewModel by viewModels {
        AuthViewModelFactory.getInstance(this)
    }

    private var historyData = HistoryEntity(title = "")
    private val recipesBahasa = mutableListOf<RecipeBahasaEntity>()
    private val recipesEnglish = mutableListOf<RecipeEnglishEntity>()

    private var index = 0

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.md_theme_primary)
        window.navigationBarColor = getColor(R.color.md_theme_background)

        // BackButton
        binding.toAppBar.setNavigationOnClickListener {
            if (historyData.id == null) backButtonDialog()
            else this.moveTo(MainActivity::class.java, true)
        }

        @Suppress("DEPRECATION")
        val result: MLResultModel? = intent.getParcelableExtra(EXTRA_RESULT)

        if (result != null) setupData(result)
        setupView()
        playAnimation()
        Log.d(TAG, "index: $index | length: ${recipesEnglish.size}")

        binding.apply {
            // Handle Save Action
            saveButton.setOnClickListener {
                val title = editText.text.trim().toString()
                if (title.isEmpty() || title.length < 3) {
                    showSnackBar(getString(R.string.invalid_min3_characters))
                    return@setOnClickListener
                }
                historyData = historyData.copy(title = title)

                if (historyData.id == null) {
                    viewModel.addHistory(historyData) { historyId ->
                        // Save temporary data history id
                        historyData = historyData.copy(id = historyId)

                        // Insert new recipes (bahasa)
                        val modifiedRecipeBahasa = recipesBahasa.map { it.copy(historyId = historyId) }
                        viewModel.addRecipeBahasa(modifiedRecipeBahasa)

                        // Insert new recipes (english)
                        val modifiedRecipeEnglish = recipesEnglish.map { it.copy(historyId = historyId) }
                        viewModel.addRecipeEnglish(modifiedRecipeEnglish)
                    }
                } else {
                    // Update title History
                    viewModel.updateHistory(historyData)
                }

                // Activate Button Save
                saveButton.setImageDrawable(getDrawable(R.drawable.ic_save_success))
                showToast(getString(R.string.saved_text))
            }

            // Handle Retry Button
            retryButton.setOnClickListener {
                // Get Suggestions, at the end, add the recipes to temporary variable
                if (getDefaultLanguage() == "in") {
                    result?.recipeBahasaEntity?.let { tokenValidationMechanism(it.first().ingredients) }
                } else {
                    result?.recipeEnglishEntity?.let { tokenValidationMechanism(it.first().ingredients) }
                }

            }
            // Handle Next Button
            nextButton.setOnClickListener {
                if (index < recipesEnglish.size - 1) {
                    index += 1
                    displayedRecipes(index, recipesBahasa, recipesEnglish)
                    previousButton.visibility = View.VISIBLE
                    // If index already at the end, hide next button
                    if (index == recipesEnglish.size - 1) nextButton.visibility = View.GONE
                }
                Log.d(TAG, "index: $index | length: ${recipesEnglish.size}")
            }
            // Handle Previous Button
            previousButton.setOnClickListener {
                if (index > 0) {
                    index -= 1
                    displayedRecipes(index, recipesBahasa, recipesEnglish)
                    nextButton.visibility = View.VISIBLE
                    // if index already at the start, hide previous button
                    if (index == 0) previousButton.visibility = View.GONE
                }
                Log.d(TAG, "index: $index | length: ${recipesEnglish.size}")
            }

            // Check if Title is Changed, than change icon save Button
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString() != historyData.title)
                        saveButton.setImageDrawable(getDrawable(R.drawable.ic_save))
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        historyData = HistoryEntity(title = "")
        recipesBahasa.clear()
        recipesEnglish.clear()
    }

    private fun tokenValidationMechanism(ingredients: List<String>) {
        lifecycleScope.launch {
            // Check Valid Token
            if (checkValidToken()) {
                getSuggestions(ingredients)
                // - - -
                Log.d(TAG, "VALID TOKEN")
            } else {
                val email = splashViewModel.getEmail()
                val password = splashViewModel.getPassword()
                // Check Email & Password Available
                if (email.isEmpty() || password.isEmpty()) {
                    moveActivityTo(this@ResultActivity, SignInActivity::class.java, true)
                    // - - -
                    Log.d(TAG, "EMAIL & PASSWORD UN-AVAILABLE")
                } else {
                    // Do Login
                    doLogin(email, password, ingredients)
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

    private fun doLogin(email: String, password: String, ingredients: List<String>) {
        splashViewModel.userLogin(email, password).observe(this@ResultActivity) { userLoginResult ->
            when (userLoginResult) {
                is Result.Loading -> {}
                is Result.Success -> {
                    getSuggestions(ingredients)
                    // - - -
                    Log.d(TAG, "LOGIN SUCCESS -> MOVE TO MAIN")
                }
                is Result.Error -> {
                    moveActivityTo(this@ResultActivity, SignInActivity::class.java, true)
                    // - - -
                    Log.d(TAG, "LOGIN FAILED -> MOVE TO SIGN-IN")
                }
            }
        }
    }

    private suspend fun checkValidToken(): Boolean {
        return suspendCoroutine { continuation ->
            splashViewModel.getProfile().observe(this@ResultActivity) { getProfileResult ->
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

    private fun displayedRecipes(
        index: Int,
        recipesBahasa: List<RecipeBahasaEntity>,
        recipesEnglish: List<RecipeEnglishEntity>
    ) {
        if (getDefaultLanguage() == "in") {
            binding.apply {
                // Set Title
                title.text = recipesBahasa[index].title

                // Set List Ingredients
                ingredientsValue.text = recipesBahasa[index].ingredients.joinToString(", ")

                // Set List Steps
                recipesBahasa[index].steps.let { steps ->
                    // Prepend numbers to each step
                    val numberedSteps = steps.mapIndexed { index, step ->
                        "${index + 1}. ${step.replace("\\s+".toRegex(), "").trim()}"
                    }.toTypedArray()
                    val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                        this@ResultActivity, android.R.layout.simple_list_item_1, numberedSteps
                    )
                    stepsValue.adapter = arrayAdapter
                }
            }
        } else {
            binding.apply {
                // Set Title
                title.text = recipesEnglish[index].title

                // Set List Ingredients
                ingredientsValue.text = recipesEnglish[index].ingredients.joinToString(", ")

                // Set List Steps
                recipesEnglish[index].steps.let { steps ->
                    // Prepend numbers to each step
                    val numberedSteps = steps.mapIndexed { index, step -> "${index + 1}. $step" }.toTypedArray()
                    val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                        this@ResultActivity, android.R.layout.simple_list_item_1, numberedSteps
                    )
                    stepsValue.adapter = arrayAdapter
                }
            }
        }
    }

    private fun getSuggestions(ingredients: List<String>) {
        lifecycleScope.launch {
            viewModel.getRecipes(ingredients).observe(this@ResultActivity) {
                when (it) {
                    is Result.Loading -> showLoading(true)
                    is Result.Success -> handleSuccess(it.data)
                    is Result.Error -> handleError(it.error)
                }
            }
        }
    }

    // handle success result get recipes from API
    private fun handleSuccess(data: RecipeResponse) {
        showLoading(false)

        val recipeBahasa = data.recipes[0]
        val recipeEnglish = data.recipes[1]

        val recipeBahasaEntity = RecipeBahasaEntity(
            title = recipeBahasa.name,
            ingredients = recipeBahasa.ingredients,
            steps = recipeBahasa.steps,
            facts = recipeBahasa.facts
        )
        val recipeEnglishEntity = RecipeEnglishEntity(
            title = recipeEnglish.name,
            ingredients = recipeEnglish.ingredients,
            steps = recipeEnglish.steps,
            facts = recipeEnglish.facts
        )

        // Add to temporary variable
        recipesBahasa.add(recipeBahasaEntity)
        recipesEnglish.add(recipeEnglishEntity)

        // Add Recipe to Local, When user already saved
        if (historyData.id != null) {
            val modifiedRecipeBahasa = recipeBahasaEntity.copy(historyId = historyData.id)
            viewModel.addRecipeBahasa(listOf(modifiedRecipeBahasa))
            val modifiedRecipeEnglish = recipeEnglishEntity.copy(historyId = historyData.id)
            viewModel.addRecipeEnglish(listOf(modifiedRecipeEnglish))
        }

        // Control displayed recipes
        index = recipesEnglish.size - 1
        displayedRecipes(index, recipesBahasa, recipesEnglish)
        binding.previousButton.visibility = View.VISIBLE
        binding.nextButton.visibility = View.GONE

        Log.d(TAG, "index: $index | length: ${recipesEnglish.size}")
    }

    // handle error result get recipes from API
    private fun handleError(error: String) {
        showLoading(false)
        Log.e("MLResultActivity", "Recipes Error: $error")
        showToast(getString(R.string.failed_to_get_recipes_txt) + error)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            title.visibility = if (isLoading) View.GONE else View.VISIBLE
            ingredientsKey.visibility = if (isLoading) View.GONE else View.VISIBLE
            ingredientsValue.visibility = if (isLoading) View.GONE else View.VISIBLE
            stepsKey.visibility = if (isLoading) View.GONE else View.VISIBLE
            stepsValue.visibility = if (isLoading) View.GONE else View.VISIBLE

            retryButton.isEnabled = !isLoading
            nextButton.isEnabled = !isLoading
            previousButton.isEnabled = !isLoading

            if (isLoading) {
                retryButton.setTextColor(getColor(R.color.gray))
                retryButton.iconTint = getColorStateList(R.color.gray)

                nextButton.setTextColor(getColor(R.color.gray))
                nextButton.iconTint = getColorStateList(R.color.gray)

                previousButton.setTextColor(getColor(R.color.gray))
                previousButton.iconTint = getColorStateList(R.color.gray)
            } else {
                retryButton.setTextColor(getColor(R.color.md_theme_primary))
                retryButton.iconTint = getColorStateList(R.color.md_theme_primary)

                nextButton.setTextColor(getColor(R.color.md_theme_primary))
                nextButton.iconTint = getColorStateList(R.color.md_theme_primary)

                previousButton.setTextColor(getColor(R.color.md_theme_primary))
                previousButton.iconTint = getColorStateList(R.color.md_theme_primary)
            }
        }
    }

    private fun showSnackBar(message: String) {
        val snackBar = Snackbar.make(
            window.decorView.rootView,
            message,
            Snackbar.LENGTH_SHORT)
        snackBar.setBackgroundTint(getColor(R.color.red))
        snackBar.duration = 3000
        snackBar.show()
    }

    private fun backButtonDialog() {
        val dialog = Dialog(this@ResultActivity)
        dialog.setContentView(R.layout.custom_dialog_save_result)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.5f)

        val btnYes: MaterialButton = dialog.findViewById(R.id.btn_yes)
        val btnNo: MaterialButton = dialog.findViewById(R.id.btn_no)
        btnYes.setOnClickListener {
            dialog.dismiss()
            finish()
        }
        btnNo.setOnClickListener { dialog.dismiss() }

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    private fun setupView() {
        // Set ImageView
        val photoUrl = historyData.photoUrl
        Glide.with(this@ResultActivity)
            .load(photoUrl)
            .placeholder(R.drawable.empty_image)
            .error(R.drawable.empty_image)
            .into(binding.imageView)
        // Set Title
        binding.editText.setText(historyData.title)
        // Set Recipe
        if (getDefaultLanguage() == "in") {
            val recipeBahasa = recipesBahasa.first()
            binding.apply {
                // Set Title
                title.text = recipeBahasa.title

                // Set List Ingredients
                ingredientsValue.text = recipeBahasa.ingredients.joinToString(", ")

                // Set List Steps
                recipeBahasa.steps.let { steps ->
                    // Prepend numbers to each step
                    val numberedSteps = steps.mapIndexed { index, step -> "${index + 1}. $step" }.toTypedArray()
                    val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                        this@ResultActivity, android.R.layout.simple_list_item_1, numberedSteps
                    )
                    stepsValue.adapter = arrayAdapter
                }
            }
            if (recipesBahasa.size > 1) {
                binding.nextButton.visibility = View.VISIBLE
            }
        } else {
            val recipesEnglish = recipesEnglish.first()
            binding.apply {
                // Set Title
                title.text = recipesEnglish.title

                // Set List Ingredients
                ingredientsValue.text = recipesEnglish.ingredients.joinToString(", ")

                // Set List Steps
                recipesEnglish.steps.let { steps ->
                    // Prepend numbers to each step
                    val numberedSteps = steps.mapIndexed { index, step -> "${index + 1}. $step" }.toTypedArray()
                    val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                        this@ResultActivity, android.R.layout.simple_list_item_1, numberedSteps
                    )
                    stepsValue.adapter = arrayAdapter
                }
            }
            if (recipesBahasa.size > 1) {
                binding.nextButton.visibility = View.VISIBLE
            }
        }
    }

    private fun setupData(result: MLResultModel) {
        result.historyEntity?.let {
            historyData = historyData.copy(id = it.id, photoUrl = it.photoUrl, title = it.title)
        }
        recipesBahasa.addAll(result.recipeBahasaEntity)
        recipesEnglish.addAll(result.recipeEnglishEntity)
    }

    private fun playAnimation() {
        val image = ObjectAnimator.ofFloat(binding.cardView, View.ALPHA, 1f).apply {
            duration = 200
            startDelay = 300
        }
        val title = ObjectAnimator.ofFloat(binding.title, View.ALPHA, 1f).setDuration(200)
        val ingredientsKey = ObjectAnimator.ofFloat(binding.ingredientsKey, View.ALPHA, 1f).setDuration(200)
        val ingredientsValue = ObjectAnimator.ofFloat(binding.ingredientsValue, View.ALPHA, 1f).setDuration(200)
        val stepsKey = ObjectAnimator.ofFloat(binding.stepsKey, View.ALPHA, 1f).setDuration(200)
        val stepsValue = ObjectAnimator.ofFloat(binding.stepsValue, View.ALPHA, 1f).setDuration(200)

        val previousButton = ObjectAnimator.ofFloat(binding.previousButton, View.ALPHA, 1f).apply {
            duration = 500
            startDelay= 300
        }
        val verticalLineLeftOfRetry = ObjectAnimator.ofFloat(binding.verticalLineLeftOfRetry, View.ALPHA, 1f).apply {
            duration = 500
            startDelay= 300
        }
        val retryButton = ObjectAnimator.ofFloat(binding.retryButton, View.ALPHA, 1f).apply {
            duration = 500
            startDelay= 300
        }
        val verticalLineRightOfRetry = ObjectAnimator.ofFloat(binding.verticalLineRightOfRetry, View.ALPHA, 1f).apply {
            duration = 500
            startDelay= 300
        }
        val nextButton = ObjectAnimator.ofFloat(binding.nextButton, View.ALPHA, 1f).apply {
            duration = 500
            startDelay= 300
        }

        val bottomAction = AnimatorSet().apply {
            playTogether(image, previousButton, verticalLineLeftOfRetry, retryButton, verticalLineRightOfRetry, nextButton)
        }

        AnimatorSet().apply {
            playSequentially(bottomAction, title, ingredientsKey, ingredientsValue, stepsKey, stepsValue)
            start()
        }
    }

    private fun enableEdgeToEdge() {
        // Enable edge-to-edge mode and make system bars transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false  // Change to false if you want light content (white icons) on the status bar
            isAppearanceLightNavigationBars = false  // Change to false if you want light content (white icons) on the navigation bar
        }
    }

    companion object {
        const val EXTRA_RESULT = "extra_result"
        const val TAG = "ResultActivity"
    }
}
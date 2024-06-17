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
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.data.factory.RecipeViewModelFactory
import com.fransbudikashira.chefies.data.local.entity.HistoryEntity
import com.fransbudikashira.chefies.data.model.MLResultModel
import com.fransbudikashira.chefies.data.local.entity.RecipeBahasaEntity
import com.fransbudikashira.chefies.data.local.entity.RecipeEnglishEntity
import com.fransbudikashira.chefies.data.remote.response.RecipeResponse
import com.fransbudikashira.chefies.databinding.ActivityResultBinding
import com.fransbudikashira.chefies.helper.Result
import com.fransbudikashira.chefies.util.getDefaultLanguage
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val viewModel: ResultViewModel by viewModels {
        RecipeViewModelFactory.getInstance(this)
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
        window.statusBarColor = getColor(R.color.primary)
        window.navigationBarColor = getColor(R.color.white)


        // BackButton
        binding.toAppBar.setNavigationOnClickListener {
            if (historyData.id == null) backButtonDialog()
            else finish()
        }

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
                showToast("Saved")
            }

            // Handle Retry Button
            retryButton.setOnClickListener {
                // Get Suggestions, at the end, add the recipes to temporary variable
                if (getDefaultLanguage() == "in") {
                    result?.recipeBahasaEntity?.let { getSuggestions(it.first().ingredients) }
                } else {
                    result?.recipeEnglishEntity?.let { getSuggestions(it.first().ingredients) }
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
                    val numberedSteps = steps.mapIndexed { index, step -> "${index + 1}. $step" }.toTypedArray()
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
        )
        val recipeEnglishEntity = RecipeEnglishEntity(
            title = recipeEnglish.name,
            ingredients = recipeEnglish.ingredients,
            steps = recipeEnglish.steps,
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
        showToast("Failed to get recipes: $error")
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
                retryButton.setTextColor(getColor(R.color.primary))
                retryButton.iconTint = getColorStateList(R.color.primary)

                nextButton.setTextColor(getColor(R.color.primary))
                nextButton.iconTint = getColorStateList(R.color.primary)

                previousButton.setTextColor(getColor(R.color.primary))
                previousButton.iconTint = getColorStateList(R.color.primary)
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

    private fun showToast(message: String) {
        Toast.makeText(this@ResultActivity, message, Toast.LENGTH_SHORT).show()
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
            .into(binding.imageView)

        if (getDefaultLanguage() == "in") {
            val recipesBahasa = recipesBahasa.first()
            binding.apply {
                // Set Title
                title.text = recipesBahasa.title

                // Set List Ingredients
                ingredientsValue.text = recipesBahasa.ingredients.joinToString(", ")

                // Set List Steps
                recipesBahasa.steps.let { steps ->
                    // Prepend numbers to each step
                    val numberedSteps = steps.mapIndexed { index, step -> "${index + 1}. $step" }.toTypedArray()
                    val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                        this@ResultActivity, android.R.layout.simple_list_item_1, numberedSteps
                    )
                    stepsValue.adapter = arrayAdapter
                }
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
        }
    }

    private fun setupData(result: MLResultModel) {
        result.historyEntity?.let {
            historyData = historyData.copy(photoUrl = it.photoUrl, title = it.title)
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
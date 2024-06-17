package com.fransbudikashira.chefies.ui.mlResult

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fransbudikashira.chefies.R
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
import com.fransbudikashira.chefies.ui.result.ResultActivity
import com.fransbudikashira.chefies.util.prettierIngredientResult
import kotlinx.coroutines.launch

class MLResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMlresultBinding

    private lateinit var result: MLResultIngredients
    private lateinit var adapter: IngredientItemAdapter
    private val ingredients = mutableListOf<String>()

    private val viewModel: MLResultViewModel by viewModels {
        MainViewModelFactory.getInstance(this)
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
        window.statusBarColor = getColor(R.color.primary)
        window.navigationBarColor = getColor(R.color.white)

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
                getSuggestions()
            }
        }

        // setup
        setUp()
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
        showToast(getString(R.string.success_get_recipes_txt))
        val recipeBahasa = data.recipes[0]
        val recipeEnglish = data.recipes[1]
        val photoUrl = result.photoUrl

        val historyEntity = HistoryEntity(
            title = "",
            photoUrl = photoUrl,
        )
        val recipeBahasaEntity = RecipeBahasaEntity(
            title = recipeBahasa.name,
            ingredients = recipeBahasa.ingredients,
            steps = recipeBahasa.steps,
        )
        val recipeEnglishEntity = RecipeEnglishEntity(
            id = null,
            title = recipeEnglish.name,
            ingredients = recipeEnglish.ingredients,
            steps = recipeEnglish.steps,
        )

        moveToResult(MLResultModel(historyEntity, listOf(recipeBahasaEntity), listOf(recipeEnglishEntity)))
    }

    // handle error result get recipes from API
    private fun handleError(error: String) {
        isLoading(false)
        Log.e("MLResultActivity", "Recipes Error: $error")
        showToast(getString(R.string.failed_to_get_recipes_txt) + error)
    }

    private fun setUp() {
        // Initial for Recycle View Layout
        val layoutManager = LinearLayoutManager(this)
        binding.rcIngredient.layoutManager = layoutManager
        // Get Intent Data
        result = intent.getParcelableExtra(EXTRA_RESULT)!!
        val resultIngredients = result.listIngredient
        resultIngredients.let {
            for (ingredient in it) {
                ingredients.add(ingredient.prettierIngredientResult(this))
            }
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
    }

}
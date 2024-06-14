package com.fransbudikashira.chefies.ui.result

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.data.MyApplication
import com.fransbudikashira.chefies.data.factory.RecipeViewModelFactory
import com.fransbudikashira.chefies.data.local.entity.HistoryEntity
import com.fransbudikashira.chefies.data.model.MLResultModel
import com.fransbudikashira.chefies.data.local.entity.RecipeBahasaEntity
import com.fransbudikashira.chefies.data.local.entity.RecipeEnglishEntity
import com.fransbudikashira.chefies.databinding.ActivityResultBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val viewModel: ResultViewModel by viewModels {
        RecipeViewModelFactory((application as MyApplication).recipeRepository)
    }

    private var historyData: HistoryEntity? = null
    private val recipesBahasa = mutableListOf<RecipeBahasaEntity>()
    private val recipesEnglish = mutableListOf<RecipeEnglishEntity>()

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
        binding.toAppBar.setNavigationOnClickListener { backButtonDialog() }

        val result: MLResultModel? = intent.getParcelableExtra(EXTRA_RESULT)
        setupView(result)
        playAnimation()

        Log.d(TAG, "HistoryEntity: $historyData")
        Log.d(TAG, "RecipeBahasa: $recipesBahasa")
        Log.d(TAG, "RecipeEnglish: $recipesEnglish")

        binding.apply {
            // Handle Save Action
            binding.saveButton.setOnClickListener {
                val title = editText.text.trim().toString()
                if (title.isEmpty() || title.length < 3) {
                    showSnackbar(getString(R.string.invalid_min3_characters))
                    return@setOnClickListener
                }
                historyData = HistoryEntity(title = title)
                Log.d(TAG, "HistoryEntity: $historyData")

                if (historyData?.id == null) {
                    viewModel.addHistory(historyData!!) { historyId ->
                        historyData = historyData?.copy(id = historyId)
                        Log.d(TAG, "HistoryEntity: $historyData")

                        recipesBahasa.addAll(generateDummyRecipesBahasa(historyId))
                        viewModel.addRecipeBahasa(recipesBahasa)

                        recipesEnglish.addAll(generateDummyRecipesEnglish(historyId))
                        viewModel.addRecipeEnglish(recipesEnglish)
                    }
                } else {
                    viewModel.updateHistory(historyData!!)
                }

                // Activate Button Save
                saveButton.setImageDrawable(getDrawable(R.drawable.ic_save_success))
                showToast("Saved")
            }
        }

        val steps = arrayOf(
            "Wash the rice throughly and rinse it clean",
            "In pot, add the rice and water in a 1:2 ratio (1 cup rice; 2 cups water)",
            "While the rice is cooking, chop the tomatoes in to small cubes",
            "Once the rice cooked, fluff it with a fork. Stir in the chopped tomatoes and season with salt and pepper to taste.",
            "Serve the tomato rice hot and enjoy!"
        )
        // Prepend numbers to each step
        val numberedSteps = steps.mapIndexed { index, step -> "${index + 1}. $step" }.toTypedArray()
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, numberedSteps
        )

        binding.stepsValue.adapter = arrayAdapter
    }

    private fun generateDummyRecipesBahasa(historyId: Long): List<RecipeBahasaEntity> {
        return listOf(
            RecipeBahasaEntity(
                title = "Nasi Goreng",
                photoUrl = "link://photo".toUri(),
                ingredients = listOf("Nasi", "Bawang Merah", "Bawang Putih", "Cabai"),
                steps = listOf("Makan", "Tidur", "Bersih-bersih"),
                historyId = historyId
            ),
            RecipeBahasaEntity(
                title = "Nasi Goreng",
                photoUrl = "link://photo".toUri(),
                ingredients = listOf("Nasi", "Bawang Merah", "Bawang Putih", "Cabai"),
                steps = listOf("Makan", "Tidur", "Bersih-bersih"),
                historyId = historyId
            ),
        )
    }

    private fun generateDummyRecipesEnglish(historyId: Long): List<RecipeEnglishEntity> {
        return listOf(
            RecipeEnglishEntity(
                title = "Fried Rice",
                photoUrl = "link://photo".toUri(),
                ingredients = listOf("Rice", "Garlic", "Shallot", "Salt"),
                steps = listOf("Eat", "Sleep", "Clean-up"),
                historyId = historyId
            ),
            RecipeEnglishEntity(
                title = "Fried Rice",
                photoUrl = "link://photo".toUri(),
                ingredients = listOf("Rice", "Garlic", "Shallot", "Salt"),
                steps = listOf("Eat", "Sleep", "Clean-up"),
                historyId = historyId
            ),
        )
    }

    private fun showSnackbar(message: String) {
        val snackbar = Snackbar.make(
            window.decorView.rootView,
            message,
            Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(getColor(R.color.red))
        snackbar.duration = 3000
        snackbar.show()
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

    private fun setupView(result: MLResultModel?) {
        binding.apply {
            Glide.with(this@ResultActivity)
                .load(result?.photoUrl)
                .into(imageView)
            ingredientsValue.text = result?.ingredients?.joinToString(", ")
        }
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
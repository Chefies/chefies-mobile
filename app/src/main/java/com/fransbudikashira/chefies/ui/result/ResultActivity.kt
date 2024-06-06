package com.fransbudikashira.chefies.ui.result

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

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

        playAnimation()

        val steps = arrayOf(
            "Wash the rice throughly and rinse it clean",
            "In pot, add the rice and water in a 1:2 ratio (1 cup rice; 2 cups water)",
            "While the rice is cooking, chop the tomatoes in to small cubes",
            "Once the rice cooked, fluff it with a fork. Stir in the chopped tomatoes and season with salt and pepper to taste.",
            "Once the rice cooked, fluff it with a fork. Stir in the chopped tomatoes and season with salt and pepper to taste.",
            "Once the rice cooked, fluff it with a fork. Stir in the chopped tomatoes and season with salt and pepper to taste.",
            "Once the rice cooked, fluff it with a fork. Stir in the chopped tomatoes and season with salt and pepper to taste.",
            "Once the rice cooked, fluff it with a fork. Stir in the chopped tomatoes and season with salt and pepper to taste.",
            "Once the rice cooked, fluff it with a fork. Stir in the chopped tomatoes and season with salt and pepper to taste.",
            "Once the rice cooked, fluff it with a fork. Stir in the chopped tomatoes and season with salt and pepper to taste.",
            "Once the rice cooked, fluff it with a fork. Stir in the chopped tomatoes and season with salt and pepper to taste.",
            "Once the rice cooked, fluff it with a fork. Stir in the chopped tomatoes and season with salt and pepper to taste.",
            "Once the rice cooked, fluff it with a fork. Stir in the chopped tomatoes and season with salt and pepper to taste.",
            "Once the rice cooked, fluff it with a fork. Stir in the chopped tomatoes and season with salt and pepper to taste.",
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

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT = "extra_result"
    }

    private fun enableEdgeToEdge() {
        // Enable edge-to-edge mode and make system bars transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false  // Change to false if you want light content (white icons) on the status bar
            isAppearanceLightNavigationBars = false  // Change to false if you want light content (white icons) on the navigation bar
        }
    }
}
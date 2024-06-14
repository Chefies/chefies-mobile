package com.fransbudikashira.chefies.data.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fransbudikashira.chefies.data.repository.RecipeRepository
import com.fransbudikashira.chefies.di.Injection
import com.fransbudikashira.chefies.ui.result.ResultViewModel

class RecipeViewModelFactory(
    private val recipeRepository: RecipeRepository
): ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ResultViewModel::class.java) -> {
                ResultViewModel(recipeRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}
package com.fransbudikashira.chefies.data.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fransbudikashira.chefies.data.repository.MainRepository
import com.fransbudikashira.chefies.data.repository.RecipeRepository
import com.fransbudikashira.chefies.data.repository.UserRepository
import com.fransbudikashira.chefies.di.Injection
import com.fransbudikashira.chefies.ui.main.history.HistoryViewModel
import com.fransbudikashira.chefies.ui.result.ResultViewModel

class RecipeViewModelFactory(
    private val recipeRepository: RecipeRepository,
    private val mainViewRepository: MainRepository,
    private val userRepository: UserRepository
): ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ResultViewModel::class.java) -> {
                ResultViewModel(recipeRepository, mainViewRepository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(recipeRepository, userRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var instance: RecipeViewModelFactory? = null
        fun getInstance(context: Context): RecipeViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: RecipeViewModelFactory(
                    Injection.provideRecipeRepository(context),
                    Injection.provideMainRepository(context),
                    Injection.provideUserRepository(context)
                )
            }.also { instance = it }
    }
}
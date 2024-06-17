package com.fransbudikashira.chefies.ui.main.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fransbudikashira.chefies.data.repository.RecipeRepository
import com.fransbudikashira.chefies.data.repository.UserRepository
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository
): ViewModel() {

    fun getRecipeBahasa() {
        viewModelScope.launch {
            recipeRepository.getAllHistoryAndRecipesBahasa()
        }
    }

    fun getRecipeEnglish() {
        viewModelScope.launch {
            recipeRepository.getAllHistoryAndRecipesEnglish()
        }
    }
}
package com.fransbudikashira.chefies.ui.result

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fransbudikashira.chefies.data.local.entity.HistoryEntity
import com.fransbudikashira.chefies.data.local.entity.RecipeBahasaEntity
import com.fransbudikashira.chefies.data.local.entity.RecipeEnglishEntity
import com.fransbudikashira.chefies.data.repository.MainRepository
import com.fransbudikashira.chefies.data.repository.RecipeRepository
import kotlinx.coroutines.launch

class ResultViewModel(
    private val recipeRepository: RecipeRepository,
    private val mainRepository: MainRepository
): ViewModel() {

    fun getRecipes(ingredients: List<String>) =
        mainRepository.getRecipes(ingredients)

    fun addHistory(history: HistoryEntity, onResult: (Long) -> Unit) {
        viewModelScope.launch {
            val historyId = recipeRepository.addHistory(history)
            onResult(historyId)
        }
    }

    fun addRecipeEnglish(recipes: List<RecipeEnglishEntity>){
        viewModelScope.launch {
            recipeRepository.addRecipeEnglish(recipes)
        }
    }

    fun addRecipeBahasa(recipes: List<RecipeBahasaEntity>) {
        viewModelScope.launch {
            recipeRepository.addRecipeBahasa(recipes)
        }
    }

    fun updateHistory(history: HistoryEntity) {
        viewModelScope.launch {
            recipeRepository.updateHistory(history)
        }
    }

    companion object {
        const val TAG = "ResultViewModel"
    }
}
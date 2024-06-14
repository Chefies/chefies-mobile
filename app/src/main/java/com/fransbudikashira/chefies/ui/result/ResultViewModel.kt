package com.fransbudikashira.chefies.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fransbudikashira.chefies.data.local.entity.HistoryEntity
import com.fransbudikashira.chefies.data.local.entity.RecipeBahasaEntity
import com.fransbudikashira.chefies.data.local.entity.RecipeEnglishEntity
import com.fransbudikashira.chefies.data.repository.RecipeRepository
import kotlinx.coroutines.launch

class ResultViewModel(private val recipeRepository: RecipeRepository): ViewModel() {

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

    fun updateRecipeBahasa(recipes: List<RecipeBahasaEntity>) {
        viewModelScope.launch {
            recipeRepository.updateRecipeBahasa(recipes)
        }
    }

    fun updateRecipeEnglish(recipes: List<RecipeEnglishEntity>) {
        viewModelScope.launch {
            recipeRepository.updateRecipeEnglish(recipes)
        }
    }

    fun getHistoryById(id: Long) = recipeRepository.getHistoryById(id)
}
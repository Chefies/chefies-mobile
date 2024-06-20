package com.fransbudikashira.chefies.ui.main.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.fransbudikashira.chefies.data.local.entity.HistoryEntity
import com.fransbudikashira.chefies.data.local.entity.RecipeBahasaEntity
import com.fransbudikashira.chefies.data.local.entity.RecipeEnglishEntity
import com.fransbudikashira.chefies.data.repository.RecipeRepository

class HistoryViewModel(
    private val recipeRepository: RecipeRepository
): ViewModel() {

    fun getAllRecipeBahasaById(id: Long): LiveData<List<RecipeBahasaEntity>> = recipeRepository.getAllRecipesBahasaById(id)

    fun getAllRecipeEnglishById(id: Long): LiveData<List<RecipeEnglishEntity>> =  recipeRepository.getAllRecipesEnglishById(id)

    fun getHistories(): LiveData<List<HistoryEntity>> = recipeRepository.getHistories()
}
package com.fransbudikashira.chefies.ui.mlResult

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.fransbudikashira.chefies.data.remote.response.RecipeResponse
import com.fransbudikashira.chefies.data.repository.MainRepository
import com.fransbudikashira.chefies.data.repository.UserRepository

class MLResultViewModel(private val mainRepository: MainRepository) : ViewModel() {
    fun getRecipes(ingredients: List<String>) =
        mainRepository.getRecipes(ingredients)

}
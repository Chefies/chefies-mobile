package com.fransbudikashira.chefies.ui.mlResult

import androidx.lifecycle.ViewModel
import com.fransbudikashira.chefies.data.repository.MainRepository

class MLResultViewModel(private val mainRepository: MainRepository) : ViewModel() {

    fun getRecipes(ingredients: List<String>) =
        mainRepository.getRecipes(ingredients)
}
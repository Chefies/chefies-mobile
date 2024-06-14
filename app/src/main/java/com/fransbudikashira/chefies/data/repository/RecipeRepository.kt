package com.fransbudikashira.chefies.data.repository

import androidx.lifecycle.LiveData
import com.fransbudikashira.chefies.data.local.entity.*
import com.fransbudikashira.chefies.data.local.room.*

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val historyDao: HistoryDao,
) {
    fun getAllHistoryAndRecipesBahasa(): LiveData<List<HistoryAndRecipesBahasa>> {
        return historyDao.getAllHistoryAndRecipesBahasa()
    }

    fun getAllHistoryAndRecipesEnglish(): LiveData<List<HistoryAndRecipesEnglish>> {
        return historyDao.getAllHistoryAndRecipesEnglish()
    }

    fun getHistoryById(id: Long): LiveData<HistoryEntity> {
        return historyDao.getHistoryById(id)
    }

    suspend fun addHistory(history: HistoryEntity): Long {
        return historyDao.insertHistory(history)
    }

    suspend fun updateHistory(history: HistoryEntity) {
        historyDao.updateHistory(history)
    }

    suspend fun updateRecipeEnglish(recipes: List<RecipeEnglishEntity>) {
        recipeDao.updateRecipeEnglish(recipes)
    }

    suspend fun updateRecipeBahasa(recipes: List<RecipeBahasaEntity>) {
        recipeDao.updateRecipeBahasa(recipes)
    }

    suspend fun addRecipeEnglish(recipes: List<RecipeEnglishEntity>) {
        recipeDao.insertRecipeEnglish(recipes)
    }

    suspend fun addRecipeBahasa(recipes: List<RecipeBahasaEntity>) {
        recipeDao.insertRecipeBahasa(recipes)
    }
}
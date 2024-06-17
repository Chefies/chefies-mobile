package com.fransbudikashira.chefies.data.repository

import androidx.lifecycle.LiveData
import com.fransbudikashira.chefies.data.local.entity.*
import com.fransbudikashira.chefies.data.local.room.*

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val historyDao: HistoryDao,
) {
    fun getAllRecipesBahasaById(id: Long): LiveData<List<RecipeBahasaEntity>> {
        return historyDao.getAllRecipesBahasaById(id)
    }

    fun getAllRecipesEnglishById(id: Long): LiveData<List<RecipeEnglishEntity>> {
        return historyDao.getAllRecipesEnglishById(id)
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

    suspend fun addRecipeEnglish(recipes: List<RecipeEnglishEntity>) {
        recipeDao.insertRecipeEnglish(recipes)
    }

    suspend fun addRecipeBahasa(recipes: List<RecipeBahasaEntity>) {
        recipeDao.insertRecipeBahasa(recipes)
    }

    companion object {
        @Volatile
        private var instance: RecipeRepository? = null
        fun getInstance(
            recipeDao: RecipeDao,
            historyDao: HistoryDao
        ): RecipeRepository =
            instance ?: synchronized(this) {
                instance ?: RecipeRepository(recipeDao, historyDao)
            }.also { instance = it }
    }
}
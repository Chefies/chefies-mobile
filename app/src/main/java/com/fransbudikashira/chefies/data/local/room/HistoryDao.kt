package com.fransbudikashira.chefies.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fransbudikashira.chefies.data.local.entity.*

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long

    @Update
    suspend fun updateHistory(history: HistoryEntity)

    @Transaction
    @Query("SELECT * FROM recipe_bahasa WHERE history_id = :id ORDER BY id DESC")
    fun getAllRecipesBahasaById(id: Long): LiveData<List<RecipeBahasaEntity>>

    @Transaction
    @Query("SELECT * FROM recipe_english WHERE history_id = :id ORDER BY id DESC")
    fun getAllRecipesEnglishById(id: Long): LiveData<List<RecipeEnglishEntity>>
}
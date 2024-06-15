package com.fransbudikashira.chefies.data.local.room

import androidx.room.*
import com.fransbudikashira.chefies.data.local.entity.*

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipeEnglish(recipes: List<RecipeEnglishEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipeBahasa(recipes: List<RecipeBahasaEntity>)
}
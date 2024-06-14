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
    @Query("SELECT * FROM history WHERE id = :id")
    fun getHistoryById(id: Long): LiveData<HistoryEntity>

    @Transaction
    @Query("SELECT * FROM history ORDER BY id DESC")
    fun getAllHistoryAndRecipesBahasa(): LiveData<List<HistoryAndRecipesBahasa>>

    @Transaction
    @Query("SELECT * FROM history ORDER BY id DESC")
    fun getAllHistoryAndRecipesEnglish(): LiveData<List<HistoryAndRecipesEnglish>>
}
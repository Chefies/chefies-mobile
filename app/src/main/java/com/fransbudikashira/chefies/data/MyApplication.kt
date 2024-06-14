package com.fransbudikashira.chefies.data

import android.app.Application
import com.fransbudikashira.chefies.data.local.room.ChefiesDatabase
import com.fransbudikashira.chefies.data.repository.RecipeRepository

class MyApplication: Application() {
    val database by lazy { ChefiesDatabase.getDatabase(this) }
    val recipeRepository by lazy { RecipeRepository(database.recipeDao(), database.historyDao()) }
}
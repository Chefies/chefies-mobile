package com.fransbudikashira.chefies.data.local.room

import android.content.Context
import androidx.room.*
import com.fransbudikashira.chefies.data.local.entity.*
import com.fransbudikashira.chefies.helper.Converters

@Database(
    entities = [HistoryEntity::class, RecipeBahasaEntity::class, RecipeEnglishEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChefiesDatabase: RoomDatabase() {

    abstract fun historyDao(): HistoryDao
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var instance: ChefiesDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): ChefiesDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChefiesDatabase::class.java,
                    "chefies_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
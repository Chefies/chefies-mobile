package com.fransbudikashira.chefies.data.local.entity

import android.net.Uri
import androidx.room.*

@Entity(tableName = "recipe_english")
data class RecipeEnglishEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long? = null,

    @ColumnInfo(name = "name")
    val title: String,

    @ColumnInfo(name = "photo_url")
    val photoUrl: Uri,

    @ColumnInfo(name = "ingredients")
    val ingredients: List<String>,

    @ColumnInfo(name = "steps")
    val steps: List<String>,

    @ColumnInfo(name = "history_id")
    val historyId: Long
)
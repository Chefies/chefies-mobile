package com.fransbudikashira.chefies.data.local.entity

import androidx.room.*

@Entity(tableName = "history")
data class HistoryEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long? = null,

    @ColumnInfo(name = "title")
    val title: String
)

data class HistoryAndRecipesBahasa(
    @Embedded
    val history: HistoryEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "history_id"
    )
    val recipes: List<RecipeBahasaEntity>
)

data class HistoryAndRecipesEnglish(
    @Embedded
    val history: HistoryEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "history_id"
    )
    val recipes: List<RecipeEnglishEntity>
)
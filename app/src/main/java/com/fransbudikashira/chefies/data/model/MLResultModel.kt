package com.fransbudikashira.chefies.data.model

import android.os.Parcelable
import com.fransbudikashira.chefies.data.local.entity.HistoryEntity
import com.fransbudikashira.chefies.data.local.entity.RecipeBahasaEntity
import com.fransbudikashira.chefies.data.local.entity.RecipeEnglishEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class MLResultModel(
    val historyEntity: HistoryEntity? = null,
    val recipeBahasaEntity: RecipeBahasaEntity,
    val recipeEnglishEntity: RecipeEnglishEntity,
) : Parcelable

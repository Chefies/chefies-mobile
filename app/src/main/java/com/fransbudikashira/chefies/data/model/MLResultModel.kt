package com.fransbudikashira.chefies.data.model

import android.net.Uri
import android.os.Parcelable
import com.fransbudikashira.chefies.data.remote.response.RecipesItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class MLResultModel(
    val photoUrl: Uri,
    val listRecipes: List<RecipesItem>,
): Parcelable

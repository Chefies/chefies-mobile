package com.fransbudikashira.chefies.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MLResultIngredients(
    val photoUrl: Uri,
    val listIngredient: List<String>?
) : Parcelable
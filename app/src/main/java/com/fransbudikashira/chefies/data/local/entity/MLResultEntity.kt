package com.fransbudikashira.chefies.data.local.entity

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MLResultEntity(
    val photoUrl: Uri,
    val ingredients: List<String>,
): Parcelable

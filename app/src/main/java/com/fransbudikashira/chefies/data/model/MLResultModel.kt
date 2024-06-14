package com.fransbudikashira.chefies.data.model

import android.net.Uri
import android.os.Parcelable
import com.fransbudikashira.chefies.data.local.entity.HistoryEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class MLResultModel(
    val photoUrl: Uri,
    val ingredients: List<String>,

): Parcelable

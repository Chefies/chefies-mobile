package com.fransbudikashira.chefies.data.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class RecipeResponse(

	@field:SerializedName("error")
	val error: Boolean,

	@field:SerializedName("recipes")
	val recipes: List<RecipesItem>
)

@Parcelize
data class RecipesItem(

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("ingredients")
	val ingredients: List<String>,

	@field:SerializedName("lang")
	val lang: String,

	@field:SerializedName("steps")
	val steps: List<String>
): Parcelable

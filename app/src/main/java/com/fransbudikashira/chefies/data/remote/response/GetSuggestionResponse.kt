package com.fransbudikashira.chefies.data.remote.response

import com.google.gson.annotations.SerializedName

data class GetSuggestionResponse(

    @field:SerializedName("listLanguage")
    val listLanguage: List<LanguageItem>,

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)

data class LanguageItem(
    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("listSuggestion")
    val listSuggestion: List<SuggestionItem>? = null
)

data class SuggestionItem(
    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("title")
    val title: String,

    @field:SerializedName("ingredients")
    val ingredients: List<String>,

    @field:SerializedName("steps")
    val steps: List<String>,
)
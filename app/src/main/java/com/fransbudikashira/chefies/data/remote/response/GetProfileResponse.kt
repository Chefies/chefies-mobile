package com.fransbudikashira.chefies.data.remote.response

import com.google.gson.annotations.SerializedName

data class GetProfileResponse (

    @field: SerializedName("detail")
    val detail: String,

    @field: SerializedName("email")
    val email: String,

    @field: SerializedName("name")
    val name: String,

    @field: SerializedName("avatar")
    val avatar: String,
)
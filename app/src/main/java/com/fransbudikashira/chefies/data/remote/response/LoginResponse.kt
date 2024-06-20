package com.fransbudikashira.chefies.data.remote.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(

    @field:SerializedName("token")
    val token: String,

    @field:SerializedName("detail")
    val detail: String,
)
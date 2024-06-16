package com.fransbudikashira.chefies.data.remote.request

import com.google.gson.annotations.SerializedName

data class PasswordUpdateRequest(
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("old_password") val oldPassword: String
)

package com.example.sumte

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("loginId")
    val loginId: String,

    val password: String
)

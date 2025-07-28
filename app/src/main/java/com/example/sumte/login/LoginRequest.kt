package com.example.sumte.login

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("loginId")
    val loginId: String,

    val password: String
)
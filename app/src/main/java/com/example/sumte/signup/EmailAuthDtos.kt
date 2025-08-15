package com.example.sumte.signup

data class EmailSendReq(val email: String)
data class EmailSendRes(
    val success: Boolean,
    val message: String,
    val cooldownRemainingSeconds: Int? = null
)

data class EmailVerifyReq(val email: String, val code: String)
data class EmailVerifyRes(val success: Boolean, val message: String)
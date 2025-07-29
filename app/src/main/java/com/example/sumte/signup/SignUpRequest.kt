package com.example.sumte.signup

data class SignUpRequest(
    val password: String,
    val loginId: String,
    val name: String,
    val nickname: String,
    val phoneNumber: String,
    val gender: String,       // "MAN" 또는 "WOMAN"
    val birthday: String,     // "YYYY-MM-DD"
    val email: String
)
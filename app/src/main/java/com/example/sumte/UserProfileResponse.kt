package com.example.sumte

data class UserProfileResponse(
    val loginId: String,
    val name: String,
    val nickname: String,
    val phoneNumber: String,
    val gender: String,
    val birthday: String,
    val email: String,
    val status: String,
    val inActiveDate: String
)

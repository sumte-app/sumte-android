package com.example.sumte.roomregister

data class RoomRegisterRequest(
    val name: String,
    val content: String,
    val price: Int,
    val checkin: Time,
    val checkout: Time,
    val standartCount: Int,
    val totalCount: Int,
    val imageUrl: String
)

data class Time(
    val hour: Int,
    val minute: Int,
    val second: Int,
    val nano: Int
)

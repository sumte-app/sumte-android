package com.example.sumte.housedetail

data class RoomInfo(
    val name: String,
    val price: Int,
    val person: Int,
    val maxPerson: Int,
    val checkInTime: String,
    val checkOutTime: String,
    val imageResId: Int
)
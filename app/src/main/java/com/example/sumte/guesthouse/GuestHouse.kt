package com.example.sumte.guesthouse

data class GuestHouse(
    val title: String,
    val location: String,
    val price: String,
    val imageResId: Int,
    val time: String,
    val id: Long,
    val imageUrl: String? = null
)

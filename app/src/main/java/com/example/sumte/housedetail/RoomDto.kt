package com.example.sumte.housedetail

data class RoomDto(
    val id: Long,
    val name: String,
    val content: String?,
    val price: Int,
    val checkin: String,
    val checkout: String,
    val standardCount: Int,
    val totalCount: Int,
    val imageUrls: String?
)

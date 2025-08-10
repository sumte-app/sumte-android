package com.example.sumte.housedetail

data class GuesthouseDto(
    val id: Int,
    val name: String,
    val rating: Double?,
    val reviewCount: Int?,
    val addressRegion: String?,
    val addressDetail: String?,
    val information: String?,
    val advertisement: String?,
    val optionServices: List<String>?,
    val targetAudience: List<String>?,
    val rooms: List<RoomDto>?,
    val imageUrls: List<String>?
)


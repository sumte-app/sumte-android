package com.example.sumte.like

import com.example.sumte.guesthouse.RoomDto

data class LikeListResponse (
    val content: List<GuestHouseResponse>,
    val totalPages: Int,
    val totalElements: Int
)

data class GuestHouseResponse(
    val id: Long,
    val name: String?,               // ← nullable
    val addressRegion: String?,      // ← nullable
    val addressDetail: String?,      // ← nullable
    val information: String?,
    val minPrice: Int?,
    val advertisement: String?,
    val optionServices: List<String>?,   // ← nullable
    val targetAudience: List<String>?,   // ← nullable
    val rooms: List<RoomDto>?,           // ← nullable
    val imageUrls: List<String>?         // ← nullable
)
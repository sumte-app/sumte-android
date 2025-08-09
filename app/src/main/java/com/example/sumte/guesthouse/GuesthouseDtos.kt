package com.example.sumte.guesthouse

data class TimeDto(
    val hour: Int,
    val minute: Int,
    val second: Int,
    val nano: Int
)

data class RoomDto(
    val id: Long,
    val name: String,
    val price: Int,
    val standardCount: Int,
    val totalCount: Int,
    val content: String,
    val checkin: TimeDto,
    val checkout: TimeDto,
    val imageUrl: String
)

data class GuesthouseDto(
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

data class PageDto<T>(
    val content: List<T> = emptyList(),
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val number: Int = 0,
    val size: Int = 0,
    val first: Boolean = false,
    val last: Boolean = false
)
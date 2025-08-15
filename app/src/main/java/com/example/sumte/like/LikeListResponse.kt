package com.example.sumte.like

import com.example.sumte.GuesthouseSummaryDto
import com.example.sumte.guesthouse.RoomDto
import com.google.gson.annotations.SerializedName

data class LikeListResponse (
    val content: List<GuestHouseResponse>,
    val totalPages: Int,
    val totalElements: Int
)

// 찜 목록 API의 전체 응답을 감싸는 클래스
data class LikedGuesthouseResponse(
    val content: List<LikedItemDto>
)

data class LikedItemDto(
    @SerializedName("guesthouseId")
    val id: Int,
    @SerializedName("guesthouseName")
    val name: String?,
    val addressRegion: String?,
    val addressDetail: String?,
    val information: String?,
    val minPrice: Int?,
    val advertisement: String?,
    val optionServices: List<String>?,
    val targetAudience: List<String>?,
    val rooms: List<RoomDto>?,
    val imageUrls: List<String>?,
    val reviewCount: Int?,
    val averageScore: Double?,
    val checkInTime: String?
)

data class GuestHouseResponse(
    @SerializedName("guesthouseId")
    val id: Int,
    @SerializedName("guesthouseName")
    val name: String?,
    val addressRegion: String?,
    val addressDetail: String?,
    val information: String?,
    val minPrice: Int?,
    val advertisement: String?,
    val optionServices: List<String>?,
    val targetAudience: List<String>?,
    val rooms: List<RoomDto>?,
    val imageUrls: List<String>?,
    val reviewCount: Int?,
    val averageScore: Double?,
    val checkInTime: String?
)
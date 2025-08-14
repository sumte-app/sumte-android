package com.example.sumte.like

import com.example.sumte.guesthouse.RoomDto
import com.google.gson.annotations.SerializedName

data class LikeListResponse (
    val content: List<GuestHouseResponse>,
    val totalPages: Int,
    val totalElements: Int
)

// 찜 목록 아이템의 상세 정보를 담는 데이터 클래스
data class LikedGuesthouse(
    @SerializedName("guestHouseId")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("addressRegion")
    val addressRegion: String?,
    // 이미지 필드명은 서버 응답에 따라 imageUrl 또는 imageUrls 등으로 맞춰야 합니다.
    // 여기서는 thumbnailUrl 이라는 이름으로 가정하겠습니다.
    @SerializedName("imageUrl") // 만약 여러 개라면 List<String> 타입의 imageUrls
    val thumbnailUrl: String?,
    @SerializedName("averageScore")
    val averageScore: Double?,
    @SerializedName("reviewCount")
    val reviewCount: Int?,
    @SerializedName("checkinTime")
    val checkInTime: String?,
    @SerializedName("minPrice")
    val minPrice: Int?
)

// 찜 목록 API의 전체 응답을 감싸는 클래스
data class LikedGuesthouseResponse(
    val content: List<LikedGuesthouse>
    // 페이지 정보 등 다른 필드가 있다면 여기에 추가
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
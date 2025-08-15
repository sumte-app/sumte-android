package com.example.sumte

import com.google.gson.annotations.SerializedName

class ApiUtils {
}

data class ApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T
)

// 새로운 API의 data 부분에 해당하는 클래스
data class GuesthouseSummaryDto(
    val id: Int,
    val name: String?,
    @SerializedName("addressRegion")
    val addressRegion: String?,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("minPrice")
    val minPrice: Int?,
    @SerializedName("checkin")
    val checkInTime: String?,
    val reviewCount: Int?,
    val averageScore: Double?,
)
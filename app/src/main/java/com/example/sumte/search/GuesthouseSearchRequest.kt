package com.example.sumte.search


@kotlinx.parcelize.Parcelize
data class GuesthouseSearchRequest(
    val viewEnableReservation: Boolean? = null,
    val checkIn: String? = null,
    val checkOut: String? = null,
    val people: Int? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val minPeople: Int? = null,
    val maxPeople: Int? = null,
    val keyword: String? = null,
    val optionService: List<String>? = null,
    val targetAudience: List<String>? = null,
    val region: List<String>? = null
) : android.os.Parcelable

data class TimeDto(val hour: Int, val minute: Int, val second: Int, val nano: Int)

data class GuesthouseItemResponse(
    val id: Long,
    val name: String?,
    val averageScore: Double?,
    val reviewCount: Int?,
    val lowerPrice: Int?,
    val addressRegion: String?,
    val checkinTime: String?,
    val imageUrl: String?
)

data class ApiPageResponse<T>(
    val success: Boolean,
    val code: String?,
    val message: String?,
    val data: PageData<T>?
)

data class PageData<T>(
    val totalElements: Int,
    val totalPages: Int,
    val size: Int,
    val content: List<T>,
    val number: Int,
    val numberOfElements: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)

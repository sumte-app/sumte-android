package com.example.sumte.housedetail

import com.google.gson.annotations.SerializedName

// 게스트하우스 객실 목록 조회
data class RoomDto(
    val id: Int,
    val name: String,
    val price: Int,
    val standardCount: Int,
    val totalCount: Int,
    val checkin: String,
    val checkout: String,
    val imageUrl: String?,
    val reservable: Boolean
)

// 객실 단건 조회
data class RoomDetailDto(
    val id: Int,
    val name: String,
    val price: Int,
    val standardCount: Int,
    val totalCount: Int,
    val content: String,
    val checkin: String,
    val checkout: String,
    val imageUrls: List<String>?
)



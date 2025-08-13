package com.example.sumte.housedetail

import com.google.gson.annotations.SerializedName

data class RoomDto(
    val id: Int,
    val name: String,
    val content: String?,
    val price: Int,
    val checkin: String,
    val checkout: String,
    val standardCount: Int,
    val totalCount: Int,
    @SerializedName("imageUrls")
    val imageUrls: List<String>? = null,
)

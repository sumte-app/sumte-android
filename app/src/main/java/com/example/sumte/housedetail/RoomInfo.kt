package com.example.sumte.housedetail

data class RoomInfo(
    val id: Int,               // 서버에서 받아오는 roomId
    val name: String,
    val price: Int,
    val checkin: String,
    val checkout: String,
    val standardCount: Int,
    val totalCount: Int,
    val imageUrl: String?,
    val reservable: Boolean? = null
)

data class RoomDetailInfo(            // 단건용 (여러 장)
    val id: Int,
    val name: String,
    val price: Int,
    val standardCount: Int,
    val totalCount: Int,
    val content: String,
    val checkin: String,
    val checkout: String,
    val imageUrls: List<String>
)


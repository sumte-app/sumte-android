package com.example.sumte.housedetail

data class RoomInfo(
    val id: Int,               // 서버에서 받아오는 roomId
    val name: String,
    val content: String,
    val price: Int,
    val checkin: String,
    val checkout: String,
    val standardCount: Int,
    val totalCount: Int,
    val imageUrl: String?
)

//data class RoomInfo(
//    val name: String,
//    val content: String,
//    val price: Int,
//    val checkin: String,
//    val checkout: String,
//    val standardCount: Int,
//    val totalCount: Int,
//    val imageUrl: String?
//)
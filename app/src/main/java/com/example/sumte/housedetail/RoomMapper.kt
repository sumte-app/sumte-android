package com.example.sumte.housedetail

fun RoomDto.toRoomInfo(): RoomInfo {
    return RoomInfo(
        name = name,
        content = content.orEmpty(),
        price = price,
        checkin = trimSec(checkin),
        checkout = trimSec(checkout),
        standardCount = standardCount,
        totalCount = totalCount,
        imageUrl = imageUrls
    )
}

private fun trimSec(s: String): String {
    return if (s.count { it == ':' } == 2 && s.endsWith(":00")) s.dropLast(3) else s
}

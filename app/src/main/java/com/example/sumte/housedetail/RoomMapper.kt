package com.example.sumte.housedetail

fun RoomDto.toRoomInfo(): RoomInfo {
    return RoomInfo(
        id = id,
        name = name,
        content = content.orEmpty(),
        price = price,
        checkin = trimSec(checkin),
        checkout = trimSec(checkout),
        standardCount = standardCount,
        totalCount = totalCount,
        imageUrl = imageUrls?.firstOrNull()  // ✅ 수정
    )
}


fun GuesthouseDto.toGuesthouseInfo(): GuesthouseInfo =
    GuesthouseInfo(
        name = name,
        address = listOfNotNull(addressRegion, addressDetail)
            .filter { it.isNotBlank() }
            .joinToString(" "),
        imageUrls = imageUrls ?: emptyList()
    )

private fun trimSec(s: String): String {
    return if (s.count { it == ':' } == 2 && s.endsWith(":00")) s.dropLast(3) else s
}

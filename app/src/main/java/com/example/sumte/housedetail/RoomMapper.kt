package com.example.sumte.housedetail

import android.util.Log

fun RoomDto.toRoomInfo(): RoomInfo {
    Log.d("RoomMapper", "dto.imageUrls=${this.imageUrl}")
    return RoomInfo(
        id = id,
        name = name,
        //content = content.orEmpty(),
        price = price,
        checkin = trimSec(checkin),
        checkout = trimSec(checkout),
        standardCount = standardCount,
        totalCount = totalCount,
        imageUrl = imageUrl.asUrlOrNull(),
        reservable = reservable
    )
}

fun RoomDetailDto.toRoomDetailInfo(): RoomDetailInfo = RoomDetailInfo(
    id = id,
    name = name,
    price = price,
    standardCount = standardCount,
    totalCount = totalCount,
    content = content,
    checkin = trimSec(checkin),
    checkout = trimSec(checkout),
    imageUrls = imageUrls.normalizeUrls()
)




fun GuesthouseDto.toGuesthouseInfo(): GuesthouseInfo =
    GuesthouseInfo(
        name = name,
        address = listOfNotNull(addressRegion, addressDetail)
            .filter { it.isNotBlank() }
            .joinToString(" "),
        imageUrls = imageUrls ?: emptyList(),
        averageScore = averageScore,
        reviewCount = reviewCount,
        maxPeople = maxPeople
    )

private fun trimSec(s: String): String {
    return if (s.count { it == ':' } == 2 && s.endsWith(":00")) s.dropLast(3) else s
}


private fun String?.asUrlOrNull(): String? {
    val t = this?.trim() ?: return null
    if (t.isEmpty() || t.equals("null", true)) return null
    return t
}

// 단건 리스트 정제
private fun List<String>?.normalizeUrls(): List<String> =
    this.orEmpty()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.equals("null", true) }
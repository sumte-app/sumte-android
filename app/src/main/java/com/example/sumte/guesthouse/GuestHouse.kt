package com.example.sumte.guesthouse

import com.example.sumte.R
import com.example.sumte.search.GuesthouseItemResponse

data class GuestHouse(
    val title: String,
    val location: String,
    val price: String,
    val imageResId: Int,
    val time: String,
    val id: Long,
    val imageUrl: String? = null
)

private fun formatCheckin(s: String?): String {
    if (s.isNullOrBlank()) return ""
    // "HH:mm" 또는 "HH:mm:ss" 형태만 포맷
    val hhmm = Regex("""^(\d{1,2}):(\d{2})(?::\d{2})?$""")
    val m = hhmm.matchEntire(s.trim())
    return if (m != null) {
        val h = m.groupValues[1].toInt()
        val min = m.groupValues[2].toInt()
        "%02d:%02d 체크인".format(h, min)
    } else {
        s // 형식이 다르면 그대로 표시
    }
}

fun List<GuesthouseItemResponse>.toUi(): List<GuestHouse> =
    map { d ->
        GuestHouse(
            id = d.id,
            title = d.name ?: "-",
            location = d.addressRegion.orEmpty(),
            price = d.lowerPrice?.let { "%,d원".format(it) } ?: "가격 정보 없음",
            imageResId = R.drawable.sumte_logo1,
            time = formatCheckin(d.checkinTime),   // ✅ 변경
            imageUrl = d.imageUrl
        )
    }

private fun mapToUi(d: GuesthouseDto): GuestHouse {
    val rooms = d.rooms.orEmpty()
    val minPrice = rooms.minOfOrNull { it.price } ?: 0
    val thumb = d.imageUrls?.firstOrNull() ?: rooms.firstOrNull()?.imageUrl
    return GuestHouse(
        id = d.id?.toLong() ?: 0L,
        title = d.name ?: "-",
        location = listOfNotNull(d.addressRegion, d.addressDetail)
            .filter { !it.isNullOrBlank() }.joinToString(" "),
        price = if (minPrice > 0) "%,d원".format(minPrice) else "가격 정보 없음",
        imageUrl = thumb,
        imageResId = R.drawable.sumte_logo1,
        time = ""
    )
}

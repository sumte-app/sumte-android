package com.example.sumte.search

import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.ZoneId

class BookInfoViewModel : ViewModel() {
    val seoulZone = ZoneId.of("Asia/Seoul")

    var startDate: LocalDate = LocalDate.now(seoulZone)
    var endDate: LocalDate = LocalDate.now(seoulZone).plusDays(1)

    var adultCount: Int = 1
    var childCount: Int = 0
}

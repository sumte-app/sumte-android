package com.example.sumte.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import java.time.LocalDate
import java.time.ZoneId

class BookInfoViewModel(application: Application) : AndroidViewModel(application) {
    val seoulZone = ZoneId.of("Asia/Seoul")
    var startDate: LocalDate = LocalDate.now(seoulZone)
    var endDate: LocalDate = LocalDate.now(seoulZone).plusDays(1)
    var adultCount: Int = 1
    var childCount: Int = 0
    var roomImageUrl: String? = null
    var keyword: String? = null
}
//바인딩 유지보수 용이를 위해 따로 관리
package com.example.sumte.common

import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.example.sumte.App
import com.example.sumte.search.BookInfoViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

fun getBookInfoViewModel(): BookInfoViewModel {
    return ViewModelProvider(
        App.instance,
        ViewModelProvider.AndroidViewModelFactory.getInstance(App.instance)
    )[BookInfoViewModel::class.java]
}

fun bindBookInfoUI(binding: Any, viewModel: BookInfoViewModel) {
    val seoulZone = ZoneId.of("Asia/Seoul")
    val formatter = DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN)


    val startDate = viewModel.startDate ?: LocalDate.now(seoulZone)
    val endDate = viewModel.endDate ?: LocalDate.now(seoulZone).plusDays(1)
    val nights = ChronoUnit.DAYS.between(startDate, endDate)
    val adultCount = viewModel.adultCount
    val childCount = viewModel.childCount

    val startDateView = binding.javaClass.getDeclaredField("startDate").get(binding) as? android.widget.TextView
    val endDateView = binding.javaClass.getDeclaredField("endDate").get(binding) as? android.widget.TextView
    val dateCountView = binding.javaClass.getDeclaredField("dateCount").get(binding) as? android.widget.TextView
    val adultCountView = binding.javaClass.getDeclaredField("adultCount").get(binding) as? android.widget.TextView
    val childCountView = binding.javaClass.getDeclaredField("childCount").get(binding) as? android.widget.TextView
    val countCommaView = binding.javaClass.getDeclaredField("countComma").get(binding) as? View

    startDateView?.text = startDate.format(formatter)
    endDateView?.text = endDate.format(formatter)
    dateCountView?.text = "${nights}박"
    adultCountView?.text = "성인 $adultCount"
    childCountView?.text = if (childCount > 0) "아동 $childCount" else ""
    countCommaView?.visibility = if (childCount > 0) View.VISIBLE else View.GONE

}



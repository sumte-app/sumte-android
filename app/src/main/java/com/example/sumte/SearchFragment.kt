package com.example.sumte

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sumte.databinding.FragmentSearchBinding
import java.time.LocalDate
import java.time.ZoneId

class SearchFragment : Fragment() {
    lateinit var binding: FragmentSearchBinding

    val seoulZone = ZoneId.of("Asia/Seoul")
    private var startDate: LocalDate? = LocalDate.now(seoulZone)
    private var endDate: LocalDate? = LocalDate.now(seoulZone).plusDays(1)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bookInfo.setOnClickListener {
            val intent = Intent(requireContext(), BookInfoActivity::class.java)
            startActivity(intent)
        }
        fun getKoreanDayOfWeek(date: LocalDate): String {
            return when (date.dayOfWeek) {
                java.time.DayOfWeek.MONDAY -> "월"
                java.time.DayOfWeek.TUESDAY -> "화"
                java.time.DayOfWeek.WEDNESDAY -> "수"
                java.time.DayOfWeek.THURSDAY -> "목"
                java.time.DayOfWeek.FRIDAY -> "금"
                java.time.DayOfWeek.SATURDAY -> "토"
                java.time.DayOfWeek.SUNDAY -> "일"
            }
        }

        val currentDay = LocalDate.now(seoulZone)
        val dayOfWeekKor = getKoreanDayOfWeek(currentDay)
        val endPlusOne = currentDay.plusDays(1)
        val endDayOfWeekKor = getKoreanDayOfWeek(endPlusOne)

        binding.startDate.text = String.format("%d.%02d %s", currentDay.monthValue, currentDay.dayOfMonth, dayOfWeekKor)
        binding.endDate.text = String.format("%d.%02d %s,", endPlusOne.monthValue, endPlusOne.dayOfMonth, endDayOfWeekKor)

    }
}
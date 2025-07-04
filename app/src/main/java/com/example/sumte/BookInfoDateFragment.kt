package com.example.sumte

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sumte.databinding.CalendarDayLayoutBinding
import com.example.sumte.databinding.FragmentBookInfoDateBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth

class BookInfoDateFragment : Fragment() {
    lateinit var binding: FragmentBookInfoDateBinding

    private var startDate: LocalDate? = LocalDate.now()
    private var endDate: LocalDate? = LocalDate.now().plusDays(1)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentBookInfoDateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
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

        binding.customCalendar.dayBinder = object : MonthDayBinder<DayViewContainer>{
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                val date = data.date
                container.textView.text = date.dayOfMonth.toString()
                container.textView.setTypeface(null, Typeface.BOLD)

                container.textView.setBackgroundResource(0)
                container.textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                container.textView.alpha = 1f
                container.textView.isClickable = true

                //선택 가능한 때 정의
                val selected = when {
                    startDate != null && endDate != null ->
                        !date.isBefore(startDate) && !date.isAfter(endDate)
                    startDate != null -> date == startDate
                    else -> false
                }
                if (selected) {
                    if (startDate != null && endDate != null) {//범위선택
                        when (date) {
                            startDate -> container.textView.setBackgroundResource(R.drawable.selcted_circle_start)
                            endDate -> container.textView.setBackgroundResource(R.drawable.selcted_circle_end)
                            in (startDate!!..endDate!!) -> container.textView.setBackgroundResource(R.drawable.selcted_middle)
                            else -> container.textView.background = null
                        }
                    } else if (startDate != null) {
                        if (date == startDate) {
                            container.textView.setBackgroundResource(R.drawable.selected_circle)
                        } else {
                            container.textView.background = null
                        }
                    } else {
                        container.textView.background = null
                    }
                } else {
                    container.textView.background = null
                }

                //일요일. 공휴일은 api도입 고려중
                if (date.dayOfWeek == java.time.DayOfWeek.SUNDAY) {
                    container.textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                }
                //오늘 이전
                if (date.isBefore(LocalDate.now())) {
                    container.textView.alpha = 0.3f
                    container.textView.isClickable = false
                    return
                }
                //오늘
                if (date == LocalDate.now()) {
                    val isNotSelected =
                        !(startDate != null && date == startDate) &&
                                !(endDate != null && date == endDate) &&
                                !(startDate != null && endDate != null && date in (startDate!!..endDate!!))
                    if (isNotSelected) {
                        container.textView.setBackgroundResource(R.drawable.today_circle)
                    }
                }

                container.textView.setOnClickListener {
                    val clickedDate = date
                    if (startDate != null && endDate == null && clickedDate == startDate) {
                        return@setOnClickListener
                    }

                    when {
                        startDate == null || endDate != null -> {
                            startDate = clickedDate
                            endDate = null
                        }
                        clickedDate.isBefore(startDate) -> {
                            startDate = clickedDate
                        }
                        else -> {
                            endDate = clickedDate
                        }
                    }

                    startDate?.let {
                        val startDayKor = getKoreanDayOfWeek(it)
                        binding.startDate.text = String.format("%d.%02d %s", it.monthValue, it.dayOfMonth, startDayKor)
                    }
                    if (endDate != null) {
                        endDate?.let {
                            val endDayKor = getKoreanDayOfWeek(it) 
                            binding.endDate.text = String.format("%d.%02d %s", it.monthValue, it.dayOfMonth, endDayKor)
                        }
                        val nights = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate)
                        binding.dateCount.text = "${nights}박"
                    } else {
                        binding.endDate.text = ""
                        binding.dateCount.text = ""
                    }
                    binding.customCalendar.notifyCalendarChanged()
                }
            }
        }
        binding.customCalendar.apply {
            val currentMonth = YearMonth.now()
            val firstMonth = currentMonth
            val lastMonth = currentMonth.plusMonths(240)
            val firstDayOfWeek = firstDayOfWeekFromLocale()

            setup(firstMonth, lastMonth, firstDayOfWeek)
            scrollToMonth(currentMonth)

            monthScrollListener = { calendarMonth: CalendarMonth ->
                val year = calendarMonth.yearMonth.year
                val month = calendarMonth.yearMonth.monthValue
                binding.todayMonthText.text = String.format("%d.%02d", year, month)
            }
        }
        
        //추후보수
        val currentDay = LocalDate.now()
        val currentMonth = YearMonth.now()
        val dayOfWeekKor = getKoreanDayOfWeek(currentDay)
        val endPlusOne = currentDay.plusDays(1)
        val endDayOfWeekKor = getKoreanDayOfWeek(endPlusOne)

        binding.startDate.text = String.format("%d.%02d %s", currentDay.monthValue, currentDay.dayOfMonth, dayOfWeekKor)
        binding.endDate.text = String.format("%d.%02d %s,", endPlusOne.monthValue, endPlusOne.dayOfMonth, endDayOfWeekKor)
        binding.todayMonthText.text = String.format("%d.%02d", currentMonth.year, currentMonth.monthValue)
    }
}
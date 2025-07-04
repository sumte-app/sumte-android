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

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null

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
                    container.textView.setBackgroundResource(R.drawable.today_circle)
                }
                //선택 가능한 때 정의
                val selected = when {
                    startDate != null && endDate != null ->
                        !date.isBefore(startDate) && !date.isAfter(endDate)
                    startDate != null -> date == startDate
                    else -> false
                }

                if (selected) {
                    if (startDate != null && endDate != null) {
                        when (date) {
                            startDate -> container.textView.setBackgroundResource(R.drawable.selcted_circle_start)
                            endDate -> container.textView.setBackgroundResource(R.drawable.selcted_circle_end)
                            in (startDate!!..endDate!!) -> container.textView.setBackgroundResource(R.drawable.selcted_middle)
                            else -> container.textView.background = null
                        }
                    } else if (startDate != null) {
                        // startDate만 있을 때는 단일 선택이므로 전체 둥근 원형 배경
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


                container.textView.setOnClickListener {
                    val clickedDate = date
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

        val currentMonth = YearMonth.now()
        binding.todayMonthText.text = String.format("%d.%02d", currentMonth.year, currentMonth.monthValue)
    }
}
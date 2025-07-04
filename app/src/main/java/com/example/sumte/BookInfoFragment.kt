package com.example.sumte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.sumte.databinding.CalendarDayLayoutBinding
import com.example.sumte.databinding.FragmentBookInfoBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class BookInfoFragment : Fragment() {
    lateinit var binding: FragmentBookInfoBinding

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentBookInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
        }

        binding.customCalendar.dayBinder = object : MonthDayBinder<DayViewContainer>{
            override fun create(view: View) = DayViewContainer(view)
            // 컨테이너가 재사용될 때마다 호출

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                // DayViewContainer의 day 변수에 data 할당
                //container.day = data
                container.textView.text = data.date.dayOfMonth.toString()
            }
        }
        binding.customCalendar.apply {
            val currentMonth = YearMonth.now()
            val firstMonth = currentMonth.minusMonths(240)
            val lastMonth = currentMonth.plusMonths(240)
            val firstDayOfWeek = firstDayOfWeekFromLocale()

            setup(firstMonth, lastMonth, firstDayOfWeek)
            scrollToMonth(currentMonth)
        }
        val daysOfWeek = daysOfWeek()

    }

}
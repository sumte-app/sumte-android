package com.example.sumte

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sumte.databinding.CalendarDayLayoutBinding
import com.example.sumte.databinding.FragmentBookInfoBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth

class BookInfoFragment : Fragment() {
    lateinit var binding: FragmentBookInfoBinding

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

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                val date = data.date
                container.textView.text = date.dayOfMonth.toString()
                container.textView.setTypeface(null, Typeface.BOLD)

                //선택불가
                if (date.isBefore(LocalDate.now())) {
                    container.textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray400)) // 흐린색
                    container.textView.alpha = 0.5f
                    container.textView.isClickable = false
                    return
                }//오늘
                if (date == LocalDate.now()) {
                    container.textView.setBackgroundResource(R.drawable.today_circle)
                    container.textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }
                //공휴일은 추후 외부 api연동 고민좀
                val isSunday = date.dayOfWeek == java.time.DayOfWeek.SUNDAY
                if (isSunday) {
                    container.textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.red)) // 빨간색
                }
                else {
                    container.textView.setBackgroundResource(0) // 배경 제거
                    container.textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
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

        }


    }

}
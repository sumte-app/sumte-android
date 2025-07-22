package com.example.sumte

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.sumte.databinding.CalendarDayLayoutBinding
import com.example.sumte.databinding.FragmentBookInfoDateBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.ZoneId
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class BookInfoDateFragment : Fragment() {
    lateinit var binding: FragmentBookInfoDateBinding
    private val viewModel: BookInfoViewModel by activityViewModels()

    val seoulZone = ZoneId.of("Asia/Seoul")
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

    private var currentYearMonth: YearMonth = YearMonth.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

            val formatter = DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN)
            startDate = viewModel.startDate ?: LocalDate.now(seoulZone)
            endDate = viewModel.endDate ?: LocalDate.now(seoulZone).plusDays(1)

            val nights = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate)

            binding.startDate.text = startDate!!.format(formatter)
            binding.endDate.text = endDate!!.format(formatter)
            binding.dateCount.text = "${nights}박"

            val adultCount = viewModel.adultCount
            val childCount = viewModel.childCount

            binding.adultCount.text = "성인 $adultCount"
            binding.childCount.text = if (childCount > 0) "아동 $childCount" else ""

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
                if (date.isBefore(LocalDate.now(seoulZone))) {
                    container.textView.alpha = 0.3f
                    container.textView.isClickable = false
                    return
                }
                //오늘
                if (date == LocalDate.now(seoulZone)) {
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

                    //val formatter = DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN)

                    startDate?.let {
                        binding.startDate.text = it.format(formatter)
                    }

                    if (endDate != null) {
                        endDate?.let {
                            binding.endDate.text = it.format(formatter)
                        }
                        val nights = ChronoUnit.DAYS.between(startDate, endDate)
                        binding.dateCount.text = "${nights}박"
                    } else {
                        binding.endDate.text = ""
                        binding.dateCount.text = ""
                    }

                    if (startDate != null && endDate != null) {
                        viewModel.startDate = startDate!!
                        viewModel.endDate = endDate!!
                    }
                    binding.customCalendar.notifyCalendarChanged()
                }
            }
        }
        binding.customCalendar.apply {
            val currentMonth = YearMonth.now(seoulZone)
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

        //화살표 클릭 시 달 이동
        binding.leftBtn.setOnClickListener {
            currentYearMonth = currentYearMonth.minusMonths(1)
            binding.customCalendar.scrollToMonth(currentYearMonth)
        }

        binding.rightBtn.setOnClickListener {
            currentYearMonth = currentYearMonth.plusMonths(1)
            binding.customCalendar.scrollToMonth(currentYearMonth)
        }

        binding.countChangeBar.setOnClickListener {
            val fragment = BookInfoCountFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.applyBtn.setOnClickListener {
            val fragment = SearchResultFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, fragment)
                .addToBackStack(null)
                .commit()
        }

//        binding.applyBtn.setOnClickListener {
//            val intent = Intent(requireContext(), SearchResultActivity::class.java)
//            startActivity(intent)
//        }
    }
}
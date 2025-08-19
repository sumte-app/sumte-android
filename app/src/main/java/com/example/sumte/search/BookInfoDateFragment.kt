package com.example.sumte.search

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.replace
import androidx.lifecycle.ViewModelProvider
import com.example.sumte.App
import com.example.sumte.R
import com.example.sumte.common.bindBookInfoUI
import com.example.sumte.common.getBookInfoViewModel
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
    private val viewModel by lazy { getBookInfoViewModel() }

    private val seoulZone = ZoneId.of("Asia/Seoul")
    private var startDate: LocalDate? = viewModel.startDate
    private var endDate: LocalDate? = viewModel.endDate
    private var currentYearMonth: YearMonth = YearMonth.now()

    private var source: String? = null
    private var guesthouseId: Int? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentBookInfoDateBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //게하예약가능 날짜 적용
        source = activity?.intent?.getStringExtra(BookInfoActivity.EXTRA_SOURCE)
        Log.d("guesthouseId_source","${source}")
        guesthouseId = activity?.intent?.getIntExtra("guesthouseId", -1)?.takeIf { it > 0 }
        Log.d("guesthouseId","${guesthouseId}")

        val formatter = DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN)
        bindBookInfoUI(binding, viewModel)

        if (source == "houseDetail" && guesthouseId != null) {
            // 예: 예약 가능한 날짜 불러오기
        }

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
                container.textView.setTextColor(ContextCompat.getColor(requireContext(),
                    R.color.black
                ))
                container.textView.alpha = 1f
                container.textView.isClickable = true

                //houseDetail에서 온 경우
                if (source == "houseDetail") {
                    // API에서 받은 예약가능/불가능 날짜 반영x
                }

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
                            in (startDate!!..endDate!!) -> container.textView.setBackgroundResource(
                                R.drawable.selcted_middle
                            )
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
                    container.textView.setTextColor(ContextCompat.getColor(requireContext(),
                        R.color.red
                    ))
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

                    binding.dateComma.visibility = if (endDate != null) View.VISIBLE else View.GONE
                    binding.customCalendar.notifyCalendarChanged()
                }
            }
        }//여기까지 캘린더 내부 bind

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
            if (startDate != null && endDate != null) {
                viewModel.startDate = startDate!!
                viewModel.endDate = endDate!!
            }
            val fragment = BookInfoCountFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        //적용할 때 뷰모델 저장
        binding.applyBtn.setOnClickListener {
            if (startDate != null && endDate != null) {
                viewModel.startDate = startDate!!
                viewModel.endDate = endDate!!
            }
            (binding.root.context as? BookInfoActivity)?.onApplyClicked()
        }
        //새 캔슬버튼
        binding.cancelBtn.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            if (fragmentManager.backStackEntryCount > 0) {
                // 같은 액티비티의 이전 프래그먼트로 돌아감
                fragmentManager.popBackStack()
            } else {
                // 다른 액티비티에서 왔다면 현재 액티비티 종료
                requireActivity().finish()
            }
        }

    }



}
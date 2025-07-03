package com.example.sumte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sumte.databinding.FragmentBookInfoBinding
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
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        startDate = today
        endDate = tomorrow
        //updateSummary()

        val firstMonth = YearMonth.from(today).minusMonths(12)
        val lastMonth = YearMonth.from(tomorrow).plusMonths(12)
        val todayYearMonth = YearMonth.now()

        binding.customCalendar.setup(firstMonth, lastMonth, DayOfWeek.SUNDAY)
        binding.customCalendar.scrollToMonth(todayYearMonth)  //현재 월로 이동



    }


}
package com.example.sumte.search

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumte.R
import com.example.sumte.databinding.FragmentSearchBinding
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class SearchFragment : Fragment() {
    lateinit var binding: FragmentSearchBinding
    private val viewModel: BookInfoViewModel by activityViewModels()

    val seoulZone = ZoneId.of("Asia/Seoul")
    private var startDate: LocalDate? = LocalDate.now(seoulZone)
    private var endDate: LocalDate? = LocalDate.now(seoulZone).plusDays(1)


    //리사이클러뷰 dummy
    private val historyList = listOf(
        History("애월 게스트하우스", "6.18 수", "6.19 목", 1, 1),
        History("월정리 해변", "7.01 월", "7.03 수", 2, 0),   // 아동 없음, endDate 없음
        History("월정리 해변 게스트하우스", "8.05 화", "8.06 수", 1, 0)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val formatter = DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN)
        startDate = viewModel.startDate ?: LocalDate.now(seoulZone)
        endDate = viewModel.endDate ?: LocalDate.now(seoulZone).plusDays(1)
        val nights = ChronoUnit.DAYS.between(startDate, endDate)
        binding.startDate.text = startDate!!.format(formatter)
        binding.endDate.text = endDate!!.format(formatter)
        binding.dateCount.text = "${nights}박"

        val adultCount = viewModel.adultCount
        val childCount = viewModel.childCount
        binding.adultCount.text = "성인 $adultCount"
        if (childCount > 0) {
            binding.childCount.visibility = View.VISIBLE
            binding.childCount.text = "아동 $childCount"
        } else {
            binding.childCount.visibility = View.GONE
        }

        //검색창
        binding.searchText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

                val keyword = binding.searchText.text.toString()

                if (keyword.isNotBlank()) {
                    val fragment = SearchResultFragment().apply {
                        arguments = Bundle().apply {
                            putString(BookInfoActivity.EXTRA_KEYWORD, keyword)
                        }
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.book_info_container, fragment)
                        .addToBackStack(null)
                        .commit()

                }

                true
            } else {
                false
            }
        }

        //히스토리 리스이클러뷰
        val adapter = HistoryAdapter(historyList)
        binding.historyRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerview.adapter = adapter


        binding.dateChangeBar.setOnClickListener {
            val fragment = BookInfoDateFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.countChangeBar.setOnClickListener {
            val fragment = BookInfoCountFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, fragment)
                .addToBackStack(null)
                .commit()
        }


        binding.backBtn.setOnClickListener {
            requireActivity().setResult(AppCompatActivity.RESULT_OK)
            requireActivity().finish()
        }
    }
}
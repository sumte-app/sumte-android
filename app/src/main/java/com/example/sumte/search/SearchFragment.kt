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

        startDate?.let { start ->
            endDate?.let { end ->
                val nights = ChronoUnit.DAYS.between(start, end)

                binding.startDate.text = start.format(formatter)
                binding.endDate.text = end.format(formatter)
                binding.dateCount.text = "${nights}박"
            }
        }

        binding.searchText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

                val keyword = binding.searchText.text.toString()

                if (keyword.isNotBlank()) {
                    val intent = Intent(requireContext(), BookInfoActivity::class.java).apply {
                        putExtra(BookInfoActivity.EXTRA_FRAGMENT_TYPE, BookInfoActivity.TYPE_SEARCH_RESULT)
                        putExtra(BookInfoActivity.EXTRA_KEYWORD, keyword)
                    }
                    startActivity(intent)
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
            val intent = Intent(requireContext(), BookInfoActivity::class.java).apply {
                putExtra(BookInfoActivity.EXTRA_FRAGMENT_TYPE, BookInfoActivity.TYPE_DATE)
            }
            startActivity(intent)
        }

        binding.countChangeBar.setOnClickListener {
            val intent = Intent(requireContext(), BookInfoActivity::class.java).apply {
                putExtra(BookInfoActivity.EXTRA_FRAGMENT_TYPE, BookInfoActivity.TYPE_COUNT)
            }
            startActivity(intent)
        }


        binding.backBtn.setOnClickListener {
            requireActivity().setResult(AppCompatActivity.RESULT_OK)
            requireActivity().finish()
        }
    }
}
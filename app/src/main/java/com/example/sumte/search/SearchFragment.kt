package com.example.sumte.search

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumte.App
import com.example.sumte.R
import com.example.sumte.databinding.FragmentSearchBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyList: MutableList<History>
    private val viewModel by lazy {
        ViewModelProvider(
            App.instance,
            ViewModelProvider.AndroidViewModelFactory.getInstance(App.instance)
        )[BookInfoViewModel::class.java]
    }
    private val seoulZone = ZoneId.of("Asia/Seoul")
    private var startDate: LocalDate? = LocalDate.now(seoulZone)
    private var endDate: LocalDate? = LocalDate.now(seoulZone).plusDays(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        historyList = loadHistoryList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.history.visibility = if (loadHistoryVisibility()) View.VISIBLE else View.GONE

        historyAdapter = HistoryAdapter(
            historyList,
            { updatedList ->
                // 아이템이 있으면 뷰 보이게, 없으면 숨기고 상태 저장
                val isVisible = updatedList.isNotEmpty()
                binding.history.visibility = if (isVisible) View.VISIBLE else View.GONE
                saveHistoryList(updatedList, isVisible)
            },
            {
                // 0개 됐을 때 콜백 (혹시 별도 처리 필요하면)
                binding.history.visibility = View.GONE
                saveHistoryList(emptyList(), false)
            }
        )
        binding.historyRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerview.adapter = historyAdapter

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

        // 검색창에서 엔터 시 히스토리 추가 및 저장, SearchResultFragment 이동
        binding.searchText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

                val keyword = binding.searchText.text.toString()

                // SearchFragment.kt
                if (keyword.isNotBlank()) {
                    val newHistory = History(
                        keyword = keyword,
                        startDate = binding.startDate.text.toString(),
                        endDate = binding.endDate.text.toString(),
                        adultCount = viewModel.adultCount,
                        childCount = viewModel.childCount
                    )

                    if (historyAdapter.contains(newHistory)) {
                        historyAdapter.removeItem(newHistory)
                    }

                    historyAdapter.addItem(newHistory)
                    historyAdapter.trimToMaxSize(10)

                    Log.d("SearchFragment", "현재 historyList 상태:")
                    historyList.forEachIndexed { index, history ->
                        Log.d("SearchFragment", "$index: $history")
                    }

                    // 화면 전환
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

        binding.allDeleteBtn.setOnClickListener {
            historyAdapter.clearAll()
            saveHistoryList(emptyList())
            binding.history.visibility = View.GONE
        }
    }

    private fun saveHistoryList(list: List<History>, isHistoryVisible: Boolean = true) {
        val prefs = requireContext().getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(list)
        editor.putString("history_list", json)
        editor.putBoolean("history_visible", isHistoryVisible)
        editor.apply()
    }
    private fun loadHistoryVisibility(): Boolean {
        val prefs = requireContext().getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("history_visible", true) // 기본값은 보여짐
    }


    private fun loadHistoryList(): MutableList<History> {
        val prefs = requireContext().getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("history_list", null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<History>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
}

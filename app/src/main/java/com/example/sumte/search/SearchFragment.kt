package com.example.sumte.search

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumte.R
import com.example.sumte.common.bindBookInfoUI
import com.example.sumte.common.getBookInfoViewModel
import com.example.sumte.databinding.FragmentSearchBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyList: MutableList<History>
    private val viewModel by lazy { getBookInfoViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), android.R.color.white)

        WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        ).isAppearanceLightStatusBars = true

        historyList = loadHistoryList()
        // 최초 진입 시 저장된 가시성 불러오기
        loadHistoryVisibility()
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
        // 날짜/인원 UI 바인딩(네가 이미 쓰던 함수)
        bindBookInfoUI(binding, viewModel)

        // --- 히스토리 가시성 ---
        binding.history.visibility = if (loadHistoryVisibility()) View.VISIBLE else View.GONE

        // --- 히스토리 어댑터 ---
        historyAdapter = HistoryAdapter(
            historyList,
            saveHistory = { updatedList ->
                val isVisible = updatedList.isNotEmpty()
                binding.history.visibility = if (isVisible) View.VISIBLE else View.GONE
                saveHistoryList(updatedList, isVisible)
            },
            onEmptyList = {
                binding.history.visibility = View.GONE
                saveHistoryList(emptyList(), false)
            }
        )

        binding.historyRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerview.adapter = historyAdapter

        // --- 검색창 설정 ---
        binding.searchText.apply {
            setSingleLine(true)
            setHorizontallyScrolling(true)
            inputType = InputType.TYPE_CLASS_TEXT
            imeOptions = EditorInfo.IME_ACTION_SEARCH

            setOnEditorActionListener { _, actionId, event ->
                val isIme =
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                            actionId == EditorInfo.IME_ACTION_DONE ||
                            actionId == EditorInfo.IME_NULL
                val isEnterKey = event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                        event.action == KeyEvent.ACTION_UP
                if (isIme || isEnterKey) {
                    submitSearch()
                    true
                } else {
                    false
                }
            }

            // 일부 키보드 보강
            setOnKeyListener { _, keyCode, keyEvent ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                    submitSearch()
                    true
                } else {
                    false
                }
            }
        }

        // --- 날짜/인원 변경 화면 이동 ---
        binding.dateChangeBar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, BookInfoDateFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.countChangeBar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, BookInfoCountFragment())
                .addToBackStack(null)
                .commit()
        }

        // --- 뒤로가기 ---
        binding.backBtn.setOnClickListener {
            requireActivity().setResult(AppCompatActivity.RESULT_OK)
            requireActivity().finish()
        }

        // --- 히스토리 전체 삭제 ---
        binding.allDeleteBtn.setOnClickListener {
            historyAdapter.clearAll()
            saveHistoryList(emptyList(), false)
        }
    }

    private fun submitSearch() {
        val keyword = binding.searchText.text?.toString()?.trim().orEmpty()
        if (keyword.isBlank()) return

        // 화면 상의 날짜/인원 값 읽기
        val checkIn = binding.startDate.text?.toString()?.takeIf { it.isNotBlank() }
        val checkOut = binding.endDate.text?.toString()?.takeIf { it.isNotBlank() }
        val people = (viewModel.adultCount + viewModel.childCount).takeIf { it > 0 }

        // --- 최근 검색어 저장/정리 ---
        val newHistory = History(
            keyword = keyword,
            startDate = checkIn ?: "",
            endDate = checkOut ?: "",
            adultCount = viewModel.adultCount,
            childCount = viewModel.childCount
        )
        if (historyAdapter.contains(newHistory)) historyAdapter.removeItem(newHistory)
        historyAdapter.addItem(newHistory)
        historyAdapter.trimToMaxSize(10)

        // --- 키보드 내리기 ---
        hideKeyboard()

        // --- 검색 요청 객체 구성 ---
        // 서버가 이름/지역 통합 검색을 지원한다면 keyword만 넣어도 되고,
        // 지역까지 시도하고 싶으면 region=listOf(keyword) 같이 보낸다.
        val request = GuesthouseSearchRequest(
            keyword = keyword,
            region = listOf(keyword),        // 서버 로직에 맞게 필요 없으면 null로
            checkIn = checkIn,
            checkOut = checkOut,
            people = people
        )

        // --- 검색 결과 화면으로 이동 ---
        val fragment = SearchResultFragment().apply {
            arguments = Bundle().apply {
                putString("keyword", keyword)                    // 기존 키
                putParcelable("searchRequest", request)          // ✅ 새로 전달: 바로 서버 검색 가능
                // 호환용(혹시 쓰고 있으면)
                putString(BookInfoActivity.EXTRA_KEYWORD, keyword)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.book_info_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun hideKeyboard() {
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun saveHistoryList(list: List<History>, isHistoryVisible: Boolean = true) {
        val prefs = requireContext().getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("history_list", Gson().toJson(list))
            .putBoolean("history_visible", isHistoryVisible)
            .apply()
    }

    private fun loadHistoryVisibility(): Boolean {
        val prefs = requireContext().getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("history_visible", true)
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

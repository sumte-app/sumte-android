package com.example.sumte.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.FilterOptions
import com.example.sumte.R
import com.example.sumte.common.bindBookInfoUI
import com.example.sumte.common.getBookInfoViewModel
import com.example.sumte.databinding.FragmentSearchResultBinding
import com.example.sumte.guesthouse.GuestHouseAdapter
import com.example.sumte.guesthouse.GuestHouseViewModel
import com.example.sumte.guesthouse.UiState
import kotlinx.coroutines.launch
import java.time.LocalDate

class SearchResultFragment : Fragment() {

    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    private val bookInfoViewModel by lazy { getBookInfoViewModel() }
    private val ghViewModel: GuestHouseViewModel by viewModels()

    private lateinit var adapter: GuestHouseAdapter
    private var loading = false
    private var keyword: String? = null

    private fun parseDate(dateStr: String): LocalDate {
        val currentYear = LocalDate.now().year
        val parts = dateStr.split(" ")
        val md = parts[0].split(".")
        return LocalDate.of(currentYear, md[0].toInt(), md[1].toInt())
    }

    private fun LocalDate.toYmd(): String =
        "%04d-%02d-%02d".format(year, monthValue, dayOfMonth)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            keyword = bundle.getString("keyword")
                ?: bundle.getString(BookInfoActivity.EXTRA_KEYWORD)

            val startDateStr = bundle.getString("startDate")
            val endDateStr = bundle.getString("endDate")
            Log.d("SearchResultFragment", "받아온 startDateStr: $startDateStr")
            Log.d("SearchResultFragment", "받아온 endDateStr: $endDateStr")

            val adultCount = bundle.getInt("adultCount", bookInfoViewModel.adultCount)
            val childCount = bundle.getInt("childCount", bookInfoViewModel.childCount)

            if (!startDateStr.isNullOrBlank() && !endDateStr.isNullOrBlank()) {
                val startDateParsed = parseDate(startDateStr)
                val endDateParsed = parseDate(endDateStr)
                bookInfoViewModel.startDate = startDateParsed
                bookInfoViewModel.endDate = endDateParsed
            }
            bookInfoViewModel.adultCount = adultCount
            bookInfoViewModel.childCount = childCount
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 상단 UI 바인딩
        bindBookInfoUI(binding, bookInfoViewModel)
        keyword?.let { binding.searchText.setText(it) }

        // 어댑터
        adapter = GuestHouseAdapter(ghViewModel) { item ->
            Log.d("SearchResult", "click item id=${item.id}")
        }
        binding.searchResultRv.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultRv.adapter = adapter

        // 페이징 스크롤 리스너
        binding.searchResultRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lm = rv.layoutManager as LinearLayoutManager
                val last = lm.findLastVisibleItemPosition()
                val shouldLoadMore = last >= (adapter.itemCount - 3)
                if (shouldLoadMore && !loading && ghViewModel.currentFilter != null) {
                    ghViewModel.fetchNextFiltered()
                }
            }
        })

        // 상태 수집
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ghViewModel.state.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            loading = true
                        }
                        is UiState.Success -> {
                            loading = false
                            adapter.submit(state.items) // 어댑터 내 submit 사용
                        }
                        is UiState.Error -> {
                            loading = false
                            Log.e("SearchResult", "검색 에러: ${state.message}")
                        }
                    }
                }
            }
        }

        // 상위/필터 프래그먼트에서 넘어온 값들
        val viewEnableReservation = arguments?.getBoolean("viewEnableReservation")
        val minPrice = arguments?.getInt("minPrice")
        val maxPrice = arguments?.getInt("maxPrice")
        val peopleArg   = arguments?.getInt("people")
        val optionService  = arguments?.getStringArrayList("optionService")
        val targetAudience = arguments?.getStringArrayList("targetAudience")
        val regionArg      = arguments?.getStringArrayList("region")

        val checkIn  = bookInfoViewModel.startDate?.toYmd()
        val checkOut = bookInfoViewModel.endDate?.toYmd()

        // 날짜가 없을 때 people을 보내면 400 날 수 있어 방지
        val hasDates = !checkIn.isNullOrBlank() && !checkOut.isNullOrBlank()
        val peopleSum = (peopleArg ?: 0).takeIf { it > 0 }
            ?: (bookInfoViewModel.adultCount + bookInfoViewModel.childCount).takeIf { it > 0 }
        val peopleForRequest = if (hasDates) peopleSum else null

        // ✅ 서버 요청 DTO 생성(이름 검색은 keyword 중심, region은 전달된 값만)
        val baseFilter = GuesthouseSearchRequest(
            viewEnableReservation = viewEnableReservation,
            checkIn = checkIn,
            checkOut = checkOut,
            people = peopleForRequest,          // 날짜 없으면 null
            keyword = keyword,
            minPrice = minPrice,
            maxPrice = maxPrice,
            optionService = optionService,
            targetAudience = targetAudience,
            region = regionArg                  // keyword로 강제 채우지 않음(AND 방지)
        )

        // 첫 검색 실행
        ghViewModel.setFilterAndRefresh(baseFilter)

        // 상단 클릭 이벤트
        binding.searchText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, SearchFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.searchResultAdjustmentsLl.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, FilteringFragment())
                .addToBackStack(null)
                .commit()
        }
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
        binding.backBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, SearchFragment())
                .addToBackStack(null)
                .commit()
        }

        // 액티비티에서 온 FilterOptions 적용(있으면 덮어쓰기)
        val filterOptions: FilterOptions? = if (android.os.Build.VERSION.SDK_INT >= 33) {
            requireActivity().intent.getParcelableExtra("filterOptions", FilterOptions::class.java)
        } else {
            @Suppress("DEPRECATION")
            requireActivity().intent.getParcelableExtra("filterOptions")
        }

        filterOptions?.let {
            val newFilter = baseFilter.copy(
                minPrice = it.priceMin,
                maxPrice = it.priceMax
            )
            ghViewModel.setFilterAndRefresh(newFilter)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

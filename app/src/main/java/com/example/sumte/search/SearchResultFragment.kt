package com.example.sumte.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.R
import com.example.sumte.common.bindBookInfoUI
import com.example.sumte.common.getBookInfoViewModel
import com.example.sumte.databinding.FragmentSearchResultBinding
import com.example.sumte.guesthouse.GuestHouseAdapter
import com.example.sumte.guesthouse.GuestHouseViewModel
import com.example.sumte.guesthouse.UiState
import com.example.sumte.housedetail.HouseDetailFragment
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

    private lateinit var viewModel: GuestHouseViewModel

    private var didInitialLoad = false
    private var currentFilter: GuesthouseSearchRequest? = null

    private fun parseDate(dateStr: String): LocalDate {
        val currentYear = LocalDate.now().year
        val parts = dateStr.split(" ")
        val md = parts[0].split(".")
        return LocalDate.of(currentYear, md[0].toInt(), md[1].toInt())
    }

    private fun regionsForRequest(uiRegions: List<String>?): List<String>? {
        if (uiRegions.isNullOrEmpty()) return null

        return when {
            uiRegions.contains("제주시") -> listOf("제주특별자치도", "제주시")
            uiRegions.contains("서귀포시") -> listOf("제주특별자치도", "서귀포시")
            uiRegions.contains("제주도") -> listOf("제주특별자치도")
            else -> uiRegions
        }
    }

    private fun LocalDate.toYmd(): String =
        "%04d-%02d-%02d".format(year, monthValue, dayOfMonth)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->

            keyword = bundle.getString("keyword")
                ?: bundle.getString(BookInfoActivity.EXTRA_KEYWORD)
            if (!keyword.isNullOrBlank()) {
                bookInfoViewModel.keyword = keyword
            }

            val startDateStr = bundle.getString("startDate")
            val endDateStr = bundle.getString("endDate")

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

        val ghViewModel = ViewModelProvider(requireActivity())[GuestHouseViewModel::class.java]
        this.viewModel = ghViewModel

        val filterVm = ViewModelProvider(requireActivity())[FilterViewModel::class.java]

        adapter = GuestHouseAdapter(
            viewModel = ghViewModel,
            onItemClick = { guestHouse ->
                val id = guestHouse.id
                Log.d("guesthouseId","$id")
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.book_info_container, HouseDetailFragment.newInstance(id.toInt()))
                    .addToBackStack(null)
                    .commit()
            }
        )

        binding.searchResultRv.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultRv.adapter = adapter

        bindBookInfoUI(binding, bookInfoViewModel)
        keyword?.let { binding.searchText.setText(bookInfoViewModel.keyword) }

        binding.searchResultRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0 || loading) return
                val lm = rv.layoutManager as LinearLayoutManager
                val last = lm.findLastVisibleItemPosition()
                if (last >= (adapter.itemCount - 3) && ghViewModel.currentFilter != null) {
                    loading = true
                    ghViewModel.fetchNextFiltered()
                }
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ghViewModel.state.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            loading = true
                            binding.tvNoResults.visibility = View.GONE
                        }
                        is UiState.Success -> {
                            loading = false
                            if (state.items.isEmpty()) {
                                binding.tvNoResults.visibility = View.VISIBLE
                                adapter.submit(emptyList())
                            } else {
                                binding.tvNoResults.visibility = View.GONE
                                adapter.submit(state.items)
                            }
                        }
                        is UiState.Error -> {
                            loading = false
                            binding.tvNoResults.visibility = View.VISIBLE
                            binding.tvNoResults.text = "검색 중 오류가 발생했습니다"
                            Log.e("SearchResult", "검색 에러: ${state.message}")
                        }
                    }
                }
            }
        }

        val initialCheckIn = bookInfoViewModel.startDate?.toYmd()
        val initialCheckOut = bookInfoViewModel.endDate?.toYmd()
        val initialHasDates = !initialCheckIn.isNullOrBlank() && !initialCheckOut.isNullOrBlank()
        val initialPeopleSum = (bookInfoViewModel.adultCount + bookInfoViewModel.childCount).takeIf { it > 0 }
        val initialPeopleForRequest = if (initialHasDates) initialPeopleSum else null
        val keywordNorm = keyword?.takeIf { it.isNotBlank() }
        val initialMinPeopleForCapacity = initialPeopleSum
        binding.searchText.setText(keywordNorm ?: "")

        val baseFilter = GuesthouseSearchRequest(
            viewEnableReservation = arguments?.getBoolean("viewEnableReservation"),
            checkIn = initialCheckIn,
            checkOut = initialCheckOut,
            people = initialPeopleForRequest,
            keyword = keywordNorm,
            minPrice = arguments?.getInt("minPrice")?.takeIf { it > 0 },
            maxPrice = arguments?.getInt("maxPrice")?.takeIf { it > 0 },
            optionService = arguments?.getStringArrayList("optionService"),
            targetAudience = arguments?.getStringArrayList("targetAudience"),
            region = null,
            minPeople = initialMinPeopleForCapacity
        )

        if (!didInitialLoad) {
            currentFilter = baseFilter
            ghViewModel.setFilterAndRefresh(baseFilter)
            didInitialLoad = true
        }

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

        // SearchResultFragment.kt

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                filterVm.selected.collect { f ->
                    val isFilterCleared = f.viewEnableReservation == null &&
                            f.people == null &&
                            f.minPrice == null && f.maxPrice == null &&
                            f.optionService.isNullOrEmpty() &&
                            f.targetAudience.isNullOrEmpty() &&
                            f.regions.isEmpty()

                    val basis = if (isFilterCleared) baseFilter else (currentFilter ?: baseFilter)

                    val newKeyword: String?
                    val newRegion: List<String>?

                    if (f.regions.isNotEmpty()) {
                        newKeyword = f.regions.first()
                        binding.searchText.setText(newKeyword)
                        newRegion = regionsForRequest(f.regions)
                    } else {
                        newKeyword = if(isFilterCleared) baseFilter.keyword else basis.keyword
                        binding.searchText.setText(newKeyword ?: "")
                        newRegion = if(isFilterCleared) baseFilter.region else basis.region
                    }

                    val latestCheckIn = bookInfoViewModel.startDate?.toYmd()
                    val latestCheckOut = bookInfoViewModel.endDate?.toYmd()
                    val hasDates = !latestCheckIn.isNullOrBlank() && !latestCheckOut.isNullOrBlank()
                    val latestPeopleSum = (bookInfoViewModel.adultCount + bookInfoViewModel.childCount).takeIf { it > 0 }

                    val peopleForRequest = if (hasDates) {
                        f.people ?: latestPeopleSum
                    } else {
                        null
                    }

                    val minPeopleForCapacity = (f.people ?: latestPeopleSum)

                    // 1. 모든 필터 조건을 종합해서 'merged' 객체를 먼저 만듭니다.
                    val merged = basis.copy(
                        viewEnableReservation = f.viewEnableReservation ?: basis.viewEnableReservation,
                        minPrice = f.minPrice ?: basis.minPrice,
                        maxPrice = f.maxPrice ?: basis.maxPrice,
                        optionService = f.optionService ?: basis.optionService,
                        targetAudience = f.targetAudience ?: basis.targetAudience,
                        keyword = newKeyword,
                        region = newRegion,
                        checkIn = latestCheckIn,
                        checkOut = latestCheckOut,
                        people = peopleForRequest,
                        minPeople = minPeopleForCapacity,
                    )

                    val anyFilterApplied = !f.optionService.isNullOrEmpty() ||
                            !f.targetAudience.isNullOrEmpty() ||
                            f.regions.isNotEmpty() ||
                            (f.minPrice != null && f.minPrice > 1000) ||
                            (f.maxPrice != null && f.maxPrice < 300000) ||
                            f.viewEnableReservation == true ||
                            f.people != null

                    if (anyFilterApplied) {
                        binding.searchFilteringIcon.setImageResource(R.drawable.filtering_success)
                    } else {
                        binding.searchFilteringIcon.setImageResource(R.drawable.adjustments)
                    }

                    // 2. 그 다음에 'merged' 객체를 사용해서 if문으로 비교하고 로그를 찍습니다.
                    if (merged != currentFilter) {
                        currentFilter = merged
                        // 실제로 서버에 보내는 요청 데이터를 로그로 출력합니다.
                        Log.d("API_REQUEST_CHECK", "요청 데이터: $merged")

                        ghViewModel.setFilterAndRefresh(merged)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
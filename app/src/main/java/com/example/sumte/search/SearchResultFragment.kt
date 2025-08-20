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
import com.example.sumte.search.FilterOptions
import com.example.sumte.R
import com.example.sumte.common.bindBookInfoUI
import com.example.sumte.common.getBookInfoViewModel
import com.example.sumte.databinding.FragmentSearchResultBinding
import com.example.sumte.guesthouse.GuestHouseAdapter
import com.example.sumte.guesthouse.GuestHouseViewModel
import com.example.sumte.guesthouse.UiState
import com.example.sumte.housedetail.HouseDetailFragment
import com.example.sumte.search.FilterViewModel
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

    // ✅ 중복 호출 방지용 플래그 & 현재 필터 보관 (로컬 변수로 다시 만들지 말 것!)
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

        // 단일/우선순위 규칙(필요 시 확장):
        return when {
            uiRegions.contains("제주시")   -> listOf("제주특별자치도", "제주시")
            uiRegions.contains("서귀포시") -> listOf("제주특별자치도", "서귀포시")
            uiRegions.contains("제주도")   -> listOf("제주특별자치도")
            else -> uiRegions // 다른 읍/면/동은 서버 규칙에 맞춰 그대로 전달(필요 시 추가 매핑)
        }
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

    private fun normalizeRegionArg(region: ArrayList<String>?): List<String>? {
        if (region.isNullOrEmpty()) return null

        if (region.size == 1) {
            val city = region[0].trim()
            val province = when (city) {
                "제주시", "서귀포시" -> "제주특별자치도"
                else -> null
            }
            return province?.let { listOf(it, city) } ?: listOf(city)
        }

        val rawProvince = region[0].trim()
        val city = region[1].trim()
        val fixedProvince = when (rawProvince) {
            "제주도", "제주특별자치도" -> "제주특별자치도"
            else -> rawProvince
        }
        return listOf(fixedProvince, city)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ ViewModel 하나로 통일
        val ghViewModel = ViewModelProvider(requireActivity())[GuestHouseViewModel::class.java]
        this.viewModel = ghViewModel

        // ★ 필터 상태 공유 VM
        val filterVm = ViewModelProvider(requireActivity())[FilterViewModel::class.java]

        // ✅ 어댑터 1회 생성 + 상세 이동
        adapter = GuestHouseAdapter(
            viewModel = ghViewModel,
            onItemClick = { guestHouse ->
                val id = guestHouse.id
            }
        )

        // ✅ 리사이클러뷰 연결
        binding.searchResultRv.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultRv.adapter = adapter

        // UI 바인딩
        bindBookInfoUI(binding, bookInfoViewModel)
        keyword?.let { binding.searchText.setText(it) }

        // ✅ 페이징 스크롤 (로딩 중엔 next 막기)
        binding.searchResultRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                if (loading) return
                val lm = rv.layoutManager as LinearLayoutManager
                val last = lm.findLastVisibleItemPosition()
                if (last >= (adapter.itemCount - 3) && ghViewModel.currentFilter != null) {
                    loading = true
                    ghViewModel.fetchNextFiltered()
                }
            }
        })

        // ✅ 상태 수집 (Success/Error에서 loading=false로 복구)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ghViewModel.state.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            loading = true
                            binding.tvNoResults.visibility = View.GONE // 로딩 중에는 숨김
                        }
                        is UiState.Success -> {
                            loading = false
                            if (state.items.isEmpty()) {
                                // ✅ 검색 결과 없음
                                binding.tvNoResults.visibility = View.VISIBLE
                                adapter.submit(emptyList())
                            } else {
                                binding.tvNoResults.visibility = View.GONE
                                adapter.submit(state.items) // 어댑터 교체 방식
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

        // ----- 필터 파라미터 준비 -----
        val rawViewEnableReservation = arguments?.getBoolean("viewEnableReservation")
        val rawMinPrice = arguments?.getInt("minPrice")
        val rawMaxPrice = arguments?.getInt("maxPrice")
        val peopleArg = arguments?.getInt("people")
        val optionService = arguments?.getStringArrayList("optionService")
        val targetAudience = arguments?.getStringArrayList("targetAudience")
        val regionArg = arguments?.getStringArrayList("region")

        val checkIn = bookInfoViewModel.startDate?.toYmd()
        val checkOut = bookInfoViewModel.endDate?.toYmd()

        val hasDates = !checkIn.isNullOrBlank() && !checkOut.isNullOrBlank()
        val peopleSum = (peopleArg ?: 0).takeIf { it > 0 }
            ?: (bookInfoViewModel.adultCount + bookInfoViewModel.childCount).takeIf { it > 0 }
        val peopleForRequest = if (hasDates) peopleSum else null

        val regionNorm = normalizeRegionArg(regionArg)

        val minPriceNorm = rawMinPrice?.takeIf { it > 0 }
        val maxPriceNorm = rawMaxPrice?.takeIf { it > 0 }
        val keywordNorm = keyword?.takeIf { !it.isNullOrBlank() }
        binding.searchText.setText(keywordNorm ?: "")

        // 지역 선택 시 keyword=city, region=null
        val regionForRequest: List<String>? = null

        val baseFilter = GuesthouseSearchRequest(
            viewEnableReservation = rawViewEnableReservation,
            checkIn = checkIn,
            checkOut = checkOut,
            people = peopleForRequest,
            keyword = keywordNorm,
            minPrice = minPriceNorm,
            maxPrice = maxPriceNorm,
            optionService = optionService,
            targetAudience = targetAudience,
            region = regionForRequest
        )

        // ✅ 초기 1회만 요청 (중복 방지)
        if (!didInitialLoad) {
            currentFilter = baseFilter
            ghViewModel.setFilterAndRefresh(baseFilter)
            didInitialLoad = true
        }

        // ----- 네비게이션 -----
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

        // 외부에서 넘어온 추가 필터 반영 (동일 필터면 호출 안 함)
        val filterOptions: FilterOptions? = if (android.os.Build.VERSION.SDK_INT >= 33) {
            requireActivity().intent.getParcelableExtra("filterOptions", FilterOptions::class.java)
        } else {
            @Suppress("DEPRECATION")
            requireActivity().intent.getParcelableExtra("filterOptions")
        }

        filterOptions?.let {
            val basis = currentFilter ?: baseFilter
            val newFilter = basis.copy(
                minPrice = it.minPrice ?: basis.minPrice,
                maxPrice = it.maxPrice ?: basis.maxPrice,
                keyword = basis.keyword ?: keywordNorm, // ✅ keyword 유지
                region = null
            )
            if (newFilter != currentFilter) {
                currentFilter = newFilter
                ghViewModel.setFilterAndRefresh(newFilter)
            }
        }

        // ★ 필터 VM 변경 수집 → 실제 변경시에만 재검색
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                filterVm.selected.collect { f ->
                    val basis = currentFilter ?: baseFilter
                    val merged = basis.copy(
                        viewEnableReservation = f.viewEnableReservation ?: basis.viewEnableReservation,
                        people = f.people ?: basis.people,
                        minPrice = f.minPrice ?: basis.minPrice,
                        maxPrice = f.maxPrice ?: basis.maxPrice,
                        optionService = f.optionService ?: basis.optionService,
                        targetAudience = f.targetAudience ?: basis.targetAudience,
                        region = regionsForRequest(f.regions) ?: basis.region,
                        keyword = basis.keyword
                        // keyword/region 유지 (우리는 keyword=도시, region=null 정책)
                    )

                    val anyFilterApplied = !f.optionService.isNullOrEmpty() ||
                    !f.targetAudience.isNullOrEmpty() ||
                            f.regions.isNotEmpty() ||
                            (f.minPrice != null && f.minPrice > 1000) ||
                            (f.maxPrice != null && f.maxPrice < 300000)

                    if (anyFilterApplied) {
                        binding.searchFilteringIcon.setImageResource(R.drawable.filtering_success)
                    } else {
                        binding.searchFilteringIcon.setImageResource(R.drawable.adjustments)
                    }

                    if (merged != currentFilter) {
                        currentFilter = merged
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

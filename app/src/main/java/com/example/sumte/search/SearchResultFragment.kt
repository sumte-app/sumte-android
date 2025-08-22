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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate

class SearchResultFragment : Fragment() {

    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    private val bookInfoViewModel by lazy { getBookInfoViewModel() }
    private val ghVM: GuestHouseViewModel by viewModels()

    private lateinit var adapter: GuestHouseAdapter
    private var loading = false
    private var keyword: String? = null

    private lateinit var viewModel: GuestHouseViewModel

    private var didInitialLoad = false
    private var currentFilter: GuesthouseSearchRequest? = null

    private var loadingAnimationJob: Job? = null

    // 범위→단일 people 재시도는 한 번만
    private var didRangeFallbackOnce = false

    // -------------------- 유틸 --------------------

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

    private fun LocalDate.toYmd(): String = "%04d-%02d-%02d".format(year, monthValue, dayOfMonth)

    // 요청 정규화: people vs (min,max) 충돌 방지 + min/max 순서 보정 + 단일값 승격
    private fun GuesthouseSearchRequest.normalized(): GuesthouseSearchRequest {
        // 둘 다 있으면 people 비우고 범위 사용(순서 보정)
        if (minPeople != null && maxPeople != null) {
            val minN = kotlin.math.min(minPeople!!, maxPeople!!)
            val maxN = kotlin.math.max(minPeople!!, maxPeople!!)
            return copy(
                people = null,
                minPeople = minN,
                maxPeople = maxN
            )
        }
        // people 이 있으면 min/max 무시
        if (people != null) {
            return copy(minPeople = null, maxPeople = null)
        }
        // 한쪽만 있으면 단일 people 로 승격
        if (minPeople != null && maxPeople == null) {
            return copy(people = minPeople, minPeople = null, maxPeople = null)
        }
        if (maxPeople != null && minPeople == null) {
            return copy(people = maxPeople, minPeople = null, maxPeople = null)
        }
        // 아무 것도 없으면 그대로
        return this
    }

    // -------------------- 생명주기 --------------------

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
                bookInfoViewModel.startDate = parseDate(startDateStr)
                bookInfoViewModel.endDate = parseDate(endDateStr)
            }
            bookInfoViewModel.adultCount = adultCount
            bookInfoViewModel.childCount = childCount
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

        // 페이징 스크롤
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

        // 상태 구독
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ghViewModel.state.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            loading = true
                            binding.searchResultRv.visibility = View.GONE
                            binding.tvNoResults.visibility = View.GONE
                            startLoadingAnimation()
                        }
                        is UiState.Success -> {
                            stopLoadingAnimation()
                            loading = false
                            didRangeFallbackOnce = false // 성공하면 fallback 플래그 초기화
                            if (state.items.isEmpty()) {
                                binding.tvNoResults.visibility = View.VISIBLE
                                binding.searchResultRv.visibility = View.GONE
                                binding.tvNoResults.text = "검색한 결과가 없습니다."
                                adapter.submit(emptyList())
                            } else {
                                binding.tvNoResults.visibility = View.GONE
                                binding.searchResultRv.visibility = View.VISIBLE
                                adapter.submit(state.items)
                            }
                        }
                        is UiState.Error -> {
                            stopLoadingAnimation()
                            loading = false

                            // ---------- 🔁 서버 호환 Fallback ----------
                            val cf = currentFilter
                            val hasRange = cf?.minPeople != null && cf.maxPeople != null
                            if (!didRangeFallbackOnce && hasRange) {
                                didRangeFallbackOnce = true
                                val fallback = cf!!.copy(
                                    // 범위 대신 people = maxPeople 로 단일 전송
                                    people = cf.maxPeople,
                                    minPeople = null,
                                    maxPeople = null,
                                    // 예약 가능 여부 null 방지
                                    viewEnableReservation = cf.viewEnableReservation ?: false
                                ).normalized()
                                currentFilter = fallback
                                Log.w("API_REQUEST_CHECK", "범위→단일 people Fallback 재시도: $fallback")
                                ghViewModel.setFilterAndRefresh(fallback)
                                return@collect
                            }
                            // ---------- Fallback 종료 ----------

                            binding.searchResultRv.visibility = View.GONE
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
        val initialPeopleSum = (bookInfoViewModel.adultCount + bookInfoViewModel.childCount).takeIf { it > 0 }
        val keywordNorm = keyword?.takeIf { it.isNotBlank() }
        binding.searchText.setText(keywordNorm ?: "")

        // 기본 필터
        val baseFilter = GuesthouseSearchRequest(
            viewEnableReservation = (arguments?.getBoolean("viewEnableReservation") ?: false),
            checkIn = initialCheckIn,
            checkOut = initialCheckOut,
            people = initialPeopleSum,
            minPeople = null,
            maxPeople = null,
            keyword = keywordNorm,
            minPrice = arguments?.getInt("minPrice")?.takeIf { it > 0 },
            maxPrice = arguments?.getInt("maxPrice")?.takeIf { it > 0 },
            optionService = arguments?.getStringArrayList("optionService"),
            targetAudience = arguments?.getStringArrayList("targetAudience"),
            region = null
        ).normalized()

        // 최초 로딩
        if (!didInitialLoad) {
            currentFilter = baseFilter
            ghViewModel.setFilterAndRefresh(currentFilter!!)
            didInitialLoad = true
        }

        // 네비게이션
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

        // 필터 변경 수신
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                filterVm.selected.collect { f ->
                    // 초기화 여부 판단 (people 포함)
                    val isFilterCleared =
                        (f.viewEnableReservation != true) &&
                                f.minPeople == null &&
                                f.maxPeople == null &&
                                f.people == null &&
                                f.minPrice == null &&
                                f.maxPrice == null &&
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
                        newKeyword = if (isFilterCleared) baseFilter.keyword else basis.keyword
                        binding.searchText.setText(newKeyword ?: "")
                        newRegion = if (isFilterCleared) baseFilter.region else basis.region
                    }

                    val latestCheckIn = bookInfoViewModel.startDate?.toYmd()
                    val latestCheckOut = bookInfoViewModel.endDate?.toYmd()
                    val latestPeopleSum =
                        (bookInfoViewModel.adultCount + bookInfoViewModel.childCount).takeIf { it > 0 }

                    // --- 인원 정규화(방어선) ---
                    val rawMin = f.minPeople
                    val rawMax = f.maxPeople
                    val rawPeople = f.people

                    val peopleForRequest: Int?
                    val minPeopleForRequest: Int?
                    val maxPeopleForRequest: Int?

                    if (rawMin != null && rawMax != null) {
                        val minN = minOf(rawMin, rawMax)
                        val maxN = maxOf(rawMin, rawMax)
                        peopleForRequest = null
                        minPeopleForRequest = minN
                        maxPeopleForRequest = maxN
                    } else if (rawPeople != null) {
                        peopleForRequest = rawPeople
                        minPeopleForRequest = null
                        maxPeopleForRequest = null
                    } else if (rawMin != null) {
                        peopleForRequest = rawMin
                        minPeopleForRequest = null
                        maxPeopleForRequest = null
                    } else if (rawMax != null) {
                        peopleForRequest = rawMax
                        minPeopleForRequest = null
                        maxPeopleForRequest = null
                    } else {
                        peopleForRequest = latestPeopleSum
                        minPeopleForRequest = null
                        maxPeopleForRequest = null
                    }
                    // --- 끝 ---

                    val merged = basis.copy(
                        viewEnableReservation = f.viewEnableReservation ?: basis.viewEnableReservation ?: false,
                        minPrice = f.minPrice ?: basis.minPrice,
                        maxPrice = f.maxPrice ?: basis.maxPrice,

                        // BEFORE
                        // optionService = f.optionService ?: basis.optionService,
                        // targetAudience = f.targetAudience ?: basis.targetAudience,
                        // region = newRegion,

                        // AFTER ✅ 빈 리스트는 '해제'로 간주하여 null 전송
                        optionService = when {
                            f.optionService != null -> f.optionService.nullIfEmpty()
                            isFilterCleared -> null
                            else -> basis.optionService
                        },
                        targetAudience = when {
                            f.targetAudience != null -> f.targetAudience.nullIfEmpty()
                            isFilterCleared -> null
                            else -> basis.targetAudience
                        },
                        region = when {
                            newRegion != null -> newRegion.nullIfEmpty()
                            isFilterCleared -> null
                            else -> basis.region
                        },

                        keyword = newKeyword,
                        checkIn = latestCheckIn,
                        checkOut = latestCheckOut,
                        people = peopleForRequest,
                        minPeople = minPeopleForRequest,
                        maxPeople = maxPeopleForRequest
                    )

                    // 필터 적용 여부(people 포함)
                    val anyFilterApplied =
                        !f.optionService.isNullOrEmpty() ||
                                !f.targetAudience.isNullOrEmpty() ||
                                f.regions.isNotEmpty() ||
                                (f.minPrice != null && f.minPrice > 1000) ||
                                (f.maxPrice != null && f.maxPrice < 300000) ||
                                f.viewEnableReservation == true ||
                                f.minPeople != null ||
                                f.maxPeople != null ||
                                f.people != null

                    if (anyFilterApplied) {
                        binding.searchFilteringIcon.setImageResource(R.drawable.filtering_success)
                    } else {
                        binding.searchFilteringIcon.setImageResource(R.drawable.adjustments)
                    }

                    val normalized = merged.normalized() // ✅ 최종 정규화
                    if (normalized != currentFilter) {
                        didRangeFallbackOnce = false // 새 필터면 다시 시도 가능
                        currentFilter = normalized
                        Log.d("API_REQUEST_CHECK", "요청 데이터(정규화 완료): $normalized")
                        ghViewModel.setFilterAndRefresh(normalized)
                    }
                }
            }
        }
    }

    fun List<String>?.nullIfEmpty(): List<String>? =
        if (this != null && this.isEmpty()) null else this

    // -------------------- 로딩 애니메이션 --------------------

    private fun startLoadingAnimation() {
        binding.loadingLayout.root.visibility = View.VISIBLE
        val dots = listOf(
            binding.loadingLayout.dot1,
            binding.loadingLayout.dot2,
            binding.loadingLayout.dot3
        )

        loadingAnimationJob?.cancel()
        loadingAnimationJob = viewLifecycleOwner.lifecycleScope.launch {
            var dotIndex = 0
            while (isActive) {
                dots.forEach { it.setImageResource(R.drawable.dot_gray) }
                dots[dotIndex].setImageResource(R.drawable.dot_green)
                delay(400)
                dotIndex = (dotIndex + 1) % dots.size
            }
        }
    }

    private fun stopLoadingAnimation() {
        loadingAnimationJob?.cancel()
        if (_binding != null) {
            binding.loadingLayout.root.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopLoadingAnimation()
        _binding = null
    }
}

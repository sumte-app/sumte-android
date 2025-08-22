package com.example.sumte.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sumte.R
import com.example.sumte.databinding.FragmentFilteringBinding

class FilteringFragment: Fragment() {
    lateinit var binding: FragmentFilteringBinding

    // [수정] 최대 인원 선택 옵션은 그대로 사용
    val personOptions = arrayOf("1명", "2명", "3명", "4명", "5명", "6명", "7명", "8명", "9명", "10명", "11명")

    // 최소 인원을 관리하는 클래스 프로퍼티
    private var minPeople: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFilteringBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val serviceTextViews = listOf(
            binding.filteringExtraServiceEventTv,
            binding.filteringExtraServicePartyTv,
            binding.filteringExtraServiceCurtainTv,
            binding.filteringExtraServiceMealTv
        )
        val targetTextViews = listOf(
            binding.filteringTargetFemaleonlyTv,
            binding.filteringTargetMaleonlyTv,
            binding.filteringTargetWithpetsTv
        )
        val regionJejusiTextView = binding.filteringTargetJejuallTv
        val regionTextView = listOf(
            binding.filteringTargetJejuallTv,
            binding.filteringTargetJejusiTv,
            binding.filteringTargetSeogwipoTv,
            binding.filteringTargetAewolTv,
            binding.filteringTargetSeongsanTv,
            binding.filteringTargetJocheonTv,
            binding.filteringTargetGujwaTv,
            binding.filteringTargetHallimTv,
            binding.filteringTargetHankyungTv,
            binding.filteringTargetDaejeongTv,
            binding.filteringTargetAndeokTv,
            binding.filteringTargetNamwonTv,
            binding.filteringTargetPyoseonTv
        )

        (serviceTextViews + targetTextViews + regionTextView).forEach { it.tag = "unselected" }

        binding.root.findViewById<View>(R.id.filtering_people_choice)?.setOnClickListener {
            binding.filteringPeopleCountTv.performClick()
        }
        binding.root.findViewById<View>(R.id.filtering_people_base)?.setOnClickListener {
            val minTv = binding.root.findViewById<View>(R.id.filteringPeopleMinTv)
            if (minTv != null) minTv.performClick() else binding.filteringPeopleCountTv.performClick()
        }

        binding.filteringCloseIv.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.filteringRangeslider.apply {
            valueFrom = 1000f
            valueTo = 300_000f
            values = listOf(1000f, 300_000f)
        }
        binding.filteringRangeslider.addOnChangeListener { slider, _, _ ->
            val minPrice = slider.values[0].toInt()
            val maxPrice = slider.values[1].toInt()
            binding.filteringMinpriceTv.text = "${minPrice}원"
            binding.filteringMaxpriceTv.text = "${maxPrice}원+"
            binding.filteringMinpriceTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            binding.filteringMaxpriceTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        }

        val onPickMax: (String) -> Unit = { selected ->
            binding.filteringPeopleCountTv.text = selected
            binding.filteringPeopleCountTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray600))
            val maxPicked = selected.removeSuffix("명").toIntOrNull()
            if (maxPicked != null && maxPicked < minPeople) {
                minPeople = maxPicked
                binding.filteringPeopleMinTv.text = "${minPeople}명"
                binding.filteringPeopleMinTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray600))
            }
        }
        val showMaxPopup: (View) -> Unit = { v ->
            val popup = PopupMenu(requireContext(), v)
            popup.menu.add("인원 선택")
            for (i in 1..12) popup.menu.add("${i}명")
            popup.setOnMenuItemClickListener { onPickMax(it.title.toString()); true }
            popup.show()
        }
        binding.filteringPeopleCountTv.setOnClickListener(showMaxPopup)
        binding.filteringPeopleCountIv.setOnClickListener(showMaxPopup)

        val onPickMin: (Int) -> Unit = { picked ->
            minPeople = picked
            binding.filteringPeopleMinTv.text = "${minPeople}명"
            binding.filteringPeopleMinTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray600))
            val currentMax = binding.filteringPeopleCountTv.text.toString().removeSuffix("명").toIntOrNull()
            if (currentMax != null && minPeople > currentMax) {
                binding.filteringPeopleCountTv.text = "${minPeople}명"
                binding.filteringPeopleCountTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray600))
            }
        }
        val showMinPopup: (View) -> Unit = { v ->
            val popup = PopupMenu(requireContext(), v)
            popup.menu.add("인원 선택")
            for (i in 1..12) popup.menu.add("${i}명")
            popup.setOnMenuItemClickListener {
                it.title.toString().removeSuffix("명").toIntOrNull()?.let(onPickMin)
                true
            }
            popup.show()
        }
        binding.filteringPeopleMinTv.setOnClickListener(showMinPopup)
        runCatching { binding.filteringPeopleMinIv.setOnClickListener(showMinPopup) }

        fun setSelected(tv: TextView, selected: Boolean) {
            if (selected) {
                tv.setBackgroundResource(R.drawable.filtering_selected)
                tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                tv.tag = "selected"
            } else {
                tv.setBackgroundResource(R.drawable.filtering_unselected)
                tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray800))
                tv.tag = "unselected"
            }
        }
        (serviceTextViews + targetTextViews).forEach { tv ->
            tv.setOnClickListener { setSelected(tv, tv.tag != "selected") }
        }
        regionTextView.forEach { tv ->
            if (tv != regionJejusiTextView) {
                tv.setOnClickListener {
                    if (regionJejusiTextView.tag == "selected") setSelected(regionJejusiTextView, false)
                    setSelected(tv, tv.tag != "selected")
                }
            }
        }
        regionJejusiTextView.setOnClickListener {
            if (regionJejusiTextView.tag != "selected") {
                regionTextView.forEach { setSelected(it, false) }
                setSelected(regionJejusiTextView, true)
            } else setSelected(regionJejusiTextView, false)
        }

        fun resetLinearLayoutTextViews(ll: LinearLayout) {
            for (i in 0 until ll.childCount) {
                val v = ll.getChildAt(i)
                if (v is TextView) {
                    v.setBackgroundResource(R.drawable.filtering_unselected)
                    v.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray800))
                    v.tag = "unselected"
                }
            }
        }

        // '초기화' 버튼 리스너
        binding.filteringResetLl.setOnClickListener {
            binding.filteringCheckbox.isChecked = false

            binding.filteringRangeslider.values = listOf(1000f, 300000f)
            binding.filteringMinpriceTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray400))
            binding.filteringMaxpriceTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray400))
            binding.filteringPricemidTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray400))

            // [수정] 최소/최대 인원 UI 모두 초기화
            binding.filteringPeopleCountTv.text = "인원 선택"
            binding.filteringPeopleCountTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray400))
            minPeople = 1
            binding.filteringPeopleMinTv.text = "1명"
            binding.filteringPeopleMinTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray600))

            resetLinearLayoutTextViews(binding.filteringExtraServiceLl)
            resetLinearLayoutTextViews(binding.filteringTargetLl)
            resetLinearLayoutTextViews(binding.filteringRegion1Ll)
            resetLinearLayoutTextViews(binding.filteringRegion2Ll)
            resetLinearLayoutTextViews(binding.filteringRegion3Ll)

            val vm = ViewModelProvider(requireActivity())[FilterViewModel::class.java]
            vm.save(FilterOptions()) // 무필터 저장
        }

        fun getSelectedItemsFromLinearLayout(ll: LinearLayout): List<String> {
            val list = mutableListOf<String>()
            for (i in 0 until ll.childCount) {
                val v = ll.getChildAt(i)
                if (v is TextView && v.tag == "selected") list.add(v.text.toString())
            }
            return list
        }

        // 필터 옵션을 UI에 적용하는 함수
        fun applyFilterToUi(f: FilterOptions) {
            binding.filteringCheckbox.isChecked = f.viewEnableReservation ?: false

            val min = (f.minPrice ?: 1000).coerceAtLeast(1000)
            val max = (f.maxPrice ?: 300000).coerceAtMost(300000)
            binding.filteringRangeslider.values = listOf(min.toFloat(), max.toFloat())
            binding.filteringMinpriceTv.text = "${min}원"
            binding.filteringMaxpriceTv.text = "${max}원+"

            // [수정] people -> maxPeople로 변경하고 최대 인원 UI 복원
            val maxPeopleStr = f.maxPeople?.let { "${it}명" } ?: "인원 선택"
            binding.filteringPeopleCountTv.text = maxPeopleStr
            binding.filteringPeopleCountTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (maxPeopleStr == "인원 선택") R.color.gray400 else R.color.gray600
                )
            )

            // [추가] 최소 인원 UI 복원
            minPeople = f.minPeople ?: 1
            binding.filteringPeopleMinTv.text = "${minPeople}명"
            binding.filteringPeopleMinTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray600))


            fun setSelectedByTexts(container: LinearLayout, wanted: Set<String>) {
                for (i in 0 until container.childCount) {
                    val v = container.getChildAt(i)
                    if (v is TextView) setSelected(v, wanted.contains(v.text.toString()))
                }
            }
            setSelectedByTexts(binding.filteringExtraServiceLl, (f.optionService ?: emptyList()).toSet())
            val uiTargets = (f.targetAudience ?: emptyList()).map { if (it == "애견동반") "애견 동반" else it }.toSet()
            setSelectedByTexts(binding.filteringTargetLl, uiTargets)

            val wantedRegions = f.regions.toSet()
            if (wantedRegions.contains("제주도")) {
                setSelected(binding.filteringTargetJejuallTv, true)
                regionTextView.forEach { if (it != binding.filteringTargetJejuallTv) setSelected(it, false) }
            } else {
                setSelected(binding.filteringTargetJejuallTv, false)
                setSelectedByTexts(binding.filteringRegion1Ll, wantedRegions)
                setSelectedByTexts(binding.filteringRegion2Ll, wantedRegions)
                setSelectedByTexts(binding.filteringRegion3Ll, wantedRegions)
            }
        }

        val filterViewModel = ViewModelProvider(requireActivity())[FilterViewModel::class.java]
        applyFilterToUi(filterViewModel.selected.value)

        // '적용' 버튼 리스너
        binding.filteringApplyTv.setOnClickListener {
            val selMin = binding.filteringRangeslider.values[0].toInt()
            val selMax = binding.filteringRangeslider.values[1].toInt()
            val minPrice = selMin.takeIf { it != 1000 }
            val maxPrice = selMax.takeIf { it != 300000 }

            // 1) 최소/최대 인원 수 취득
            var pickedMin: Int? = minPeople.takeIf { it > 1 } // 1명은 의미 없는 기본값이므로 null
            var pickedMax: Int? = binding.filteringPeopleCountTv.text.toString()
                .takeIf { it != "인원 선택" }
                ?.removeSuffix("명")
                ?.toIntOrNull()

            // 2) 범위 보정 (min > max -> 스왑)
            if (pickedMin != null && pickedMax != null && pickedMin > pickedMax) {
                val tmp = pickedMin
                pickedMin = pickedMax
                pickedMax = tmp
            }

            // 3) 서버용 인원 파라미터 정규화 규칙
            // - 둘 다 있으면: minPeople/maxPeople 로 보냄 (people=null)
            // - 하나만 있으면: 단일 people 로 보냄 (min/max=null)
            val normalizedMin: Int?
            val normalizedMax: Int?
            val normalizedPeople: Int?
            when {
                pickedMin != null && pickedMax != null -> {
                    normalizedMin = pickedMin
                    normalizedMax = pickedMax
                    normalizedPeople = null
                }
                pickedMax != null -> { // 최대만 선택됨
                    normalizedMin = null
                    normalizedMax = null
                    normalizedPeople = pickedMax
                }
                pickedMin != null -> { // 최소만 선택됨
                    normalizedMin = null
                    normalizedMax = null
                    normalizedPeople = pickedMin
                }
                else -> { // 아무 것도 선택 안함
                    normalizedMin = null
                    normalizedMax = null
                    normalizedPeople = null
                }
            }

            fun mapService(s: String) = when (s.trim()) {
                "이벤트" -> "이벤트"
                "파티" -> "파티"
                "개인커튼" -> "개인커튼"
                "조식포함" -> "조식포함"
                else -> s
            }
            fun mapTarget(s: String) = when (s.trim()) {
                "여성전용" -> "여성전용"
                "남성전용" -> "남성전용"
                "애견 동반" -> "애견동반"
                else -> s.replace(" ", "")
            }
            fun mapRegion(s: String) = when (s.trim()) {
                "제주도 전체" -> "제주도"
                "제주시" -> "제주시"
                "서귀포시" -> "서귀포시"
                else -> s
            }
            fun selected(ll: LinearLayout) = getSelectedItemsFromLinearLayout(ll)

            val optionService = selected(binding.filteringExtraServiceLl).map(::mapService)
            val targetAudience = selected(binding.filteringTargetLl).map(::mapTarget)
            val regions = buildList {
                addAll(selected(binding.filteringRegion1Ll))
                addAll(selected(binding.filteringRegion2Ll))
                addAll(selected(binding.filteringRegion3Ll))
            }.map(::mapRegion)

            val viewEnableReservation = if (binding.filteringCheckbox.isChecked) true else null

            val vm = ViewModelProvider(requireActivity())[FilterViewModel::class.java]
            vm.save(
                FilterOptions(
                    viewEnableReservation = viewEnableReservation,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    // ⚠️ 여기서는 정규화된 결과만 저장
                    minPeople = normalizedMin,
                    maxPeople = normalizedMax,
                    people = normalizedPeople,
                    optionService = optionService,
                    targetAudience = targetAudience,
                    regions = regions
                )
            )

            parentFragmentManager.popBackStack()
        }

    }
}
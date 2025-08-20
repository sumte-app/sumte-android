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
import com.example.sumte.search.FilterOptions
import com.example.sumte.search.FilterViewModel
import com.example.sumte.R
import com.example.sumte.databinding.FragmentFilteringBinding

class FilteringFragment: Fragment() {
    lateinit var binding: FragmentFilteringBinding
    val personOptions = arrayOf("1명", "2명", "3명", "4명", "5명", "6명", "7명", "8명", "9명", "10명", "11명")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
        val allTextViews = listOf(regionJejusiTextView) + regionTextView

        binding.filteringCloseIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

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
            binding.filteringMinpriceTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primary
                )
            )
            binding.filteringMaxpriceTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primary
                )
            )
        }

        binding.filteringPeopleCountTv.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menu.add("인원 선택")
            for (i in 1..12) {
                popup.menu.add("${i}명")
            }
            popup.setOnMenuItemClickListener { menuItem ->
                val selected = menuItem.title.toString()
                binding.filteringPeopleCountTv.text = selected
                binding.filteringPeopleCountTv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray600
                    )
                )
                true
            }
            popup.show()
        }
        binding.filteringPeopleCountIv.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menu.add("인원 선택")
            for (i in 1..12) {
                popup.menu.add("${i}명")
            }
            popup.setOnMenuItemClickListener { menuItem ->
                val selected = menuItem.title.toString()
                binding.filteringPeopleCountTv.text = selected
                binding.filteringPeopleCountTv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray600
                    )
                )
                true
            }
            popup.show()
        }

        serviceTextViews.forEach { textView ->
            textView.setOnClickListener {
                val isSelected = textView.tag == "selected"
                if (isSelected) {
                    textView.setBackgroundResource(R.drawable.filtering_unselected)
                    textView.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.gray800
                        )
                    )
                    textView.tag = "unselected"
                } else {
                    textView.setBackgroundResource(R.drawable.filtering_selected)
                    textView.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.primary
                        )
                    )
                    textView.tag = "selected"
                }
            }
        }

        targetTextViews.forEach { textView ->
            textView.setOnClickListener {
                val isSelected = textView.tag == "selected"
                if (isSelected) {
                    textView.setBackgroundResource(R.drawable.filtering_unselected)
                    textView.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.gray800
                        )
                    )
                    textView.tag = "unselected"
                } else {
                    textView.setBackgroundResource(R.drawable.filtering_selected)
                    textView.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.primary
                        )
                    )
                    textView.tag = "selected"
                }
            }
        }

        fun setSelected(textView: TextView, selected: Boolean) {
            if (selected) {
                textView.setBackgroundResource(R.drawable.filtering_selected)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                textView.tag = "selected"
            } else {
                textView.setBackgroundResource(R.drawable.filtering_unselected)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray800))
                textView.tag = "unselected"
            }
        }

        regionTextView.forEach { textView ->
            if (textView != regionJejusiTextView) {
                textView.setOnClickListener {

                    if (regionJejusiTextView.tag == "selected") {
                        setSelected(regionJejusiTextView, false)
                    }
                    val isSelected = textView.tag == "selected"
                    if (isSelected) {
                        textView.setBackgroundResource(R.drawable.filtering_unselected)
                        textView.setTextColor(
                            ContextCompat.getColor(
                                binding.root.context,
                                R.color.gray800
                            )
                        )
                        textView.tag = "unselected"
                    } else {
                        textView.setBackgroundResource(R.drawable.filtering_selected)
                        textView.setTextColor(
                            ContextCompat.getColor(
                                binding.root.context,
                                R.color.primary
                            )
                        )
                        textView.tag = "selected"
                    }
                }
            }
        }
        regionJejusiTextView.setOnClickListener {
            val isSelected = regionJejusiTextView.tag == "selected"
            if (!isSelected) {
                regionTextView.forEach { setSelected(it, false) }
                setSelected(regionJejusiTextView, true)
            } else {
                setSelected(regionJejusiTextView, false)
            }
        }


        fun resetLinearLayoutTextViews(linearLayout: LinearLayout) {
            for (i in 0 until linearLayout.childCount) {
                val view = linearLayout.getChildAt(i)
                if (view is TextView) {
                    view.setBackgroundResource(R.drawable.filtering_unselected)
                    view.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray800))
                }
            }
        }

        binding.filteringResetLl.setOnClickListener {
            binding.filteringCheckbox.isChecked = false
            binding.filteringRangeslider.values = listOf(1000f, 300000f)
            binding.filteringMinpriceTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray400
                )
            )
            binding.filteringMaxpriceTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray400
                )
            )
            binding.filteringPricemidTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray400
                )
            )
            binding.filteringPeopleCountTv.setText("인원 선택")
            resetLinearLayoutTextViews(binding.filteringExtraServiceLl)
            resetLinearLayoutTextViews(binding.filteringTargetLl)
            resetLinearLayoutTextViews(binding.filteringRegion1Ll)
            resetLinearLayoutTextViews(binding.filteringRegion2Ll)
            resetLinearLayoutTextViews(binding.filteringRegion3Ll)

            val filterViewModel = ViewModelProvider(requireActivity())[FilterViewModel::class.java]
            filterViewModel.save(FilterOptions())
        }

        fun getSelectedItemsFromLinearLayout(linearLayout: LinearLayout): List<String> {
            val selectedItems = mutableListOf<String>()
            for (i in 0 until linearLayout.childCount) {
                val view = linearLayout.getChildAt(i)
                if (view is TextView) {
                    val background = view.background
                    if (background.constantState == ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.filtering_selected
                        )?.constantState
                    ) {
                        selectedItems.add(view.text.toString())
                    }
                }
            }
            return selectedItems
        }

        fun applyFilterToUi(f: FilterOptions) {
            // 체크박스
            binding.filteringCheckbox.isChecked = f.viewEnableReservation ?: false

            // 가격
            val min = (f.minPrice ?: 1000).coerceAtLeast(1000)
            val max = (f.maxPrice ?: 300000).coerceAtMost(300000)
            binding.filteringRangeslider.values = listOf(min.toFloat(), max.toFloat())
            binding.filteringMinpriceTv.text = "${min}원"
            binding.filteringMaxpriceTv.text = "${max}원+"

            // 인원
            val peopleStr = f.people?.let { "${it}명" } ?: "인원 선택"
            binding.filteringPeopleCountTv.text = peopleStr
            binding.filteringPeopleCountTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (peopleStr == "인원 선택") R.color.gray400 else R.color.gray600
                )
            )

            // 공통 선택 유틸
            fun setSelectedByTexts(container: LinearLayout, wanted: Set<String>) {
                for (i in 0 until container.childCount) {
                    val v = container.getChildAt(i)  // ← named arg 쓰지 말 것!
                    if (v is TextView) {
                        val shouldSelect = wanted.contains(v.text.toString())
                        setSelected(v, shouldSelect)  // ← named arg 쓰지 말 것!
                    }
                }
            }

            // 서비스
            setSelectedByTexts(
                binding.filteringExtraServiceLl,
                (f.optionService ?: emptyList()).toSet()
            )

            // 타겟 (애견동반 -> "애견 동반" 으로 UI 매핑)
            val uiTargets = (f.targetAudience ?: emptyList())
                .map { if (it == "애견동반") "애견 동반" else it }
                .toSet()
            setSelectedByTexts(binding.filteringTargetLl, uiTargets)

            // 지역 (저장된 문자열 그대로 매칭)
            val wantedRegions = f.regions.toSet()
            if (wantedRegions.contains("제주도")) {
                setSelected(binding.filteringTargetJejuallTv, true)
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
                regionTextView.forEach {
                    if (it != binding.filteringTargetJejuallTv) setSelected(
                        it,
                        false
                    )
                }
            } else {
                setSelected(binding.filteringTargetJejuallTv, false)
                setSelectedByTexts(binding.filteringRegion1Ll, wantedRegions)
                setSelectedByTexts(binding.filteringRegion2Ll, wantedRegions)
                setSelectedByTexts(binding.filteringRegion3Ll, wantedRegions)
            }
        }


        val filterViewModel = ViewModelProvider(requireActivity())[FilterViewModel::class.java]

        applyFilterToUi(filterViewModel.selected.value)

        binding.filteringApplyTv.setOnClickListener {
            val priceMin = binding.filteringRangeslider.values[0].toInt()
            val priceMax = binding.filteringRangeslider.values[1].toInt()

            val peopleCountStr = binding.filteringPeopleCountTv.text.toString()
            val people =
                peopleCountStr.takeIf { it != "인원 선택" }?.removeSuffix("명")?.toIntOrNull() ?: 1


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

            val optionService = (selected(binding.filteringExtraServiceLl)).map(::mapService)
            val targetAudience = (selected(binding.filteringTargetLl)).map(::mapTarget)

            val regions = buildList {
                addAll(selected(binding.filteringRegion1Ll))
                addAll(selected(binding.filteringRegion2Ll))
                addAll(selected(binding.filteringRegion3Ll))
            }.map(::mapRegion)

            filterViewModel.save(
                FilterOptions(
                    viewEnableReservation = binding.filteringCheckbox.isChecked,
                    minPrice = priceMin,
                    maxPrice = priceMax,
                    people = people,
                    optionService = optionService.ifEmpty { null },
                    targetAudience = targetAudience.ifEmpty { null },
                    regions = regions
                )
            )

            parentFragmentManager.popBackStack()


        }
    }
}
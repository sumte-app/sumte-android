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
import com.example.sumte.FilterOptions
import com.example.sumte.FilterViewModel
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
        binding=FragmentFilteringBinding.inflate(inflater, container, false)
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
        val targetTextViews= listOf(
            binding.filteringTargetFemaleonlyTv,
            binding.filteringTargetMaleonlyTv,
            binding.filteringTargetWithpetsTv
        )
        val regionJejusiTextView = binding.filteringTargetJejuallTv
        val regionTextView= listOf(
            binding.filteringTargetJejuallTv, binding.filteringTargetJejusiTv, binding.filteringTargetSeogwipoTv, binding.filteringTargetAewolTv,
            binding.filteringTargetSeongsanTv, binding.filteringTargetJocheonTv, binding.filteringTargetGujwaTv, binding.filteringTargetHallimTv,
            binding.filteringTargetHankyungTv, binding.filteringTargetDaejeongTv, binding.filteringTargetAndeokTv, binding.filteringTargetNamwonTv,
            binding.filteringTargetPyoseonTv
        )
        val allTextViews=listOf(regionJejusiTextView)+regionTextView

        binding.filteringCloseIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // RangeSlider 부분
        binding.filteringRangeslider.addOnChangeListener { slider, _, _ ->
            val minPrice = slider.values[0].toInt()
            val maxPrice = slider.values[1].toInt()
            binding.filteringMinpriceTv.text = "${minPrice}원"
            binding.filteringMaxpriceTv.text="${maxPrice}원+"
            binding.filteringMinpriceTv.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.primary
            ))
            binding.filteringMaxpriceTv.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.primary
            ))
        }

        //인원 선택 드롭다운 부분
        binding.filteringPeopleCountTv.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menu.add("인원 선택")
            for (i in 1..12) {
                popup.menu.add("${i}명")
            }
            popup.setOnMenuItemClickListener { menuItem ->
                val selected = menuItem.title.toString()
                binding.filteringPeopleCountTv.text = selected
                binding.filteringPeopleCountTv.setTextColor(ContextCompat.getColor(requireContext(),
                    R.color.gray600
                ))
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
                binding.filteringPeopleCountTv.setTextColor(ContextCompat.getColor(requireContext(),
                    R.color.gray600
                ))
                true
            }
            popup.show()
        }

        //부가 서비스 선택시
        serviceTextViews.forEach{ textView->
            textView.setOnClickListener{
                val isSelected = textView.tag=="selected"
                if(isSelected){
                    textView.setBackgroundResource(R.drawable.filtering_unselected)
                    textView.setTextColor(ContextCompat.getColor(binding.root.context,
                        R.color.gray800
                    ))
                    textView.tag = "unselected"
                }
                else{
                    textView.setBackgroundResource(R.drawable.filtering_selected)
                    textView.setTextColor(ContextCompat.getColor(binding.root.context,
                        R.color.primary
                    ))
                    textView.tag = "selected"
                }
            }
        }

        //이용 대상 선택시
        targetTextViews.forEach{textView->
            textView.setOnClickListener{
                val isSelected=textView.tag=="selected"
                if(isSelected){
                    textView.setBackgroundResource(R.drawable.filtering_unselected)
                    textView.setTextColor(ContextCompat.getColor(binding.root.context,
                        R.color.gray800
                    ))
                    textView.tag = "unselected"
                }
                else{
                    textView.setBackgroundResource(R.drawable.filtering_selected)
                    textView.setTextColor(ContextCompat.getColor(binding.root.context,
                        R.color.primary
                    ))
                    textView.tag = "selected"
                }
            }
        }
        //지역 설정 선택시
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

        regionTextView.forEach{textView->
            if (textView != regionJejusiTextView) {
                textView.setOnClickListener {
                    // If "제주도 전체" is selected, unselect it first
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
            val isSelected = regionJejusiTextView.tag=="selected"
            if (!isSelected) {
                // 이벤트 선택 → 나머지 다 해제 + 이벤트 선택
                regionTextView.forEach { setSelected(it, false) }
                setSelected(regionJejusiTextView, true)
            } else {
                setSelected(regionJejusiTextView, false)
            }
        }

        //재설정 버튼
        fun resetLinearLayoutTextViews(linearLayout: LinearLayout) {
            for (i in 0 until linearLayout.childCount) {
                val view = linearLayout.getChildAt(i)
                if (view is TextView) {
                    view.setBackgroundResource(R.drawable.filtering_unselected)
                    view.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray800))
                }
            }
        }

        binding.filteringResetLl.setOnClickListener{
            binding.filteringCheckbox.isChecked = false
            binding.filteringRangeslider.values = listOf(1000f, 100000f)
            binding.filteringMinpriceTv.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.gray400
            ))
            binding.filteringMaxpriceTv.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.gray400
            ))
            binding.filteringPricemidTv.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.gray400
            ))
            binding.filteringPeopleCountTv.setText("인원 선택")
            resetLinearLayoutTextViews(binding.filteringExtraServiceLl)
            resetLinearLayoutTextViews(binding.filteringTargetLl)
            resetLinearLayoutTextViews(binding.filteringRegion1Ll)
            resetLinearLayoutTextViews(binding.filteringRegion2Ll)
            resetLinearLayoutTextViews(binding.filteringRegion3Ll)
        }

        fun getSelectedItemsFromLinearLayout(linearLayout: LinearLayout): List<String> {
            val selectedItems = mutableListOf<String>()
            for (i in 0 until linearLayout.childCount) {
                val view = linearLayout.getChildAt(i)
                if (view is TextView) {
                    val background = view.background
                    if (background.constantState == ContextCompat.getDrawable(requireContext(),
                            R.drawable.filtering_selected
                        )?.constantState) {
                        selectedItems.add(view.text.toString())
                    }
                }
            }
            return selectedItems
        }

        val filterViewModel = ViewModelProvider(requireActivity())[FilterViewModel::class.java]
        binding.filteringApplyTv.setOnClickListener {
            val priceMin=binding.filteringRangeslider.values[0].toInt()
            val priceMax=binding.filteringRangeslider.values[0].toInt()
            val peopleCount=binding.filteringPeopleCountTv.text.toString().takeIf{it != "인원 선택"}
            val selectedServices=getSelectedItemsFromLinearLayout(binding.filteringExtraServiceLl)
            val selectedTargets=getSelectedItemsFromLinearLayout(binding.filteringTargetLl)
            val selectedRegions1=getSelectedItemsFromLinearLayout(binding.filteringRegion1Ll)
            val selectedRegions2=getSelectedItemsFromLinearLayout(binding.filteringRegion2Ll)
            val selectedRegions3=getSelectedItemsFromLinearLayout(binding.filteringRegion3Ll)

            val filterOptions = FilterOptions(
                availableOnly = true, // or false depending on toggle/switch 상태
                priceMin = priceMin,
                priceMax = priceMax,
                peopleCount = peopleCount,
                selectedServices = selectedServices,
                selectedTargets = selectedTargets,
                selectedRegions1 = selectedRegions1,
                selectedRegions2 = selectedRegions2,
                selectedRegions3 = selectedRegions3
            )
            filterViewModel.applyFilters(filterOptions)
            //activity로 값들 전달
//            val intent = Intent(requireContext(), SearchResultActivity::class.java)
//            intent.putExtra("filterOptions", filterOptions)
//            startActivity(intent)

            filterViewModel.applyFilters(filterOptions)
            parentFragmentManager.beginTransaction()
                .replace(R.id.search_result_all_cl, SearchResultFragment())
                .addToBackStack(null)
                .commit()

        }

        //fragment한테 전달할거면 이거로
//        val bundle=Bundle().apply{
//            putInt("price_min", priceMin)
//            putInt("price_max", priceMax)
//            putString("people_count", peopleCount ?: "")
//            putStringArrayList("services", ArrayList(selectedServices))
//            putStringArrayList("targets", ArrayList(selectedTargets))
//            putStringArrayList("regions", ArrayList(selectedRegions1))
//            putStringArrayList("regions", ArrayList(selectedRegions2))
//            putStringArrayList("regions", ArrayList(selectedRegions3))
//        }
        //받는 쪽에서 arguments?.getInt("price_min")로 꺼내면 됨
    }
}
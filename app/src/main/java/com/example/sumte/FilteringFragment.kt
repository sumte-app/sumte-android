package com.example.sumte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sumte.databinding.FragmentFilteringBinding
import com.google.android.material.chip.Chip

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
                binding.filteringPeopleCountTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray600))
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
                binding.filteringPeopleCountTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray600))
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
                    textView.setTextColor(ContextCompat.getColor(binding.root.context, R.color.gray800))
                    textView.tag = "unselected"
                }
                else{
                    textView.setBackgroundResource(R.drawable.filtering_selected)
                    textView.setTextColor(ContextCompat.getColor(binding.root.context, R.color.primary))
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
                    textView.setTextColor(ContextCompat.getColor(binding.root.context, R.color.gray800))
                    textView.tag = "unselected"
                }
                else{
                    textView.setBackgroundResource(R.drawable.filtering_selected)
                    textView.setTextColor(ContextCompat.getColor(binding.root.context, R.color.primary))
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



        //재설정 버튼 아직 미완성
        binding.filteringResetLl.setOnClickListener{
            binding.filteringCheckbox.isChecked = false
            binding.filteringRangeslider.values = listOf(1000f, 100000f)
            binding.filteringPeopleCountTv.setText("인원 선택")
            for (i in 0 until binding.filteringExtraServiceLl.childCount) {
                val chip = binding.filteringExtraServiceLl.getChildAt(i) as Chip
                chip.isChecked = false
            }
            for (i in 0 until binding.filteringTargetLl.childCount) {
                val chip = binding.filteringTargetLl.getChildAt(i) as Chip
                chip.isChecked = false
            }
        }

    }
}
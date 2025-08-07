package com.example.sumte.mybook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumte.databinding.FragmentBookedListMainBinding

class BookedListMainFragment : Fragment(){
    lateinit var binding: FragmentBookedListMainBinding

    private val bookedList = listOf(
        BookedData(
            bookedDate = "2025-05-01",
            dayCount = 7,
            houseName = "애월 게스트하우스",
            roomType = "디럭스룸",
            startDate = "6.18 수",
            endDate = "6.19목",
            dateCount = "1박",
            adultCount = 2,
            childCount = 1
        ),
        BookedData(
            bookedDate = "2025-05-01",
            dayCount = 7,
            houseName = "제주 애월 게스트하우스",
            roomType = "4인 도미토리",
            startDate = "6.18 수",
            endDate = "6.19목",
            dateCount = "1박",
            adultCount = 2,
            childCount = 0
        )
    )



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentBookedListMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.backBtn.setOnClickListener {
            requireActivity().finish()
        }
        val adapter = BookedAdapter(bookedList)
        binding.bookedListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.bookedListRecyclerview.adapter = adapter
    }
}
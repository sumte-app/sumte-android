package com.example.sumte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumte.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
        val guestList = listOf(
            GuestHouse("서귀포 섬게스트하우스", "서귀포항 근처", "19,000원~", R.drawable.sample_house1, "1"),
            GuestHouse("제주 꿀림 365", "애월읍", "23,000원~", R.drawable.sample_house2, "2"),
            GuestHouse("제주 달숲 게스트하우스", "협재 버스정류장", "80,000원~", R.drawable.sample_house3, "3"),
            // ...
        )

        binding.guesthouseRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = GuestHouseAdapter(guestList)
        }
        binding.searchBox.setOnClickListener {
            (activity as? MainActivity)?.navigateToSearchFragment()
        }


    }
}
package com.example.sumte

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        val adapter = GuestHouseAdapter(guestList) { clickedGuest ->

            // HouseDetailFragment로 전환
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, HouseDetailFragment()) // FrameLayout ID
                .addToBackStack(null)
                .commit()
        }


        binding.guesthouseRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        binding.searchBoxLl.setOnClickListener {
            (activity as? MainActivity)?.navigateToSearchFragment()
        }

        binding.adsTv.setOnClickListener {
            logout()
        }


    }

    private fun logout() {
        requireActivity().getSharedPreferences("auth", AppCompatActivity.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        startActivity(Intent(requireContext(), LoginActivity::class.java))
    }
}
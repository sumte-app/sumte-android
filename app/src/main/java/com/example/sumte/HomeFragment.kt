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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumte.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: GuestHouseViewModel
    private lateinit var adapter: GuestHouseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentHomeBinding.inflate(inflater, container, false)
        // ReviewWrite 실행을 위한 임시 클릭리스너
        binding.mainLogoIv.setOnClickListener {
            val intent = Intent(activity, ReviewWriteActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[GuestHouseViewModel::class.java]
        val guestList = listOf(
            GuestHouse("서귀포 섬게스트하우스", "서귀포항 근처", "19,000", R.drawable.sample_house1, "16:00", 1L),
            GuestHouse("제주 꿀림 365", "애월읍", "23,000", R.drawable.sample_house2, "16:00",2L),
            GuestHouse("제주 달숲 게스트하우스", "협재 버스정류장", "80,000", R.drawable.sample_house3, "16:00",3L),
            // ...
        )
        adapter = GuestHouseAdapter(
            items = guestList,
            viewModel = viewModel,
            onItemClick = { clickedGuest ->
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, HouseDetailFragment())
                    .addToBackStack(null)
                    .commit()
            }
        )
        binding.guesthouseRv.layoutManager = LinearLayoutManager(requireContext())
        binding.guesthouseRv.adapter = adapter
//        val adapter = GuestHouseAdapter(
//            items = guestList,
//            onItemClick = { clickedGuest -> // 아이템 전체 클릭 시 동작
//                // HouseDetailFragment로 전환
//                requireActivity().supportFragmentManager.beginTransaction()
//                    .replace(R.id.main_container, HouseDetailFragment()) // FrameLayout ID
//                    .addToBackStack(null)
//                    .commit()
//            },
//            onHeartClick = { clickedGuest -> // 하트 이미지 클릭 시 동작
//                viewModel.addToLiked(clickedGuest)
//                showToast("하트 클릭: ${clickedGuest.title}")
//            }
//        )


//        binding.guesthouseRv.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            this.adapter = adapter
//        }

        binding.searchBoxLl.setOnClickListener {
            (activity as? MainActivity)?.navigateToSearchFragment()
        }
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
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
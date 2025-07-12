package com.example.sumte

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumte.databinding.FragmentHouseDetailBinding

class HouseDetailFragment : Fragment() {

    lateinit var binding : FragmentHouseDetailBinding
    private lateinit var adapter: RoomInfoAdapter

    val sampleRooms = listOf(
        RoomInfo("남자 도미토리 4인", 28000, 4, 8, "17:00", "11:00", R.drawable.sample_room1),
        RoomInfo("여자 도미토리 2인", 30000, 2, 4, "15:00", "11:00", R.drawable.sample_room2),
        RoomInfo("프라이빗 싱글룸", 50000, 1, 1, "16:00", "10:00", R.drawable.sample_room1)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHouseDetailBinding.inflate(inflater,container,false)



        val imageList = listOf(
            R.drawable.sample_house1,
            R.drawable.sample_house2,
            R.drawable.sample_house3
        )

        val imageAdapter = HouseImageAdapter(imageList)
        binding.vpHouseImage.adapter = imageAdapter

        adapter = RoomInfoAdapter(sampleRooms) { room -> }

        binding.rvInfo.adapter = adapter
        binding.rvInfo.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }



}
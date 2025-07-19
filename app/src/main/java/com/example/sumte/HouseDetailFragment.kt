package com.example.sumte

import android.graphics.Color
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.sumte.databinding.FragmentHouseDetailBinding

class HouseDetailFragment : Fragment() {

    lateinit var binding : FragmentHouseDetailBinding
    private lateinit var adapter: RoomInfoAdapter

    val sampleRooms = listOf(
        RoomInfo("남자 도미토리 4인", 28000, 4, 8, "17:00", "11:00", R.drawable.sample_room1),
        RoomInfo("여자 도미토리 2인", 30000, 2, 4, "15:00", "11:00", R.drawable.sample_room2),
        RoomInfo("프라이빗 싱글룸", 50000, 1, 1, "16:00", "10:00", R.drawable.sample_room1)
    )

    val sampleReviews = listOf(
        Review(
            id = "1",
            title = "가성비 최고의 숙소",
            content = "깨끗하고 위치도 좋았어요. 다음에도 또 오고 싶어요!",
            date = "2025-07-13",
            imageUrls = null,
            rating = 4.5f
        ),
        Review(
            id = "2",
            title = "아쉬움이 좀 있었어요",
            content = "전체적으로는 괜찮았지만 화장실이 조금 불편했어요.",
            date = "2025-07-14",
            imageUrls = null,
            rating = 3.0f
        )
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

        binding.vpHouseImage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tvPageIndicator.text = "${position + 1} | ${imageList.size}"
            }
        })

        binding.vpHouseImage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePageIndicator(position + 1, imageList.size)
            }
        })


        updatePageIndicator(1, imageList.size)

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }


        val reviewAdapter = ReviewCardAdapter(sampleReviews)
        binding.rvReviewList.adapter = reviewAdapter
        binding.rvReviewList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        return binding.root
    }


    //inddicator 색상 조정
    private fun updatePageIndicator(current: Int, total: Int){
        val indicatorText = "$current | $total"
        val spannable = SpannableString(indicatorText)

        val currentLength = current.toString().length

        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#FFFFFF")),
            0,
            currentLength,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvPageIndicator.text = spannable
    }



}
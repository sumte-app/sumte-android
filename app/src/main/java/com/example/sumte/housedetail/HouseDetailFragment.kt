package com.example.sumte.housedetail

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.sumte.HouseImageAdapter
import com.example.sumte.R
import com.example.sumte.Review
import com.example.sumte.housedetail.ReviewCardAdapter
import com.example.sumte.housedetail.RoomInfo
import com.example.sumte.housedetail.RoomInfoAdapter
import com.example.sumte.RoomRegisterActivity
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
        binding.rvReviewList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)


        //rvInfo 아이템 사이 간격
        val divider = DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
            ContextCompat.getDrawable(requireContext(), R.drawable.divider)?.let {
                setDrawable(it)
            }
        }
        binding.rvInfo.addItemDecoration(divider)

        binding.rvInfo.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)


                outRect.top = if (position == 0) 0 else dpToPx(12)
                outRect.bottom = dpToPx(12)
            }
        })

        binding.shareIcon.setOnClickListener {
            Log.d("HouseDetailFragment", "Share icon clicked")
            val intent = Intent(requireContext(), RoomRegisterActivity::class.java)
            intent.putExtra("guesthouseId", 1)
            startActivity(intent)
        }



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

    //dp px 변환
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
        ).toInt()
    }


}
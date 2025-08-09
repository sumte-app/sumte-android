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
import android.widget.Toast
import androidx.core.content.ContextCompat

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.sumte.App
import com.example.sumte.HouseImageAdapter
import com.example.sumte.R
import com.example.sumte.RetrofitClient
import com.example.sumte.housedetail.RoomInfo
import com.example.sumte.housedetail.RoomInfoAdapter
import com.example.sumte.roomregister.RoomRegisterActivity
import com.example.sumte.databinding.FragmentHouseDetailBinding
import com.example.sumte.review.Review
import com.example.sumte.review.ReviewCardAdapter
import com.example.sumte.search.BookInfoActivity
import com.example.sumte.search.BookInfoCountFragment
import com.example.sumte.search.BookInfoDateFragment
import com.example.sumte.search.BookInfoViewModel
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class HouseDetailFragment : Fragment() {

    private lateinit var binding: FragmentHouseDetailBinding
    private lateinit var adapter: RoomInfoAdapter
    private val viewModel by lazy {
        ViewModelProvider(
            App.instance,
            ViewModelProvider.AndroidViewModelFactory.getInstance(App.instance)
        )[BookInfoViewModel::class.java]
    }

    // ViewModel
    private val vm: HouseDetailViewModel by lazy {
        val repo = RoomRepository(RetrofitClient.roomService)
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HouseDetailViewModel(repo) as T
        }.create(HouseDetailViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHouseDetailBinding.inflate(inflater, container, false)
        val imageList = listOf(
            R.drawable.sample_house1,
            R.drawable.sample_house2,
            R.drawable.sample_house3
        )
        binding.vpHouseImage.adapter = HouseImageAdapter(imageList)
        binding.vpHouseImage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePageIndicator(position + 1, imageList.size)
            }
        })
        updatePageIndicator(1, imageList.size)

        // --- 객실 리스트 어댑터 (실데이터) ---

        adapter = RoomInfoAdapter(emptyList()) { room ->
            // 예약 버튼 클릭 시 처리 (필요시 구현)
        }
        binding.rvInfo.adapter = adapter
        binding.rvInfo.layoutManager = LinearLayoutManager(requireContext())

        // 구분선
        val divider = DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
            ContextCompat.getDrawable(requireContext(), R.drawable.divider)?.let { setDrawable(it) }
        }
        binding.rvInfo.addItemDecoration(divider)
        // 간격
        binding.rvInfo.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val position = parent.getChildAdapterPosition(view)
                outRect.top = if (position == 0) 0 else dpToPx(12)
                outRect.bottom = dpToPx(12)
            }
        })

        // --- 리뷰 가로 리스트 (네 샘플 유지) ---
        val sampleReviews = listOf(
            Review("1", "가성비 최고의 숙소", "깨끗하고 위치도 좋았어요. 다음에도 또 오고 싶어요!", "2025-07-13", null, 4.5f),
            Review("2", "아쉬움이 좀 있었어요", "전체적으로는 괜찮았지만 화장실이 조금 불편했어요.", "2025-07-14", null, 3.0f)
        )
        binding.rvReviewList.adapter = ReviewCardAdapter(sampleReviews)
        binding.rvReviewList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // 뒤로가기 & 등록 이동
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.shareIcon.setOnClickListener {
            Log.d("HouseDetailFragment", "Share icon clicked")
            val intent = Intent(requireContext(), com.example.sumte.roomregister.RoomRegisterActivity::class.java)
            intent.putExtra("guesthouseId", 1)
            startActivity(intent)
        }

        // ViewModel 상태 관찰
        observeState()

        // 실제 API 호출 (게스트하우스/날짜는 실제 값으로 교체)
        val guesthouseId = 1
        val startDate = "2025-08-08"
        val endDate = "2025-08-29"
        vm.loadRooms(guesthouseId, startDate, endDate)

        return binding.root
    }

    private fun observeState() {
        vm.state.observe(viewLifecycleOwner) { st ->
            // st가 어떤 상태인지 로그로 확인
            Log.d("HouseDetailFragment", "State changed: ${st::class.java.simpleName}")

            when (st) {
                is RoomUiState.Success -> {
                    // Success 상태일 때, 리스트가 비어있는지, 데이터가 있는지 확인
                    Log.d("HouseDetailFragment", "Success! Item count: ${st.items.size}")
                    if (st.items.isNotEmpty()) {
                        Log.d("HouseDetailFragment", "First item: ${st.items[0]}")
                    }
                    adapter.submitList(st.items)
                }
                is RoomUiState.Error -> {
                    // 에러 상태일 때, 어떤 메시지가 오는지 확인
                    Log.e("HouseDetailFragment", "Error: ${st.msg}")
                    Toast.makeText(requireContext(), st.msg, Toast.LENGTH_SHORT).show()
                }
                RoomUiState.Loading -> {
                    // 로딩 상태인지 확인
                    Log.d("HouseDetailFragment", "State is Loading...")
                }
            }
        }
    }

    // indicator 색상 조정
    private fun updatePageIndicator(current: Int, total: Int) {
        val indicatorText = "$current | $total"
        val spannable = SpannableString(indicatorText)
        val currentLength = current.toString().length
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#FFFFFF")),
            0, currentLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvPageIndicator.text = spannable
    }

    //dp px 변환
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
        ).toInt()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val formatter = DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN)

        val startDate = viewModel.startDate
        val endDate = viewModel.endDate
        val nights = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate)

        binding.startDate.text = startDate.format(formatter)
        binding.endDate.text = endDate.format(formatter)
        binding.dateCount.text = "${nights}박"

        binding.adultCount.text = "성인 ${viewModel.adultCount}"
        binding.childCount.text =
            if (viewModel.childCount > 0) "아동 ${viewModel.childCount}" else ""

        binding.countComma.visibility = if (viewModel.childCount > 0) View.VISIBLE else View.GONE

        binding.dateChangeBar.setOnClickListener {
            val intent = Intent(requireContext(), BookInfoActivity::class.java)
            intent.putExtra(BookInfoActivity.EXTRA_FRAGMENT_TYPE, BookInfoActivity.TYPE_DATE)
            startActivity(intent)
        }

        binding.countChangeBar.setOnClickListener {
            val intent = Intent(requireContext(), BookInfoActivity::class.java)
            intent.putExtra(BookInfoActivity.EXTRA_FRAGMENT_TYPE, BookInfoActivity.TYPE_COUNT)
            startActivity(intent)
        }
    }
    //재시작할 때
    override fun onResume() {
        super.onResume()
        updateUIFromViewModel()
    }

    private fun updateUIFromViewModel() {
        val formatter = DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN)

        val sDate = viewModel.startDate
        val eDate = viewModel.endDate

        if (sDate != null && eDate != null) {
            binding.startDate.text = sDate.format(formatter)
            binding.endDate.text = eDate.format(formatter)
            val nights = ChronoUnit.DAYS.between(sDate, eDate)
            binding.dateCount.text = "${nights}박"
        }

        binding.adultCount.text = "성인 ${viewModel.adultCount}"
        binding.childCount.text = if (viewModel.childCount > 0) "아동 ${viewModel.childCount}" else ""
        binding.countComma.visibility = if (viewModel.childCount > 0) View.VISIBLE else View.GONE
    }

}
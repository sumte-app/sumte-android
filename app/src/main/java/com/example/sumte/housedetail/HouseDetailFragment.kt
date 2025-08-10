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
import com.example.sumte.R
import com.example.sumte.RetrofitClient
import com.example.sumte.databinding.FragmentHouseDetailBinding
import com.example.sumte.review.Review
import com.example.sumte.review.ReviewCardAdapter
import com.example.sumte.search.BookInfoActivity
import com.example.sumte.search.BookInfoViewModel
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class HouseDetailFragment : Fragment() {

    private lateinit var binding: FragmentHouseDetailBinding
    private lateinit var adapter: RoomInfoAdapter
    private lateinit var imageAdapter: HouseImageAdapter

    private val viewModel by lazy {
        ViewModelProvider(
            App.instance,
            ViewModelProvider.AndroidViewModelFactory.getInstance(App.instance)
        )[BookInfoViewModel::class.java]
    }

    //게하 id값 받아오기
    companion object {
        private const val ARG_GUESTHOUSE_ID = "guesthouseId"
        private const val TEST_GUESTHOUSE_ID = 2

        fun newInstance(guesthouseId: Int) = HouseDetailFragment().apply {
            arguments = Bundle().apply { putInt(ARG_GUESTHOUSE_ID, guesthouseId) }
        }
    }

    private var guesthouseId: Int = TEST_GUESTHOUSE_ID //임시 수정 필요

    // ViewModel
    private val vm: HouseDetailViewModel by lazy {
        val repo = RoomRepository(RetrofitClient.roomService)
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HouseDetailViewModel(repo) as T
        }.create(HouseDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val argId = arguments?.getInt(ARG_GUESTHOUSE_ID)
        if (argId != null && argId > 0) {
            guesthouseId = argId
        } else {
            Log.w("HD/F", "No guesthouseId in args. Using TEST_GUESTHOUSE_ID=$guesthouseId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHouseDetailBinding.inflate(inflater, container, false)

        imageAdapter = HouseImageAdapter(emptyList()) // 아래에 submit(List<String>)가 있는 버전
        binding.vpHouseImage.adapter = imageAdapter
        binding.vpHouseImage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePageIndicator(position + 1, imageAdapter.itemCount)
            }
        })
        updatePageIndicator(1, 0)

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

        Log.d("HD/F", "ARG id=" + arguments?.getInt("guesthouseId")) // ① 전달 확인
        Log.d("HD/F", "use id=$guesthouseId")
        // ViewModel 상태 관찰
        observeState()
        observeHeader()

        // 실제 API 호출 (게스트하우스/날짜는 실제 값으로 교체)
        //val guesthouseId = 2
        val startDate = "2025-08-08"
        val endDate = "2025-08-29"
        vm.loadRooms(guesthouseId, startDate, endDate)
        Log.d("HD/F", "call loadGuesthouse($guesthouseId)")
        vm.loadGuesthouse(guesthouseId)

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

    private fun observeHeader() {
        vm.header.observe(viewLifecycleOwner) { h ->
            Log.d("HD/F", "header updated: name=${h.name}, addr=${h.address}, imgs=${h.imageUrls.size}")
            binding.tvTitle.text = h.name
            binding.tvLocation.text = h.address ?: ""

            imageAdapter.submit(h.imageUrls)           // URL 리스트 주입
            updatePageIndicator(1, h.imageUrls.size)
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
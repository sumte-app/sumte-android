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
import androidx.lifecycle.lifecycleScope

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.sumte.App
import com.example.sumte.R
import com.example.sumte.ReservationRequest
import com.example.sumte.RetrofitClient
import com.example.sumte.common.bindBookInfoUI
import com.example.sumte.common.getBookInfoViewModel
import com.example.sumte.databinding.FragmentHouseDetailBinding
import com.example.sumte.guesthouse.GuestHouseViewModel
import com.example.sumte.reservation.ReservationRepository
import com.example.sumte.review.Review
import com.example.sumte.review.ReviewCardAdapter
import com.example.sumte.search.BookInfoActivity
import com.example.sumte.search.BookInfoViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class HouseDetailFragment : Fragment() {

    private lateinit var binding: FragmentHouseDetailBinding
    private lateinit var adapter: RoomInfoAdapter
    private lateinit var imageAdapter: HouseImageAdapter

    private val bookInfoVM by lazy { getBookInfoViewModel() }

    //게하 id값 받아오기
    companion object {
        private const val ARG_GUESTHOUSE_ID = "guesthouseId"
        fun newInstance(guesthouseId: Int) = HouseDetailFragment().apply {
            arguments = Bundle().apply { putInt(ARG_GUESTHOUSE_ID, guesthouseId) }
        }
    }

    private var guesthouseId: Int = -1

    // ViewModel
    private val houseDetailVM: HouseDetailViewModel by lazy {
        val repo = RoomRepository(RetrofitClient.roomService)
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HouseDetailViewModel(repo) as T
        }.create(HouseDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        guesthouseId = arguments?.getInt(ARG_GUESTHOUSE_ID) ?: -1
        if (guesthouseId <= 0) {
            // 인자 없으면 즉시 종료(개발 중엔 토스트 + 로그)
            Log.e("HD/F", "guesthouseId missing. args=$arguments")
            Toast.makeText(requireContext(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHouseDetailBinding.inflate(inflater, container, false)

        imageAdapter = HouseImageAdapter() // 아래에 submit(List<String>)가 있는 버전
        binding.vpHouseImage.adapter = imageAdapter
        binding.vpHouseImage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePageIndicator(position + 1, imageAdapter.itemCount)
            }
        })
        updatePageIndicator(1, 0)

        adapter = RoomInfoAdapter(emptyList()) { room ->
            val request = ReservationRequest(
                roomId = room.id,
                adultCount = bookInfoVM.adultCount,
                childCount = bookInfoVM.childCount,
                startDate = "${bookInfoVM.startDate}",
                endDate = "${bookInfoVM.endDate}"
            )
            Log.d("Reservation_Request", request.toString())

            lifecycleScope.launch {
                val repository = ReservationRepository(requireContext())
                val response = repository.createReservation(request)
                if (response?.isSuccessful == true) {
                    Toast.makeText(requireContext(), "예약 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("Reservation_Fail", "code=${response?.code()}, msg=${response?.message()}, body=${response?.errorBody()?.string()}")
                    Toast.makeText(requireContext(), "예약 실패", Toast.LENGTH_SHORT).show()
                }
            }
            val start = bookInfoVM.startDate
            val end   = bookInfoVM.endDate
            val nights = maxOf(1, java.time.temporal.ChronoUnit.DAYS.between(start, end).toInt())
            val amount = room.price * nights

            val intent = Intent(requireContext(), com.example.sumte.payment.PaymentActivity::class.java).apply {
                putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_ROOM_ID, room.id)
                putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_ROOM_NAME, room.name)
                putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_GUESTHOUSE_NAME, binding.tvTitle.text?.toString())
                putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_START, start.toString()) // "YYYY-MM-DD"
                putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_END,   end.toString())
                putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_CHECKIN_TIME, room.checkin)
                putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_CHECKOUT_TIME, room.checkout)
                putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_ADULT, bookInfoVM.adultCount)
                putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_CHILD, bookInfoVM.childCount)
                putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_AMOUNT, amount)

                // putExtra(PaymentExtras.EXTRA_RES_ID, reservationId)
            }
            startActivity(intent)

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

        // 리뷰 샘플
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
        
        if (guesthouseId > 0){
            val startDate = "2025-08-08"
            val endDate   = "2025-08-29"

            Log.d("HD/F", "call loadGuesthouse($guesthouseId)")
            houseDetailVM.loadGuesthouse(guesthouseId)
            Log.d("HD/F", "call loadRooms($guesthouseId)")
            houseDetailVM.loadRooms(guesthouseId, startDate, endDate)}


        return binding.root
    }

    private fun observeState() {
        houseDetailVM.state.observe(viewLifecycleOwner) { st ->
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
        houseDetailVM.header.observe(viewLifecycleOwner) { h ->
            Log.d("HD/F", "header updated: name=${h.name}, addr=${h.address}, imgs=${h.imageUrls.size}")
            binding.tvTitle.text = h.name
            binding.tvLocation.text = h.address ?: ""

            val urls = h.imageUrls
            imageAdapter.submitList(urls) {
                // 리스트 반영 후 인디케이터 갱신 (현재 페이지 보존)
                val total = urls.size
                val current = if (total == 0) 0
                else (binding.vpHouseImage.currentItem + 1).coerceAtMost(total)
                updatePageIndicator(current, total)
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
        //뷰모델로 초기화
        bindBookInfoUI(binding, bookInfoVM)

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
        bindBookInfoUI(binding, bookInfoVM)
    }

}
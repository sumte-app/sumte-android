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
import androidx.core.view.isVisible

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.sumte.ApiClient
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
import com.example.sumte.review.ReviewListFragment
import com.example.sumte.search.BookInfoActivity
import com.example.sumte.search.BookInfoViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class HouseDetailFragment : Fragment() {

    private lateinit var binding: FragmentHouseDetailBinding
    private lateinit var adapter: RoomInfoAdapter
    private lateinit var imageAdapter: HouseImageAdapter
    private val reviewAdapter = ReviewCardAdapter()

    // 찜 상태 관리를 위한 ViewModel
    private val guestHouseVM: GuestHouseViewModel by lazy {
        ViewModelProvider(requireActivity())[GuestHouseViewModel::class.java]
    }
    //예약정보 뷰모델
    private val bookInfoVM by lazy { getBookInfoViewModel() }
    private val vm: GuestHouseReviewViewModel by viewModels {

        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val api = ApiClient.reviewService
                val repo = ReviewRepository(api)
                @Suppress("UNCHECKED_CAST")
                return GuestHouseReviewViewModel(repo) as T
            }
        }
    }

    companion object {
        private const val ARG_GUESTHOUSE_ID = "guesthouseId"
        fun newInstance(guesthouseId: Int) = HouseDetailFragment().apply {
            arguments = Bundle().apply { putInt(ARG_GUESTHOUSE_ID, guesthouseId) }
        }
    }

    private var guesthouseId: Int = -1

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


        binding.llSeeAllReviews.setOnClickListener {
            val headerData = houseDetailVM.header.value
            if (headerData != null) {
                val averageScore = headerData.averageScore
                val reviewCount = headerData.reviewCount
                Log.d("DEBUG_HouseDetail", "전달하려는 averageScore 값: $averageScore")

                // ReviewListFragment 인스턴스 생성
                val reviewListFragment = ReviewListFragment()

                // 데이터를 담을 Bundle 생성
                val bundle = Bundle()
                bundle.putLong("guesthouseId_key", guesthouseId.toLong()) // guesthouseId도 함께 전달
                bundle.putDouble("averageScore_key", averageScore ?: 0.0)
                bundle.putInt("reviewCount_key", reviewCount ?: 0)

                reviewListFragment.arguments = bundle

                parentFragmentManager.beginTransaction()
//                    .replace(R.id.main_container, reviewListFragment)
                    .add(R.id.main_container, reviewListFragment) 
                    .hide(this)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), "정보를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivHouseAllReview.setOnClickListener {
            val headerData = houseDetailVM.header.value
            if (headerData != null) {
                val averageScore = headerData.averageScore
                val reviewCount = headerData.reviewCount
                Log.d("DEBUG_HouseDetail", "전달하려는 averageScore 값: $averageScore")

                // ReviewListFragment 인스턴스 생성
                val reviewListFragment = ReviewListFragment()

                // 데이터를 담을 Bundle 생성
                val bundle = Bundle()
                bundle.putLong("guesthouseId_key", guesthouseId.toLong()) // guesthouseId도 함께 전달
                bundle.putDouble("averageScore_key", averageScore ?: 0.0)
                bundle.putInt("reviewCount_key", reviewCount ?: 0)

                reviewListFragment.arguments = bundle

                parentFragmentManager.beginTransaction()
//                    .replace(R.id.main_container, reviewListFragment)
                    .add(R.id.main_container, reviewListFragment)
                    .hide(this)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), "정보를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        adapter = RoomInfoAdapter(emptyList()) { room ->
            val start = bookInfoVM.startDate
            val end   = bookInfoVM.endDate
            val nights = maxOf(1, java.time.temporal.ChronoUnit.DAYS.between(start, end).toInt())
            val totalAmount = room.price * nights

            val request = ReservationRequest(
                roomId = room.id,
                adultCount = bookInfoVM.adultCount,
                childCount = bookInfoVM.childCount,
                startDate = start.toString(),
                endDate = end.toString()
            )

            lifecycleScope.launch {
                val repository = ReservationRepository(requireContext())
                val response = repository.createReservation(request)

                if (response?.isSuccessful == true && response.body()?.success == true) {
                    val resId = response.body()?.data?.reservationId
                    if (resId == null) {
                        Log.e("Reservation", "reservationId is null in success body: ${response.body()}")
                        Toast.makeText(requireContext(), "예약 실패(응답 오류)", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    Toast.makeText(requireContext(), "예약 성공", Toast.LENGTH_SHORT).show()
                    Log.d("Reservation", "reservationId=$resId")


                    val intent = Intent(requireContext(), com.example.sumte.payment.PaymentActivity::class.java).apply {
                        putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_RES_ID, resId) // ★ 필수
                        putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_ROOM_ID, room.id)
                        putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_ROOM_NAME, room.name)
                        putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_GUESTHOUSE_NAME, binding.tvTitle.text?.toString())
                        putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_START, start.toString())
                        putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_END,   end.toString())
                        putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_CHECKIN_TIME, room.checkin)
                        putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_CHECKOUT_TIME, room.checkout)
                        putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_ADULT, bookInfoVM.adultCount)
                        putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_CHILD, bookInfoVM.childCount)
                        putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_AMOUNT, totalAmount) // 총액을 넘김
                    }
                    startActivity(intent)
                } else {
                    val code = response?.code()
                    val msg  = response?.message()
                    val err  = response?.errorBody()?.string()
                    Log.e("Reservation_Fail", "code=$code, msg=$msg, body=$err")
                    Toast.makeText(requireContext(), "예약 실패", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }
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


        binding.rvReviewList.apply {
            adapter = reviewAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collect { st ->
                when (st) {
                    is ReviewUiState.Loading -> {
                        // 필요하면 로딩 인디케이터 보여주기
                    }
                    is ReviewUiState.Success -> {
                        reviewAdapter.submitList(st.items) // ★ 여기서 주입
                    }
                    is ReviewUiState.Error -> {
                        Toast.makeText(requireContext(), st.msg, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

// 최초 로드 (guesthouseId를 Long으로 변환)
        vm.loadFirst(guesthouseId.toLong(), size = 10)

        // 뒤로가기 & 등록 이동
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.shareIcon.setOnClickListener {
            val intent = Intent(requireContext(), com.example.sumte.roomregister.RoomRegisterActivity::class.java)
            intent.putExtra("guesthouseId", 1)
            startActivity(intent)
        }

        Log.d("HD/F", "ARG id=" + arguments?.getInt("guesthouseId"))
        Log.d("HD/F", "use id=$guesthouseId")
        // ViewModel 상태 관찰
        observeState()
        observeHeader()

        if (guesthouseId > 0){
            val fmt = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
            val startDate = bookInfoVM.startDate.format(fmt)
            val endDate   = bookInfoVM.endDate.format(fmt)

            Log.d("HD/F", "call loadGuesthouse($guesthouseId)")
            houseDetailVM.loadGuesthouse(guesthouseId)
            Log.d("HD/F", "call loadRooms($guesthouseId)")
            houseDetailVM.loadRooms(guesthouseId, startDate, endDate)}
        return binding.root
    }

    private fun observeState() {
        houseDetailVM.state.observe(viewLifecycleOwner) { st ->
            // st 상태 확인
            Log.d("HouseDetailFragment", "State changed: ${st::class.java.simpleName}")

            when (st) {
                is RoomUiState.Success -> {

                    Log.d("HouseDetailFragment", "Success! Item count: ${st.items.size}")
                    if (st.items.isNotEmpty()) {
                        Log.d("HouseDetailFragment", "First item: ${st.items[0]}")
                    }
                    adapter.submitList(st.items)
                }
                is RoomUiState.Error -> {

                    Log.e("HouseDetailFragment", "Error: ${st.msg}")
                    Toast.makeText(requireContext(), st.msg, Toast.LENGTH_SHORT).show()
                }
                RoomUiState.Loading -> {

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
            binding.tvReview.text = String.format("%.1f", h.averageScore ?: 0.0)
            binding.tvReviewCount.text = h.reviewCount.toString()

            binding.tvReviewScore.text = String.format("%.1f", h.averageScore ?: 0.0)
            binding.tvReviewCount2.text = h.reviewCount.toString()

            val urls = h.imageUrls
            imageAdapter.submitList(urls) {

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

        bindBookInfoUI(binding, bookInfoVM)

        binding.homeIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        //객실 예약가능 정보 반영캘린더
        binding.dateChangeBar.setOnClickListener {
            val intent = Intent(requireContext(), BookInfoActivity::class.java).apply {
                putExtra(BookInfoActivity.EXTRA_FRAGMENT_TYPE, BookInfoActivity.TYPE_DATE)
                putExtra(BookInfoActivity.EXTRA_SOURCE, "house_detail") // source 전달
                putExtra("guesthouseId", guesthouseId)
            }
            startActivity(intent)
        }

        binding.countChangeBar.setOnClickListener {
            val intent = Intent(requireContext(), BookInfoActivity::class.java).apply {
                putExtra(BookInfoActivity.EXTRA_FRAGMENT_TYPE, BookInfoActivity.TYPE_COUNT)
                putExtra(BookInfoActivity.EXTRA_SOURCE, "house_detail") // source 전달
                putExtra("guesthouseId", guesthouseId)
            }
            startActivity(intent)
        }

        // 찜 상태 확인 및 클릭 리스너
        setupLikeButton()

        // ViewModel에 저장된 위치로 스크롤 복원
        binding.nVHouseDetail.post {
            binding.nVHouseDetail.scrollTo(0, houseDetailVM.scrollPosition)
        }
    }
    //재시작할 때
    override fun onResume() {
        super.onResume()
        bindBookInfoUI(binding, bookInfoVM)
    }
    // 찜 버튼 초기 설정 함수
    private fun setupLikeButton() {
        // 찜 상태가 변경될 때마다 UI를 자동으로 업데이트
        viewLifecycleOwner.lifecycleScope.launch {
            guestHouseVM.likedGuestHouseIds.collect { likedIds ->
                val isLiked = likedIds.contains(guesthouseId)
                updateLikeButtonUI(isLiked)
            }
        }

        // 찜 버튼 클릭 시 찜 추가/삭제 로직 실행
        binding.ivLike.setOnClickListener {
            val isCurrentlyLiked = guestHouseVM.likedGuestHouseIds.value.contains(guesthouseId)
            if (isCurrentlyLiked) {
                guestHouseVM.removeLike(guesthouseId)
            } else {
                guestHouseVM.addLike(guesthouseId)
            }
        }
    }

    // 찜 상태에 따라 하트 아이콘을 변경하는 함수
    private fun updateLikeButtonUI(isLiked: Boolean) {
        if (isLiked) {
            binding.ivLike.setImageResource(R.drawable.heart_black)
        } else {
            binding.ivLike.setImageResource(R.drawable.heart)
        }
    }

    override fun onDestroyView() {
        houseDetailVM.scrollPosition = binding.nVHouseDetail.scrollY
        super.onDestroyView()
    }

}
package com.example.sumte.housedetail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.sumte.R
import com.example.sumte.ReservationRequest
import com.example.sumte.RetrofitClient
import com.example.sumte.common.bindBookInfoUI
import com.example.sumte.common.getBookInfoViewModel
import com.example.sumte.databinding.ActivityRoomDetailBinding
import com.example.sumte.payment.PaymentActivity
import com.example.sumte.reservation.ReservationRepository
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import kotlin.math.abs

class ActivityRoomDetail : AppCompatActivity() {

    private lateinit var binding: ActivityRoomDetailBinding

    // 날짜/인원 선택 정보를 공유하는 VM
    private val bookInfoVM by lazy { getBookInfoViewModel() }

    // 방 상세 VM
    private val houseDetailVM: HouseDetailViewModel by lazy {
        val factory = HouseDetailViewModelFactory(RoomRepository(RetrofitClient.roomService))
        ViewModelProvider(this, factory)[HouseDetailViewModel::class.java]
    }

    // 이미지 페이저 어댑터
    private lateinit var pagerAdapter: RoomImagePagerAdapter

    // 페이지 변경 콜백
    private var pagerCallback: ViewPager2.OnPageChangeCallback? = null

    private var baseReservableFromIntent: Boolean = false
    private var totalCountFromIntent: Int = -1
    private var lastDisableReason: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        baseReservableFromIntent = intent.getBooleanExtra("reservableBase", false)
        totalCountFromIntent = intent.getIntExtra("totalCountBase", -1)

        if (intent.hasExtra("reservableBase") && intent.hasExtra("totalCountBase") && totalCountFromIntent > 0) {
            recomputeReserveButton(baseReservableFromIntent, totalCountFromIntent)
        } else {
            // 상세 로드 전 임시 상태
            binding.btnReserve.isEnabled = false
            binding.btnReserve.isClickable = false
            lastDisableReason = "객실 정보를 불러오는 중입니다."
        }

        // ViewPager2 초기화
        pagerAdapter = RoomImagePagerAdapter()
        binding.vpRoom.adapter = pagerAdapter
        binding.vpRoom.offscreenPageLimit = 1
        binding.vpRoom.setPageTransformer { page, pos ->
            page.alpha = 0.25f + (1 - abs(pos)) * 0.75f
        }

        // 페이지 선택 시 배지 업데이트
        pagerCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePagerCounter(position)
            }
        }
        binding.vpRoom.registerOnPageChangeCallback(pagerCallback!!)

        // 초기 테스트 이미지 2장
        setRoomImages(
            listOf(
                resourceUri(R.drawable.room_detail1),
                resourceUri(R.drawable.room_detail1) // 다른 테스트 이미지 있으면 교체
            )
        )

        // 뒤로가기 버튼
        binding.backBtn.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // 날짜·인원 UI 바인딩
        bindBookInfoUI(binding, bookInfoVM)

        // intent로 넘어온 roomId 받기
        val roomId = intent.getIntExtra("roomId", -1)
        Log.d("RoomInfoAdapter", "Sending roomId=$roomId")
        if (roomId == -1) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 방 상세 데이터 관찰
        houseDetailVM.roomDetail.observe(this) { room: RoomDetailInfo ->
            bindRoomInfo(room)
            setRoomImages(room.imageUrls)   // 서버 이미지 뷰페이저 반영

            recomputeReserveButton(baseReservableFromIntent, room.totalCount)
        }

        // 상세 API 호출
        houseDetailVM.loadRoom(roomId)

        // 예약 버튼 클릭
        binding.btnReserve.setOnClickListener {
            if (!binding.btnReserve.isEnabled) {
                lastDisableReason?.let { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
                return@setOnClickListener
            }

            val rid = intent.getIntExtra("roomId", -1)
            if (rid == -1) return@setOnClickListener

            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val request = ReservationRequest(
                roomId = rid,
                adultCount = bookInfoVM.adultCount,
                childCount = bookInfoVM.childCount,
                startDate = bookInfoVM.startDate.format(formatter),
                endDate = bookInfoVM.endDate.format(formatter)
            )

            // ✅ 여기서 생성 (스코프 보장)
            val repository = ReservationRepository(this@ActivityRoomDetail)

            binding.btnReserve.isEnabled = false
            lifecycleScope.launch {
                try {
                    val response = repository.createReservation(request)
                    if (response?.isSuccessful == true && response.body()?.success == true) {
                        val resId = response.body()?.data?.reservationId
                        if (resId == null) {
                            Toast.makeText(this@ActivityRoomDetail, "예약 실패(응답 오류)", Toast.LENGTH_SHORT).show()
                            // 실패 → 버튼 상태 복구
                            recomputeReserveButton(
                                baseReservableFromIntent,
                                houseDetailVM.roomDetail.value?.totalCount ?: totalCountFromIntent
                            )
                            return@launch
                        }

                        // (선택) 금액 계산
                        val nights = kotlin.math.max(
                            1,
                            java.time.temporal.ChronoUnit.DAYS.between(bookInfoVM.startDate, bookInfoVM.endDate).toInt()
                        )
                        val totalAmount = (houseDetailVM.roomDetail.value?.price ?: 0) * nights

                        // (선택) createdAt 포맷
                        val createdAtIso = java.time.ZonedDateTime
                            .now(java.time.ZoneId.of("Asia/Seoul"))
                            .toLocalDateTime()
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"))

                        // ✅ 결제 화면으로 필요한 값 전달 (Fragment와 일관)
                        val intent = Intent(this@ActivityRoomDetail, PaymentActivity::class.java).apply {
                            putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_RES_ID, resId)
                            putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_ROOM_ID, rid)
                            putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_ROOM_NAME, binding.tvRoomName.text?.toString())
                            // 게스트하우스명은 이 화면에 없으면 생략 or 상단에서 인텐트로 받아둔 값을 쓰세요.
                            putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_START, bookInfoVM.startDate.format(formatter))
                            putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_END,   bookInfoVM.endDate.format(formatter))
                            putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_CHECKIN_TIME, houseDetailVM.roomDetail.value?.checkin)
                            putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_CHECKOUT_TIME, houseDetailVM.roomDetail.value?.checkout)
                            putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_ADULT, bookInfoVM.adultCount)
                            putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_CHILD, bookInfoVM.childCount)
                            putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_AMOUNT, totalAmount)
                            putExtra(com.example.sumte.payment.PaymentExtras.EXTRA_CREATED_AT, createdAtIso)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@ActivityRoomDetail,
                            "예약 실패: ${response?.body()?.message ?: response?.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        // 실패 → 버튼 상태 복구
                        recomputeReserveButton(
                            baseReservableFromIntent,
                            houseDetailVM.roomDetail.value?.totalCount ?: totalCountFromIntent
                        )
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ActivityRoomDetail, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                    // 오류 → 버튼 상태 복구
                    recomputeReserveButton(
                        baseReservableFromIntent,
                        houseDetailVM.roomDetail.value?.totalCount ?: totalCountFromIntent
                    )
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        recomputeReserveButton(
            baseReservableFromIntent,
            houseDetailVM.roomDetail.value?.totalCount ?: totalCountFromIntent
        )
    }

    override fun onDestroy() {
        pagerCallback?.let { binding.vpRoom.unregisterOnPageChangeCallback(it) }
        super.onDestroy()
    }

    private fun bindRoomInfo(room: RoomDetailInfo) {
        binding.apply {
            tvRoomName.text = room.name
            tvPrice.text = String.format("%,d원", room.price)
            tvPeopleInfo1.text = "기준인원 ${room.standardCount}인"
            tvPeopleInfo2.text = "(정원 ${room.totalCount}인)"
            tvCheckInOutText.text = "체크인 ${room.checkin} - 체크아웃 ${room.checkout}"
            tvBasicDesc.text = room.content


        }


    }

    /** 이미지 목록 세팅 (null/빈 경우 기본 이미지) */
    fun setRoomImages(urls: List<String>?) {
        val images = if (!urls.isNullOrEmpty()) urls
        else listOf(resourceUri(R.drawable.room_detail1))

        pagerAdapter.submit(images)

        // 숫자 배지 표시
        val count = pagerAdapter.itemCount
        binding.pagerCounter.visibility = if (count > 1) View.VISIBLE else View.GONE
        updatePagerCounter(binding.vpRoom.currentItem)
    }

    /** 현재 페이지 | 전체 페이지 갱신 */
    private fun updatePagerCounter(currentPos: Int) {
        val total = pagerAdapter.itemCount
        if (total > 1) {
            binding.pagerCounter.text = "${currentPos + 1} | $total"
        }
    }

    private fun resourceUri(@androidx.annotation.DrawableRes resId: Int): String {
        return "android.resource://$packageName/$resId"
    }

    class HouseDetailViewModelFactory(
        private val repo: RoomRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HouseDetailViewModel(repo) as T
        }
    }

    private fun recomputeReserveButton(reservable: Boolean, totalCount: Int?) {
        val people = bookInfoVM.adultCount + bookInfoVM.childCount
        val enabled = reservable && (totalCount != null) && totalCount > 0 && (people <= totalCount)

        binding.btnReserve.isEnabled = enabled
        binding.btnReserve.isClickable = enabled
        binding.btnReserve.text = if (enabled) "예약하기" else "예약불가"

        lastDisableReason = when {
            !reservable -> "해당 객실은 현재 예약 불가 상태입니다."
            totalCount == null || totalCount <= 0 -> "객실 정보를 불러오는 중입니다."
            people > totalCount -> "선택 인원이 정원을 초과했습니다. (선택: ${people}명 / 정원: ${totalCount}명)"
            else -> null
        }
    }
}

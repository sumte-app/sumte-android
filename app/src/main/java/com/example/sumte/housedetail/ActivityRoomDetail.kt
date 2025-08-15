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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        }

        // 상세 API 호출
        houseDetailVM.loadRoom(roomId)

        // 예약 버튼 클릭
        binding.btnReserve.setOnClickListener {
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

            val repository = ReservationRepository(this)

            lifecycleScope.launch {
                try {
                    val response = repository.createReservation(request)
                    if (response?.isSuccessful == true && response.body()?.success == true) {
                        Toast.makeText(this@ActivityRoomDetail, "예약 성공", Toast.LENGTH_SHORT).show()
                        Log.d("ReservationResponse", "${response.body()}")
                        startActivity(Intent(this@ActivityRoomDetail, PaymentActivity::class.java))
                    } else {
                        Toast.makeText(
                            this@ActivityRoomDetail,
                            "예약 실패: ${response?.body()?.message ?: response?.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@ActivityRoomDetail,
                        "네트워크 오류: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
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
}

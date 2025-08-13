
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
import com.bumptech.glide.Glide
import com.example.sumte.App
import com.example.sumte.ReservationRequest
import com.example.sumte.ReservationResponse

import com.example.sumte.RetrofitClient
import com.example.sumte.databinding.ActivityRoomDetailBinding
import com.example.sumte.payment.PaymentActivity
import com.example.sumte.reservation.ReservationRepository
import com.example.sumte.search.BookInfoViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class ActivityRoomDetail : AppCompatActivity() {
    private val bookInfoVM by lazy {
        ViewModelProvider(
            App.instance,
            ViewModelProvider.AndroidViewModelFactory.getInstance(App.instance)
        )[BookInfoViewModel::class.java]
    }

    private lateinit var binding: ActivityRoomDetailBinding

    private val houseDetailVM: HouseDetailViewModel by lazy {
        val factory = HouseDetailViewModelFactory(RoomRepository(RetrofitClient.roomService))
        ViewModelProvider(this, factory)[HouseDetailViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // intent로 넘어온 roomId 받기
        val roomId = intent.getIntExtra("roomId", -1)
        Log.d("RoomInfoAdapter", "Sending roomId=${roomId}")
        if (roomId == -1) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // LiveData observe
        houseDetailVM.state.observe(this) { state ->
            when (state) {
                is RoomUiState.Loading -> {
                    // 로딩 처리 가능
                }

                is RoomUiState.Success -> {
                    val room = state.items[0] // 단일 조회니까 첫 번째 아이템
                    bindRoomInfo(room)
                }

                is RoomUiState.Error -> {
                    Toast.makeText(this, state.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ViewModel에서 API 호출
        houseDetailVM.loadRoom(roomId)

        binding.backBtn.setOnClickListener { onBackPressedDispatcher.onBackPressed() }


        // 날짜·인원 정보 초기화
        val formatter = DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN)

        val startDate = bookInfoVM.startDate
        val endDate = bookInfoVM.endDate
        val nights = ChronoUnit.DAYS.between(startDate, endDate)

        binding.startDate.text = startDate.format(formatter)
        binding.endDate.text = endDate.format(formatter)
        binding.dateCount.text = "${nights}박"

        binding.adultCount.text = "성인 ${bookInfoVM.adultCount}"
        binding.childCount.text =
            if (bookInfoVM.childCount > 0) "아동 ${bookInfoVM.childCount}" else ""

        binding.countComma.visibility = if (bookInfoVM.childCount > 0) View.VISIBLE else View.GONE


        //예약버튼
        binding.btnReserve.setOnClickListener {
            val roomId = intent.getIntExtra("roomId", -1)
            if (roomId == -1) return@setOnClickListener

            val request = ReservationRequest(
                roomId = roomId,
                adultCount = bookInfoVM.adultCount,
                childCount = bookInfoVM.childCount,
                startDate = "${bookInfoVM.startDate}",
                endDate = "${bookInfoVM.endDate}"
            )

            val repository = ReservationRepository(this)

            lifecycleScope.launch {
                try {
                    val response = repository.createReservation(request)
                    if (response?.isSuccessful == true && response.body()?.success == true) {
                        Toast.makeText(this@ActivityRoomDetail, "예약 성공", Toast.LENGTH_SHORT).show()
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

        private fun bindRoomInfo(room: RoomInfo) {
        binding.apply {
            tvRoomName.text = room.name
            tvPrice.text = "${String.format("%,d", room.price)}원"
            tvPeopleInfo1.text = "기준인원 ${room.standardCount}인"
            tvPeopleInfo2.text = "(정원 ${room.totalCount}인)"
            tvCheckInOutText.text = "체크인 ${room.checkin} - 체크아웃 ${room.checkout}"
            tvBasicDesc.text = room.content

//            room.imageUrl?.let {
//                Glide.with(this@ActivityRoomDetail)
//                    .load(it)
//                    .placeholder(android.R.color.darker_gray)
//                    .into(ivRoomImage)
//            }
        }
    }

    class HouseDetailViewModelFactory(
        private val repo: RoomRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HouseDetailViewModel(repo) as T
        }
    }
}
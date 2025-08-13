
package com.example.sumte.housedetail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.sumte.RetrofitClient
import com.example.sumte.databinding.ActivityRoomDetailBinding
import com.example.sumte.payment.PaymentActivity

class ActivityRoomDetail : AppCompatActivity() {

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
        binding.btnReserve.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            startActivity(intent)
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
//package com.example.sumte.housedetail
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.viewModels
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.example.sumte.RetrofitClient
//import com.example.sumte.databinding.ActivityRoomDetailBinding
//import com.example.sumte.payment.PaymentActivity
//
//class ActivityRoomDetail : AppCompatActivity() {
//
//    private lateinit var binding: ActivityRoomDetailBinding
//    // Fragment에서 ViewModel 생성
//    private val houseDetailVM: HouseDetailViewModel by lazy {
//        val factory = HouseDetailViewModelFactory(RoomRepository(RetrofitClient.roomService))
//        ViewModelProvider(this, factory)[HouseDetailViewModel::class.java]
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityRoomDetailBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        houseDetailVM.state.observe(this) { state ->
//            Log.d("RoomDetail", "state changed: $state")
//            when (state) {
//                is RoomUiState.Loading -> { /* 로딩 */ }
//                is RoomUiState.Success -> {
//                    val room = state.items[0]
//                    binding.apply {
//                        tvRoomName.text = room.name
//                        tvPrice.text = "${room.price}원"
//                        tvPeopleInfo1.text = "기준인원 ${room.standardCount}인"
//                        tvPeopleInfo2.text = "정원 ${room.totalCount}인"
//                        tvCheckInOutText.text = "체크인 ${room.checkin} · 체크아웃 ${room.checkout}"
////                        room.imageUrl?.let {
////                            Glide.with(this@ActivityRoomDetail)
////                                .load(it)
////                                .placeholder(android.R.color.darker_gray)
////                                .into(ivRoomImage)
////                        }
//                    }
//                }
//                is RoomUiState.Error -> {
//                    Toast.makeText(this, state.msg, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        // housedetail frag에서 intent로 넘어온 데이터 받기
//        val roomId = intent.getIntExtra("roomId", -1)
//        if (roomId != -1) {
//            houseDetailVM.loadRoom(roomId)  // 여기서 API 호출
//        }
//
//        // 뒤로가기 버튼 클릭 시 이전 화면으로
//        binding.backBtn.setOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }
//
//        binding.btnReserve.setOnClickListener {
//            val intent = Intent(this, PaymentActivity::class.java)
//            startActivity(intent)
//        }
//    }
//    class HouseDetailViewModelFactory(
//        private val repo: RoomRepository
//    ) : ViewModelProvider.Factory {
//        override fun <T : ViewModel> create(modelClass: Class<T>): T {
//            return HouseDetailViewModel(repo) as T
//        }
//    }
//}

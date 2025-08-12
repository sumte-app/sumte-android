package com.example.sumte.housedetail

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sumte.databinding.ActivityRoomDetailBinding
import com.example.sumte.payment.PaymentActivity

class ActivityRoomDetail : AppCompatActivity() {

    private lateinit var binding: ActivityRoomDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // intent로 넘어온 데이터 받기
        val roomId = intent.getIntExtra("roomId", -1)

        // 뒤로가기 버튼 클릭 시 이전 화면으로
        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnReserve.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            startActivity(intent)
        }
    }
}

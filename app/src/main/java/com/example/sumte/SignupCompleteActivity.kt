package com.example.sumte

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sumte.databinding.ActivitySignupCompleteBinding


class SignupCompleteActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupCompleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 닉네임 받아서 텍스트 설정
        val nickname = intent.getStringExtra("nickname") ?: "회원님"
        binding.tvSub.text = "${nickname}님, 제주에서 숨 쉬는 순간 머무는 장소, 숨터에 오신 것을 환영합니다!"

        // 확인 버튼 클릭 시 동작
        binding.btnConfirm.setOnClickListener {
            // 예: 메인 화면으로 이동 or finish()
            finish()
        }
    }
}
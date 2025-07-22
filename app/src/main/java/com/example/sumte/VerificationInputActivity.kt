package com.example.sumte


import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.sumte.databinding.ActivityVerificationInputBinding

class VerificationInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationInputBinding
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 이전 화면에서 이메일 받아와서 etPhoneNumber에 넣기
        val email = intent.getStringExtra("email") ?: ""
        binding.etPhoneNumber.setText(email)

        // 2. 초기 타이머 텍스트 설정
        binding.tvTimer.text = "03:00"
        startTimer()

        // 3. 인증번호 입력값 감지해서 버튼 활성화
        binding.etVerificationCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val code = s.toString()
                if (code.length == 6) {
                    binding.btnCompleteVerification.isEnabled = true
                    binding.btnCompleteVerification.setBackgroundResource(R.drawable.input_field_true)
                } else {
                    binding.btnCompleteVerification.isEnabled = false
                    binding.btnCompleteVerification.setBackgroundResource(R.drawable.login_button_unable)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnCompleteVerification.setOnClickListener {
            val email = intent.getStringExtra("email") // EmailInputActivity → VerificationInputActivity로 전달된 값

            val intent = Intent(this, PasswordInputActivity::class.java)
            intent.putExtra("email", email) // PasswordInputActivity로 다시 전달
            startActivity(intent)
            finish()
        }


    }

    private fun startTimer() {
        countDownTimer?.cancel()  // 기존 타이머 취소
        countDownTimer = object : CountDownTimer(3 * 60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.tvTimer.text = "시간 초과"
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }


}
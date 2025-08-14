package com.example.sumte.signup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sumte.R
import com.example.sumte.VerificationInputActivity
import com.example.sumte.databinding.ActivityEmailInputBinding
import com.example.sumte.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EmailInputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmailInputBinding

    // EmailDuplicationApi 사용
    private val api = RetrofitClient.emailDuplicationApi

    private var emailCheckJob: Job? = null
    private var isEmailAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 초깃값
        updateNextButton(isEnabled = false)
        hideEmailError()

        // 이메일 형식 + 중복 확인 (디바운스 400ms)
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s?.toString()?.trim().orEmpty()

                // 1) 입력 바뀌면 항상 초기화
                emailCheckJob?.cancel()
                isEmailAvailable = false
                updateNextButton(false)   // ← 매 타이핑마다 버튼 잠금

                // 2) 빈 값
                if (email.isBlank()) {
                    hideEmailError()
                    return
                }

                // 3) 기본 형식 검사
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    isEmailAvailable = false
                    showEmailError("이메일 형식이 올바르지 않습니다.")
                    updateNextButton(false)
                    return
                }

// 4) 도메인 강제: 정확히 gmail.com만 허용 (대소문자 구분)
                val at = email.lastIndexOf('@')
                if (at == -1) {
                    isEmailAvailable = false
                    showEmailError("이메일 형식이 올바르지 않습니다.")
                    updateNextButton(false)
                    return
                }
                val domain = email.substring(at + 1)   // '@' 뒤 도메인만 추출
                if (domain != "gmail.com") {           // ← 대소문자 구분 비교
                    isEmailAvailable = false
                    showEmailError("이메일 형식이 올바르지 않습니다.")
                    updateNextButton(false)
                    return
                }


                // 5) 형식/도메인 OK → 디바운스 후 중복 확인
                hideEmailError()
                emailCheckJob = lifecycleScope.launch {
                    delay(400)
                    try {
                        val res = api.checkEmailDuplicate(email)
                        val body = res.body()
                        val available = res.isSuccessful && (body?.success == true)

                        if (available) {
                            isEmailAvailable = true
                            hideEmailError()
                            updateNextButton(true)   // ← 여기서만 버튼 켬
                        } else {
                            showEmailError(body?.message ?: "이미 사용 중인 이메일입니다.")
                        }
                    } catch (e: Exception) {
                        showEmailError("네트워크 오류로 이메일 확인 실패")
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 다음 버튼 클릭 시 인증 화면으로 이동
        binding.btnNext.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            // 방어 로직(이중 클릭/비정상 진입 방지)
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || !isEmailAvailable) return@setOnClickListener

            val intent = Intent(this@EmailInputActivity, VerificationInputActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }
    }

    private fun updateNextButton(isEnabled: Boolean) {
        binding.btnNext.isEnabled = isEnabled
        val bg = if (isEnabled) R.drawable.input_field_true else R.drawable.login_button_unable
        binding.btnNext.setBackgroundResource(bg)
    }

    private fun showEmailError(msg: String) {
        binding.emailErrorArrow.visibility = View.VISIBLE
        binding.emailErrorText.visibility = View.VISIBLE
        binding.emailErrorText.text = msg
        // 필요하면 입력창 배경도 에러용으로 변경
        // binding.etEmail.setBackgroundResource(R.drawable.input_field_error_selector)
    }

    private fun hideEmailError() {
        binding.emailErrorArrow.visibility = View.GONE
        binding.emailErrorText.visibility = View.GONE
        // 정상 배경으로 되돌리기 원하면 여기서 처리
        // binding.etEmail.setBackgroundResource(R.drawable.input_field_selector)
    }
}

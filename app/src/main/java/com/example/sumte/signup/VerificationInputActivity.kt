package com.example.sumte.signup

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sumte.R
import com.example.sumte.databinding.ActivityVerificationInputBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class VerificationInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationInputBinding
    private var countDownTimer: CountDownTimer? = null

    // --- 서버 기본 설정 ---
    private val baseUrl = "https://sumteapi.duckdns.org"
    private val client by lazy { OkHttpClient() }
    private val jsonMedia by lazy { "application/json; charset=utf-8".toMediaType() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) 이전 화면에서 이메일 받아서 세팅
        val email = intent.getStringExtra("email") ?: ""
        binding.etPhoneNumber.setText(email)

        // 2) 초기 타이머 세팅
        binding.tvTimer.text = "03:00"
        startTimer(180)

        // 3) 6자리 입력 시 버튼 활성화
        binding.etVerificationCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val ok = (s?.length == 6)
                binding.btnCompleteVerification.isEnabled = ok
                binding.btnCompleteVerification.setBackgroundResource(
                    if (ok) R.drawable.input_field_true else R.drawable.login_button_unable
                )
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val backBtn = findViewById<ImageButton>(R.id.backBtn)
        backBtn.setOnClickListener {
            finish() // 현재 화면 닫고 이전 화면으로
        }

        // 4) 인증 완료 → /email/verify
        binding.btnCompleteVerification.setOnClickListener {
            val code = binding.etVerificationCode.text?.toString().orEmpty()
            if (email.isBlank()) {
                return@setOnClickListener
            }
            if (code.length != 6) {
                return@setOnClickListener
            }
            verifyCode(email, code)
        }

        // 5) 재전송 → /email/send
        binding.tvResend.setOnClickListener {
            if (email.isBlank()) {
                return@setOnClickListener
            }
            sendCode(email)
        }

        // 6) 화면 진입 시 1회 자동 발송 (원하지 않으면 이 줄 주석 처리)
        if (email.isNotBlank()) {
            sendCode(email)
        }
    }

    // ----------------- 네트워크 호출 -----------------

    private fun sendCode(email: String) {
        binding.tvResend.isEnabled = false

        lifecycleScope.launch {
            val body = JSONObject().put("email", email).toString().toRequestBody(jsonMedia)
            val req = Request.Builder()
                .url("$baseUrl/email/send")
                .post(body)
                .addHeader("Accept", "application/json")
                .build()

            val result = withContext(Dispatchers.IO) {
                runCatching { client.newCall(req).execute() }.getOrElse { null }
            }

            binding.tvResend.isEnabled = true

            if (result == null) {
                toast("전송 실패: 네트워크 오류")
                return@launch
            }

            result.use { resp ->
                if (!resp.isSuccessful) {
                    toast("전송 실패: ${resp.code}")
                    return@launch
                }
                val text = resp.body?.string().orEmpty()
                // 예시 응답:
                // { "success": true, "message": "인증번호가 발송되었습니다.", "cooldownRemainingSeconds": 0 }
                try {
                    val json = JSONObject(text)
                    val success = json.optBoolean("success", false)
                    val message = json.optString("message", "인증번호 전송")
                    val cooldown = json.optInt("cooldownRemainingSeconds", 0)

                    if (success) {
                        toast(message)
                        val sec = if (cooldown > 0) cooldown else 180
                        startTimer(sec)
                    } else {
                        toast("전송 실패: $message")
                    }
                } catch (_: Exception) {
                    toast("전송됨 (응답 파싱 실패)")
                    startTimer(180)
                }
            }
        }
    }

    private fun verifyCode(email: String, code: String) {
        binding.btnCompleteVerification.isEnabled = false
        binding.btnCompleteVerification.text = "확인 중..."

        lifecycleScope.launch {
            val body = JSONObject()
                .put("email", email)
                .put("code", code)
                .toString()
                .toRequestBody(jsonMedia)

            val req = Request.Builder()
                .url("$baseUrl/email/verify")
                .post(body)
                .addHeader("Accept", "application/json")
                .build()

            val result = withContext(Dispatchers.IO) {
                runCatching { client.newCall(req).execute() }.getOrElse { null }
            }

            binding.btnCompleteVerification.isEnabled = true
            binding.btnCompleteVerification.text = "인증 완료"

            if (result == null) {
                toast("인증 실패: 네트워크 오류")
                return@launch
            }

            result.use { resp ->
                if (!resp.isSuccessful) {
                    toast("인증 실패: ${resp.code}")
                    return@launch
                }
                val text = resp.body?.string().orEmpty()
                // 예시 응답: { "success": true, "message": "인증이 완료되었습니다." }
                try {
                    val json = JSONObject(text)
                    val success = json.optBoolean("success", false)
                    val message = json.optString("message", "인증 처리")

                    if (success) {
                        toast(message)
                        val intent = Intent(this@VerificationInputActivity, PasswordInputActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        toast("인증 실패: $message")
                    }
                } catch (_: Exception) {
                    toast("인증 처리 완료")
                    val intent = Intent(this@VerificationInputActivity, PasswordInputActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    // ----------------- 타이머 -----------------

    private fun startTimer(totalSeconds: Int) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(totalSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }
            override fun onFinish() {
                binding.tvTimer.text = "시간 초과"
                binding.btnCompleteVerification.isEnabled = false
                binding.btnCompleteVerification.setBackgroundResource(R.drawable.login_button_unable)
                binding.etVerificationCode.text?.clear()
                binding.tvResend.isEnabled = true // 재전송 가능

                // 팝업 띄우기
                showTimeoutDialog()
            }
        }.start()
    }

    private fun showTimeoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_timeout, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // 바깥 터치로 닫히지 않게
            .create()

        // 확인 버튼 동작
        dialogView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnOk)
            .setOnClickListener {
                dialog.dismiss()
                // 필요하면 재전송 버튼 활성화
                binding.tvResend.isEnabled = true
            }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val widthPx = (300 * resources.displayMetrics.density).toInt()
        dialog.window?.setLayout(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)

    }



    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

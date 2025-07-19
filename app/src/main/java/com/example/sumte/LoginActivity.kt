package com.example.sumte

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.sumte.databinding.ActivityLoginBinding
import com.example.sumte.databinding.DialogLoginFailedBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import kotlin.jvm.java

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false
    private lateinit var authService: AuthService
    private val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#\$%^&*()_+{}\\[\\]:;\"'<>,.?/~`-]).{6,}$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etEmail.setBackgroundResource(R.drawable.edittext_selector)
        binding.etPassword.setBackgroundResource(R.drawable.edittext_selector)


        authService = RetrofitClient.instance.create(AuthService::class.java)

        //서버 닫혀있을 때
//        authService = if (BuildConfig.DEBUG) {
//            object : AuthService {
//                override suspend fun login(request: LoginRequest): retrofit2.Response<LoginResponse> {
//                    kotlinx.coroutines.delay(1000) // 네트워크 대기 시뮬레이션
//
//                    return if (request.email == "test@example.com" && request.password == "Test123!") {
//                        val fakeResponse = LoginResponse(
//                            token = "mock-token-1234",
//                            userId = 1L,
//                            userName = "테스트유저"
//                        )
//                        Response.success(fakeResponse)
//                    } else {
//                        Response.error(401, okhttp3.ResponseBody.create(null, "Unauthorized"))
//                    }
//                }
//            }
//        } else {
//            RetrofitClient.instance.create(AuthService::class.java)
//        }

        //포커스 초기화
        binding.root.setOnClickListener {
            hideKeyboard()
        }

        //비밀번호 토글
        binding.ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.ivTogglePassword.setImageResource(R.drawable.show)
            } else {
                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.ivTogglePassword.setImageResource(R.drawable.hide)
            }

            binding.etPassword.setSelection(binding.etPassword.text.length)
        }


        // 입력 값 변경 감지 → 버튼 상태 업데이트
        binding.etEmail.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)

        //로그인
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            resetValidationState()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tvEmailError.visibility = View.VISIBLE
                binding.etEmail.setBackgroundResource(R.drawable.edittext_error_background)
                return@setOnClickListener
            }

            if (!passwordRegex.matches(password)) {
                binding.tvPasswordError.visibility = View.VISIBLE
                binding.etPassword.setBackgroundResource(R.drawable.edittext_error_background)
                return@setOnClickListener
            }

            login(email, password)

        }

        // 포커스 변경 시 에러 리셋
        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val email = binding.etEmail.text.toString().trim()
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.tvEmailError.visibility = View.VISIBLE
                    binding.etEmail.setBackgroundResource(R.drawable.edittext_error_background)
                } else {
                    binding.tvEmailError.visibility = View.GONE
                    binding.etEmail.setBackgroundResource(R.drawable.edittext_selector)
                }
            }
        }

        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = binding.etPassword.text.toString().trim()
                if (!passwordRegex.matches(password)) {
                    binding.tvPasswordError.visibility = View.VISIBLE
                    binding.etPassword.setBackgroundResource(R.drawable.edittext_error_background)
                } else {
                    binding.tvPasswordError.visibility = View.GONE
                    binding.etPassword.setBackgroundResource(R.drawable.edittext_selector)
                }
            }
        }

        //회원가입 클릭시 SignupActivity로 전환
//        binding.tvSignUp.setOnClickListener {
//            startActivity(Intent(this, SignUpActivity::class.java))
//        }
    }
    //test용
    private fun fakeLogin(email: String, password: String) {
        lifecycleScope.launch {
            delay(1000) // fake network delay

            if (email == "test@example.com" && password == "Test123!") {
                Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } else {
                showCustomErrorDialog()
            }
        }
    }

    private fun login(email: String, password: String) {
        val loginRequest = LoginRequest(loginId = email, password = password)

        lifecycleScope.launch {
            try {
                val response = authService.login(loginRequest)
                if (response.isSuccessful) {
                    val body = response.body()

                    Log.d("LoginResponse", "서버 메시지: ${body?.message}")

                    if (body != null && body.success) {
                        val token = body.data?.accessToken ?: ""


                        getSharedPreferences("auth", MODE_PRIVATE)
                            .edit()
                            .putString("access_token", token)
                            .apply()

                        Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()


                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        showCustomErrorDialog()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("LoginResponse", "실패 응답: $errorBody")
                    showCustomErrorDialog()
                }
            } catch (e: Exception) {
                Log.e("LoginError", "서버 오류: ${e.localizedMessage}")
                Toast.makeText(this@LoginActivity, "서버 오류: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun resetValidationState() {
        binding.tvEmailError.visibility = View.GONE
        binding.tvPasswordError.visibility = View.GONE
        binding.etEmail.setBackgroundResource(R.drawable.edittext_selector)
        binding.etPassword.setBackgroundResource(R.drawable.edittext_selector)
    }

    // 버튼 활성화 조건 검사
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateLoginButtonState()
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun updateLoginButtonState() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPasswordValid = passwordRegex.matches(password)

        binding.btnLogin.isEnabled = isEmailValid && isPasswordValid
    }

    private fun showCustomErrorDialog() {
        val dialogBinding = DialogLoginFailedBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setDimAmount(0.5f) // 배경 어두움 정도 설정 (선택)

        dialog.show()
    }

    private fun hideKeyboard() {
        currentFocus?.let { view ->
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
    }


}
package com.example.sumte.login

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.sumte.login.LoginRequest
import com.example.sumte.MainActivity
import com.example.sumte.R
import com.example.sumte.RetrofitClient
import com.example.sumte.databinding.ActivityLoginBinding
import com.example.sumte.databinding.DialogLoginFailedBinding
import com.example.sumte.signup.EmailInputActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false

    private var isAllChecked = false
    private lateinit var authService: AuthService

    private val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#\$%^&*()_+{}\\[\\]:;\"'<>,.?/~`-]).{6,}$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etEmail.setBackgroundResource(R.drawable.edittext_selector)
        binding.etPassword.setBackgroundResource(R.drawable.edittext_selector)


        authService = RetrofitClient.instance.create(AuthService::class.java)

        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("access_token", null)
        if (!token.isNullOrEmpty()) {

            Log.d("AuthToken", "Saved token: $token")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
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

        binding.tvSignUp.setOnClickListener {
            binding.termsContainer.visibility = View.VISIBLE
        }



        binding.btnTermsConfirm.setOnClickListener {
            val intent = Intent(this, EmailInputActivity::class.java)
            startActivity(intent)
        }

        setupAgreementLogic()

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

                    if (body != null && body.success) {
                        val token = body.data?.accessToken ?: ""

                        // 토큰 저장
                        getSharedPreferences("auth", MODE_PRIVATE)
                            .edit()
                            .putString("access_token", token)
                            .apply()

                        // 프로필 정보 조회 API 호출
                        fetchUserProfile(token)

                    } else {
                        showCustomErrorDialog()
                    }
                } else {
                    showCustomErrorDialog()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "서버 오류: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 프로필 API 호출 함수
    private suspend fun fetchUserProfile(token: String) {
        try {
            val profileResponse = authService.getUserProfile("Bearer $token")
            if (profileResponse.isSuccessful) {
                val profile = profileResponse.body()
                if (profile != null) {
                    // 닉네임, 이메일 등 필요한 정보 저장하기
                    val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    sharedPref.edit()
                        .putString("nickname", profile.nickname)
                        .putString("email", profile.email)
                        .apply()

                    Toast.makeText(this@LoginActivity, "로그인 및 프로필 조회 성공!", Toast.LENGTH_SHORT).show()

                    // 메인 화면으로 이동
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()

                } else {
                    Toast.makeText(this@LoginActivity, "프로필 조회 실패", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@LoginActivity, "프로필 조회 실패: ${profileResponse.code()}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this@LoginActivity, "프로필 API 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }


//    private fun login(email: String, password: String) {
//        val loginRequest = LoginRequest(loginId = email, password = password)
//
//        lifecycleScope.launch {
//            try {
//                val response = authService.login(loginRequest)
//                if (response.isSuccessful) {
//                    val body = response.body()
//
//                    Log.d("LoginResponse", "서버 메시지: ${body?.message}")
//
//                    if (body != null && body.success) {
//                        val token = body.data?.accessToken ?: ""
//
//
//                        getSharedPreferences("auth", MODE_PRIVATE)
//                            .edit()
//                            .putString("access_token", token)
//                            .apply()
//
//                        Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
//
//
//                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
//                        finish()
//                    } else {
//                        showCustomErrorDialog()
//                    }
//                } else {
//                    val errorBody = response.errorBody()?.string()
//                    Log.e("LoginResponse", "실패 응답: $errorBody")
//                    showCustomErrorDialog()
//                }
//            } catch (e: Exception) {
//                Log.e("LoginError", "서버 오류: ${e.localizedMessage}")
//                Toast.makeText(this@LoginActivity, "서버 오류: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
//            }
//        }
//    }



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

    private fun setupAgreementLogic() {
        val essentialButtons = listOf(
            binding.essentialBtn1,
            binding.essentialBtn2,
            binding.essentialBtn3
        )
        val optionalButtons = listOf(
            binding.selectBtn1,
            binding.selectBtn2,
            binding.selectBtn3
        )

        binding.agreeBtn.setOnClickListener {
            isAllChecked = !isAllChecked
            val checkDrawable = if (isAllChecked) R.drawable.check_check else R.drawable.check
            val agreeBtnBackground = if (isAllChecked) R.drawable.all_agree_true_button else R.drawable.all_agree_button

            binding.agreeBtn.setBackgroundResource(agreeBtnBackground)

            (essentialButtons + optionalButtons).forEach {
                it.setBackgroundResource(checkDrawable)
                it.tag = isAllChecked
            }

            updateConfirmButtonState()
        }


        (essentialButtons + optionalButtons).forEach { btn ->
            btn.setOnClickListener {
                val isChecked = (btn.tag as? Boolean) ?: false
                val nowChecked = !isChecked
                val res = if (nowChecked) R.drawable.check_check else R.drawable.check

                btn.setBackgroundResource(res)
                btn.tag = nowChecked

                updateAllAgreeState(essentialButtons + optionalButtons)
                updateConfirmButtonState()
            }
        }
    }


    private fun updateAllAgreeState(allButtons: List<View>) {
        val allChecked = allButtons.all { (it.tag as? Boolean) == true }
        isAllChecked = allChecked

        val agreeBtnRes = if (allChecked) R.drawable.all_agree_true_button else R.drawable.all_agree_button
        binding.agreeBtn.setBackgroundResource(agreeBtnRes)
    }

    private fun updateConfirmButtonState() {
        val essentialButtons = listOf(
            binding.essentialBtn1,
            binding.essentialBtn2,
            binding.essentialBtn3
        )
        val allEssentialChecked = essentialButtons.all { (it.tag as? Boolean) == true }

        binding.btnTermsConfirm.isEnabled = allEssentialChecked
        binding.btnTermsConfirm.alpha = if (allEssentialChecked) 1f else 0.5f

        if (allEssentialChecked) {
            binding.btnTermsConfirm.setBackgroundResource(R.drawable.input_field_true)
        } else {
            binding.btnTermsConfirm.setBackgroundResource(R.drawable.login_button_unable)
        }
    }








}
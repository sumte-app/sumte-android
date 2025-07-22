package com.example.sumte

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.sumte.databinding.ActivitySignupProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.Period

class SignupProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupProfileBinding
    private var isNicknameAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 실명 입력 감지
        binding.etName.addTextChangedListener { updateSubmitButtonState() }

        // 생년월일 유효성 검사
        binding.etYear.addTextChangedListener(birthWatcher)
        binding.etMonth.addTextChangedListener(birthWatcher)
        binding.etDay.addTextChangedListener(birthWatcher)

        // 성별 선택 감지
        binding.rgGender.setOnCheckedChangeListener { _, _ ->
            updateSubmitButtonState()
        }

        // 닉네임 입력 감지 및 중복 확인
        binding.etNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val nickname = s?.toString()?.trim() ?: ""
                if (nickname.isNotEmpty()) {
                    checkNicknameAvailability(nickname)
                } else {
                    isNicknameAvailable = false
                    resetNicknameUI()
                    updateSubmitButtonState()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 가입 완료 버튼 클릭
        binding.btnSubmit.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val nickname = binding.etNickname.text.toString().trim()
            val gender = if (binding.rbMale.isChecked) "MAN" else "WOMAN"
            val birthday = "${binding.etYear.text}-${binding.etMonth.text}-${binding.etDay.text}"
            val password = intent.getStringExtra("password") ?: ""
            val email = intent.getStringExtra("email") ?: ""

            val request = SignUpRequest(
                loginId = email,
                password = password,
                name = name,
                phoneNumber = "010-0000-0000",
                nickname = nickname,
                gender = gender,
                birthday = birthday,
                email = email
            )

            Log.d("SignUpRequest", "회원가입 요청 정보: $request")

            RetrofitClient.apiService.signUp(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SignupProfileActivity, "회원가입 성공", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SignupProfileActivity, SignupCompleteActivity::class.java)
                        intent.putExtra("nickname", nickname)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@SignupProfileActivity, "회원가입 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@SignupProfileActivity, "네트워크 오류: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private val birthWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val year = binding.etYear.text.toString().toIntOrNull()
            val month = binding.etMonth.text.toString().toIntOrNull()
            val day = binding.etDay.text.toString().toIntOrNull()

            if (year != null && month != null && day != null) {
                try {
                    if (isUnder19(year, month, day)) {
                        binding.etYear.setBackgroundResource(R.drawable.input_field_error)
                        binding.etMonth.setBackgroundResource(R.drawable.input_field_error)
                        binding.etDay.setBackgroundResource(R.drawable.input_field_error)
                        binding.birthErrorArrow.visibility = View.VISIBLE
                        binding.birthErrorText.visibility = View.VISIBLE
                    } else {
                        binding.etYear.setBackgroundResource(R.drawable.input_field_selector)
                        binding.etMonth.setBackgroundResource(R.drawable.input_field_selector)
                        binding.etDay.setBackgroundResource(R.drawable.input_field_selector)
                        binding.birthErrorArrow.visibility = View.GONE
                        binding.birthErrorText.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    binding.etYear.setBackgroundResource(R.drawable.input_field_error)
                    binding.etMonth.setBackgroundResource(R.drawable.input_field_error)
                    binding.etDay.setBackgroundResource(R.drawable.input_field_error)
                }
            }
            updateSubmitButtonState()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun isUnder19(year: Int, month: Int, day: Int): Boolean {
        return try {
            val birthDate = LocalDate.of(year, month, day)
            val today = LocalDate.now()
            Period.between(birthDate, today).years < 19
        } catch (e: Exception) {
            true
        }
    }

    private fun checkNicknameAvailability(nickname: String) {
        RetrofitClient.apiService.checkNickname(nickname)
            .enqueue(object : Callback<NicknameResponse> {
                override fun onResponse(
                    call: Call<NicknameResponse>,
                    response: Response<NicknameResponse>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        if (result?.success == true) {
                            isNicknameAvailable = true
                            binding.etNickname.setBackgroundResource(R.drawable.input_field_selector)
                            binding.nicknameErrorArrow.visibility = View.GONE
                            binding.nicknameErrorText.visibility = View.GONE

                        } else {
                            isNicknameAvailable = false
                            showNicknameError()
                        }
                    } else {
                        isNicknameAvailable = false
                        showNicknameError()
                    }
                    updateSubmitButtonState()
                }

                override fun onFailure(call: Call<NicknameResponse>, t: Throwable) {
                    isNicknameAvailable = false
                    showNicknameError()
                    updateSubmitButtonState()
                }
            })
    }

    private fun showNicknameError() {
        binding.etNickname.setBackgroundResource(R.drawable.input_field_error)
        binding.nicknameErrorArrow.visibility = View.VISIBLE
        binding.nicknameErrorText.visibility = View.VISIBLE
    }

    private fun resetNicknameUI() {
        binding.etNickname.setBackgroundResource(R.drawable.input_field_selector)
        binding.nicknameErrorArrow.visibility = View.GONE
        binding.nicknameErrorText.visibility = View.GONE
    }

    private fun updateSubmitButtonState() {
        val nameValid = binding.etName.text.toString().isNotBlank()
        val year = binding.etYear.text.toString().toIntOrNull()
        val month = binding.etMonth.text.toString().toIntOrNull()
        val day = binding.etDay.text.toString().toIntOrNull()
        val birthValid = if (year != null && month != null && day != null) !isUnder19(year, month, day) else false
        val nicknameValid = binding.etNickname.text.toString().isNotBlank() && isNicknameAvailable
        val genderSelected = binding.rgGender.checkedRadioButtonId != -1

        val allValid = nameValid && birthValid && nicknameValid && genderSelected

        binding.btnSubmit.isEnabled = allValid
        binding.btnSubmit.setBackgroundResource(
            if (allValid) R.drawable.input_field_true else R.drawable.login_button_unable
        )
    }
}
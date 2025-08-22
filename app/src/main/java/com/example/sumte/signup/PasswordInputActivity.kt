package com.example.sumte.signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.sumte.R
import com.example.sumte.databinding.ActivityPasswordInputBinding

class PasswordInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordInputBinding
    private var isPasswordVisible = false
    private var isPasswordConfirmVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.setOnClickListener {
            // InputMethodManager를 가져옵니다.
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // 현재 포커스를 가진 뷰에서 키보드를 숨깁니다.
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            // EditText의 포커스를 해제합니다.
            currentFocus?.clearFocus()
        }

        //  이메일 hint 설정
        val emailFromIntent = intent.getStringExtra("email")
        binding.etEmail.hint = emailFromIntent ?: "example@email.com"

        val backBtn = findViewById<ImageButton>(R.id.backBtn)
        backBtn.setOnClickListener {
            finish() // 현재 화면 닫고 이전 화면으로
        }

        //  비밀번호 보기/숨기기 토글
        binding.ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            binding.etPassword.inputType = if (isPasswordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
            binding.ivTogglePassword.setImageResource(
                if (isPasswordVisible) R.drawable.password_display_eye else R.drawable.password_hide_eye
            )

        }

        //  비밀번호 확인 토글
        binding.btnTogglePassword2.setOnClickListener {
            isPasswordConfirmVisible = !isPasswordConfirmVisible
            binding.etPasswordConfirm.inputType = if (isPasswordConfirmVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            binding.etPasswordConfirm.setSelection(binding.etPasswordConfirm.text?.length ?: 0)
            binding.btnTogglePassword2.setImageResource(
                if (isPasswordConfirmVisible) R.drawable.password_display_eye else R.drawable.password_hide_eye
            )
        }

        //  유효성 검사 + 일치 여부 확인 + 테두리 설정 + 버튼 활성화
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pw = binding.etPassword.text.toString()
                val confirm = binding.etPasswordConfirm.text.toString()

                val valid = isPasswordValid(pw)
                val isSame = pw == confirm

                if (!valid && pw.isNotEmpty()) {
                    binding.etPassword.setBackgroundResource(R.drawable.input_field_error)
                    binding.ivArrowCurveLeftRightPassword.visibility = View.VISIBLE
                    binding.ivPasswordErrorText.visibility = View.VISIBLE
                } else {
                    binding.etPassword.setBackgroundResource(R.drawable.input_field_selector)
                    binding.ivArrowCurveLeftRightPassword.visibility = View.GONE
                    binding.ivPasswordErrorText.visibility = View.GONE
                }

                if (!isSame && confirm.isNotEmpty()) {
                    binding.etPasswordConfirm.setBackgroundResource(R.drawable.input_field_error)
                    binding.ivArrowCurveLeftRightPasswordcheck.visibility = View.VISIBLE
                    binding.ivPasswordCheckErrorText.visibility = View.VISIBLE
                } else {
                    binding.etPasswordConfirm.setBackgroundResource(R.drawable.input_field_selector)
                    binding.ivArrowCurveLeftRightPasswordcheck.visibility = View.GONE
                    binding.ivPasswordCheckErrorText.visibility = View.GONE
                }

                if (valid && isSame) {
                    binding.btnNext.isEnabled = true
                    binding.btnNext.setBackgroundResource(R.drawable.input_field_true)
                } else {
                    binding.btnNext.isEnabled = false
                    binding.btnNext.setBackgroundResource(R.drawable.login_button_unable)
                }


            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.etPassword.addTextChangedListener(watcher)
        binding.etPasswordConfirm.addTextChangedListener(watcher)

        //  다음 버튼 클릭 시 동작
        binding.btnNext.setOnClickListener {
            val email = intent.getStringExtra("email") ?: ""
            val password = binding.etPassword.text.toString()

            val intent = Intent(this, SignupProfileActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("password", password)
            startActivity(intent)
        }

    }

    //  비밀번호 유효성 검사: 영문 + 숫자 + 특수문자 포함, 6자 이상
    private fun isPasswordValid(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#\$%^&*()_+=\\-\\[\\]{};:'\"\\\\|,.<>/?]).{6,}$")
        return regex.matches(password)
    }


}
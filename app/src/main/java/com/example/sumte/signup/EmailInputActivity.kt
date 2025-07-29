package com.example.sumte.signup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.sumte.R
import com.example.sumte.VerificationInputActivity
import com.example.sumte.databinding.ActivityEmailInputBinding

class EmailInputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmailInputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 이메일 형식 유효성 검사
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s.toString()
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.btnNext.isEnabled = true
                    binding.btnNext.setBackgroundResource(R.drawable.input_field_true)
                } else {
                    binding.btnNext.isEnabled = false
                    binding.btnNext.setBackgroundResource(R.drawable.login_button_unable)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 다음 버튼 클릭 시 VerificationInputActivity로 이동
        binding.btnNext.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val intent = Intent(this@EmailInputActivity, VerificationInputActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

    }
}
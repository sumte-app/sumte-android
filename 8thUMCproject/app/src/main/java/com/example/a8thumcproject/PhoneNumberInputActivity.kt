package com.example.a8thumcproject

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.a8thumcproject.databinding.ActivityPhoneNumberInputBinding

class PhoneNumberInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneNumberInputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneNumberInputBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.phoneInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val phone = s.toString().trim()
                val isValid = phone.matches(Regex("^010-\\d{4}-\\d{4}$"))

                binding.btnSendVerification.isEnabled = isValid
                binding.btnSendVerification.background = ContextCompat.getDrawable(
                    this@PhoneNumberInputActivity,
                    if (isValid) R.drawable.input_field_true else R.drawable.login_button_unable
                )
            }

            override fun afterTextChanged(s: Editable?) {}
        })

    }
}

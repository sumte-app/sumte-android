package com.example.sumte.payment

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sumte.R
import com.example.sumte.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var selectedPaymentMethod: String = "kakao"
    private var isAllChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupPaymentButtons()
        setupAgreementLogic()
        updatePayButtonState()
    }

    private fun setupPaymentButtons() {

        binding.btnKakaoPay.isSelected = true
        binding.btnEasyPay.isSelected = false

        binding.btnKakaoPay.setOnClickListener {
            selectPaymentMethod("kakao")
        }

        binding.btnEasyPay.setOnClickListener {
            selectPaymentMethod("easy")
        }
    }

    private fun selectPaymentMethod(method: String) {
        selectedPaymentMethod = method
        binding.btnKakaoPay.isSelected = method == "kakao"
        binding.btnEasyPay.isSelected = method == "easy"
    }

    private fun setupAgreementLogic() {
        val allAgreeImage = binding.ivAllAgree
        val termsList = listOf(
            binding.cbTerms1,
            binding.cbTerms2,
            binding.cbTerms3,
            binding.cbTerms4,
            binding.cbTerms5
        )

        binding.clAllAgree.setOnClickListener {
            isAllChecked = !isAllChecked
            termsList.forEach { it.isChecked = isAllChecked }
            updateAllAgreeVisual()
            updatePayButtonState()
        }

        // 개별 체크박스 변경 시 전체 동의 상태 반영
        termsList.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, _ ->
                val allChecked = termsList.all { it.isChecked }
                isAllChecked = allChecked
                updateAllAgreeVisual()
                updatePayButtonState()
            }
        }
    }

    private fun updateAllAgreeVisual() {
        // 체크 상태에 따라 이미지 변경
        val imageRes = if (isAllChecked) R.drawable.checkbox_check else R.drawable.checkbox
        binding.ivAllAgree.setImageResource(imageRes)
    }

    private fun updatePayButtonState() {
        val allTermsChecked = listOf(
            binding.cbTerms1,
            binding.cbTerms2,
            binding.cbTerms3,
            binding.cbTerms4,
            binding.cbTerms5
        ).all { it.isChecked }

        val isEnabled = selectedPaymentMethod.isNotEmpty() && allTermsChecked
        binding.btnPay.isEnabled = isEnabled
        binding.btnPay.alpha = if (isEnabled) 1f else 0.5f
    }

}
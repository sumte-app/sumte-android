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

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }


        binding.ivBack.setOnClickListener {
            finish()
        }
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
        val termsList = listOf(
            binding.cbTerms1,
            binding.cbTerms2,
            binding.cbTerms3,
            binding.cbTerms4,
            binding.cbTerms5
        )

        fun updateAllAgreeState() {
            val allChecked = termsList.all { it.isChecked }
            isAllChecked = allChecked
            binding.clAllAgree.isSelected = allChecked
            updateAllAgreeVisual()
            updatePayButtonState()
        }

        // ✅ 전체 동의 클릭 시 모든 체크박스 상태 동기화
        binding.clAllAgree.setOnClickListener {
            isAllChecked = !isAllChecked

            // 리스너 잠시 제거
            termsList.forEach { it.setOnCheckedChangeListener(null) }
            termsList.forEach { it.isChecked = isAllChecked }
            termsList.forEach { it.setOnCheckedChangeListener { _, _ -> updateAllAgreeState() } }

            binding.clAllAgree.isSelected = isAllChecked
            updateAllAgreeVisual()
            updatePayButtonState()
        }

        // ✅ 개별 항목 클릭 시 전체 상태 업데이트
        termsList.forEach { cb ->
            cb.setOnCheckedChangeListener { _, _ ->
                updateAllAgreeState()
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
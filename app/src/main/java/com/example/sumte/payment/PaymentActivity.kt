package com.example.sumte.payment

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.sumte.App
import com.example.sumte.R
import com.example.sumte.databinding.ActivityPaymentBinding
import com.example.sumte.search.BookInfoViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var selectedPaymentMethod: String = "kakao"
    private var isAllChecked = false
    private val viewModel by lazy {
        ViewModelProvider(
            App.instance,
            ViewModelProvider.AndroidViewModelFactory.getInstance(App.instance)
        )[BookInfoViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }

        val formatter = DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN)

        val startDate = viewModel.startDate
        val endDate = viewModel.endDate
        val nights = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate)

        binding.startDate.text = startDate.format(formatter)
        binding.endDate.text = endDate.format(formatter)
        binding.dateCount.text = "${nights}박"

        binding.adultCount.text = "성인 ${viewModel.adultCount}"
        binding.childCount.text =
            if (viewModel.childCount > 0) "아동 ${viewModel.childCount}" else ""

        binding.countComma.visibility = if (viewModel.childCount > 0) View.VISIBLE else View.GONE



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
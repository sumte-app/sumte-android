package com.example.sumte.payment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.sumte.App
import com.example.sumte.R
import com.example.sumte.RetrofitClient
import com.example.sumte.databinding.ActivityPaymentBinding
import com.example.sumte.payment.PaymentExtras.EXTRA_AMOUNT
import com.example.sumte.payment.PaymentExtras.EXTRA_END
import com.example.sumte.payment.PaymentExtras.EXTRA_GUESTHOUSE_NAME
import com.example.sumte.payment.PaymentExtras.EXTRA_ROOM_NAME
import com.example.sumte.payment.PaymentExtras.EXTRA_START
import com.example.sumte.search.BookInfoViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var selectedPaymentMethod: String = "kakao"
    private var isAllChecked = false

    private var currentPaymentId: Int? = null
    private var handledDeepLink = false
    private var launchedPaymentId: Int? = null

    private var navigatedToComplete = false

    private val viewModel by lazy {
        ViewModelProvider(
            App.instance,
            ViewModelProvider.AndroidViewModelFactory.getInstance(App.instance)
        )[BookInfoViewModel::class.java]
    }


    private val payVm by lazy {
        ViewModelProvider(
            this,
            PaymentVMFactory(PaymentRepository(applicationContext))
        )[PaymentViewModel::class.java]
    }

    private var openedOnce = false

    private fun bindExtrasToUi(){
        // title
        val guesthouseName = intent.getStringExtra(EXTRA_GUESTHOUSE_NAME)
        val roomName       = intent.getStringExtra(EXTRA_ROOM_NAME)
        val amount = intent.getIntExtra(EXTRA_AMOUNT, 0)
        val start = intent.getStringExtra(EXTRA_START)?.let(LocalDate::parse)
        val end   = intent.getStringExtra(EXTRA_END)?.let(LocalDate::parse)
        val fmt   = DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN)
        val pretty = NumberFormat.getInstance(Locale.KOREA).format(amount)
        val ciRaw    = intent.getStringExtra(PaymentExtras.EXTRA_CHECKIN_TIME)
        val coRaw    = intent.getStringExtra(PaymentExtras.EXTRA_CHECKOUT_TIME)
        val ci    = trimSec(ciRaw)
        val co    = trimSec(coRaw)


        binding.tvTitle.text = guesthouseName
        binding.tvRoomTitle.text = roomName
        binding.tvPrice.text = "${pretty}원"


        binding.startDate.text = start?.format(fmt) ?: "-"
        binding.endDate.text   = end?.format(fmt) ?: "-"
        val nights = if (start != null && end != null)
            maxOf(1, ChronoUnit.DAYS.between(start, end).toInt())
        else 1
        if (start != null && end != null){
            binding.tvCheckInDate.text = "${formatDetailLine(start)}\n${ci}"
            binding.tvCheckOutDate.text = "${formatDetailLine(end)}\n${co}"
        }

        binding.dateCount.text = "${nights}박"
        binding.tvStay.text = "숙박 / ${nights}박"

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
            onBackPressedDispatcher.onBackPressed()
        }

        payVm.state.onEach { st ->
            when (st) {
                is PayUiState.Success -> {
                    hideProcessingDialog()

                    currentPaymentId = st.data.paymentId
                    persistPaymentId(currentPaymentId!!)



                    if (openedOnce || handledDeepLink) {
                        Log.d("Payment","skip reopen (openedOnce=$openedOnce, handled=$handledDeepLink)")
                        return@onEach
                    }

                    launchedPaymentId = st.data.paymentId
                    openPayment(st.data.appScheme, st.data.paymentUrl)
                }
                is PayUiState.Error -> {  }
                else -> Unit
            }
        }.launchIn(lifecycleScope)

        payVm.approveState.onEach { st ->
            when (st) {
                is ApproveUiState.Loading -> showProcessingDialog()
                is ApproveUiState.Success -> {
                    hideProcessingDialog()
                    Log.d("Payment", "approve success: code=${st.data.code}, msg=${st.data.message}")
                    if (!navigatedToComplete) {
                        showPaymentCompleteFragment(st.data)
                    }
                }
                is ApproveUiState.Error -> {
                    hideProcessingDialog()
                    Log.d("Payment", "approve error: ${st.message}")
                    showPaymentFailedFragment(st.message)
                }
                else -> Unit
            }
        }.launchIn(lifecycleScope)





        bindExtrasToUi()
        setupPaymentButtons()
        setupAgreementLogic()
        updatePayButtonState()


        binding.btnPay.setOnClickListener {
            if (!binding.btnPay.isEnabled) return@setOnClickListener
            val resId  = intent.getIntExtra(PaymentExtras.EXTRA_RES_ID, -1)
            if (resId <= 0) {

                Toast.makeText(this, "예약 정보가 없습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalAmount = intent.getIntExtra(EXTRA_AMOUNT, 0)
            Log.d("Payment", "start pay: reservationId=$resId, amount=$totalAmount")
            payVm.startKakao(resId, totalAmount)
        }

        Log.d("Payment", "onCreate data=${intent?.data}")
        handleDeepLink(intent?.data)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d("Payment", "onNewIntent data=${intent?.data}")
        handleDeepLink(intent?.data)
    }

    override fun onResume() {
        super.onResume()
        Log.d("Payment", "onResume data=${intent?.data}")
        if (!handledDeepLink) handleDeepLink(intent?.data)
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


        binding.clAllAgree.setOnClickListener {
            isAllChecked = !isAllChecked


            termsList.forEach { it.setOnCheckedChangeListener(null) }
            termsList.forEach { it.isChecked = isAllChecked }
            termsList.forEach { it.setOnCheckedChangeListener { _, _ -> updateAllAgreeState() } }

            binding.clAllAgree.isSelected = isAllChecked
            updateAllAgreeVisual()
            updatePayButtonState()
        }


        termsList.forEach { cb ->
            cb.setOnCheckedChangeListener { _, _ ->
                updateAllAgreeState()
            }
        }
    }


    private fun updateAllAgreeVisual() {

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


    private val TAG_PROCESSING = "payment_processing"

    private fun showProcessingDialog() {
        if (supportFragmentManager.findFragmentByTag(TAG_PROCESSING) == null) {
            PaymentDialogFragment().show(supportFragmentManager, TAG_PROCESSING)
        }
    }

    private fun hideProcessingDialog() {
        (supportFragmentManager.findFragmentByTag(TAG_PROCESSING) as? DialogFragment)
            ?.dismissAllowingStateLoss()
    }

    private val TAG_ERROR = "payment_error"

    // PaymentActivity 내부에 추가
    private fun showPaymentCompleteFragment(
        resp: PaymentApproveResponse<PaymentApproveData>
    ) {
        val d = resp.data

        navigatedToComplete = true

        val frag = PaymentCompleteFragment().apply {
            arguments = Bundle().apply {
                putInt("amount", d.amount.total)
                putString("method", d.paymentMethodType)
                putString("paymentId", d.partnerOrderId)
                putString("tid", d.tid)
            }
        }

        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(
                android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out
            )
            .replace(R.id.paymentRootContainer, frag, "payment_complete")
            .addToBackStack("payment_complete")
            .commitAllowingStateLoss()
    }


    private fun showPaymentFailedFragment(message: String) {
        hideProcessingDialog()
        Log.d("Payment","showPaymentFailedFragment: $message")

        val frag = PaymentFailedFragment().apply {
            arguments = Bundle().apply { putString("message", message) }
        }
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(
                android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out
            )
            .replace(R.id.paymentRootContainer, frag, "payment_error")
            .addToBackStack("payment_error")
            .commitAllowingStateLoss()

    }


    private fun openPaymentUrl(url: String) {
        if (openedOnce) return
        openedOnce = true

        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    }

    private fun trimSec(time: String?): String? {
        if (time.isNullOrBlank()) return null
        return if (time.count { it == ':' } == 2 && time.endsWith(":00")) time.dropLast(3) else time
    }



    private fun formatDetailLine(date: LocalDate): String {
        val dateFmt = DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", Locale.KOREAN)
        return "${date.format(dateFmt)}".trim()
    }

    private fun handleDeepLink(uri: Uri?) {
        if (uri == null || handledDeepLink) return

        Log.d("Payment", "handleDeepLink uri=$uri scheme=${uri.scheme} host=${uri.host} path=${uri.path} q=${uri.query}")


        val ours = (uri.scheme == "myapp" && uri.host == "pay")

        if (!ours) {
            Log.d("Payment", "handleDeepLink: not our callback")
            return
        }


        val path = uri.path.orEmpty().lowercase()
        val branch = when {
            "cancel" in path -> "cancel"
            "fail"   in path -> "fail"

            "success" in path || "/kakaopay/callback" in path -> "success"
            else -> "success"
        }
        Log.d("Payment", "deeplink branch=$branch")

        when (branch) {
            "cancel" -> {
                handledDeepLink = true
                showPaymentFailedFragment("결제가 취소되었습니다.")
            }
            "fail" -> {
                handledDeepLink = true
                showPaymentFailedFragment("결제에 실패했습니다.")
            }
            else -> {
                val pgToken = uri.getQueryParameter("pg_token")
                    ?: uri.getQueryParameter("pgToken")
                    ?: uri.getQueryParameter("token")

                val payId = uri.getQueryParameter("paymentId")?.toIntOrNull()
                    ?: uri.getQueryParameter("payment_id")?.toIntOrNull()
                    ?: currentPaymentId
                    ?: retrievePaymentIdPersisted()

                Log.d("Payment", "approve params pgToken=${pgToken?.take(6)}****, payId=$payId")

                if (pgToken.isNullOrBlank() || payId == null) {
                    Log.w("Payment", "deeplink missing params. pgToken=$pgToken, payId=$payId")

                    handledDeepLink = true
                    showPaymentFailedFragment("승인 정보가 누락되었습니다.")
                    return
                }


                handledDeepLink = true
                showProcessingDialog()
                Log.d("Payment", ">>> calling payVm.approve()")
                payVm.approve(payId, pgToken)
            }
        }
    }







    private fun persistPaymentId(id: Int) {
        getSharedPreferences("pay", MODE_PRIVATE)
            .edit()
            .putInt("payment_id", id)
            .apply()
    }

    private fun retrievePaymentIdPersisted(): Int? {
        val v = getSharedPreferences("pay", MODE_PRIVATE)
            .getInt("payment_id", -1)
        return v.takeIf { it > 0 }
    }

    private fun clearPersistedPaymentId() {
        getSharedPreferences("pay", MODE_PRIVATE)
            .edit()
            .remove("payment_id")
            .apply()
    }

    private fun openUrl(url: String) {
        if (openedOnce) return
        openedOnce = true
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun openPayment(appScheme: String?, webUrl: String) {
        if (openedOnce) {
            return
        }
        openedOnce = true
        try {
            if (!appScheme.isNullOrBlank()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(appScheme)))
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)))
            }
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)))
        }
    }



}
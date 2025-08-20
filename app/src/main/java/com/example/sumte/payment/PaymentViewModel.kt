package com.example.sumte.payment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout


sealed class PayUiState {
    object Idle : PayUiState()
    object Loading : PayUiState()
    data class Success(val data: PaymentData) : PayUiState()
    data class Error(val msg: String) : PayUiState()
}

sealed interface ApproveUiState {
    object Idle : ApproveUiState
    object Loading : ApproveUiState
    data class Success(
        val data: PaymentApproveResponse<PaymentApproveData>
    ) : ApproveUiState
    data class Error(val message: String) : ApproveUiState
}

sealed interface ManualApproveUiState {
    object Idle : ManualApproveUiState
    object Loading : ManualApproveUiState
    data class Success(val message: String) : ManualApproveUiState
    data class Error(val message: String) : ManualApproveUiState
}

class PaymentViewModel(private val repo: PaymentRepository) : ViewModel() {
    private val _state = MutableStateFlow<PayUiState>(PayUiState.Idle)
    val state: StateFlow<PayUiState> = _state

    @Volatile private var inFlight = false

    fun startKakao(reservationId: Int, amount: Int) {
        if (inFlight) return
        inFlight = true
        _state.value = PayUiState.Loading


        Log.d("PayVM", "resId=$reservationId")

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val data = repo.requestKakaoPay(reservationId, amount)
                _state.value = PayUiState.Success(data)
            } catch (e: Exception) {
                val msg = when (e) {
                    is retrofit2.HttpException -> {
                        val body = e.response()?.errorBody()?.string().orEmpty()
                        "HTTP ${e.code()}: ${body.ifBlank { e.message() ?: "서버 오류" }}"
                    }
                    is java.io.IOException -> "네트워크 오류가 발생했어요. 연결을 확인해주세요."
                    else -> e.message ?: "결제 요청 실패"
                }
                _state.value = PayUiState.Error(msg)
            } finally {
                inFlight = false
            }
        }
    }

    private val _approveState = MutableStateFlow<ApproveUiState>(ApproveUiState.Idle)
    val approveState: StateFlow<ApproveUiState> = _approveState

    fun approve(paymentId: Int, pgToken: String) {
        _approveState.value = ApproveUiState.Loading
        viewModelScope.launch {
            try {
                val res = repo.approvePayment(paymentId, pgToken)

                if (res == null) {
                    _approveState.value = ApproveUiState.Error("로그인이 필요합니다.")
                    return@launch
                }

                val code = res.code()
                if (!res.isSuccessful) {
                    val err = res.errorBody()?.string().orEmpty()
                    android.util.Log.e("PayVM", "approve HTTP $code error=$err")
                    _approveState.value =
                        ApproveUiState.Error("HTTP $code: ${err.ifBlank { "서버 오류" }}")
                    return@launch
                }

                val body = res.body()
                android.util.Log.d(
                    "PayVM",
                    "approve body success=${body?.success} code=${body?.code} msg=${body?.message}"
                )

                if (body?.success == true) {
                    _approveState.value = ApproveUiState.Success(body)   // ★ 래퍼 그대로
                } else {
                    _approveState.value = ApproveUiState.Error(body?.message ?: "결제 승인 실패")
                }
            } catch (e: Exception) {
                android.util.Log.e("PayVM", "approve exception", e)
                _approveState.value = ApproveUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

        private val _manualApproveState = MutableStateFlow<ManualApproveUiState>(ManualApproveUiState.Idle)
        val manualApproveState: StateFlow<ManualApproveUiState> = _manualApproveState

        @Volatile private var manualInFlight = false
    fun approveManual(paymentId: Int) {
        if (manualInFlight) return
        manualInFlight = true

        Log.d("PayVM", "approveManual called with paymentId=$paymentId")

        _manualApproveState.value = ManualApproveUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withTimeout(10_000) { // ✅ 10초 타임아웃
                    val res = repo.approvePaymentManual(paymentId)
                    if (res == null) {
                        _manualApproveState.value = ManualApproveUiState.Error("로그인이 필요합니다.")
                        return@withTimeout
                    }
                    if (!res.isSuccessful) {
                        val err = res.errorBody()?.string().orEmpty()
                        _manualApproveState.value =
                            ManualApproveUiState.Error("HTTP ${res.code()}: ${err.ifBlank { "서버 오류" }}")
                        return@withTimeout
                    }
                    val body = res.body()
                    if (body?.success == true) {
                        _manualApproveState.value =
                            ManualApproveUiState.Success(body.message ?: "결제가 완료되었습니다.")
                    } else {
                        _manualApproveState.value =
                            ManualApproveUiState.Error(body?.message ?: "임의 승인 실패")
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _manualApproveState.value = ManualApproveUiState.Error("응답 지연(타임아웃)")
            } catch (e: Exception) {
                _manualApproveState.value = ManualApproveUiState.Error(e.message ?: "네트워크 오류")
            } finally {
                manualInFlight = false
            }
        }
    }




            fun reset() {
                _state.value = PayUiState.Idle
            }

            fun resetManualApprove() {
                _manualApproveState.value = ManualApproveUiState.Idle
            }
        }


package com.example.sumte.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


sealed class PayUiState {
    object Idle : PayUiState()
    object Loading : PayUiState()
    data class Success(val data: PaymentData) : PayUiState()
    data class Error(val msg: String) : PayUiState()
}

class PaymentViewModel(private val repo: PaymentRepository) : ViewModel() {
    private val _state = MutableStateFlow<PayUiState>(PayUiState.Idle)
    val state: StateFlow<PayUiState> = _state

    @Volatile private var inFlight = false

    fun startKakao(reservationId: Int, amount: Int) {
        if (inFlight) return
        inFlight = true
        _state.value = PayUiState.Loading

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

    // 필요하면 성공/실패 후 화면에서 다시 Idle로 돌리고 싶을 때 사용
    fun reset() { _state.value = PayUiState.Idle }
}

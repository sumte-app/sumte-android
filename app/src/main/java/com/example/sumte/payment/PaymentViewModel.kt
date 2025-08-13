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

    fun startKakao(reservationId: Int, amount: Int) {
        _state.value = PayUiState.Loading
        viewModelScope.launch {
            runCatching { repo.requestKakaoPay(reservationId, amount) }
                .onSuccess { _state.value = PayUiState.Success(it) }
                .onFailure { _state.value = PayUiState.Error(it.message ?: "결제 요청 실패") }
        }
    }
}
package com.example.sumte.housedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sumte.review.ReviewItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ReviewUiState {
    object Idle : ReviewUiState()
    object Loading : ReviewUiState()
    data class Success(
        val items: List<ReviewItem>,
        val page: Int,
        val isLast: Boolean
    ) : ReviewUiState()

    data class Error(val msg: String) : ReviewUiState()
}

// ★ ViewModel 은 sealed class 바깥으로 분리!
class GuestHouseReviewViewModel(private val repo: ReviewRepository) : ViewModel() {

    private val _state = MutableStateFlow<ReviewUiState>(ReviewUiState.Idle)
    val state: StateFlow<ReviewUiState> = _state

    // ★ ReviewItem 으로 누적
    private val accumulated = mutableListOf<ReviewItem>()
    private var currentPage = 0
    private var isLast = false
    private var loading = false

    fun loadFirst(guesthouseId: Long, size: Int = 10) {
        accumulated.clear()
        currentPage = 0
        isLast = false
        loadNext(guesthouseId, size)
    }

    fun loadNext(guesthouseId: Long, size: Int = 10) {
        if (loading || isLast) return
        loading = true
        _state.value = ReviewUiState.Loading
        viewModelScope.launch {
            val result = repo.fetchGuesthouseReviews(guesthouseId, currentPage, size)
            result.onSuccess { page ->
                accumulated += page.content          // 타입 일치 OK
                isLast = page.last                   // ReviewPageResponse 에 last 필드 있어야 함
                _state.value = ReviewUiState.Success(
                    items = accumulated.toList(),
                    page = currentPage,
                    isLast = isLast
                )
                currentPage += 1
            }.onFailure { e ->
                _state.value = ReviewUiState.Error(e.message ?: "Unknown error")
            }
            loading = false
        }
    }
}

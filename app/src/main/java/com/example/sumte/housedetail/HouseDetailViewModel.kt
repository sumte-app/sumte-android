package com.example.sumte.housedetail

import androidx.lifecycle.*
import kotlinx.coroutines.launch

sealed interface RoomUiState {
    object Loading : RoomUiState
    data class Success(val items: List<RoomInfo>) : RoomUiState
    data class Error(val msg: String) : RoomUiState
}

class HouseDetailViewModel(
    private val repo: RoomRepository
) : ViewModel() {

    private val _state = MutableLiveData<RoomUiState>()
    val state: LiveData<RoomUiState> = _state

    // 단건 조회 (roomId로)
    fun loadRoom(roomId: Int) {
        _state.value = RoomUiState.Loading
        viewModelScope.launch {
            try {
                val item = repo.fetchRoom(roomId)
                _state.value = RoomUiState.Success(listOf(item)) // 리스트 어댑터 재사용
            } catch (e: Exception) {
                _state.value = RoomUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    // 목록 조회 (guesthouseId + 날짜 범위)
    fun loadRooms(guesthouseId: Int, startDate: String, endDate: String) {
        _state.value = RoomUiState.Loading
        viewModelScope.launch {
            try {
                val items = repo.fetchRooms(guesthouseId, startDate, endDate)
                _state.value = RoomUiState.Success(items)
            } catch (e: Exception) {
                _state.value = RoomUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }
}

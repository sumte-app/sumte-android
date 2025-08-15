package com.example.sumte.housedetail

import androidx.lifecycle.*
import kotlinx.coroutines.launch

sealed interface RoomUiState {
    object Loading : RoomUiState
    data class Success(val items: List<RoomInfo>) : RoomUiState
    data class Error(val msg: String) : RoomUiState
}

data class GuesthouseInfo(
    val name: String,
    val address: String?,
    val imageUrls: List<String>,
    val averageScore: Double?,
    val reviewCount: Int?,
)



class HouseDetailViewModel(
    private val repo: RoomRepository
) : ViewModel() {

    private val _state = MutableLiveData<RoomUiState>()
    val state: LiveData<RoomUiState> = _state

    private val _header = MutableLiveData<GuesthouseInfo>()
    val header: LiveData<GuesthouseInfo> = _header

    // 스크롤 위치를 저장할 변수 추가
    var scrollPosition: Int = 0


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

    fun loadGuesthouse(guesthouseId: Int) {
        viewModelScope.launch {
            try {

                val d = repo.fetchGuesthouse(guesthouseId)
                _header.value = GuesthouseInfo(
                    name = d.name,
                    address = d.address,
                    imageUrls = d.imageUrls ?: emptyList(),
                    averageScore= d.averageScore,
                    reviewCount = d.reviewCount
                )
            } catch (_: Exception) {
                // 헤더 실패 시 조용히 무시(필요하면 별도 에러 상태 추가 가능)
            }
        }
    }



}

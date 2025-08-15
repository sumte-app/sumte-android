package com.example.sumte.housedetail

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import java.time.LocalDate

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

    private val _roomDetail = MutableLiveData<RoomDetailInfo>()
    val roomDetail: LiveData<RoomDetailInfo> = _roomDetail

    private val _roomDetailLoading = MutableLiveData<Boolean>()
    val roomDetailLoading: LiveData<Boolean> = _roomDetailLoading

    private val _roomDetailError = MutableLiveData<String?>()
    val roomDetailError: LiveData<String?> = _roomDetailError

    private val _unavailableDates = MutableLiveData<List<LocalDate>>(emptyList())
    val unavailableDates: LiveData<List<LocalDate>> = _unavailableDates


    // 단건 조회 (roomId로)
    fun loadRoom(roomId: Int) {
        _roomDetailLoading.value = true
        _roomDetailError.value = null
        viewModelScope.launch {
            runCatching { repo.fetchRoom(roomId) }          // ← RoomDetailInfo 반환
                .onSuccess { _roomDetail.value = it }
                .onFailure { _roomDetailError.value = it.message ?: "네트워크 오류" }
            _roomDetailLoading.value = false
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

    fun loadUnavailableDates(roomId: Int) {
        viewModelScope.launch {
            val r = repo.fetchUnavailableDates(roomId)
            if (r.isSuccess) {
                _unavailableDates.value = r.getOrDefault(emptyList())
            } else {
                Log.e("HD/VM", "fetchUnavailableDates failed", r.exceptionOrNull())
                _unavailableDates.value = emptyList()
            }
        }
    }





}

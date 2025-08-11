package com.example.sumte.guesthouse

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sumte.ApiClient
import com.example.sumte.R
import com.example.sumte.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GuestHouseViewModel(
    private val api: GuesthouseApi = RetrofitClient.api
) : ViewModel() {
    private val likeService = ApiClient.likeService

    // 1. 찜 상태를 Guesthouse 객체 전체가 아닌, ID Set으로 관리.
    private val _likedGuestHouseIds = MutableStateFlow<Set<Int>>(emptySet())
    val likedGuestHouseIds: StateFlow<Set<Int>> = _likedGuestHouseIds

    init {
        // 2. ViewModel이 생성될 때, 서버에서 현재 찜 목록을 가져와 상태를 초기화
        loadInitialLikes()
    }

    private fun loadInitialLikes() {
        viewModelScope.launch {
            try {
                // 페이지 크기를 충분히 크게 설정하여 모든 찜 목록을 가져옵니다.
                val response = likeService.getLikes(size = 200)
                if (response.isSuccessful) {
                    // 성공 시, 응답받은 찜 목록의 ID들만 추출하여 Set으로 만듭니다.
                    val likedIds: Set<Int> = response.body()?.content
                        ?.map { it.id.toInt() }  // 여기서 Int로 변환
                        ?.toSet()
                        ?: emptySet()
                    _likedGuestHouseIds.value = likedIds
                } else {
                    Log.e("GuestHouseViewModel", "초기 찜 목록 로딩 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GuestHouseViewModel", "초기 찜 목록 로딩 중 에러", e)
            }
        }
    }

    // 3. isLiked 함수를 StateFlow의 값을 직접 확인하도록 변경합니다.
    fun isLiked(guestHouse: GuestHouse): Boolean {
        return _likedGuestHouseIds.value.contains(guestHouse.id)
    }

    // 4. toggleLike 함수가 서버 API를 직접 호출하도록
    fun toggleLike(guestHouse: GuestHouse) {
        viewModelScope.launch {
            val isCurrentlyLiked = isLiked(guestHouse)
            val id = guestHouse.id

            try {
                val response = if (isCurrentlyLiked) {
                    likeService.removeLikes(guestHouse.id)
                } else {
                    likeService.addLikes(guestHouse.id)
                }

                if (response.isSuccessful) {
                    val currentIds = _likedGuestHouseIds.value.toMutableSet()
                    if (isCurrentlyLiked) currentIds.remove(guestHouse.id) else currentIds.add(guestHouse.id)
                    _likedGuestHouseIds.value = currentIds
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(
                        "GuestHouseViewModel",
                        "찜 상태 변경 실패: code=${response.code()}, error=$errorBody"
                    )
                }
            } catch (e: Exception) {
                Log.e("GuestHouseViewModel", "에러 발생", e)
            }
        }
    }

    // -----------------------------
    // 홈 목록 캐시 (뒤로가기 복원용)
    // -----------------------------
    val items = mutableListOf<GuestHouse>()   // ← HomeFragment에서 바로 사용
    var nextPage: Int = 1                     // UI 기준 1-based
    var isLastPageCached: Boolean = false

    // -----------------------------
    // DTO -> UI 매핑 (null/빈값 안전)
    // -----------------------------
    private fun mapToUi(dtos: List<GuesthouseDto>): List<GuestHouse> =
        dtos.map { d ->
            val rooms = d.rooms.orEmpty()
            val minPrice = d.minPrice ?: 0

            // 썸네일: 게스트하우스 이미지 → 첫 방 이미지 → 없으면 빈 문자열
            val thumb = d.imageUrls.orEmpty().firstOrNull()
                ?: rooms.firstOrNull()?.imageUrl
                ?: ""

            GuestHouse(
                id = d.id, // Long
                title = d.name ?: "-",
                location = listOfNotNull(d.addressRegion, d.addressDetail)
                    .filter { it.isNotBlank() }
                    .joinToString(" "),
                price = if (minPrice > 0) String.format("%,d원", minPrice) else "가격 정보 없음",
                imageUrl = thumb.ifBlank { null },
                imageResId = R.drawable.sumte_logo1, // 프로젝트에 존재하는 기본 이미지로 교체 가능
                time = "" // 필요하면 체크인/아웃 등으로 채워도 됨
            )
        }

    // -----------------------------
    // 서버에서 한 페이지 가져오기 (서버는 0-based)
    // -----------------------------
    suspend fun fetchPage(serverPage: Int, pageSize: Int): List<GuestHouse> {
        Log.d("GH", "fetchPage() called page=$serverPage size=$pageSize")
        return try {
            val res = api.getGuesthousesHome(serverPage, pageSize)
            Log.d("GH", "raw body: ${res.errorBody()?.string() ?: res.body()}")
            Log.d("GH", "/guesthouse/home -> ${res.code()}")
            if (res.isSuccessful) {
                val dtos = res.body()?.data?.content.orEmpty()
                Log.d("GH", "content size=${dtos.size}")
                mapToUi(dtos)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("GH", "fetchPage error", e)
            emptyList()
        }
    }

    // -----------------------------
    // UI 1-based → 서버 0-based 보정 + 캐시 갱신
    // -----------------------------
    suspend fun fetchPageAndCache(pageUi: Int, pageSize: Int): List<GuestHouse> {
        val serverPage = pageUi - 1
        val list = fetchPage(serverPage, pageSize)

        if (pageUi == 1) items.clear()
        items.addAll(list)

        if (list.isEmpty()) {
            isLastPageCached = true
        } else {
            nextPage = pageUi + 1
        }
        return list
    }
}

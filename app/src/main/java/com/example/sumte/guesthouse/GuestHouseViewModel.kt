package com.example.sumte.guesthouse

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sumte.ApiClient
import com.example.sumte.R
import com.example.sumte.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GuestHouseViewModel(
    private val api: GuesthouseApi = RetrofitClient.api
) : ViewModel() {

    private val likeService = ApiClient.likeService

    // =============================
    // 찜 상태 (홈의 guestHouseId = Int 기준)
    // =============================
    private val _likedGuestHouseIds = MutableStateFlow<Set<Int>>(emptySet())
    val likedGuestHouseIds: StateFlow<Set<Int>> = _likedGuestHouseIds

    private val _initialLikesLoaded = MutableStateFlow(false)
    val initialLikesLoaded: StateFlow<Boolean> = _initialLikesLoaded

    init { loadInitialLikes() }

    private fun loadInitialLikes() {
        viewModelScope.launch {
            try {
                val response = likeService.getLikes(size = 200)
                if (response.isSuccessful) {
                    // 서버 스키마에 맞춰 Int로 추출
                    val likedIds: Set<Int> = response.body()?.content
                        ?.mapNotNull { it.id }   // 필요 시 .toInt() 로 변환
                        ?.toSet()
                        ?: emptySet()
                    _likedGuestHouseIds.value = likedIds
                    Log.d("ViewModel_Likes", "초기 찜 목록: ${_likedGuestHouseIds.value}")
                } else {
                    Log.e("GuestHouseViewModel", "초기 찜 로딩 실패 code=${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GuestHouseViewModel", "초기 찜 로딩 에러", e)
            } finally {
                _initialLikesLoaded.value = true
            }
        }
    }

    fun isLiked(guestHouse: GuestHouse): Boolean {
        return _likedGuestHouseIds.value.contains(guestHouse.id.toInt())
    }

    fun toggleLike(guestHouse: GuestHouse, onStateUpdated: () -> Unit) {
        viewModelScope.launch {
            val idInt = guestHouse.id.toInt()
            val isCurrentlyLiked = _likedGuestHouseIds.value.contains(idInt)
            try {
                val res = if (isCurrentlyLiked) {
                    likeService.removeLikes(idInt)
                } else {
                    likeService.addLikes(idInt)
                }
                if (res.isSuccessful) {
                    val cur = _likedGuestHouseIds.value.toMutableSet()
                    if (isCurrentlyLiked) cur.remove(idInt) else cur.add(idInt)
                    _likedGuestHouseIds.value = cur
                    onStateUpdated()
                } else {
                    Log.e("GuestHouseViewModel", "찜 변경 실패 code=${res.code()} body=${res.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("GuestHouseViewModel", "찜 변경 에러", e)
            }
        }
    }

    // =============================
    // 홈 목록 캐시 (뒤로가기 복원용)
    // =============================
    val items = mutableListOf<GuestHouse>()   // HomeFragment에서 사용
    var nextPage: Int = 1                     // UI 1-based
    var isLastPageCached: Boolean = false

    // =============================
    // 홈 DTO -> UI 매핑
    // (홈 응답의 imageUrl은 사용하지 않음. 이미지는 /images로 따로)
    // =============================
    private fun mapHomeToUi(dtos: List<GuesthouseHomeItemDto>): List<GuestHouse> =
        dtos.map { d ->
            val minPrice = d.minPrice ?: 0
            GuestHouse(
                id = d.guestHouseId.toLong(),              // 내부 모델은 Long 유지
                title = d.name,
                location = d.addressRegion.orEmpty(),
                price = if (minPrice > 0) "%,d원".format(minPrice) else "가격 정보 없음",
                imageUrl = null,                               // ← 일단 비워두고,
                imageResId = R.drawable.sumte_logo1,           // placeholder
                time = d.checkInTime.orEmpty()
            )
        }





    private suspend fun fetchGuesthouseThumbUrl(guestHouseId: Int): String? =
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                Log.d("IMG", "[REQ] /images ownerType=GUESTHOUSE ownerId=$guestHouseId")
                val res = api.getImages(ownerType = "GUESTHOUSE", ownerId = guestHouseId.toLong())
                if (!res.isSuccessful) {
                    Log.e("IMG", "[FAIL] /images code=${res.code()} body=${res.errorBody()?.string()}")
                    return@withContext null
                }
                val list = res.body().orEmpty()
                Log.d("IMG", "[OK] /images size=${list.size} ghId=$guestHouseId")
                if (list.isEmpty()) {
                    Log.w("IMG", "[EMPTY] no images for ghId=$guestHouseId")
                    return@withContext null
                }
                val url = list.minByOrNull { it.sortOrder }?.url?.trim()
                Log.d("IMG", "[PICK] ghId=$guestHouseId url=$url")
                return@withContext url
            } catch (e: Exception) {
                Log.e("IMG", "[EXC] /images ghId=$guestHouseId", e)
                return@withContext null
            }
        }


    suspend fun fetchPage(serverPage: Int, pageSize: Int): List<GuestHouse> {
        Log.d("GH", "fetchPage() page=$serverPage size=$pageSize")
        return try {
            val res = api.getGuesthousesHome(serverPage, pageSize)
            Log.d("GH", "/guesthouse/home -> ${res.code()}")
            if (!res.isSuccessful) return emptyList()

            val homeDtos: List<GuesthouseHomeItemDto> = res.body()?.data?.content.orEmpty()
            val base = mapHomeToUi(homeDtos) // imageUrl=null 상태

            // 썸네일 병렬 조회 후 주입
            val withThumbs: List<GuestHouse> = coroutineScope {
                base.map { gh ->
                    async<GuestHouse> {
                        Log.d("IMG", "will fetch thumb for ghId=${gh.id}")
                        val url = fetchGuesthouseThumbUrl(gh.id.toInt())  // ✅ Int
                        Log.d("IMG", "got thumb for ghId=${gh.id} -> $url")
                        if (!url.isNullOrBlank()) gh.copy(imageUrl = url) else gh
                    }
                }.awaitAll()
            }

            withThumbs
        } catch (e: Exception) {
            Log.e("GH", "fetchPage error", e)
            emptyList()
        }
    }





    // =============================
    // UI 1-based → 서버 0-based 보정 + 캐시 갱신
    // =============================
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

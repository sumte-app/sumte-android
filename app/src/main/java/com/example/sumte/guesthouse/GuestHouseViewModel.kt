package com.example.sumte.guesthouse

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sumte.ApiClient
import com.example.sumte.GuesthouseSummaryDto
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
    // 찜한 게스트하우스의 ID 만을 SET 형태로 저장
    private val _likedGuestHouseIds = MutableStateFlow<Set<Int>>(emptySet())
    val likedGuestHouseIds: StateFlow<Set<Int>> = _likedGuestHouseIds

    // LikeFragment에 보여줄 실제 데이터 목록.
    private val _likedGuesthouses = MutableStateFlow<List<GuesthouseSummaryDto>>(emptyList())
    val likedGuesthouses: StateFlow<List<GuesthouseSummaryDto>> = _likedGuesthouses

    // 찜 목록을 서버에서 불러오는 함수
//    fun loadLikedGuesthouses() {
//        viewModelScope.launch {
//            try {
//                // LikeService의 getLikes 함수를 호출
//                val response = likeService.getLikes()
//                if (response.isSuccessful) {
//                    _likedGuesthouses.value = response.body()?.content ?: emptyList()
//                }
//            } catch (e: Exception) {
//                // 에러 처리
//                Log.e("GuestHouseViewModel", "Failed to load liked guesthouses", e)
//            }
//        }
//    }
    fun loadLikedGuesthouses() {
        viewModelScope.launch {
            try {
                // 1단계: 찜한 게스트하우스의 ID 목록을 가져옵니다.
                val likesResponse = likeService.getLikes()
                if (!likesResponse.isSuccessful) {
                    _likedGuesthouses.value = emptyList()
                    Log.e("GuestHouseViewModel", "찜 목록 ID 로딩 실패: ${likesResponse.code()}")
                    return@launch
                }

                val guesthouseIds = likesResponse.body()?.content?.map { it.id } ?: emptyList()

                if (guesthouseIds.isEmpty()) {
                    _likedGuesthouses.value = emptyList()
                    return@launch
                }

                // 2단계: 각 ID에 해당하는 게스트하우스 요약 정보를 병렬로 가져옵니다.
                val summaryList = guesthouseIds.map { id ->
                    async {
                        try {
                            val summaryResponse = likeService.getGuesthouseSummary(id)
                            if (summaryResponse.isSuccessful) {
                                summaryResponse.body()?.data
                            } else {
                                Log.e("GuestHouseViewModel", "개별 요약 정보 로딩 실패 (ID: $id): ${summaryResponse.code()}")
                                null
                            }
                        } catch (e: Exception) {
                            Log.e("GuestHouseViewModel", "개별 요약 정보 로딩 중 예외 발생 (ID: $id)", e)
                            null
                        }
                    }
                }.awaitAll().filterNotNull() // 모든 작업이 끝날 때까지 기다린 후, 성공한(null이 아닌) 결과만 모읍니다.

                // 3단계: 최종적으로 만들어진 DTO 리스트를 StateFlow에 바로 할당합니다.
                _likedGuesthouses.value = summaryList

            } catch (e: Exception) {
                Log.e("GuestHouseViewModel", "찜 목록 로딩 과정에서 전체 오류 발생", e)
                _likedGuesthouses.value = emptyList()
            }
        }
    }

    // 찜 취소 함수 (ID를 받아서 처리)
    fun removeLike(guesthouseId: Int) {
        Log.d("DEBUG_LIKE", "===[ 찜 취소 시도 ]=== ID: $guesthouseId")
        viewModelScope.launch {
            try {
                val response = likeService.removeLikes(guesthouseId)
                if (response.isSuccessful) {
                    // 1. HomeFragment를 위한 ID 목록 업데이트
                    Log.d("DEBUG_LIKE", "[성공] API 응답 코드: ${response.code()}")
                    _likedGuestHouseIds.value = _likedGuestHouseIds.value - guesthouseId

                    // 2. LikeFragment를 위한 찜 목록 리스트 업데이트
                    _likedGuesthouses.value = _likedGuesthouses.value.filterNot { it.id == guesthouseId }
                }else{
                    val errorBody = response.errorBody()?.string()
                    Log.e("DEBUG_LIKE", "[실패] API 응답 코드: ${response.code()}, 에러 메시지: $errorBody")
                }
            } catch (e: Exception) {
                // 에러 처리
                Log.e("DEBUG_LIKE", "[예외 발생] 찜 취소 중 에러", e)
            }
        }
    }

    fun addLike(guesthouseId: Int) {
        viewModelScope.launch {
            try {
                val response = likeService.addLikes(guesthouseId)
                if (response.isSuccessful) {
                    // 찜이 추가되었으므로, 전체 찜 목록을 다시 불러와서
                    // 두 StateFlow를 모두 최신 상태로 유지.
                    loadLikedGuesthouses() // 찜 목록 리스트 갱신
                    updateLikedStatusForVisibleItems() // 홈 화면 찜 상태 갱신
                }
            } catch (e: Exception) {
                Log.e("GuestHouseViewModel", "Failed to add like", e)
            }
        }
    }

//    private val _initialLikesLoaded = MutableStateFlow(false)
//    val initialLikesLoaded: StateFlow<Boolean> = _initialLikesLoaded
//
//    init { loadInitialLikes() }

    suspend fun updateLikedStatusForVisibleItems() {
        // ViewModel이 가진 전체 아이템 목록이 비어있으면 찜 목록도 비웁니다.
        if (items.isEmpty()) {
            _likedGuestHouseIds.value = emptySet()
            return
        }

        // 현재 `items` 리스트에 있는 모든 게스트하우스의 ID를 Int 리스트로 변환
        val currentVisibleIds = items.map { it.id.toInt() }

        try {
            // 새로운 API를 호출하여 현재 보이는 ID들 중 찜한 ID 목록을 가져옵니다.
            val response = likeService.checkFavorites(guesthouseIds = currentVisibleIds)

            if (response.isSuccessful) {
                // API가 성공적으로 찜된 ID 목록(List<Int>)을 반환하면 Set으로 변환하여 덮어씁니다.
                _likedGuestHouseIds.value = response.body()?.toSet() ?: emptySet()
                Log.d("ViewModel_Likes", "찜 상태 업데이트 완료: ${_likedGuestHouseIds.value}")
            } else {
                Log.e("ViewModel_Likes", "찜 상태 업데이트 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("ViewModel_Likes", "찜 상태 업데이트 중 에러", e)
        }
    }
//    private fun loadInitialLikes() {
//        viewModelScope.launch {
//            try {
//                val response = likeService.getLikes(size = 200)
//                if (response.isSuccessful) {
//                    // 서버 스키마에 맞춰 Int로 추출
//                    val likedIds: Set<Int> = response.body()?.content
//                        ?.mapNotNull { it.id }   // 필요 시 .toInt() 로 변환
//                        ?.toSet()
//                        ?: emptySet()
//                    _likedGuestHouseIds.value = likedIds
//                    Log.d("ViewModel_Likes", "초기 찜 목록: ${_likedGuestHouseIds.value}")
//                } else {
//                    Log.e("GuestHouseViewModel", "초기 찜 로딩 실패 code=${response.code()}")
//                }
//            } catch (e: Exception) {
//                Log.e("GuestHouseViewModel", "초기 찜 로딩 에러", e)
//            } finally {
//                _initialLikesLoaded.value = true
//            }
//        }
//    }

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
                    loadLikedGuesthouses()
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

    // 로드된 모든 게스트하우스의 ID 목록을 저장할 StateFlow
    private val _guesthouseIds = MutableStateFlow<List<Int>>(emptyList())
    val guesthouseIds: StateFlow<List<Int>> = _guesthouseIds

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
        updateLikedStatusForVisibleItems()
        // 캐시된 items 리스트를 기반으로 ID 목록을 갱신
//        _guesthouseIds.value = items.map { it.id.toInt() }

        if (list.isEmpty()) {
            isLastPageCached = true
        } else {
            nextPage = pageUi + 1
        }
        return list
    }

}

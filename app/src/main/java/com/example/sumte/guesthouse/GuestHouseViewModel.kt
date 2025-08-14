package com.example.sumte.guesthouse

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sumte.ApiClient
import com.example.sumte.R
import com.example.sumte.RetrofitClient
import com.example.sumte.search.GuesthouseItemResponse
import com.example.sumte.search.GuesthouseSearchRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ---------- UI State (검색 결과 화면용) ----------
sealed interface UiState {
    object Loading : UiState
    data class Success(val items: List<GuestHouse>, val isLast: Boolean) : UiState
    data class Error(val message: String?) : UiState
}

class GuestHouseViewModel(
    private val api: GuesthouseApi = RetrofitClient.api
) : ViewModel() {

    private val likeService = ApiClient.likeService

    // ---------- 찜 상태 ----------
    private val _likedGuestHouseIds = MutableStateFlow<Set<Int>>(emptySet())
    val likedGuestHouseIds: StateFlow<Set<Int>> = _likedGuestHouseIds

    private val _initialLikesLoaded = MutableStateFlow(false)
    val initialLikesLoaded: StateFlow<Boolean> = _initialLikesLoaded

    init {
        loadInitialLikes()
    }

    private fun loadInitialLikes() {
        viewModelScope.launch {
            try {
                val response = likeService.getLikes(size = 200)
                if (response.isSuccessful) {
                    val likedIds: Set<Int> = response.body()?.content
                        ?.mapNotNull { it.id }?.toSet() ?: emptySet()
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

    fun isLiked(guestHouse: GuestHouse): Boolean =
        _likedGuestHouseIds.value.contains(guestHouse.id.toInt())

    fun toggleLike(guestHouse: GuestHouse, onStateUpdated: () -> Unit) {
        viewModelScope.launch {
            val idInt = guestHouse.id.toInt()
            val isCurrentlyLiked = _likedGuestHouseIds.value.contains(idInt)
            try {
                val res = if (isCurrentlyLiked) likeService.removeLikes(idInt)
                else likeService.addLikes(idInt)
                if (res.isSuccessful) {
                    val cur = _likedGuestHouseIds.value.toMutableSet()
                    if (isCurrentlyLiked) cur.remove(idInt) else cur.add(idInt)
                    _likedGuestHouseIds.value = cur
                    onStateUpdated()
                } else {
                    Log.e(
                        "GuestHouseViewModel",
                        "찜 변경 실패 code=${res.code()} body=${res.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("GuestHouseViewModel", "찜 변경 에러", e)
            }
        }
    }

    // ---------- 홈 목록 캐시(뒤로가기 복원용) ----------
    val items = mutableListOf<GuestHouse>()   // HomeFragment에서 사용
    var nextPage: Int = 1                     // UI 1-based
    var isLastPageCached: Boolean = false

    // 홈 DTO -> UI (이미지 URL은 /images로 따로)
    private fun mapHomeToUi(dtos: List<GuesthouseHomeItemDto>): List<GuestHouse> =
        dtos.map { d ->
            val minPrice = d.minPrice ?: 0
            GuestHouse(
                id = (d.guestHouseId ?: 0).toLong(),
                title = d.name.orEmpty(),
                location = d.addressRegion.orEmpty(),
                price = if (minPrice > 0) "%,d원".format(minPrice) else "가격 정보 없음",
                imageUrl = null,
                imageResId = R.drawable.sumte_logo1,
                time = d.checkInTime.orEmpty()
            )
        }

    private suspend fun fetchGuesthouseThumbUrl(guestHouseId: Int): String? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("IMG", "[REQ] /images ownerType=GUESTHOUSE ownerId=$guestHouseId")
                val res = api.getImages(ownerType = "GUESTHOUSE", ownerId = guestHouseId.toLong())
                if (!res.isSuccessful) {
                    Log.e("IMG", "[FAIL] /images code=${res.code()} body=${res.errorBody()?.string()}")
                    return@withContext null
                }
                val list = res.body().orEmpty()
                Log.d("IMG", "[OK] /images size=${list.size} ghId=$guestHouseId")
                if (list.isEmpty()) return@withContext null
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
            val res = api.getGuesthousesHome(
                keyword = null,                 // ✅ 홈목록은 키워드 없이
                page = serverPage,
                size = pageSize
            )
            Log.d("GH", "/guesthouse/home -> ${res.code()}")
            if (!res.isSuccessful) return emptyList()

            val homeDtos: List<GuesthouseHomeItemDto> = res.body()?.data?.content.orEmpty()
            val base = mapHomeToUi(homeDtos)

            // 썸네일 병렬 조회 후 주입
            coroutineScope {
                base.map { gh ->
                    async {
                        val url = fetchGuesthouseThumbUrl(gh.id.toInt())
                        if (!url.isNullOrBlank()) gh.copy(imageUrl = url) else gh
                    }
                }.awaitAll()
            }
        } catch (e: Exception) {
            Log.e("GH", "fetchPage error", e)
            emptyList()
        }
    }

    // UI 1-based → 서버 0-based 보정 + 캐시 갱신
    suspend fun fetchPageAndCache(pageUi: Int, pageSize: Int): List<GuestHouse> {
        val serverPage = pageUi - 1
        val list = fetchPage(serverPage, pageSize)

        if (pageUi == 1) items.clear()
        items.addAll(list)

        isLastPageCached = list.isEmpty()
        if (!isLastPageCached) nextPage = pageUi + 1
        return list
    }

    // ---------- 검색/필터 상태 ----------
    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state

    var currentFilter: GuesthouseSearchRequest? = null
        private set

    private var filterPage = 1              // UI 1-based
    private val filterSize = 20
    private var filterIsLast = false
    private val filteredLoaded = mutableListOf<GuestHouse>()

    /**
     * SearchFragment에서 keyword만 넘길 때:
     * setFilterAndRefresh(GuesthouseSearchRequest(keyword = "제주시"))
     * 다른 필터(가격/인원/옵션 등)는 copy로 채워서 전달.
     */
    fun setFilterAndRefresh(filter: GuesthouseSearchRequest) {
        currentFilter = filter
        filterPage = 1
        filterIsLast = false
        filteredLoaded.clear()
        fetchNextFiltered()
    }

    fun clearFilter() {
        currentFilter = null
        _state.value = UiState.Success(items.toList(), isLastPageCached)
    }

    // 변경 전 fetchNextFiltered()를 아래로 전부 교체
    fun fetchNextFiltered() {
        val filter = currentFilter ?: return
        if (filterIsLast) return

        _state.value = UiState.Loading

        viewModelScope.launch {
            // 공통: 클라이언트 키워드 필터 함수
            fun applyClientKeywordFilter(list: List<GuestHouse>, keyword: String?): List<GuestHouse> {
                val kw = keyword?.trim()?.lowercase().orEmpty()
                if (kw.isEmpty()) return list
                return list.filter { gh ->
                    gh.title.lowercase().contains(kw) ||
                            gh.location.lowercase().contains(kw)
                }
            }

            // 1) 1차: 현재 필터로 조회
            val firstResult = runCatching {
                Log.d("SEARCH", "REQ(1) page=$filterPage kw=${filter.keyword} region=${filter.region} people=${filter.people}")
                api.searchGuesthouses(page = filterPage, size = filterSize, body = filter)
            }.getOrElse { e ->
                _state.value = UiState.Error(e.message)
                return@launch
            }

            if (!firstResult.success || firstResult.data == null) {
                _state.value = UiState.Error(firstResult.message ?: "search failed")
                return@launch
            }

            var pageData = firstResult.data!!
            Log.d("SEARCH", "RES(1) ok=${firstResult.success} page=${pageData.number} recv=${pageData.content.size} last=${pageData.last}")

            // 1차 결과 매핑 + ✅ 클라 키워드 필터
            var uiItems = applyClientKeywordFilter(pageData.content.toUi(), filter.keyword)

            // 2) 2차: 0/1페이지 + 0건 + keyword 있고 region 비었을 때 region=[keyword]로 재조회
            val needSecondTry = (filterPage == 1 || filterPage == 0) &&
                    uiItems.isEmpty() &&
                    !filter.keyword.isNullOrBlank() &&
                    (filter.region.isNullOrEmpty())

            if (needSecondTry) {
                val filterWithRegion = filter.copy(region = listOf(filter.keyword!!.trim()))
                val secondResult = runCatching {
                    Log.d("SEARCH", "REQ(2) page=$filterPage kw=${filterWithRegion.keyword} region=${filterWithRegion.region} people=${filterWithRegion.people}")
                    api.searchGuesthouses(page = filterPage, size = filterSize, body = filterWithRegion)
                }.getOrElse { e ->
                    _state.value = UiState.Error(e.message)
                    return@launch
                }

                if (secondResult.success && secondResult.data != null) {
                    pageData = secondResult.data!!
                    Log.d("SEARCH", "RES(2) ok=${secondResult.success} page=${pageData.number} recv=${pageData.content.size} last=${pageData.last}")
                    // 2차 결과 매핑 + ✅ 클라 키워드 필터
                    uiItems = applyClientKeywordFilter(pageData.content.toUi(), filter.keyword)
                }
            }

            // 3) 폴백: 그래도 0개면 /guesthouse/home?keyword= 로 한 번 더
            val keywordOnly = !filter.keyword.isNullOrBlank()
            if ((filterPage == 1 || filterPage == 0) && uiItems.isEmpty() && keywordOnly) {
                try {
                    val kw = filter.keyword!!.trim()
                    val homeRes = api.getGuesthousesHome(keyword = kw, page = 0, size = filterSize)
                    if (homeRes.isSuccessful) {
                        val homeDtos = homeRes.body()?.data?.content.orEmpty()
                        Log.d("SEARCH", "FALLBACK recv=${homeDtos.size}")
                        val fallbackUi = homeDtos.map { d ->
                            val minPrice = d.minPrice ?: 0
                            GuestHouse(
                                id = (d.guestHouseId ?: 0).toLong(),
                                title = d.name.orEmpty(),
                                location = d.addressRegion.orEmpty(),
                                price = if (minPrice > 0) "%,d원".format(minPrice) else "가격 정보 없음",
                                imageUrl = d.imageUrl,
                                imageResId = R.drawable.sumte_logo1,
                                time = d.checkInTime.orEmpty()
                            )
                        }
                        // ✅ 폴백 결과도 반드시 클라 키워드 필터
                        val filteredFallback = applyClientKeywordFilter(fallbackUi, kw)
                        filteredLoaded += filteredFallback
                        filterIsLast = true // 폴백은 한 페이지만
                        _state.value = UiState.Success(filteredLoaded.toList(), filterIsLast)
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e("SEARCH", "FALLBACK error", e)
                    // 폴백 실패시 아래 기본 처리로 진행
                }
            }

            // ---- 기본 처리 (1/2차 결과 사용, 이미 클라 필터 적용됨) ----
            filteredLoaded += uiItems
            filterIsLast = pageData.last || uiItems.isEmpty()
            if (!filterIsLast) filterPage += 1

            _state.value = UiState.Success(filteredLoaded.toList(), filterIsLast)
        }
    }


}

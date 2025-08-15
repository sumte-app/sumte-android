package com.example.sumte.guesthouse

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sumte.ApiClient
import com.example.sumte.GuesthouseSummaryDto
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


sealed interface UiState {
    object Loading : UiState
    data class Success(val items: List<GuestHouse>, val isLast: Boolean) : UiState
    data class Error(val message: String?) : UiState
}

class GuestHouseViewModel(
    private val api: GuesthouseApi = RetrofitClient.api
) : ViewModel() {

    private val likeService = ApiClient.likeService

    private val _likedGuestHouseIds = MutableStateFlow<Set<Int>>(emptySet())
    val likedGuestHouseIds: StateFlow<Set<Int>> = _likedGuestHouseIds


    private val _likedGuesthouses = MutableStateFlow<List<GuesthouseSummaryDto>>(emptyList())
    val likedGuesthouses: StateFlow<List<GuesthouseSummaryDto>> = _likedGuesthouses


    var clientKeywordOverride: String? = null

    private fun makeRegionPayload(city: String?): List<String>? {
        if (city.isNullOrBlank()) return null
        val province = when (city) {
            "제주시", "서귀포시" -> "제주도"
            else -> null
        }
        return province?.let { listOf(it, city) } ?: listOf(city)
    }

    private fun normalizeRegion(region: List<String>?): List<String>? {
        if (region.isNullOrEmpty()) return null
        return if (region.size == 1) {
            makeRegionPayload(region[0])
        } else {
            region.take(2)
        }
    }


    fun loadLikedGuesthouses() {
        viewModelScope.launch {
            try {
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
                }.awaitAll().filterNotNull()

                _likedGuesthouses.value = summaryList

            } catch (e: Exception) {
                Log.e("GuestHouseViewModel", "찜 목록 로딩 과정에서 전체 오류 발생", e)
                _likedGuesthouses.value = emptyList()
            }
        }
    }

    fun removeLike(guesthouseId: Int) {
        Log.d("DEBUG_LIKE", "===[ 찜 취소 시도 ]=== ID: $guesthouseId")
        viewModelScope.launch {
            try {
                val response = likeService.removeLikes(guesthouseId)
                if (response.isSuccessful) {
                    Log.d("DEBUG_LIKE", "[성공] API 응답 코드: ${response.code()}")
                    _likedGuestHouseIds.value = _likedGuestHouseIds.value - guesthouseId
                    _likedGuesthouses.value = _likedGuesthouses.value.filterNot { it.id == guesthouseId }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DEBUG_LIKE", "[실패] API 응답 코드: ${response.code()}, 에러 메시지: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_LIKE", "[예외 발생] 찜 취소 중 에러", e)
            }
        }
    }

    fun addLike(guesthouseId: Int) {
        viewModelScope.launch {
            try {
                val response = likeService.addLikes(guesthouseId)
                if (response.isSuccessful) {
                    loadLikedGuesthouses()
                    updateLikedStatusForVisibleItems()
                }
            } catch (e: Exception) {
                Log.e("GuestHouseViewModel", "Failed to add like", e)
            }
        }
    }

    suspend fun updateLikedStatusForVisibleItems() {
        if (items.isEmpty()) {
            _likedGuestHouseIds.value = emptySet()
            return
        }

        val currentVisibleIds = items.map { it.id.toInt() }

        try {
            val response = likeService.checkFavorites(guesthouseIds = currentVisibleIds)

            if (response.isSuccessful) {
                _likedGuestHouseIds.value = response.body()?.toSet() ?: emptySet()
                Log.d("ViewModel_Likes", "찜 상태 업데이트 완료: ${_likedGuestHouseIds.value}")
            } else {
                Log.e("ViewModel_Likes", "찜 상태 업데이트 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("ViewModel_Likes", "찜 상태 업데이트 중 에러", e)
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
                val res = if (isCurrentlyLiked) likeService.removeLikes(idInt)
                else likeService.addLikes(idInt)
                if (res.isSuccessful) {
                    val cur = _likedGuestHouseIds.value.toMutableSet()
                    if (isCurrentlyLiked) cur.remove(idInt) else cur.add(idInt)
                    _likedGuestHouseIds.value = cur
                    loadLikedGuesthouses()
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

    val items = mutableListOf<GuestHouse>()
    var nextPage: Int = 1
    var isLastPageCached: Boolean = false


    private val _guesthouseIds = MutableStateFlow<List<Int>>(emptyList())
    val guesthouseIds: StateFlow<List<Int>> = _guesthouseIds


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
                time = d.checkInTime.orEmpty(),
                averageScore = d.averageScore,
                reviewCount = d.reviewCount
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
                keyword = null,
                page = serverPage,
                size = pageSize
            )
            Log.d("GH", "/guesthouse/home -> ${res.code()}")
            if (!res.isSuccessful) return emptyList()

            val homeDtos: List<GuesthouseHomeItemDto> = res.body()?.data?.content.orEmpty()


            homeDtos.forEachIndexed { idx, d ->
                Log.d(
                    "HOME_DTO",
                    "[$idx] id=${d.guestHouseId}, name=${d.name}, " +
                            "avg=${d.averageScore}, reviews=${d.reviewCount}, " +
                            "checkIn=${d.checkInTime}, minPrice=${d.minPrice}, img=${d.imageUrl}"
                )
            }

            val base = mapHomeToUi(homeDtos)


            base.forEachIndexed { idx, gh ->
                Log.d("HOME_UI", "[$idx] id=${gh.id}, title=${gh.title}, time=${gh.time}")
            }


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


    suspend fun fetchPageAndCache(pageUi: Int, pageSize: Int): List<GuestHouse> {
        val serverPage = pageUi - 1
        val list = fetchPage(serverPage, pageSize)

        if (pageUi == 1) items.clear()
        items.addAll(list)
        updateLikedStatusForVisibleItems()

        isLastPageCached = list.isEmpty()
        if (!isLastPageCached) nextPage = pageUi + 1
        return list
    }


    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state

    var currentFilter: GuesthouseSearchRequest? = null
        private set

    private var filterPage = 1
    private val filterSize = 20
    private var filterIsLast = false
    private val filteredLoaded = mutableListOf<GuestHouse>()

    fun setFilterAndRefresh(filter: GuesthouseSearchRequest) {
        val normalized = filter.copy(region = normalizeRegion(filter.region))

        currentFilter = normalized
        filterPage = 1
        filterIsLast = false
        filteredLoaded.clear()

        Log.d("SEARCH", "setFilter region(norm)=${normalized.region}")
        fetchNextFiltered()
    }

    fun clearFilter() {
        currentFilter = null
        _state.value = UiState.Success(items.toList(), isLastPageCached)
    }

    fun fetchNextFiltered() {
        val filter = currentFilter ?: return
        if (filterIsLast) return

        _state.value = UiState.Loading

        viewModelScope.launch {
            fun normalizeRegionLocal(region: List<String>?): List<String>? {
                if (region.isNullOrEmpty()) return null
                return if (region.size == 1) {
                    makeRegionPayload(region[0])
                } else region.take(2)
            }

            fun applyClientKeywordFilter(list: List<GuestHouse>, keyword: String?): List<GuestHouse> {
                val kw = keyword?.trim()?.lowercase().orEmpty()
                if (kw.isEmpty()) return list
                return list.filter { gh ->
                    gh.title.lowercase().contains(kw) || gh.location.lowercase().contains(kw)
                }
            }

            val filterNorm = filter.copy(region = normalizeRegionLocal(filter.region))


            Log.d(
                "SEARCH",
                "REQ(1) page=$filterPage kw=${filterNorm.keyword} region=${filterNorm.region} people=${filterNorm.people}"
            )
            val firstResult = runCatching {
                api.searchGuesthouses(page = filterPage, size = filterSize, body = filterNorm)
            }.getOrElse { e ->
                _state.value = UiState.Error(e.message); return@launch
            }

            if (!firstResult.success || firstResult.data == null) {
                _state.value = UiState.Error(firstResult.message ?: "search failed"); return@launch
            }

            var pageData = firstResult.data!!
            Log.d(
                "SEARCH",
                "RES(1) ok=${firstResult.success} page=${pageData.number} recv=${pageData.content.size} last=${pageData.last}"
            )


            var uiItems = applyClientKeywordFilter(
                pageData.content.toUi(),
                clientKeywordOverride ?: filterNorm.keyword
            )


            val needSecondTry = (filterPage == 1 || filterPage == 0) &&
                    uiItems.isEmpty() &&
                    !filterNorm.keyword.isNullOrBlank() &&
                    (filterNorm.region.isNullOrEmpty())

            if (needSecondTry) {
                val regionFromKw = normalizeRegionLocal(listOf(filterNorm.keyword!!.trim()))
                val filterWithRegion = filterNorm.copy(region = regionFromKw)

                Log.d(
                    "SEARCH",
                    "REQ(2) page=$filterPage kw=${filterWithRegion.keyword} region=${filterWithRegion.region} people=${filterWithRegion.people}"
                )
                val secondResult = runCatching {
                    api.searchGuesthouses(page = filterPage, size = filterSize, body = filterWithRegion)
                }.getOrElse { e ->
                    _state.value = UiState.Error(e.message); return@launch
                }

                if (secondResult.success && secondResult.data != null) {
                    pageData = secondResult.data!!
                    Log.d(
                        "SEARCH",
                        "RES(2) ok=${secondResult.success} page=${pageData.number} recv=${pageData.content.size} last=${pageData.last}"
                    )
                    uiItems = applyClientKeywordFilter(
                        pageData.content.toUi(),
                        clientKeywordOverride ?: filterNorm.keyword
                    )
                }
            }


            val keywordOnly = !filterNorm.keyword.isNullOrBlank()
            if ((filterPage == 1 || filterPage == 0) && uiItems.isEmpty() && keywordOnly) {
                try {
                    val kw = filterNorm.keyword!!.trim()
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
                                time = d.checkInTime.orEmpty(),
                                averageScore = d.averageScore,
                                reviewCount = d.reviewCount
                            )
                        }

                        val filteredFallback = applyClientKeywordFilter(
                            fallbackUi,
                            clientKeywordOverride ?: kw
                        )
                        filteredLoaded += filteredFallback
                        filterIsLast = true
                        _state.value = UiState.Success(filteredLoaded.toList(), filterIsLast)
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e("SEARCH", "FALLBACK error", e)
                }
            }



            filteredLoaded += uiItems
            filterIsLast = pageData.last || uiItems.isEmpty()
            if (!filterIsLast) filterPage += 1

            _state.value = UiState.Success(filteredLoaded.toList(), filterIsLast)
        }
    }
}

package com.example.sumte.guesthouse

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sumte.R
import com.example.sumte.RetrofitClient

class GuestHouseViewModel(
    private val api: GuesthouseApi = RetrofitClient.api
) : ViewModel() {

    // -----------------------------
    // 즐겨찾기(좋아요) 관리
    // -----------------------------
    private val _likedList = MutableLiveData<MutableList<GuestHouse>>(mutableListOf())
    val likedList: LiveData<MutableList<GuestHouse>> = _likedList

    fun addToLiked(guestHouse: GuestHouse) {
        val cur = _likedList.value ?: mutableListOf()
        if (cur.none { it.id == guestHouse.id }) {
            _likedList.value = (cur + guestHouse).toMutableList()
        }
    }

    fun removeFromLiked(guestHouse: GuestHouse) {
        val cur = _likedList.value ?: mutableListOf()
        _likedList.value = cur.filter { it.id != guestHouse.id }.toMutableList()
    }

    fun isLiked(guestHouse: GuestHouse): Boolean =
        _likedList.value?.any { it.id == guestHouse.id } == true

    fun toggleLike(guestHouse: GuestHouse) {
        if (isLiked(guestHouse)) removeFromLiked(guestHouse) else addToLiked(guestHouse)
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

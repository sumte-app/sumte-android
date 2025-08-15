package com.example.sumte.search

import com.example.sumte.guesthouse.GuesthouseApi

class GuesthouseRepository(
    private val api: GuesthouseApi
) {
    suspend fun search(
        page: Int,
        size: Int,
        req: GuesthouseSearchRequest
    ): PageData<GuesthouseItemResponse> {
        val res = api.searchGuesthouses(page, size, req)
        if (!res.success) throw IllegalStateException(res.message ?: "search failed")
        return res.data ?: throw IllegalStateException("empty data")
    }
}
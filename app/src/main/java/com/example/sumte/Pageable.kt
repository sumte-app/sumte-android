package com.example.sumte

data class Pageable(
    val offset: Int,
    val sort: Sort,
    val pageNumber: Int,
    val pageSize: Int,
    val paged: Boolean,
    val unpaged: Boolean
)
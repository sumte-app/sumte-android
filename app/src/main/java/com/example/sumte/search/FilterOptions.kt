package com.example.sumte.search

data class FilterOptions(
    val viewEnableReservation: Boolean? = null,
    val people: Int? = null, // [삭제] 기존 people 필드는 삭제합니다.
    val minPeople: Int? = null,    // [추가] 최소 인원을 저장할 필드
    val maxPeople: Int? = null,    // [추가] 최대 인원을 저장할 필드
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val optionService: List<String>? = null,
    val targetAudience: List<String>? = null,
    val regions: List<String> = emptyList()
)
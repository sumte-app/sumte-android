package com.example.sumte.search

data class FilterOptions(
    val viewEnableReservation: Boolean? = null,
    val people: Int? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val optionService: List<String>? = null,
    val targetAudience: List<String>? = null,
    val regions: List<String> = emptyList()
)
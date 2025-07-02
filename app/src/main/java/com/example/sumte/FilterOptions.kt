package com.example.sumte

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class FilterOptions(
    val availableOnly: Boolean,
    val minPrice: Int,
    val maxPrice: Int,
    val adults: Int,
    val children: Int,
    val services: List<String>,
    val targetGroups: List<String>
)

class FilterViewModel : ViewModel() {
    val filters = MutableLiveData<FilterOptions>()

    fun applyFilters(options: FilterOptions) {
        filters.value = options
    }
}
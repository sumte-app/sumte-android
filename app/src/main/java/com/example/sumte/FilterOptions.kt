package com.example.sumte

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class FilterOptions(
    val availableOnly: Boolean,
    val priceMin: Int,
    val priceMax: Int,
    val peopleCount: String?,
    val selectedServices: List<String>,
    val selectedTargets: List<String>,
    val selectedRegions1: List<String>,
    val selectedRegions2: List<String>,
    val selectedRegions3: List<String>
) : Parcelable

class FilterViewModel : ViewModel() {
    val filters = MutableLiveData<FilterOptions>()

    fun applyFilters(options: FilterOptions) {
        filters.value = options
    }
}
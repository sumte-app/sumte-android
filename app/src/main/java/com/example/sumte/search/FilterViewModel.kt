package com.example.sumte.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FilterViewModel : ViewModel() {
    private val _selected = MutableStateFlow(FilterOptions())
    val selected: StateFlow<FilterOptions> = _selected

    fun save(new: FilterOptions) {
        _selected.value = new
    }

    fun reset() {
        _selected.value = FilterOptions()
    }

    fun replace(new: FilterOptions) { _selected.value = new }
}
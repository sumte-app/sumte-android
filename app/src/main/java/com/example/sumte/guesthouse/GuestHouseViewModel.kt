package com.example.sumte.guesthouse

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GuestHouseViewModel : ViewModel() {
    private val _likedList = MutableLiveData<MutableList<GuestHouse>>(mutableListOf())
    val likedList: LiveData<MutableList<GuestHouse>> = _likedList

    fun addToLiked(guestHouse: GuestHouse) {
        if (!_likedList.value!!.any { it.id == guestHouse.id }) {
            _likedList.value = _likedList.value!!.apply { add(guestHouse) }
        }
    }

    fun removeFromLiked(guestHouse: GuestHouse) {
        _likedList.value = _likedList.value!!.apply { removeIf { it.id == guestHouse.id } }
    }

    fun isLiked(guestHouse: GuestHouse): Boolean {
        return _likedList.value?.any { it.id == guestHouse.id } == true
    }

    fun toggleLike(guestHouse: GuestHouse) {
        val currentList = _likedList.value!!
        if (currentList.any { it.id == guestHouse.id }) {
            _likedList.value = currentList.apply { removeIf { it.id == guestHouse.id } }
        } else {
            _likedList.value = currentList.apply { add(guestHouse) }
        }
    }
}
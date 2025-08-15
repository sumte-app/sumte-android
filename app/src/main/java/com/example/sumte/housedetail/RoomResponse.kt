package com.example.sumte.housedetail

import com.google.gson.annotations.SerializedName

data class RoomResponse<T>(

    val success: Boolean,
    val code: String,
    val message: String,
    @SerializedName("data")
    val data: T
)



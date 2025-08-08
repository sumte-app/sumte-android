package com.example.sumte.housedetail

import com.google.gson.annotations.SerializedName

data class RoomResponse<T>(

    val success: Boolean,
    val code: String,
    val message: String,
    @SerializedName("data")
    val data: T // data 필드의 타입이 고정되지 않고, T에 따라 유연하게 바뀝니다.
)



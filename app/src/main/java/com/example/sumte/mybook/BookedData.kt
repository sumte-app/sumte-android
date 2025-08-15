package com.example.sumte.mybook

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookedData(
    val bookedDate: String,
    // val dayCount: Int, // 전달 안 하므로 주석 처리
    val houseName: String,
    val roomType: String,
    val startDate: String,
    val endDate: String,
    val dateCount: String,
    val adultCount: Int,
    val childCount: Int,
    val roomId: Int,
    val canWriteReview: Boolean,
    val reviewWritten: Boolean
) : Parcelable

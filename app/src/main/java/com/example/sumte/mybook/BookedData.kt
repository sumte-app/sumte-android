package com.example.sumte.mybook

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookedData(
    val reservationId: Int,
    val bookedDate: String,
    val houseName: String,
    val roomType: String,
    val startDate: String,
    val endDate: String,
    val dateCount: String,
    val adultCount: Int,
    val childCount: Int,
    val status: String,
    val canWriteReview: Boolean,
    val reviewWritten: Boolean
)
 : Parcelable

package com.example.sumte.mybook

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookedData(
    val roomImg: String?,
    val reservationId: Int,
    val houseName: String,
    val roomType: String,
    val startDate: String,
    val endDate: String,
    val dateCount: String,
    val adultCount: Int,
    val childCount: Int,
    val status: String,
    val roomId: Long,
    val canWriteReview: Boolean,
    var reviewWritten: Boolean,
    val reservedAt: String
) : Parcelable

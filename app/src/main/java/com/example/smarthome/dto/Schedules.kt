package com.example.smarthome.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Schedules (
    val deviceId: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val active: Boolean = false,
    val scheduleId: String? = null
) : Parcelable
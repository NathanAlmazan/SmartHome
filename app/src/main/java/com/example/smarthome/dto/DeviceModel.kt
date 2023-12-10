package com.example.smarthome.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class DeviceModel(
    val deviceName: String,
    val deviceId: String? = null,
    val devicePass: String? = null,
    val deviceCategory: String? = null,
    val deviceStatus: Boolean = false,
    val controller: Boolean = false,
    val deviceTimer: Boolean = false,
    val deviceSchedule: Boolean = false,
    val outlet: Int = 0,
) : Parcelable
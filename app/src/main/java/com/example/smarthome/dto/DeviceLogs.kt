package com.example.smarthome.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class DeviceLogs(
    val deviceId: String,
    val deviceName: String,
    val opened: String,
    val closed: String,
    val consumed: Float
) : Parcelable

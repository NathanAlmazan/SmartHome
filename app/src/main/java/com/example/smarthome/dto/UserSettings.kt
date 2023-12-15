package com.example.smarthome.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Serializable
@Parcelize
data class UserSettings(
    val costPerWatt: Double = 12.00,
    val maxWattPerDay: Double = 3.00,
    val frequency: String = "Daily"
) : Parcelable

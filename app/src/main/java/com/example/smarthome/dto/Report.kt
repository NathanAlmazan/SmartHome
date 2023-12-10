package com.example.smarthome.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Report (
    val power: Float,
    val current: Float,
    val voltage: Float,
    val energy: Float,
    val frequency: Float,
    val powerFactor: Float,
    val recordedAt: String,
    val consumption: Float,
    val cost: Float
) : Parcelable
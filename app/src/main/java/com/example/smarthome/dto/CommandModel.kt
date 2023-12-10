package com.example.smarthome.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class CommandModel(
    val recipient: String,
    val value: Int,
    val action: String,
    val sender: String? = null
) : Parcelable
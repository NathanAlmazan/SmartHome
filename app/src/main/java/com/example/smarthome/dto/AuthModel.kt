package com.example.smarthome.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class AuthModel(
    val id: String,
    val token: String
) : Parcelable
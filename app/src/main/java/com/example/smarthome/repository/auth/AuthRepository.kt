package com.example.smarthome.repository.auth

import com.example.smarthome.dto.AuthModel
import com.example.smarthome.dto.DeviceModel
import com.example.smarthome.network.Result

interface AuthRepository {
    suspend fun login(deviceId: String): Result<AuthModel>
    suspend fun register(deviceName: String): Result<DeviceModel>

    sealed class Endpoints(val url: String) {
        data object LOGIN : Endpoints("/login")
        data object REGISTRATION : Endpoints("/register")
    }
}
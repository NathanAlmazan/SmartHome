package com.example.smarthome.repository.device

import com.example.smarthome.dto.CommandModel
import com.example.smarthome.dto.DeviceModel
import com.example.smarthome.network.Result
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    suspend fun initializeSession(token: String): Result<Boolean>
    suspend fun sendCommand(command: CommandModel): Result<CommandModel>
    suspend fun updateDevice(device: DeviceModel): Result<DeviceModel>
    suspend fun getAllDevices(): Result<List<DeviceModel>>
    fun observeIncomingCommands(): Flow<CommandModel>

    sealed class Endpoints(val url: String) {
        data object DEVICES : Endpoints("/devices")
        data object DEVICE : Endpoints("/device")
    }
}
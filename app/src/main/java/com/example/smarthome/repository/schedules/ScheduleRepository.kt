package com.example.smarthome.repository.schedules

import com.example.smarthome.dto.Schedules
import com.example.smarthome.network.Result

interface ScheduleRepository {
    suspend fun addSchedule(schedule: Schedules): Result<Schedules>
    suspend fun removeSchedule(deviceId: String): Result<Schedules>
    suspend fun getDeviceSchedule(deviceId: String):  Result<Schedules>

    sealed class Endpoints(val url: String) {
        data object SCHEDULE : Endpoints("/schedule")
    }
}
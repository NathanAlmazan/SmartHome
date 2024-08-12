package com.example.smarthome.repository.report

import com.example.smarthome.dto.DeviceLogs
import com.example.smarthome.dto.History
import com.example.smarthome.dto.Report
import com.example.smarthome.dto.Summary
import com.example.smarthome.dto.UserSettings
import com.example.smarthome.network.Result
import java.util.Date

interface ReportRepository {
    suspend fun getHistory(scope: String): Result<List<History>>
    suspend fun getEnergyReport(datetime: Date): Result<Report>
    suspend fun getEnergySummary(): Result<List<Summary>>
    suspend fun getCostPerWatt(): Result<UserSettings>
    suspend fun getDeviceConsumed(deviceId: String): Result<List<DeviceLogs>>
    suspend fun updateCostPerWatt(settings: UserSettings): Result<UserSettings>

    sealed class Endpoints(val url: String) {
        data object ENERGY : Endpoints("/energy")
        data object SETTINGS : Endpoints("/cost")
        data object HISTORY : Endpoints("/history")
        data object LOGS : Endpoints("/consumption")
    }
}
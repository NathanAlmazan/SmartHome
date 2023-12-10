package com.example.smarthome.repository.report

import com.example.smarthome.dto.Report
import com.example.smarthome.dto.Summary
import com.example.smarthome.network.Result
import java.util.Date

interface ReportRepository {
    suspend fun getEnergyReport(datetime: Date): Result<Report>
    suspend fun getEnergySummary(): Result<List<Summary>>

    sealed class Endpoints(val url: String) {
        data object ENERGY : Endpoints("/energy")
    }
}
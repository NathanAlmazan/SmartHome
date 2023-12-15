package com.example.smarthome.repository.report

import com.example.smarthome.dto.Report
import com.example.smarthome.dto.Summary
import com.example.smarthome.dto.UserSettings
import com.example.smarthome.network.FormattedException
import com.example.smarthome.network.RestApiBuilder
import com.example.smarthome.network.Result
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.util.Date

class ReportRepositoryImpl(private val client: RestApiBuilder): ReportRepository {
    override suspend fun getEnergyReport(datetime: Date): Result<Report> {
        return try {
            Result.Success(client.api.get(ReportRepository.Endpoints.ENERGY.url + "/" + datetime.time).body())
        } catch (exception: FormattedException) {
            Result.Error(exception.formattedErrorMessage)
        } catch (exception: Exception) {
            Result.Error("Server or network error")
        }
    }

    override suspend fun getEnergySummary(): Result<List<Summary>> {
        return try {
            Result.Success(client.api.get(ReportRepository.Endpoints.ENERGY.url).body())
        } catch (exception: FormattedException) {
            Result.Error(exception.formattedErrorMessage)
        } catch (exception: Exception) {
            Result.Error("Server or network error")
        }
    }

    override suspend fun getCostPerWatt(): Result<UserSettings> {
        return try {
            Result.Success(client.api.get(ReportRepository.Endpoints.SETTINGS.url).body())
        } catch (exception: FormattedException) {
            Result.Error(exception.formattedErrorMessage)
        } catch (exception: Exception) {
            Result.Error("Server or network error")
        }
    }

    override suspend fun updateCostPerWatt(settings: UserSettings): Result<UserSettings> {
        return try {
            Result.Success(client.api.post(ReportRepository.Endpoints.SETTINGS.url) {
                contentType(ContentType.Application.Json)
                setBody(settings)
            }.body())
        } catch (exception: FormattedException) {
            Result.Error(exception.formattedErrorMessage)
        } catch (exception: Exception) {
            Result.Error("Server or network error")
        }
    }


}
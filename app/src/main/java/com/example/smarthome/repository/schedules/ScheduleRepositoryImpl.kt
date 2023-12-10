package com.example.smarthome.repository.schedules

import com.example.smarthome.dto.Schedules
import com.example.smarthome.network.FormattedException
import com.example.smarthome.network.RestApiBuilder
import com.example.smarthome.network.Result
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ScheduleRepositoryImpl(private val client: RestApiBuilder): ScheduleRepository {
    override suspend fun addSchedule(schedule: Schedules): Result<Schedules> {
        return try {
            Result.Success(client.api.post(ScheduleRepository.Endpoints.SCHEDULE.url + "/" + schedule.deviceId) {
                contentType(ContentType.Application.Json)
                setBody(schedule)
            }.body())
        } catch (exception: FormattedException) {
            Result.Error(exception.formattedErrorMessage)
        } catch (exception: Exception) {
            Result.Error("Server or network error")
        }
    }

    override suspend fun removeSchedule(deviceId: String): Result<Schedules> {
        return try {
            Result.Success(client.api.get(ScheduleRepository.Endpoints.SCHEDULE.url + "/" + deviceId + "/stop").body())
        } catch (exception: FormattedException) {
            Result.Error(exception.formattedErrorMessage)
        } catch (exception: Exception) {
            Result.Error("Server or network error")
        }
    }

    override suspend fun getDeviceSchedule(deviceId: String): Result<Schedules> {
        return try {
            Result.Success(client.api.get(ScheduleRepository.Endpoints.SCHEDULE.url + "/" + deviceId).body())
        } catch (exception: FormattedException) {
            Result.Error(exception.formattedErrorMessage)
        } catch (exception: Exception) {
            Result.Error("Server or network error")
        }
    }
}
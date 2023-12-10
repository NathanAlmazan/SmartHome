package com.example.smarthome.repository.auth

import com.example.smarthome.dto.AuthModel
import com.example.smarthome.dto.DeviceModel
import com.example.smarthome.network.FormattedException
import com.example.smarthome.network.RestApiBuilder
import com.example.smarthome.network.Result
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthRepositoryImpl(private val client: RestApiBuilder): AuthRepository {
    override suspend fun login(deviceId: String): Result<AuthModel> {
        return try {
            Result.Success(client.api.get(AuthRepository.Endpoints.LOGIN.url + "/" + deviceId).body())
        } catch (exception: FormattedException) {
            Result.Error(exception.formattedErrorMessage)
        } catch (exception: Exception) {
            Result.Error("Server or network error")
        }
    }

    override suspend fun register(deviceName: String): Result<DeviceModel> {
        return try {
            Result.Success(client.api.post(AuthRepository.Endpoints.REGISTRATION.url) {
                contentType(ContentType.Application.Json)
                setBody(DeviceModel(deviceName))
            }.body())
        } catch (exception: FormattedException) {
            Result.Error(exception.formattedErrorMessage)
        } catch (exception: Exception) {
            Result.Error("Server or network error")
        }
    }
}
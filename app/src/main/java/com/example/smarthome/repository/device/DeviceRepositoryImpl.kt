package com.example.smarthome.repository.device

import android.util.Log
import com.example.smarthome.dto.CommandModel
import com.example.smarthome.dto.DeviceModel
import com.example.smarthome.network.ApiBuilder
import com.example.smarthome.network.FormattedException
import com.example.smarthome.network.RestApiBuilder
import com.example.smarthome.network.Result
import com.example.smarthome.network.WebSocketBuilder
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DeviceRepositoryImpl(
    private val wsClient: WebSocketBuilder,
    private val httpClient: RestApiBuilder
): DeviceRepository {
    private var socket: WebSocketSession? = null

    override suspend fun initializeSession(token: String): Result<Boolean> {
        return try {
            socket = wsClient.socket.webSocketSession {
                header(ApiBuilder.AUTHORIZATION, token)
            }

            Result.Success(socket?.isActive == true)
        } catch (exception: FormattedException) {
            Result.Error(exception.formattedErrorMessage)
        } catch (exception: Exception) {
            Result.Error("Server or network error")
        }
    }

    override suspend fun sendCommand(command: CommandModel): Result<CommandModel> {
        return try {
            val request = Json.encodeToString(command)
            socket?.send(Frame.Text(request))

            Log.d("WSS Status", if (socket != null) "Sent Request" else "Failed to Send.")
            Log.d("Command", request)
            Result.Success(command)
        } catch (exception: Exception) {
            Result.Error(exception.message ?: "Unknown Error")
        }
    }

    override suspend fun updateDevice(device: DeviceModel): Result<DeviceModel> {
        return try {
            Result.Success(httpClient.api.post(DeviceRepository.Endpoints.DEVICE.url + "/" + device.deviceId){
                contentType(ContentType.Application.Json)
                setBody(device)
            }.body())
        } catch (exception: FormattedException) {
            Result.Error(exception.formattedErrorMessage)
        } catch (exception: Exception) {
            Result.Error("Server or network error")
        }
    }

    override suspend fun getAllDevices(): Result<List<DeviceModel>> {
        return try {
            Result.Success(httpClient.api.get(DeviceRepository.Endpoints.DEVICES.url).body())
        } catch (exception: FormattedException) {
            Result.Error(exception.formattedErrorMessage)
        } catch (exception: Exception) {
            Result.Error("Server or network error")
        }
    }

    override fun observeIncomingCommands(): Flow<CommandModel> {
        return try {
            socket?.incoming?.receiveAsFlow()?.filter { it is Frame.Text }
                ?.map {
                    val json = (it as? Frame.Text)?.readText() ?: ""
                    Json.decodeFromString(json)
                } ?: flow { }
        } catch (ex: Exception) {
            flow { }
        }
    }
}
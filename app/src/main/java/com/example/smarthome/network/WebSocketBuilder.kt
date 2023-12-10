package com.example.smarthome.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

class WebSocketBuilder : ApiBuilder() {
    private val BASE_URL = "wss://automation.nat911.com"

    val socket: HttpClient = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.ALL
        }

        install(ContentNegotiation) {
            json()
        }

        defaultRequest {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
        }
        install(WebSockets)
    }
}
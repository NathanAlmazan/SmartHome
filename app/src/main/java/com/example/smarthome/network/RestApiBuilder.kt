package com.example.smarthome.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json

class RestApiBuilder : ApiBuilder() {
    private val BASE_URL = "https://automation.nat911.com"

    val api: HttpClient = HttpClient(Android) {
        install(Logging) {
            level = LogLevel.ALL
        }
        expectSuccess = true

        install(ContentNegotiation) {
            json()
        }

        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, _ ->
                throw FormattedException(exception.localizedMessage ?: "Unknown Error")
            }
        }

        defaultRequest {
            url(BASE_URL)
        }
    }
}
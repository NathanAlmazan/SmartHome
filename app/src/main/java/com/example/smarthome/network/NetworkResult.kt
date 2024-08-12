package com.example.smarthome.network

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: String) : Result<Nothing>()
}

class FormattedException(text: String) : IllegalStateException(text) {
    var formattedErrorMessage: String = try {
        val split = text.split("Text: \"")
        split[1].replace("\"", "")
    } catch (ex: Exception) {
        text
    }
}
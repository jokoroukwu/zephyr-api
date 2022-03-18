package io.github.jokoroukwu.zephyrapi.http

import kotlinx.serialization.json.Json

object JsonMapper {
    val instance = Json {
        ignoreUnknownKeys = true
    }
}
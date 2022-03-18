package io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer

import kotlinx.serialization.Serializable

@Serializable
data class SerializableTestResult(
    val id: Long,
    val testResultStatusId: Long,
    val executionTime: Long,
    val comment: String = ""
)
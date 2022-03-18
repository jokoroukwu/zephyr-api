package io.github.jokoroukwu.zephyrapi.publication.testcyclecreator

import io.github.jokoroukwu.zephyrapi.publication.testcycleupdater.TestRunItem
import kotlinx.serialization.Serializable

@Serializable
data class CreateTestCycleResponse(
    val id: Long,
    val key: String,
    val serializableTestRunItems: List<TestRunItem> = emptyList()
)
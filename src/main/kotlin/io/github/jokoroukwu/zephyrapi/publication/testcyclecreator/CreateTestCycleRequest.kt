package io.github.jokoroukwu.zephyrapi.publication.testcyclecreator

import kotlinx.serialization.Serializable

@Serializable
data class CreateTestCycleRequest(
    val projectId: Long,
    val name: String,
    val plannedStartDate: String?,
    val plannedEndDate: String?
)
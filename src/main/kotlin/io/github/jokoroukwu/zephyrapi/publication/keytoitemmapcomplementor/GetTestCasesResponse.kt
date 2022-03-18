package io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor

import kotlinx.serialization.Serializable

@Serializable
data class GetTestCasesResponse(
    val total: Int,
    val startAt: Int,
    val maxResults: Int,
    val results: List<TestCaseItem> = emptyList()
)
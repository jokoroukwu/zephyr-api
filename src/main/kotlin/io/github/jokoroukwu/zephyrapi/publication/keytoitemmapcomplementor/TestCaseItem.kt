package io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor

import kotlinx.serialization.Serializable

@Serializable
data class TestCaseItem(
    val id: Long,
    val key: String,
    val projectId: Long,
    val testData: List<TestDataItem>,
    val testScript: TestScriptItem
)
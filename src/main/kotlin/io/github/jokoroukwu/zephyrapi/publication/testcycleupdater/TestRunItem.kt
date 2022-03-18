package io.github.jokoroukwu.zephyrapi.publication.testcycleupdater

import kotlinx.serialization.Serializable

@Serializable
class TestRunItem(
    val index: Int,
    val lastTestResult: UpdateTestCycleTestResult,
    val id: Int? = null
)
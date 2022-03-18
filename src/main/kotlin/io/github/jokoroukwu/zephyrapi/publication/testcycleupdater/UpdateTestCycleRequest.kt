package io.github.jokoroukwu.zephyrapi.publication.testcycleupdater

import kotlinx.serialization.Serializable

@Serializable
class UpdateTestCycleRequest(
    val testRunId: Long,
    val addedTestRunItems: List<TestRunItem>
)
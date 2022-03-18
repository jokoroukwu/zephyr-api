package io.github.jokoroukwu.zephyrapi.publication

data class ZephyrTestResult(
    val testCaseId: Long = -1,
    override val testCaseKey: String = "",
    override val isSuccess: Boolean = true,
    override val testDataResults: List<TestDataResult> = emptyList(),
    override val startTime: Long,
    override val endTime: Long
) : TestResult



package io.github.jokoroukwu.zephyrapi.publication

data class ZephyrTestCycle(
    val id: Long = -1,
    val key: String = "",
    override val name: String = "Anonymous Test Cycle",
    override val testResults: List<ZephyrTestResult> = emptyList(),
    override val startTime: Long,
    override val endTime: Long
) : TestRun

package io.github.jokoroukwu.zephyrapi.publication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("testRun")
data class TestRunBase(
    override val name: String,
    override val testResults: List<TestResultBase>,
    override val startTime: Long,
    override val endTime: Long,
) : TestRun {

    constructor(name: String, testResults: List<TestResultBase>) : this(
        name,
        testResults,
        testResults.minOf(TestResult::startTime),
        testResults.maxOf(TestResult::endTime)
    )

}
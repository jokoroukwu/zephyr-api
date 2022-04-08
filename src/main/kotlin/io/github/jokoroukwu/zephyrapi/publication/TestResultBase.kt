package io.github.jokoroukwu.zephyrapi.publication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("testResult")
data class TestResultBase(
    override val testCaseKey: String,
    override val testDataResults: List<TestDataResultBase>,
    override val startTime: Long,
    override val endTime: Long,
    override val isSuccess: Boolean
) : TestResult {

    constructor(testCaseKey: String, testDataResults: List<TestDataResultBase>, startTime: Long, endTime: Long)
            : this(testCaseKey, testDataResults, startTime, endTime, testDataResults.all(TestDataResult::isSuccess))

}
package io.github.jokoroukwu.zephyrapi.publication

interface TestResult : DurationRange {

    val testCaseKey: String

    val testDataResults: List<TestDataResult>

    val isSuccess: Boolean
}
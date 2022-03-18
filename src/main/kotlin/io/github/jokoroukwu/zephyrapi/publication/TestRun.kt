package io.github.jokoroukwu.zephyrapi.publication

interface TestRun : DurationRange {

    val name: String

    val testResults: List<TestResult>

}
package io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor

data class ProcessedReportTestResult(
    val id: Long,
    val testCase: TestCase,
    val dataSets: StepGroups
)
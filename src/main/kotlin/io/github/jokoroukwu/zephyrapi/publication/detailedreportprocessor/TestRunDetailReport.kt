package io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor

import kotlinx.serialization.Serializable

@Serializable
class TestRunDetailReport(
    val testResults: List<ReportTestResult>
)
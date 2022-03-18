package io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor

import kotlinx.serialization.Serializable

@Serializable
class GetDetailedReportResponse(
    val testRunsDetailReports: List<TestRunDetailReport>
)
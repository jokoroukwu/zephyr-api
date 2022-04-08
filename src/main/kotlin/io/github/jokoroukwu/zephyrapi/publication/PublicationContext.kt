package io.github.jokoroukwu.zephyrapi.publication

import io.github.jokoroukwu.zephyrapi.config.ZephyrConfig
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.ReportTestResult
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.TestScriptResult
import io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor.TestCaseItem
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.SerializableTestResult
import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.TestResultStatus
import java.util.*

typealias StatusMap = Map<TestResultStatus, Long>

data class PublicationContext(
    val zephyrConfig: ZephyrConfig,
    val projectId: Long = -1,
    val testCycles: Collection<ZephyrTestCycle> = emptyList(),
    val statusMap: StatusMap = emptyMap(),
    val testCaseIdToReportTestResultsMap: Map<Long, Queue<ReportTestResult>> = emptyMap(),
    val testCaseKeyToItemMap: Map<String, TestCaseItem> = emptyMap(),
    val finalizedSteps: List<TestScriptResult> = emptyList(),
    val finalizedTestResults: List<SerializableTestResult> = emptyList()
)

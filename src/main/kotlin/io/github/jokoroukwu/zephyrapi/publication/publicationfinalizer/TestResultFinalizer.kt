package io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer

import io.github.jokoroukwu.zephyrapi.publication.StatusMap
import io.github.jokoroukwu.zephyrapi.publication.TestResult
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.ReportTestResult

interface TestResultFinalizer {

    fun finalizeTestResult(
        reportTestResult: ReportTestResult,
        testResult: TestResult,
        statusMap: StatusMap
    ): TestResultFinalization
}
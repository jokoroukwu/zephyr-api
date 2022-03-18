package io.github.jokoroukwu.zephyrapi.publication.testcyclefilter

import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestResult
import io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor.TestCaseItem

interface TestResultFilter {

    fun filterTestResult(zephyrTestResult: ZephyrTestResult, testCaseItem: TestCaseItem): FilteredResult
}
package io.github.jokoroukwu.zephyrapi.publication.testcyclefilter

import io.github.jokoroukwu.zephyrapi.publication.TestDataResult
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestResult
import io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor.TestCaseItem
import java.util.*
import kotlin.collections.ArrayList

class OutOfBoundsStepFilter : TestResultFilter {

    override fun filterTestResult(zephyrTestResult: ZephyrTestResult, testCaseItem: TestCaseItem): FilteredResult {
        val issues = LinkedList<String>()
        val stepsCount = testCaseItem.testScript.steps.size
        return zephyrTestResult
            .testDataResults
            .filter(stepsCount) {
                issues.add(
                    "failed step out of bounds: {" +
                            "test_data_index: ${it.index}, " +
                            "step: ${it.failedStepIndex!!.inc()}, " +
                            "actual_number_of_steps: $stepsCount}"
                )
            }.takeUnless(List<TestDataResult>::isEmpty)
            ?.let { FilteredResult(testResult = zephyrTestResult.copy(testDataResults = it), issues = issues) }
            ?: FilteredResult(testResult = null, issues = issues)
    }

    private inline fun List<TestDataResult>.filter(
        actualStepsCount: Int,
        onIgnore: (TestDataResult) -> Unit
    ): List<TestDataResult> {
        return ArrayList<TestDataResult>(size).also {
            forEach { dataSet ->
                val failedStepIndex = dataSet.failedStepIndex
                when {
                    failedStepIndex == null -> it.add(dataSet)
                    failedStepIndex < actualStepsCount -> it.add(dataSet)
                    else -> onIgnore(dataSet)
                }
            }
        }
    }
}

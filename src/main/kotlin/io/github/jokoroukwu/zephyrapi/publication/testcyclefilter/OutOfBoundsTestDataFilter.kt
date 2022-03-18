package io.github.jokoroukwu.zephyrapi.publication.testcyclefilter

import io.github.jokoroukwu.zephyrapi.publication.TestDataResult
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestResult
import io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor.TestCaseItem
import java.util.*

class OutOfBoundsTestDataFilter(
    private val nextFilter: TestResultFilter = OutOfBoundsStepFilter()
) : TestResultFilter {

    override fun filterTestResult(zephyrTestResult: ZephyrTestResult, testCaseItem: TestCaseItem): FilteredResult {
        return if (zephyrTestResult.isNotDataDriven()) {
            nextFilter.filterTestResult(zephyrTestResult, testCaseItem)
        } else {
            with(zephyrTestResult.testDataResults) {
                val problems = LinkedList<String>()
                filter(testCaseItem.testData.size) { problems.add("test data index $it is out of bounds") }
                    .takeUnless(List<TestDataResult>::isEmpty)
                    ?.let { zephyrTestResult.copy(testDataResults = it) }
                    ?.let { testResult ->
                        nextFilter.filterTestResult(testResult, testCaseItem)
                            .apply { mergeProblems(problems) }
                    }
                    ?: FilteredResult(testResult = null, issues = problems)

            }
        }
    }

    private fun FilteredResult.mergeProblems(issues: List<String>) =
        if (issues.isNotEmpty()) copy(issues = issues + this.issues) else this

    private inline fun List<TestDataResult>.filter(
        fetchedDataSetCount: Int,
        onIgnore: (Int) -> Unit
    ): List<TestDataResult> {
        return if (size <= fetchedDataSetCount) {
            this
        } else {
            (fetchedDataSetCount until size).forEach { i -> onIgnore(get(i).index) }
            subList(0, fetchedDataSetCount)
        }
    }

    private fun ZephyrTestResult.isNotDataDriven() = testDataResults.size <= 1
}
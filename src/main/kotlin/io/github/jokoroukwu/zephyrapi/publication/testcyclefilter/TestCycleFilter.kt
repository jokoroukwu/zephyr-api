package io.github.jokoroukwu.zephyrapi.publication.testcyclefilter

import io.github.jokoroukwu.zephyrapi.publication.PublicationData
import io.github.jokoroukwu.zephyrapi.publication.PublicationDataProcessor
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestCycle
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestResult
import io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor.TestCaseItem
import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.TestResultStatusComplementor
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class TestCycleFilter(
    private val testResultFilter: TestResultFilter = OutOfBoundsTestDataFilter(),
    private val nextProcessor: PublicationDataProcessor = TestResultStatusComplementor()
) : PublicationDataProcessor {

    override fun process(publicationData: PublicationData): Boolean {
        return publicationData.testCycles.takeUnless(Collection<ZephyrTestCycle>::isEmpty)
            ?.run {
                ArrayList<ZephyrTestCycle>(size).also {
                    forEach { cycle ->
                        cycle.filterNotEmpty()
                            ?.filterResults(publicationData.testCaseKeyToItemMap)
                            ?.filterNotEmpty()
                            ?.let(it::add)
                    }
                }
            }
            ?.takeUnless(List<ZephyrTestCycle>::isEmpty)
            ?.let { publicationData.copy(testCycles = it) }
            ?.let(nextProcessor::process)
            ?: false.also { logger.info { "No test cycles to create" } }
    }


    private fun ZephyrTestCycle.filterNotEmpty(): ZephyrTestCycle? =
        if (testResults.isEmpty()) {
            logger.warn { "Test cycle '$name' will be ignored: no test results to publish" }
            null
        } else {
            this
        }

    private fun ZephyrTestCycle.filterResults(testCaseKeyToItemMap: Map<String, TestCaseItem>): ZephyrTestCycle {
        val filteredResults = ArrayList<ZephyrTestResult>(testResults.size)
        testResults.forEach { result ->
            testCaseKeyToItemMap[result.testCaseKey]
                ?.let { item -> testResultFilter.filterTestResult(result.copy(testCaseId = item.id), item) }
                ?.also { filteredResult -> filteredResult.testResult?.also(filteredResults::add) }
                ?.logIssues(result.testCaseKey)
                ?: logger.warn { "Test result '${result.testCaseKey}' will be ignored: test case does not exist in JIRA server" }
        }
        return copy(testResults = filteredResults)
    }

    private fun FilteredResult.logIssues(testCaseKey: String) {
        if (issues.isNotEmpty()) {
            val newLine = System.lineSeparator()
            logger.warn {
                "Test result '$testCaseKey' has the following issues: $newLine\t[${problemsToString(issues)}$newLine\t]"
            }
        }
    }


    private fun problemsToString(problems: Collection<String>) =
        problems.joinToString(
            prefix = System.lineSeparator() + "\t\t",
            separator = ",${System.lineSeparator()}\t\t"
        )

}

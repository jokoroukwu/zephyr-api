package io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer


import io.github.jokoroukwu.zephyrapi.publication.PublicationContext
import io.github.jokoroukwu.zephyrapi.publication.PublicationDataProcessor
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.TestScriptResult
import io.github.jokoroukwu.zephyrapi.publication.testresultupdater.TestResultUpdater
import mu.KotlinLogging
import java.util.*
import kotlin.collections.ArrayList

private val logger = KotlinLogging.logger { }

class PublicationDataFinalizer(
    private val nextProcessor: PublicationDataProcessor = TestResultUpdater(),
    private val testResultFinalizer: TestResultFinalizer = TestResultFinalizerImpl()
) : PublicationDataProcessor {

    override fun process(publicationContext: PublicationContext): Boolean {
        val testCaseIdToTestResults = publicationContext.testCaseIdToReportTestResultsMap
        val finalizedTestResults = ArrayList<SerializableTestResult>(testCaseIdToTestResults.size)
        val finalizedSteps = LinkedList<TestScriptResult>()
        for (testCycle in publicationContext.testCycles) {
            for (testResult in testCycle.testResults) {
                testCaseIdToTestResults[testResult.testCaseId]
                    ?.poll()
                    ?.let { testResultFinalizer.finalizeTestResult(it, testResult, publicationContext.statusMap) }
                    ?.also { finalizedSteps.addAll(it.finalizedSteps) }
                    ?.also { finalizedTestResults.add(it.finalizedResult) }
                    ?: logger.warn {
                        "{test_cycle_key: ${testCycle.key}, test_case_key: ${testResult.testCaseKey}," +
                                " warning: unable to update test result: test result does not exist in JIRA server:" +
                                " this may only happen if a third party had deleted test result after its creation" +
                                " but before it was updated}"
                    }
            }
        }
        return nextProcessor.process(
            publicationContext.copy(finalizedSteps = finalizedSteps, finalizedTestResults = finalizedTestResults)
        )
    }
}





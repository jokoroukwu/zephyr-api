package io.github.jokoroukwu.zephyrapi.publication.testcycleupdater

import io.github.jokoroukwu.zephyrapi.http.SupervisorIOContext
import io.github.jokoroukwu.zephyrapi.publication.PublicationData
import io.github.jokoroukwu.zephyrapi.publication.PublicationDataProcessor
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestCycle
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.DetailedReportProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

class TestCycleUpdater(
    private val updateTestCycleRequestSender: UpdateTestCycleRequestSender = UpdateTestCycleRequestSender(),
    private val nextProcessor: PublicationDataProcessor = DetailedReportProcessor()
) : PublicationDataProcessor {

    /**
     * Updates previously created test cycles by populating them with [ZephyrTestCycle.testResults].
     */
    override fun process(publicationData: PublicationData): Boolean {
        return publicationData.testCycles.takeUnless(Collection<ZephyrTestCycle>::isEmpty)
            ?.let(::asyncUpdateTestCycles)
            ?.let { nextProcessor.process(publicationData) }
            ?: false.also { logger.info { "No test cycles to update" } }
    }

    private fun asyncUpdateTestCycles(testCycles: Collection<ZephyrTestCycle>) = runBlocking {
        launch(SupervisorIOContext) {
            for (testCycle in testCycles) {
                doUpdateTestCycle(testCycle)
            }
        }.join()
    }

    private fun CoroutineScope.doUpdateTestCycle(testCycle: ZephyrTestCycle) =
        launch {
            //  initialized lazily
            var testCaseKeys: Collection<String> = emptyList()
            logger.debug {
                "Adding test cases to test cycle: {name: '%s', key: '%s', test_cases: %s}".format(
                    testCycle.name,
                    testCycle.key,
                    testCycle.testCaseKeys().also { testCaseKeys = it }
                )
            }
            updateTestCycleRequestSender.updateTestCycle(testCycle)
            logger.debug {
                "Test cases successfully added: test_cycle: {name: '%s', key: '%s', test_cases: %s}".format(
                    testCycle.name,
                    testCycle.key,
                    testCaseKeys
                )
            }
        }

    private fun ZephyrTestCycle.testCaseKeys(): Collection<String> {
        return testResults.mapTo(ArrayList(testResults.size), io.github.jokoroukwu.zephyrapi.publication.TestResult::testCaseKey)
    }

}

private val logger = KotlinLogging.logger { }
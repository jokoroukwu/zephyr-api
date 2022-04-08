package io.github.jokoroukwu.zephyrapi

import io.github.jokoroukwu.zephyrapi.config.ZephyrConfig
import io.github.jokoroukwu.zephyrapi.publication.*
import io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor.TestCaseItemComplementor
import mu.KotlinLogging
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger { }

/**
 * The entrypoint to Zephyr for JIRA server API
 */
open class ZephyrClient(private val publicationDataProcessor: PublicationDataProcessor = TestCaseItemComplementor()) {

    companion object Default : ZephyrClient()

    fun publishTestResults(testRuns: Collection<TestRun>, zephyrConfig: ZephyrConfig) {
        val publishedSuccessfully: Boolean
        val time = measureTimeMillis {
            val publication = PublicationContext(zephyrConfig = zephyrConfig, testCycles = testRuns.toZephyrTestCycles())
            publishedSuccessfully = publicationDataProcessor.process(publication)
        }
        if (publishedSuccessfully) {
            logger.info {
                "Test results published in ${time.toDouble().div(1000)} seconds"
            }
        }
    }

    private fun Collection<TestRun>.toZephyrTestCycles() =
        mapTo(ArrayList(size)) {
            ZephyrTestCycle(
                name = it.name,
                testResults = it.testResults.toZephyrTestResults(),
                startTime = it.startTime,
                endTime = it.endTime,
            )
        }

    private fun Collection<TestResult>.toZephyrTestResults() =
        mapTo(ArrayList(size)) {
            ZephyrTestResult(
                testCaseKey = it.testCaseKey,
                startTime = it.startTime,
                endTime = it.endTime,
                isSuccess = it.isSuccess,
                testDataResults = it.testDataResults,
            )
        }
}

package io.github.jokoroukwu.zephyrapi.publication.testresultupdater

import io.github.jokoroukwu.zephyrapi.publication.PublicationData
import io.github.jokoroukwu.zephyrapi.publication.PublicationDataProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class TestResultUpdater(
    private val updateTestResultsRequestSender: UpdateTestResultsRequestSender = UpdateTestResultsRequestSender(),
    private val updateTestScriptResultsSender: UpdateTestScriptResultsRequestSender = UpdateTestScriptResultsRequestSender()
) : PublicationDataProcessor {

    override fun process(publicationData: PublicationData): Boolean {
        if (publicationData.finalizedTestResults.isEmpty()) {
            logger.info { "No test results to update" }
            return false;
        }
        runBlocking {
            launch(Dispatchers.IO) {
                updateTestResultsRequestSender.updateTestResults(publicationData.finalizedTestResults)
            }
            launch(Dispatchers.IO) {
                updateTestScriptResultsSender.updateTestScriptResults(publicationData.finalizedSteps)
            }
        }
        return true
    }
}
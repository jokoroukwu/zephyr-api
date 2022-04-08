package io.github.jokoroukwu.zephyrapi.publication.testresultupdater

import io.github.jokoroukwu.zephyrapi.publication.PublicationContext
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

    override fun process(publicationContext: PublicationContext): Boolean {
        if (publicationContext.finalizedTestResults.isEmpty()) {
            logger.info { "No test results to update" }
            return false;
        }
        with(publicationContext) {
            runBlocking {
                launch(Dispatchers.IO) {
                    updateTestResultsRequestSender.updateTestResults(finalizedTestResults, zephyrConfig)
                }
                launch(Dispatchers.IO) {
                    updateTestScriptResultsSender.updateTestScriptResults(finalizedSteps, zephyrConfig)
                }
            }
        }
        return true
    }
}
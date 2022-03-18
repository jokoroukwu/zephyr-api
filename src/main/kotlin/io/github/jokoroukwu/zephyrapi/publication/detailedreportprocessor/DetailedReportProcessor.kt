package io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor

import io.github.jokoroukwu.zephyrapi.api.*
import io.github.jokoroukwu.zephyrapi.publication.PublicationData
import io.github.jokoroukwu.zephyrapi.publication.PublicationDataProcessor
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestCycle
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.PublicationDataFinalizer
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger { }

class DetailedReportProcessor(
    private val getDetailedReportSender: GetDetailedReportSender = GetDetailedReportSender(),
    private val nextProcessor: PublicationDataProcessor = PublicationDataFinalizer()
) : PublicationDataProcessor {

    override fun process(publicationData: PublicationData): Boolean {
        return fetchTestRunReports(publicationData)
            .takeUnless(List<TestRunDetailReport>::isEmpty)
            ?.let(::groupReportTestResultsByTestCaseId)
            ?.let { publicationData.copy(testCaseIdToReportTestResultsMap = it) }
            ?.let(nextProcessor::process)
            ?: false.also { logger.info { "Fetched detailed report was empty" } }
    }


    private fun fetchTestRunReports(publicationData: PublicationData): List<TestRunDetailReport> {
        //  initialized lazily
        var testCyclesString = ""
        logger.debug {
            testCyclesString = publicationData.testCycles.formatToString()
            "Fetching detailed report for test cycles: $testCyclesString"
        }

        return getDetailedReportSender
            .getDetailedReport(publicationData)
            .testRunsDetailReports
            .also {
                logger.debug { "Detailed report successfully fetched for test cycles: $testCyclesString" }
            }
    }

    private fun groupReportTestResultsByTestCaseId(testRunDetailReports: Collection<TestRunDetailReport>): Map<Long, Queue<ReportTestResult>>? {
        val numberOfTestResults = testRunDetailReports.sumOf { it.testResults.size }
        return testRunDetailReports
            .takeIf { numberOfTestResults > 0 }
            ?.flatMapTo(ArrayList(numberOfTestResults), TestRunDetailReport::testResults)
            ?.groupingBy { it.testCase.id }
            ?.aggregateTo(HashMap(numberOfTestResults, 1F))
            { _, queue, value, _ -> (queue ?: LinkedList()).also { it.add(value) } }
    }

    private fun Collection<ZephyrTestCycle>.formatToString() = joinToString(
        prefix = "[{",
        postfix = "}]",
        separator = "} , {",
        transform = { "name: ${it.name}, key: ${it.key}" }
    )
}

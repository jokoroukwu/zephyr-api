package io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor

import io.github.jokoroukwu.zephyrapi.publication.PublicationContext
import io.github.jokoroukwu.zephyrapi.publication.PublicationDataProcessor
import io.github.jokoroukwu.zephyrapi.publication.TestResult
import io.github.jokoroukwu.zephyrapi.publication.TestRun
import io.github.jokoroukwu.zephyrapi.publication.testcyclefilter.TestCycleFilter
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger { }

class TestCaseItemComplementor(
    private val getTestCasesRequestSender: GetTestCasesRequestSender = GetTestCasesRequestSender(),
    private val nextProcessor: PublicationDataProcessor = TestCycleFilter(),
) : PublicationDataProcessor {


    /**
     * Fetches test case items, which are required for further processing,
     * from JIRA to complement [PublicationContext].
     */
    override fun process(publicationContext: PublicationContext): Boolean {
        var data = publicationContext
        return publicationContext
            .testCycles
            .testCasesKeys()
            .takeUnless(Set<String>::isEmpty)
            ?.let { getTestCasesRequestSender.requestTestCases(it, publicationContext.zephyrConfig) }
            ?.let(GetTestCasesResponse::results)
            //  may still have fetched zero items
            ?.takeUnless(List<TestCaseItem>::isEmpty)
            ?.also { data = data.copy(projectId = it[0].projectId) }
            ?.let(::mapTestCaseKeyToTestCaseItem)
            ?.let { data.copy(testCaseKeyToItemMap = it) }
            ?.let(nextProcessor::process)
            ?: false.also { logger.info { "No test case keys to fetch" } }
    }

    private fun mapTestCaseKeyToTestCaseItem(testCaseItems: List<TestCaseItem>): Map<String, TestCaseItem> =
        testCaseItems.associateByTo(HashMap(testCaseItems.size, 1F), TestCaseItem::key)


    private fun Collection<TestRun>.testCasesKeys(): Set<String> =
        flatMap(TestRun::testResults)
            .map(TestResult::testCaseKey)
            .filter(String::isNotBlank)
            .toHashSet()

}

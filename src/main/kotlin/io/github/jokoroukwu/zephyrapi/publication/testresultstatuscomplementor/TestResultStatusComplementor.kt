package io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor

import io.github.jokoroukwu.zephyrapi.config.ZephyrConfig
import io.github.jokoroukwu.zephyrapi.http.ZephyrException
import io.github.jokoroukwu.zephyrapi.publication.PublicationContext
import io.github.jokoroukwu.zephyrapi.publication.PublicationDataProcessor
import io.github.jokoroukwu.zephyrapi.publication.testcyclecreator.TestCycleCreator
import java.util.*


class TestResultStatusComplementor(
    private val getTestResultStatusesRequestSender: GetTestResultStatusesRequestSender = GetTestResultStatusesRequestSender(),
    private val nextProcessor: PublicationDataProcessor = TestCycleCreator()
) : PublicationDataProcessor {

    /**
     * Complements [publicationContext] with test result status map
     */
    override fun process(publicationContext: PublicationContext) = with(publicationContext) {
        getTestResultStatusToIdMap(projectId, zephyrConfig)
            .let { publicationContext.copy(statusMap = it) }
            .let(nextProcessor::process)
    }


    private fun getTestResultStatusToIdMap(projectId: Long, zephyrConfig: ZephyrConfig): Map<TestResultStatus, Long> {
        return getTestResultStatusesRequestSender.getTestResultStatusesRequest(projectId, zephyrConfig)
            .associateTo(EnumMap(TestResultStatus::class.java)) { it.name to it.id }
            .also(::validateStatusMap)
    }

    private fun validateStatusMap(statusMap: Map<TestResultStatus, Long>) {
        val statuses: MutableSet<TestResultStatus> =
            EnumSet.allOf(TestResultStatus::class.java).apply { removeAll(statusMap.keys) }

        if (statuses.isNotEmpty()) {
            throw ZephyrException("The following test result status ids are missing from fetched data: $statuses")
        }
    }
}
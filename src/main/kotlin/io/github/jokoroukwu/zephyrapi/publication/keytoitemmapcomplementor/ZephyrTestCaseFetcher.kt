package io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor

import java.util.*

class ZephyrTestCaseFetcher(
    private val getTestCasesRequestSender: GetTestCasesRequestSender = GetTestCasesRequestSender(),
) {

    /**
     * Returns a container with Zephyr project id,
     * where provided test case keys are mapped to their Zephyr ids or null if there are no matches
     *
     * @param testCaseKeys test case keys to be mapped with corresponding ids
     */
    fun fetchProjectWithTestCases(testCaseKeys: Collection<String>): ZephyrProjectWithTestCases? {
        return getTestCasesRequestSender.requestTestCases(testCaseKeys)
            .results
            .takeUnless(List<TestCaseItem>::isEmpty)
            ?.let { ZephyrProjectWithTestCases(it[0].projectId, mapTestCasesToId(it)) }
    }

    private fun mapTestCasesToId(resultItems: Collection<TestCaseItem>): Map<String, Long> {
        return Collections.unmodifiableMap(resultItems.associateTo(HashMap(resultItems.size, 1F))
        { it.key to it.id })
    }
}
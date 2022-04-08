package io.github.jokoroukwu.zephyrapi.testresultstatuscomplementor

import io.github.jokoroukwu.zephyrapi.AbstractTest
import io.github.jokoroukwu.zephyrapi.http.ZephyrException
import io.github.jokoroukwu.zephyrapi.publication.PublicationContext
import io.github.jokoroukwu.zephyrapi.publication.PublicationDataProcessor
import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.GetTestResultStatusesRequestSender
import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.SerializableTestResultStatusItem
import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.TestResultStatus
import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.TestResultStatusComplementor
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.testng.annotations.Test
import java.util.*
import kotlin.random.Random

class TestResultStatusComplementorTest : AbstractTest() {
    private val getTestResultStatusesRequestSenderMock = mockk<GetTestResultStatusesRequestSender>()
    private val nextProcessorMock = mockk<PublicationDataProcessor>()
    private val validStatusToIdMap = EnumMap<TestResultStatus, Long>(TestResultStatus::class.java).apply {
        EnumSet.allOf(TestResultStatus::class.java).forEachIndexed { i, value -> put(value, i.toLong()) }
    }

    private val testResultStatusComplementor =
        TestResultStatusComplementor(getTestResultStatusesRequestSenderMock, nextProcessorMock);


    override fun getObjectsToUnmock(): Array<Any> = arrayOf(getTestResultStatusesRequestSenderMock, nextProcessorMock)

    @Test
    fun `should not call next processor when some statuses were not fetched`() {
        val publicationData = PublicationContext(zephyrConfig = dummyConfig, testCycles = emptyList())
        every { getTestResultStatusesRequestSenderMock.getTestResultStatusesRequest(any(), any()) } returns listOf(
            SerializableTestResultStatusItem(1, TestResultStatus.BLOCKED)
        )
        publicationData.runCatching(testResultStatusComplementor::process)

        verify(exactly = 0) { nextProcessorMock.process(any()) }
    }

    @Test
    fun `should throw exception when some statuses were not fetched`() {
        val publicationData = PublicationContext(zephyrConfig = dummyConfig, testCycles = emptyList())
        every { getTestResultStatusesRequestSenderMock.getTestResultStatusesRequest(any(), any()) } returns listOf(
            SerializableTestResultStatusItem(1, TestResultStatus.BLOCKED)
        )
        val exception = publicationData.runCatching(testResultStatusComplementor::process)
            .exceptionOrNull()
        Assertions.assertThat(exception)
            .`as`("check expected thrown when some statuses were not fetched")
            .isInstanceOf(ZephyrException::class.java)
    }

    @Test
    fun `should call request sender with expected project id`() {
        val publicationData =
            PublicationContext(zephyrConfig = dummyConfig, projectId = Random.nextLong(), testCycles = emptyList())
        justRun { getTestResultStatusesRequestSenderMock.getTestResultStatusesRequest(any(), any()) }
        publicationData.runCatching(testResultStatusComplementor::process)

        with(publicationData) {
            verify(exactly = 1) {
                getTestResultStatusesRequestSenderMock.getTestResultStatusesRequest(projectId, zephyrConfig)
            }
        }
    }

    @Test
    fun `should call next processor with expected publication data`() {
        val statusItems = validStatusToIdMap.entries
            .map { SerializableTestResultStatusItem(it.value, it.key) }
        every { getTestResultStatusesRequestSenderMock.getTestResultStatusesRequest(any(), any()) } returns statusItems
        every { nextProcessorMock.process(any()) } returns true

        val inputPublicationData = PublicationContext(zephyrConfig = dummyConfig, testCycles = emptyList())
        testResultStatusComplementor.process(inputPublicationData)

        val expectedArg = inputPublicationData.copy(statusMap = validStatusToIdMap)
        verify(exactly = 1) { nextProcessorMock.process(expectedArg) }
    }
}
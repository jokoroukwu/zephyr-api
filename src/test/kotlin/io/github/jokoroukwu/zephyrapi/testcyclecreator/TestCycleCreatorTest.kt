package io.github.jokoroukwu.zephyrapi.testcyclecreator

import io.github.jokoroukwu.zephyrapi.AbstractTest
import io.github.jokoroukwu.zephyrapi.publication.PublicationData
import io.github.jokoroukwu.zephyrapi.publication.PublicationDataProcessor
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestCycle
import io.github.jokoroukwu.zephyrapi.publication.testcyclecreator.CreateTestCycleRequestSender
import io.github.jokoroukwu.zephyrapi.publication.testcyclecreator.CreateTestCycleResponse
import io.github.jokoroukwu.zephyrapi.publication.testcyclecreator.TestCycleCreator
import io.mockk.*
import org.assertj.core.api.Assertions
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class TestCycleCreatorTest : AbstractTest() {

    private val createTestCycleRequestSenderMock = mockk<CreateTestCycleRequestSender>()
    private val nextProcessorMock = mockk<PublicationDataProcessor>()
    private val testCycleCreator = TestCycleCreator(createTestCycleRequestSenderMock, nextProcessorMock)

    override fun getObjectsToUnmock(): Array<Any> {
        return arrayOf(createTestCycleRequestSenderMock, nextProcessorMock)
    }

    @DataProvider
    private fun nonEmptyPublicationDataProvider(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                PublicationData(
                    testCycles = listOf(
                        ZephyrTestCycle(startTime = 1, endTime = 2),
                        ZephyrTestCycle(startTime = 1, endTime = 2)
                    )
                ),
                listOf(
                    CreateTestCycleResponse(id = 1, key = "key-one"), CreateTestCycleResponse(id = 2, key = "key-two")
                )
            )
        )
    }

    @Test(dataProvider = "nonEmptyPublicationDataProvider")
    fun `should call createTestCycleRequestSender with expected arguments`(
        publicationData: PublicationData,
        createTestCycleResponses: List<CreateTestCycleResponse>
    ) {
        every { nextProcessorMock.process(any()) } returns true
        coEvery { createTestCycleRequestSenderMock.createTestCycle(any(), any()) } returnsMany createTestCycleResponses

        testCycleCreator.process(publicationData)

        coVerify(exactly = 2) {
            createTestCycleRequestSenderMock.createTestCycle(
                publicationData.projectId, publicationData.testCycles.first()
            )
        }

    }

    @Test(dataProvider = "nonEmptyPublicationDataProvider")
    private fun `should propagate next processor result`(
        publicationData: PublicationData,
        createTestCycleResponses: List<CreateTestCycleResponse>
    ) {
        every { nextProcessorMock.process(any()) } returns false
        coEvery { createTestCycleRequestSenderMock.createTestCycle(any(), any()) } returnsMany createTestCycleResponses
        val result = testCycleCreator.process(publicationData)
        Assertions.assertThat(result)
            .`as`("check next processor result is propagated")
            .isFalse
    }

    @Test(dataProvider = "nonEmptyPublicationDataProvider")
    fun `should call next processor with expected argument`(
        publicationData: PublicationData,
        createTestCycleResponses: List<CreateTestCycleResponse>
    ) {
        val capturedArg = slot<PublicationData>()
        every { nextProcessorMock.process(capture(capturedArg)) } returns true
        coEvery { createTestCycleRequestSenderMock.createTestCycle(any(), any()) } returnsMany createTestCycleResponses

        testCycleCreator.process(publicationData)
        val expectedCycles = createTestCycleResponses
            .mapTo(HashSet(2)) { ZephyrTestCycle(id = it.id, key = it.key, startTime = 1, endTime = 2) }
        val expectedArg = publicationData.copy(testCycles = expectedCycles)
        val actualArg = capturedArg.captured.copy(testCycles = HashSet(capturedArg.captured.testCycles))

        Assertions.assertThat(actualArg)
            .`as`("should call next processor with expected args")
            .isEqualTo(expectedArg)
    }

    @DataProvider
    private fun noToBeProcessedPublicationDataProvider(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(PublicationData(testCycles = emptyList()))
        )
    }

    @Test(dataProvider = "noToBeProcessedPublicationDataProvider")
    private fun `should not call createTestCycleRequestSender`(publicationData: PublicationData) {
        testCycleCreator.process(publicationData)
        verify(exactly = 0) { nextProcessorMock.process(any()) }
    }

    @Test(dataProvider = "noToBeProcessedPublicationDataProvider")
    private fun `should return false when no test cycles are present`(publicationData: PublicationData) {
        val result = testCycleCreator.process(publicationData)
        Assertions.assertThat(result)
            .`as`("check returned false when no test cycles are present")
            .isFalse
    }

    @Test(dataProvider = "noToBeProcessedPublicationDataProvider")
    private fun `should not call next processor when no tests cycles are present`(publicationData: PublicationData) {
        testCycleCreator.process(publicationData)
        verify(exactly = 0) { nextProcessorMock.process(any()) }
    }

}
package io.github.jokoroukwu.zephyrapi.testcycleupdater

import io.github.jokoroukwu.zephyrapi.AbstractTest
import io.github.jokoroukwu.zephyrapi.publication.PublicationData
import io.github.jokoroukwu.zephyrapi.publication.PublicationDataProcessor
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestCycle
import io.github.jokoroukwu.zephyrapi.publication.testcycleupdater.TestCycleUpdater
import io.github.jokoroukwu.zephyrapi.publication.testcycleupdater.UpdateTestCycleRequestSender
import io.mockk.*
import org.assertj.core.api.Assertions
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class TestCycleUpdaterTest : AbstractTest() {
    private val updateTestCycleRequestSenderMock = mockk<UpdateTestCycleRequestSender>()
    private val nextProcessorMock = mockk<PublicationDataProcessor>()
    private val testCycleUpdater = TestCycleUpdater(updateTestCycleRequestSenderMock, nextProcessorMock)

    override fun getObjectsToUnmock(): Array<Any> {
        return arrayOf(updateTestCycleRequestSenderMock, nextProcessorMock)
    }

    @DataProvider
    private fun toBeProcessedPublicationDataProvider(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                PublicationData(
                    testCycles = listOf(
                        ZephyrTestCycle(startTime = 1, endTime = 2),
                        ZephyrTestCycle(startTime = 1, endTime = 2)
                    )
                )
            )
        )
    }

    @Test(dataProvider = "toBeProcessedPublicationDataProvider")
    private fun `should call request sender with expected args`(publicationData: PublicationData) {
        every { nextProcessorMock.process(any()) } returns true
        coJustRun { updateTestCycleRequestSenderMock.updateTestCycle(any()) }

        testCycleUpdater.process(publicationData)

        coVerify(exactly = 2) { updateTestCycleRequestSenderMock.updateTestCycle(publicationData.testCycles.first()) }

    }

    @Test(dataProvider = "toBeProcessedPublicationDataProvider")
    private fun `should call nex processor with expected arguments`(publicationData: PublicationData) {
        every { nextProcessorMock.process(any()) } returns true
        coJustRun { updateTestCycleRequestSenderMock.updateTestCycle(any()) }
        testCycleUpdater.process(publicationData)
        verify(exactly = 1) { nextProcessorMock.process(publicationData) }
    }

    @Test(dataProvider = "toBeProcessedPublicationDataProvider")
    private fun `should pass next processor result`(publicationData: PublicationData) {
        every { nextProcessorMock.process(any()) } returns false
        coJustRun { updateTestCycleRequestSenderMock.updateTestCycle(any()) }
        val result = testCycleUpdater.process(publicationData)
        Assertions.assertThat(result)
            .`as`("check next processor result was passed")
            .isFalse
    }

    @DataProvider
    private fun notTobeProcessedPublicationDataProvider(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(PublicationData(testCycles = emptyList()))
        )
    }

    @Test(dataProvider = "notTobeProcessedPublicationDataProvider")
    private fun `should not call request sender when no test cycles are present`(publicationData: PublicationData) {
        testCycleUpdater.process(publicationData)
        coVerify(exactly = 0) { updateTestCycleRequestSenderMock.updateTestCycle(any()) }
    }

    @Test(dataProvider = "notTobeProcessedPublicationDataProvider")
    private fun `should not call next processor when no test cycles are present`(publicationData: PublicationData) {
        testCycleUpdater.process(publicationData)
        verify(exactly = 0) { nextProcessorMock.process(any()) }
    }

    @Test(dataProvider = "notTobeProcessedPublicationDataProvider")
    private fun `should return false when no test cycles are present`(publicationData: PublicationData) {
        val result = testCycleUpdater.process(publicationData)
        Assertions.assertThat(result)
            .`as`("check returns false when no test cycles are present")
            .isFalse
    }
}
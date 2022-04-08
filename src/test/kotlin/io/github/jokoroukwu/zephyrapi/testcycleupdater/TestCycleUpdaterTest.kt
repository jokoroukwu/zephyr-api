package io.github.jokoroukwu.zephyrapi.testcycleupdater

import io.github.jokoroukwu.zephyrapi.AbstractTest
import io.github.jokoroukwu.zephyrapi.publication.PublicationContext
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
                PublicationContext(
                    zephyrConfig = dummyConfig,
                    testCycles = listOf(
                        ZephyrTestCycle(startTime = 1, endTime = 2),
                        ZephyrTestCycle(startTime = 1, endTime = 2)
                    )
                )
            )
        )
    }

    @Test(dataProvider = "toBeProcessedPublicationDataProvider")
    private fun `should call request sender with expected args`(publicationContext: PublicationContext) {
        every { nextProcessorMock.process(any()) } returns true
        coJustRun { updateTestCycleRequestSenderMock.updateTestCycle(any(), any()) }

        testCycleUpdater.process(publicationContext)

        with(publicationContext) {
            coVerify(exactly = 2) { updateTestCycleRequestSenderMock.updateTestCycle(testCycles.first(), zephyrConfig) }
        }
    }

    @Test(dataProvider = "toBeProcessedPublicationDataProvider")
    private fun `should call nex processor with expected arguments`(publicationContext: PublicationContext) {
        every { nextProcessorMock.process(any()) } returns true
        coJustRun { updateTestCycleRequestSenderMock.updateTestCycle(any(), publicationContext.zephyrConfig) }
        testCycleUpdater.process(publicationContext)
        verify(exactly = 1) { nextProcessorMock.process(publicationContext) }
    }

    @Test(dataProvider = "toBeProcessedPublicationDataProvider")
    private fun `should pass next processor result`(publicationContext: PublicationContext) {
        every { nextProcessorMock.process(any()) } returns false
        coJustRun { updateTestCycleRequestSenderMock.updateTestCycle(any(), publicationContext.zephyrConfig) }
        val result = testCycleUpdater.process(publicationContext)
        Assertions.assertThat(result)
            .`as`("check next processor result was passed")
            .isFalse
    }

    @DataProvider
    private fun notTobeProcessedPublicationDataProvider(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(PublicationContext(testCycles = emptyList(), zephyrConfig = dummyConfig))
        )
    }

    @Test(dataProvider = "notTobeProcessedPublicationDataProvider")
    private fun `should not call request sender when no test cycles are present`(publicationContext: PublicationContext) {
        testCycleUpdater.process(publicationContext)
        coVerify(exactly = 0) { updateTestCycleRequestSenderMock.updateTestCycle(any(), any()) }
    }

    @Test(dataProvider = "notTobeProcessedPublicationDataProvider")
    private fun `should not call next processor when no test cycles are present`(publicationContext: PublicationContext) {
        testCycleUpdater.process(publicationContext)
        verify(exactly = 0) { nextProcessorMock.process(any()) }
    }

    @Test(dataProvider = "notTobeProcessedPublicationDataProvider")
    private fun `should return false when no test cycles are present`(publicationContext: PublicationContext) {
        val result = testCycleUpdater.process(publicationContext)
        Assertions.assertThat(result)
            .`as`("check returns false when no test cycles are present")
            .isFalse
    }
}
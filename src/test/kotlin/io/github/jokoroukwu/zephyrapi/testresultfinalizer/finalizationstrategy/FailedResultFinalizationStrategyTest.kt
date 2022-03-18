package io.github.jokoroukwu.zephyrapi.testresultfinalizer.finalizationstrategy

import io.github.jokoroukwu.zephyrapi.publication.TestDataResult
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.ReportStepResult
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.TestScriptResult
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.datasetfinalization.FailedResultFinalizationStrategy
import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.TestResultStatus.*
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkObject
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.testng.annotations.*
import kotlin.random.Random

class FailedResultFinalizationStrategyTest {
    private val strategy = FailedResultFinalizationStrategy
    private val statusMap = mapOf(FAIL to 1L, PASS to 0L, BLOCKED to 2L)
    private val datasetResultMock = mockk<TestDataResult>()
    private val failureMessage = StringBuilder().apply {
        repeat(16) { append(Random.nextInt(0x30, 0x7A).toChar()) }
    }.toString()

    @AfterMethod(alwaysRun = true)
    fun tearDown() {
        clearAllMocks()
    }

    @AfterClass(alwaysRun = true)
    fun afterClass() {
        unmockkObject(datasetResultMock)
    }

    @Test(dataProvider = "reportStepsAndFailedIndexProvider")
    fun `should return expected test script results`(steps: List<ReportStepResult>, failedIndex: Int) {
        every { datasetResultMock.failureMessage } returns failureMessage
        every { datasetResultMock.failedStepIndex } returns failedIndex
        val expectedTestScriptResults = ArrayList<TestScriptResult>()
                expectedTestScriptResults.add(
                    steps[failedIndex].toTestScriptResult(statusMap.getValue(FAIL), failureMessage)
                )
        (0 until failedIndex).forEach { i ->
            expectedTestScriptResults.add(
                steps[i].toTestScriptResult(statusMap.getValue(PASS))
            )
        }
        (failedIndex + 1 until steps.size).forEach { i ->
            expectedTestScriptResults.add(
                steps[i].toTestScriptResult(statusMap.getValue(BLOCKED))
            )
        }

        val result = strategy.finalizeResult(statusMap, steps, datasetResultMock)

        Assertions.assertThat(result.testScriptResults)
            .`as`("test script results validation")
            .containsExactlyInAnyOrderElementsOf(expectedTestScriptResults)
    }

    @DataProvider
    private fun reportStepsAndFailedIndexProvider(): Array<Array<Any>> {
        val reportStepResult = ReportStepResult(1L, 1)
        val stepResultList = listOf(reportStepResult, reportStepResult, reportStepResult, reportStepResult)
        return arrayOf(
            arrayOf(stepResultList, 0),
            arrayOf(stepResultList, 1),
            arrayOf(stepResultList, 2),
            arrayOf(stepResultList, 3)
        )
    }

    @Test
    fun `should return just comment when no failed step index provided`() {
        every { datasetResultMock.failedStepIndex } returns null
        every { datasetResultMock.failureMessage } returns failureMessage
        val result = strategy.finalizeResult(statusMap, emptyList(), datasetResultMock)
        with(SoftAssertions()) {
            assertThat(result.testScriptResults)
                .`as`("test script result list validation")
                .isEmpty()
            assertThat(result.warningMessage)
                .`as`("warning message validation")
                .isNull()

            assertThat(result.comment)
                .`as`("comment validation")
                .isEqualTo(failureMessage)

            assertAll()
        }
    }

    @Test
    fun `should return comment and warning when failed step index out of bound`() {
        val failedStepIndex = 1;
        every { datasetResultMock.failedStepIndex } returns failedStepIndex
        every { datasetResultMock.failureMessage } returns failureMessage

        val result = strategy.finalizeResult(statusMap, emptyList(), datasetResultMock)
        with(SoftAssertions()) {
            assertThat(result.testScriptResults)
                .`as`("test script result list validation")
                .isEmpty()
            assertThat(result.warningMessage)
                .`as`("warning message validation")
                .isEqualTo(FailedResultFinalizationStrategy.warningMessage, failedStepIndex + 1, 0)

            assertThat(result.comment)
                .`as`("comment validation")
                .isEqualTo(failureMessage)

            assertAll()
        }
    }
}
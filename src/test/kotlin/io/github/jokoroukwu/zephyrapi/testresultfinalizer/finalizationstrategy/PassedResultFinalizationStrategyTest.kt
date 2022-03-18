package io.github.jokoroukwu.zephyrapi.testresultfinalizer.finalizationstrategy

import io.github.jokoroukwu.zephyrapi.publication.TestDataResult
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.ReportStepResult
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.TestScriptResult
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.datasetfinalization.DatasetFinalizationStrategy
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.datasetfinalization.PassedResultFinalizationStrategy
import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.TestResultStatus
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.unmockkObject
import org.assertj.core.api.SoftAssertions
import org.testng.annotations.*

class PassedResultFinalizationStrategyTest {
    private val statusMap = mapOf(TestResultStatus.PASS to 1L)
    private val passedId = statusMap.getValue(TestResultStatus.PASS)
    private val strategy: DatasetFinalizationStrategy = PassedResultFinalizationStrategy
    private lateinit var testDataResult: TestDataResult

    @BeforeMethod
    fun setUp() {
        testDataResult = mockk()
    }

    @AfterMethod(alwaysRun = true)
    fun tearDown() {
        clearAllMocks()
    }

    @AfterClass
    fun afterClass() {
        unmockkObject(testDataResult)
    }

    @Test(dataProvider = "testScriptResultsProvider")
    fun `should return test script results`(stepResults: List<ReportStepResult>) {
        val result = strategy.finalizeResult(statusMap, stepResults, testDataResult)

        with(SoftAssertions()) {
            assertThat(result.comment)
                .`as`("comment validation")
                .isNull()
            assertThat(result.warningMessage)
                .`as`("error message validation")
                .isNull()
            val expectedSteps = stepResults.map { TestScriptResult(id = it.id, testResultStatusId = passedId) }
            assertThat(result.testScriptResults)
                .`as`("test script results validation")
                .containsExactlyInAnyOrderElementsOf(expectedSteps)
            assertAll()
        }
    }

    @DataProvider
    private fun testScriptResultsProvider(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                listOf(ReportStepResult(1, 0)),
            ),
            arrayOf(
                listOf(ReportStepResult(1, 0), ReportStepResult(2, 1))
            )
        )
    }

    @Test
    fun `should return comment only when no steps are present`() {
        val result = strategy.finalizeResult(statusMap, emptyList(), testDataResult)

        with(SoftAssertions()) {
            assertThat(result.comment)
                .`as`("comment validation")
                .isEqualTo(TestResultStatus.PASS.toString())

            assertThat(result.testScriptResults)
                .`as`("test script results validation")
                .isEmpty()

            assertThat(result.warningMessage)
                .`as`("error message validation")
                .isNull()

            assertAll()
        }
    }

}
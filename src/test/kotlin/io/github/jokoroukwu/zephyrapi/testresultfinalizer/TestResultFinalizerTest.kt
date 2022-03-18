package io.github.jokoroukwu.zephyrapi.testresultfinalizer

import io.github.jokoroukwu.zephyrapi.publication.*
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.ReportStepResult
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.ReportTestResult
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.TestCase
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.PublicationDataFinalizer
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.SerializableTestResult
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.TestResultFinalization
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.TestResultFinalizer
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.datasetfinalization.FinalizationStrategyFactory
import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.TestResultStatus
import io.github.jokoroukwu.zephyrapi.softly
import io.mockk.*
import org.testng.annotations.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class TestResultFinalizerTest {
    private val testCaseId = 1L
    private val statusMap: StatusMap = TestResultStatus.values()
        .associateWithTo(EnumMap(TestResultStatus::class.java)) { status -> status.ordinal.toLong() }
    private lateinit var dataFinalizer: PublicationDataFinalizer

    private val datasetResultMock = mockk<TestDataResult>()
    private val strategyFactoryMock = mockk<FinalizationStrategyFactory>()
    private val testResultFinalizerMock = mockk<TestResultFinalizer>()
    private val nextProcessorMock = mockk<PublicationDataProcessor>(relaxed = true)
    private val testResult = ZephyrTestResult(
        testCaseId = testCaseId,
        startTime = System.currentTimeMillis(),
        endTime = System.currentTimeMillis() + Random.nextLong(100),
        testDataResults = listOf(datasetResultMock)
    )
    private val reportTestResult =
        ReportTestResult(2, TestCase(testCaseId, "key", "name"), listOf(ReportStepResult(id = 1, index = 0)))

    @BeforeClass
    fun setUp() {
        dataFinalizer = PublicationDataFinalizer(nextProcessorMock, testResultFinalizerMock)
    }

    @AfterMethod(alwaysRun = true)
    fun tearDown() {
        clearAllMocks()
    }

    @AfterClass(alwaysRun = true)
    fun afterClass() {
        unmockkObject(testResultFinalizerMock, strategyFactoryMock)
    }

    @DataProvider
    private fun singleTestCycleDataProvider(): Array<Array<Any>> {
        val singleResultTestCycle = ZephyrTestCycle(testResults = listOf(testResult), startTime = 1, endTime = 2)
        val singleCycleSingleResultData = PublicationData(
            statusMap = statusMap,
            testCycles = listOf(singleResultTestCycle),
            testCaseIdToReportTestResultsMap = mapOf(testCaseId to LinkedList<ReportTestResult>().apply {
                add(reportTestResult)
            })
        )
        val multiTestResultCycle = singleResultTestCycle.copy(testResults = listOf(testResult, testResult))
        val multiCycleMultiResultData = PublicationData(
            statusMap = statusMap,
            testCycles = listOf(singleResultTestCycle, multiTestResultCycle),
            testCaseIdToReportTestResultsMap = mapOf(testCaseId to LinkedList<ReportTestResult>().apply {
                add(reportTestResult)
                add(reportTestResult)
                add(reportTestResult)
            })
        )
        return arrayOf(
            arrayOf(singleCycleSingleResultData),
            arrayOf(multiCycleMultiResultData)
        )
    }

    @Test(dataProvider = "singleTestCycleDataProvider")
    private fun `should call finalizer with expected args`(publicationData: PublicationData) {
        val expectedTestResults = publicationData.testCycles.flatMap(TestRun::testResults)
        val expectedReportTestResults = publicationData.testCaseIdToReportTestResultsMap.flatMap { it.value }

        val capturedReportTestResults = ArrayList<ReportTestResult>(5)
        val capturedTestResults = ArrayList<TestResult>(5)
        val capturedStatusMap = ArrayList<StatusMap>(5)

        every {
            testResultFinalizerMock.finalizeTestResult(
                capture(capturedReportTestResults),
                capture(capturedTestResults),
                capture(capturedStatusMap)
            )
            //  return value is out of test scope
        } returns TestResultFinalization(SerializableTestResult(1, 1, 1), emptyList())

        dataFinalizer.process(publicationData)

        softly {
            assertThat(capturedStatusMap)
                .`as`("status map argument should be equal to expected")
                .containsExactlyInAnyOrderElementsOf(Collections.nCopies(expectedTestResults.size, statusMap))

            assertThat(capturedTestResults)
                .`as`("TestResults arguments should be equal to expected")
                .containsExactlyInAnyOrderElementsOf(expectedTestResults)

            assertThat(capturedReportTestResults)
                .`as`("ReportTestResults arguments should be equal to expected")
                .containsExactlyInAnyOrderElementsOf(expectedReportTestResults)

            assertAll()
        }
    }

    @Test
    private fun `should not call finalizer when report test result is not present`() {
        val publicationData = PublicationData(
            statusMap = statusMap,
            testCycles = listOf(ZephyrTestCycle(startTime = 1, endTime = 2, testResults = listOf(testResult)))
        )

        dataFinalizer.process(publicationData)
        verify(exactly = 0) { testResultFinalizerMock.finalizeTestResult(any(), any(), any()) }
    }
}
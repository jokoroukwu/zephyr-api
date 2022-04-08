package io.github.jokoroukwu.zephyrapi.testcyclefilter

import io.github.jokoroukwu.zephyrapi.AbstractTest
import io.github.jokoroukwu.zephyrapi.publication.PublicationContext
import io.github.jokoroukwu.zephyrapi.publication.PublicationDataProcessor
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestCycle
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestResult
import io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor.TestCaseItem
import io.github.jokoroukwu.zephyrapi.publication.testcyclefilter.FilteredResult
import io.github.jokoroukwu.zephyrapi.publication.testcyclefilter.TestCycleFilter
import io.github.jokoroukwu.zephyrapi.publication.testcyclefilter.TestResultFilter
import io.github.jokoroukwu.zephyrapi.softly
import io.mockk.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.testng.annotations.*
import org.testng.annotations.Test

class TestCycleFilterTest : AbstractTest() {
    private val testCaseItemMock = mockk<TestCaseItem>()
    private val nextProcessorMock = mockk<PublicationDataProcessor>()
    private val testResultFilterMock = mockk<TestResultFilter>()
    private val testCycleFilter =
        TestCycleFilter(testResultFilter = testResultFilterMock, nextProcessor = nextProcessorMock)

    @BeforeClass
    fun initMocks() {
        every { testCaseItemMock.id } returns 1
        // true is the default return value and is out of test scope
        every { nextProcessorMock.process(any()) } returns true
    }

    @AfterMethod(alwaysRun = true)
    override fun baseTearDown() {
        clearAllMocks()
        every { nextProcessorMock.process(any()) } returns true
        every { testCaseItemMock.id } returns 1
    }


    override fun getObjectsToUnmock(): Array<Any> {
        return arrayOf(testResultFilterMock, nextProcessorMock)
    }

    @Test
    private fun `should not call next processor when test filter returns no results`() {
        val resultOne = ZephyrTestResult(testCaseKey = "key-one", startTime = 1, endTime = 2)
        val resultTwo = ZephyrTestResult(testCaseKey = "key-two", startTime = 1, endTime = 2)
        val map = hashMapOf("key-one" to testCaseItemMock, "key-two" to testCaseItemMock)

        val publicationData = PublicationContext(
            zephyrConfig = dummyConfig,
            testCycles = listOf(
                ZephyrTestCycle(testResults = listOf(resultOne), startTime = 1, endTime = 2),
                ZephyrTestCycle(testResults = listOf(resultTwo), startTime = 1, endTime = 2)
            ),
            testCaseKeyToItemMap = map
        )
        val filterReturnValues = listOf(
            FilteredResult(issues = listOf("problem one", "problem two")),
            FilteredResult(issues = listOf("problem three", "problem four"))
        )

        every { testResultFilterMock.filterTestResult(any(), any()) } returnsMany filterReturnValues

        val result = testCycleFilter.process(publicationData)

        verify(exactly = 0) { nextProcessorMock.process(publicationData) }
        Assertions.assertThat(result).`as`("process result").isFalse
    }

    @Test(dataProvider = "callNextProcessorWithExpectedCyclesAfterResultFilterProvider")
    private fun `should call next processor with expected arguments after result filter`(
        publicationContext: PublicationContext,
        expectedTestCycles: List<ZephyrTestCycle>,
        resultFilterMockReturnValues: List<FilteredResult>
    ) {
        val capturedResult = slot<ZephyrTestResult>()
        every {
            testResultFilterMock.filterTestResult(
                capture(capturedResult),
                any()
            )
        } returnsMany resultFilterMockReturnValues

        val capturedData = slot<PublicationContext>()
        every { nextProcessorMock.process(capture(capturedData)) } returns true
        testCycleFilter.process(publicationContext)

        val actualData = capturedData.captured

        softly {
            assertThat(actualData.finalizedSteps).`as`("finalized steps").isEqualTo(publicationContext.finalizedSteps)
            assertThat(actualData.finalizedTestResults).`as`("finalized test results")
                .isEqualTo(publicationContext.finalizedTestResults)
            assertThat(actualData.projectId).`as`("project id").isEqualTo(publicationContext.projectId)
            assertThat(actualData.testCaseKeyToItemMap).`as`("test case key to id map")
                .isEqualTo(publicationContext.testCaseKeyToItemMap)
            assertThat(actualData.testCaseIdToReportTestResultsMap).`as`("test case id to report test result map")
                .isEqualTo(publicationContext.testCaseIdToReportTestResultsMap)
            assertThat(actualData.statusMap).`as`("test result status to id map")
                .isEqualTo(publicationContext.statusMap)
            assertThat(actualData.testCycles)
                .`as`("check contains expected test cycles")
                .containsExactlyInAnyOrderElementsOf(expectedTestCycles)

            assertAll()
        }
    }

    @DataProvider
    private fun callNextProcessorWithExpectedCyclesAfterResultFilterProvider(): Array<Array<Any?>> {

        val resultOne = ZephyrTestResult(testCaseKey = "key-one", startTime = 1, endTime = 2)
        val resultTwo = ZephyrTestResult(testCaseKey = "key-two", startTime = 1, endTime = 2)
        val map = hashMapOf("key-one" to testCaseItemMock, "key-two" to testCaseItemMock)
        return arrayOf(
            arrayOf(
                PublicationContext(
                    zephyrConfig = dummyConfig,
                    testCycles = listOf(
                        ZephyrTestCycle(testResults = listOf(resultOne), startTime = 1, endTime = 2),
                        ZephyrTestCycle(testResults = listOf(resultOne, resultTwo), startTime = 1, endTime = 2)
                    ),
                    testCaseKeyToItemMap = map
                ),
                listOf(
                    ZephyrTestCycle(testResults = listOf(resultOne), startTime = 1, endTime = 2),
                    ZephyrTestCycle(testResults = listOf(resultOne), startTime = 1, endTime = 2)
                ),
                listOf(
                    FilteredResult(testResult = resultOne),
                    FilteredResult(testResult = resultOne),
                    FilteredResult(issues = listOf("problem one", "problem two"))
                )
            ),
            arrayOf(
                PublicationContext(
                    zephyrConfig = dummyConfig,
                    testCycles = listOf(
                        ZephyrTestCycle(testResults = listOf(resultOne), startTime = 1, endTime = 2),
                        ZephyrTestCycle(testResults = listOf(resultTwo), startTime = 1, endTime = 2)
                    ),
                    testCaseKeyToItemMap = map
                ),
                listOf(ZephyrTestCycle(testResults = listOf(resultOne), startTime = 1, endTime = 2)),
                listOf(
                    FilteredResult(testResult = resultOne),
                    FilteredResult(issues = listOf("problem one", "problem two"))
                )
            )
        )
    }

    @Test(dataProvider = "callNextProcessorWithExpectedCyclesProvider")
    private fun `should call next processor with expected arguments`(
        publicationContext: PublicationContext,
        expectedTestCycles: List<ZephyrTestCycle>
    ) {
        val capturedResult = slot<ZephyrTestResult>()
        every { testResultFilterMock.filterTestResult(capture(capturedResult), any()) } answers {
            FilteredResult(testResult = capturedResult.captured)
        }
        val capturedData = slot<PublicationContext>()
        every { nextProcessorMock.process(capture(capturedData)) } returns true

        testCycleFilter.process(publicationContext)

        val actualData = capturedData.captured
        with(SoftAssertions()) {
            assertThat(actualData.finalizedSteps).`as`("finalized steps").isEqualTo(publicationContext.finalizedSteps)
            assertThat(actualData.finalizedTestResults).`as`("finalized test results")
                .isEqualTo(publicationContext.finalizedTestResults)
            assertThat(actualData.projectId).`as`("project id").isEqualTo(publicationContext.projectId)
            assertThat(actualData.testCaseKeyToItemMap).`as`("test case key to id map")
                .isEqualTo(publicationContext.testCaseKeyToItemMap)
            assertThat(actualData.testCaseIdToReportTestResultsMap).`as`("test case id to report test result map")
                .isEqualTo(publicationContext.testCaseIdToReportTestResultsMap)
            assertThat(actualData.statusMap).`as`("test result status to id map")
                .isEqualTo(publicationContext.statusMap)
            assertThat(actualData.testCycles)
                .`as`("check contains expected test cycles")
                .containsExactlyInAnyOrderElementsOf(expectedTestCycles)

            assertAll()
        }
    }

    @DataProvider
    private fun callNextProcessorWithExpectedCyclesProvider(): Array<Array<Any?>> {
        val resultOne = ZephyrTestResult(testCaseId = 1, testCaseKey = "key-one", startTime = 1, endTime = 2)
        val resultTwo = ZephyrTestResult(testCaseId = 2, testCaseKey = "key-two", startTime = 1, endTime = 2)
        val map = hashMapOf("key-one" to testCaseItemMock)

        return arrayOf(
            arrayOf(
                PublicationContext(
                    zephyrConfig = dummyConfig,
                    testCycles = listOf(
                        ZephyrTestCycle(testResults = listOf(resultOne), startTime = 1, endTime = 2),
                        ZephyrTestCycle(testResults = listOf(resultOne, resultTwo), startTime = 1, endTime = 2)
                    ),
                    testCaseKeyToItemMap = map
                ),
                listOf(
                    ZephyrTestCycle(testResults = listOf(resultOne), startTime = 1, endTime = 2),
                    ZephyrTestCycle(testResults = listOf(resultOne), startTime = 1, endTime = 2)
                )
            ),
            arrayOf(
                PublicationContext(
                    zephyrConfig = dummyConfig,
                    testCycles = listOf(
                        ZephyrTestCycle(testResults = listOf(resultOne), startTime = 1, endTime = 2),
                        ZephyrTestCycle(testResults = listOf(resultTwo), startTime = 1, endTime = 2)
                    ),
                    testCaseKeyToItemMap = map

                ),
                listOf(ZephyrTestCycle(testResults = listOf(resultOne), startTime = 1, endTime = 2))
            )
        )
    }

    @Test(dataProvider = "callFilterOnExpectedResultsProvider")
    private fun `should call test result filter with expected arguments`(
        publicationContext: PublicationContext,
        verifyCalled: List<ZephyrTestResult>,
        verifyNotCalled: List<ZephyrTestResult>
    ) {
        val capturedResult = slot<ZephyrTestResult>()
        every { testResultFilterMock.filterTestResult(capture(capturedResult), any()) } answers {
            FilteredResult(testResult = capturedResult.captured)
        }

        testCycleFilter.process(publicationContext)

        verifyCalled.forEach {
            verify(exactly = 1) {
                testResultFilterMock.filterTestResult(
                    it,
                    publicationContext.testCaseKeyToItemMap[it.testCaseKey]!!
                )
            }
        }
        verifyNotCalled.forEach {
            verify(exactly = 0) {
                testResultFilterMock.filterTestResult(
                    it,
                    any()
                )
            }
        }
    }

    @DataProvider
    private fun callFilterOnExpectedResultsProvider(): Array<Array<Any?>> {
        val allValidResultOne = ZephyrTestResult(testCaseKey = "key-one", startTime = 1, endTime = 2)
        val allValidResultTwo = ZephyrTestResult(testCaseKey = "key-two", startTime = 1, endTime = 2)
        val allValidTestResultsData = PublicationContext(
            zephyrConfig = dummyConfig,
            testCycles = listOf(
                ZephyrTestCycle(testResults = listOf(allValidResultOne), startTime = 1, endTime = 2),
                ZephyrTestCycle(testResults = listOf(allValidResultTwo), startTime = 1, endTime = 2),
            ),
            testCaseKeyToItemMap = hashMapOf(
                "key-one" to testCaseItemMock,
                "key-two" to testCaseItemMock
            )
        )

        val partiallyValidResultOne = ZephyrTestResult(testCaseKey = "key-one", startTime = 1, endTime = 2)
        val partiallyValidResultTwo = ZephyrTestResult(testCaseKey = "key-two", startTime = 1, endTime = 2)
        val partiallyValidResultThree = ZephyrTestResult(testCaseKey = "key-three", startTime = 1, endTime = 2)
        val partiallyValidResultFour = ZephyrTestResult(testCaseKey = "key-four", startTime = 1, endTime = 2)

        val partiallyValidTestResultsData = PublicationContext(
            zephyrConfig = dummyConfig,
            testCycles = listOf(
                ZephyrTestCycle(
                    testResults = listOf(partiallyValidResultOne, partiallyValidResultThree),
                    startTime = 1,
                    endTime = 2
                ),
                ZephyrTestCycle(
                    testResults = listOf(partiallyValidResultTwo, partiallyValidResultFour),
                    startTime = 1,
                    endTime = 2
                ),
            ),
            testCaseKeyToItemMap = hashMapOf(
                "key-one" to testCaseItemMock,
                "key-two" to testCaseItemMock,
            )
        )
        return arrayOf(
            arrayOf(
                allValidTestResultsData,
                listOf(
                    partiallyValidResultOne.copy(testCaseId = testCaseItemMock.id),
                    allValidResultTwo.copy(testCaseId = testCaseItemMock.id)
                ),
                emptyList<ZephyrTestResult>()
            ),
            arrayOf(
                partiallyValidTestResultsData,
                listOf(
                    partiallyValidResultOne.copy(testCaseId = testCaseItemMock.id),
                    allValidResultTwo.copy(testCaseId = testCaseItemMock.id)
                ),
                listOf(
                    partiallyValidResultThree.copy(testCaseId = testCaseItemMock.id),
                    partiallyValidResultFour.copy(testCaseId = testCaseItemMock.id)
                )
            )
        )
    }

    @Test
    private fun `should not call next processor when results are empty`() {
        justRun { testResultFilterMock.filterTestResult(any(), any()) }

        val publicationData = PublicationContext(
            zephyrConfig = dummyConfig,
            testCycles = emptyList(),
            testCaseKeyToItemMap = mapOf("key" to mockk(relaxed = true))
        )
        val result = testCycleFilter.process(publicationData)

        verify(exactly = 0) { testResultFilterMock.filterTestResult(any(), any()) }
        verify(exactly = 0) { nextProcessorMock.process(any()) }

        Assertions.assertThat(result).`as`("test cycle filter result").isFalse
    }

    @Test(dataProvider = "emptyTestCycleProvider")
    private fun `should not call next processor when test cycle is empty`(publicationContext: PublicationContext) {
        justRun { testResultFilterMock.filterTestResult(any(), any()) }

        val result = testCycleFilter.process(publicationContext)

        verify(exactly = 0) { testResultFilterMock.filterTestResult(any(), any()) }
        verify(exactly = 0) { nextProcessorMock.process(any()) }

        Assertions.assertThat(result).`as`("test cycle filter result").isFalse
    }

    @DataProvider
    private fun emptyTestCycleProvider() =
        arrayOf<Array<Any>>(
            //  single empty test cycle
            arrayOf(
                PublicationContext(
                    zephyrConfig = dummyConfig,
                    testCycles = listOf(ZephyrTestCycle(startTime = 1, endTime = 2))
                )
            ),
            //  multiple empty test cycles
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

    @Test(dataProvider = "noTestCaseFetchedProvider")
    private fun `should not call next processor when no test cases fetched`(publicationContext: PublicationContext) {
        justRun { testResultFilterMock.filterTestResult(any(), any()) }

        val result = testCycleFilter.process(publicationContext)

        verify(exactly = 0) { testResultFilterMock.filterTestResult(any(), any()) }
        verify(exactly = 0) { nextProcessorMock.process(any()) }

        Assertions.assertThat(result).`as`("process result").isFalse
    }

    @DataProvider
    private fun noTestCaseFetchedProvider() =
        arrayOf<Array<Any?>>(
            //  single test cycle; single test result
            arrayOf(
                PublicationContext(
                    zephyrConfig = dummyConfig,
                    testCycles = listOf(
                        ZephyrTestCycle(
                            testResults = listOf(
                                ZephyrTestResult(
                                    testCaseKey = "key",
                                    startTime = 1,
                                    endTime = 2
                                )
                            ),
                            startTime = 1,
                            endTime = 2
                        )
                    )
                )
            ),
            //  single test cycle; multiple test results
            arrayOf(
                PublicationContext(
                    zephyrConfig = dummyConfig,
                    testCycles = listOf(
                        ZephyrTestCycle(
                            testResults = listOf(
                                ZephyrTestResult(testCaseKey = "key", startTime = 1, endTime = 2),
                                ZephyrTestResult(testCaseKey = "other-key", startTime = 1, endTime = 2)
                            ),
                            startTime = 1,
                            endTime = 2
                        )
                    )
                )
            ),
            //  multiple test cycles; single test result
            arrayOf(
                PublicationContext(
                    zephyrConfig = dummyConfig,
                    testCycles = listOf(
                        ZephyrTestCycle(
                            testResults = listOf(
                                ZephyrTestResult(
                                    testCaseKey = "key-one",
                                    startTime = 1,
                                    endTime = 2
                                )
                            ),
                            startTime = 1,
                            endTime = 2
                        ),
                        ZephyrTestCycle(
                            testResults = listOf(
                                ZephyrTestResult(
                                    testCaseKey = "key-two",
                                    startTime = 1,
                                    endTime = 2
                                )
                            ),
                            startTime = 1,
                            endTime = 2
                        )
                    )
                )
            ),
            //  multiple test cycles; multiple test results
            arrayOf(
                PublicationContext(
                    zephyrConfig = dummyConfig,
                    testCycles = listOf(
                        ZephyrTestCycle(
                            testResults = listOf(
                                ZephyrTestResult(testCaseKey = "key-one", startTime = 1, endTime = 2),
                                ZephyrTestResult(testCaseKey = "key-two", startTime = 1, endTime = 2)
                            ),
                            startTime = 1,
                            endTime = 2
                        ),
                        ZephyrTestCycle(
                            testResults = listOf(
                                ZephyrTestResult(testCaseKey = "key-three", startTime = 1, endTime = 2),
                                ZephyrTestResult(testCaseKey = "key-four", startTime = 1, endTime = 2)
                            ),
                            startTime = 1,
                            endTime = 2
                        )
                    )
                )
            )
        )
}
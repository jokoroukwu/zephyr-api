package io.github.jokoroukwu.zephyrapi.testcasekeycomplementor

import io.github.jokoroukwu.zephyrapi.AbstractTest
import io.github.jokoroukwu.zephyrapi.publication.*
import io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor.*
import io.mockk.*
import org.assertj.core.api.Assertions
import org.testng.annotations.*

class TestCaseKeyComplementorTest: AbstractTest() {
    private val getTestCaseRequestSenderMock = mockk<GetTestCasesRequestSender>()
    private val nextProcessorMock = mockk<PublicationDataProcessor>()
    private lateinit var testCaseItemComplementor: TestCaseItemComplementor

    @BeforeMethod
    fun setUp() {
        testCaseItemComplementor = TestCaseItemComplementor(getTestCaseRequestSenderMock, nextProcessorMock)
    }

    @AfterMethod(alwaysRun = true)
    fun tearDown() {
        clearAllMocks()
    }

    @AfterClass(alwaysRun = true)
    private fun afterClass() {
        unmockkObject(getTestCaseRequestSenderMock, nextProcessorMock)
    }

    @DataProvider
    private fun toBeProcessedResults(): Array<Array<Any?>> {
        val testResultOne = ZephyrTestResult(testCaseKey = "1", startTime = 1, endTime = 2)
        val testResultTwo = ZephyrTestResult(testCaseKey = "2", startTime = 1, endTime = 2)
        return arrayOf(
            arrayOf(
                testResultOne, testResultTwo
            )
        )
    }

    @Test(dataProvider = "toBeProcessedResults")
    private fun `should propagate next processor result`(
        zephyrTestResultOne: ZephyrTestResult,
        zephyrTestResultTwo: ZephyrTestResult
    ) {
        val resultList = listOf(zephyrTestResultOne, zephyrTestResultTwo)
        val testCaseItems = resultList
            .map(ZephyrTestResult::testCaseKey)
            .map {
                TestCaseItem(
                    id = -1, key = it, projectId = -1, testData = emptyList(), testScript = TestScriptItem(emptyList())
                )
            }
        every { getTestCaseRequestSenderMock.requestTestCases(any(),any()) } returns GetTestCasesResponse(
            total = -1,
            startAt = -1,
            maxResults = -1,
            results = testCaseItems
        )
        every { nextProcessorMock.process(any()) } returns false
        val inputPublicationData = PublicationContext(
            zephyrConfig = dummyConfig,
            testCycles = listOf(
                ZephyrTestCycle(
                    testResults = resultList,
                    startTime = resultList.minOf(TestResult::startTime),
                    endTime = resultList.maxOf(TestResult::endTime)
                )
            )
        )
        val result = testCaseItemComplementor.process(inputPublicationData)

        Assertions.assertThat(result)
            .`as`("check next processor result is propagated")
            .isFalse
    }

    @Test(dataProvider = "toBeProcessedResults")
    private fun `should call next processor with expected publication data`(
        zephyrTestResultOne: ZephyrTestResult,
        zephyrTestResultTwo: ZephyrTestResult
    ) {
        val resultList = listOf(zephyrTestResultOne, zephyrTestResultTwo)
        val testCaseItems = resultList
            .map(ZephyrTestResult::testCaseKey)
            .map {
                TestCaseItem(
                    id = -1, key = it, projectId = -1, testData = emptyList(), testScript = TestScriptItem(emptyList())
                )
            }
        every { getTestCaseRequestSenderMock.requestTestCases(any(),any()) } returns GetTestCasesResponse(
            total = -1,
            startAt = -1,
            maxResults = -1,
            results = testCaseItems
        )
        val actualPublicationData = slot<PublicationContext>()
        val inputPublicationData = PublicationContext(
            zephyrConfig = dummyConfig,
            testCycles = listOf(
                ZephyrTestCycle(
                    testResults = resultList,
                    startTime = resultList.minOf(TestResult::startTime),
                    endTime = resultList.maxOf(TestResult::endTime)
                )
            )
        )
        val expectedPublicationData =
            inputPublicationData.copy(testCaseKeyToItemMap = testCaseItems.associateBy(TestCaseItem::key))
        every { nextProcessorMock.process(capture(actualPublicationData)) } returns true
        testCaseItemComplementor.process(inputPublicationData)


        Assertions.assertThat(actualPublicationData.captured)
            .`as`("check next processor is called with expected publication data")
            .isEqualTo(expectedPublicationData)
    }

    @Test(dataProvider = "toBeProcessedResults")
    private fun `should call request sender with expected arguments`(
        zephyrTestResultOne: ZephyrTestResult,
        zephyrTestResultTwo: ZephyrTestResult
    ) {
        val capturedArgs = slot<Collection<String>>()
        justRun { getTestCaseRequestSenderMock.requestTestCases(capture(capturedArgs),any()) }
        val publicationData = PublicationContext(
            zephyrConfig = dummyConfig,
            testCycles = listOf(
                ZephyrTestCycle(
                    testResults = listOf(zephyrTestResultOne),
                    startTime = zephyrTestResultOne.startTime,
                    endTime = zephyrTestResultOne.startTime
                ),
                ZephyrTestCycle(
                    testResults = listOf(zephyrTestResultTwo),
                    startTime = zephyrTestResultTwo.startTime,
                    endTime = zephyrTestResultTwo.endTime
                )
            )
        )
        testCaseItemComplementor.runCatching { process(publicationData) }

        val expectedArgs = listOf(zephyrTestResultOne, zephyrTestResultTwo)
            .map(ZephyrTestResult::testCaseKey)
        Assertions.assertThat(capturedArgs.captured)
            .`as`("check request sender called with expected test case keys")
            .containsExactlyInAnyOrderElementsOf(expectedArgs)
    }

    @DataProvider
    private fun notToBeProcessedCycles(): Array<Array<Any?>> {
        return arrayOf(
            arrayOf(
                PublicationContext(
                    zephyrConfig = dummyConfig,

                    testCycles = listOf(
                        ZephyrTestCycle(startTime = 1, endTime = 2),
                        ZephyrTestCycle(
                            testResults = listOf(ZephyrTestResult(startTime = 1, endTime = 2)),
                            startTime = 1,
                            endTime = 2
                        ),
                        ZephyrTestCycle(
                            testResults = listOf(
                                ZephyrTestResult(
                                    testCaseKey = "  ", startTime = 1,
                                    endTime = 2
                                )
                            ),
                            startTime = 1,
                            endTime = 2
                        )
                    )
                )
            )
        )
    }

    @Test(dataProvider = "notToBeProcessedCycles")
    private fun `should not call request sender`(publicationContext: PublicationContext) {
        justRun { getTestCaseRequestSenderMock.requestTestCases(any(),any()) }
        publicationContext.runCatching(testCaseItemComplementor::process)

        verify(exactly = 0) { getTestCaseRequestSenderMock.requestTestCases(any(),any()) }
    }

    @Test(dataProvider = "notToBeProcessedCycles")
    private fun `should return false when no test case keys are present`(publicationContext: PublicationContext) {
        val result = testCaseItemComplementor.process(publicationContext)

        Assertions.assertThat(result)
            .`as`("should return false when no test case keys are present")
            .isFalse
    }

    @Test(dataProvider = "notToBeProcessedCycles")
    private fun `should not call next processor when no test case keys are present`(publicationContext: PublicationContext) {
        testCaseItemComplementor.process(publicationContext);

        verify(exactly = 0) { nextProcessorMock.process(any()) }
    }

    @Test(dataProvider = "notToBeProcessedCycles")
    private fun `should not call request sender when no test case keys are present`(publicationContext: PublicationContext) {
        testCaseItemComplementor.process(publicationContext);

        verify(exactly = 0) { getTestCaseRequestSenderMock.requestTestCases(any(),any()) }
    }

    override fun getObjectsToUnmock(): Array<Any> = arrayOf()
}
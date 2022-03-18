package io.github.jokoroukwu.zephyrapi.testcyclefilter

import io.github.jokoroukwu.zephyrapi.publication.TestDataResult
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestResult
import io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor.TestCaseItem
import io.github.jokoroukwu.zephyrapi.publication.testcyclefilter.OutOfBoundsStepFilter
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkObject
import org.assertj.core.api.SoftAssertions
import org.testng.annotations.AfterClass
import org.testng.annotations.AfterMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class OutOfBoundsStepFilterTest {
    private val filter = OutOfBoundsStepFilter()
    private val testCaseItemMock = mockk<TestCaseItem>()
    private val dataSetResultMock = mockk<TestDataResult>()


    @AfterMethod(alwaysRun = true)
    fun tearDown() {
        unmockkObject(testCaseItemMock, dataSetResultMock)
    }

    @AfterClass(alwaysRun = true)
    fun afterClass() {
        clearAllMocks()
    }

    @Test(dataProvider = "outOfBoundsStepProvider")
    fun `should return null result when all steps out of bounds`(
        testCaseItem: TestCaseItem,
        testResult: ZephyrTestResult
    ) {
        val result = filter.filterTestResult(testResult, testCaseItem)
        with(SoftAssertions()) {
            assertThat(result.testResult)
                .`as`("check test result is null")
                .isNull()

            val expectedProblemCount = testResult.testDataResults.size
            assertThat(result.issues.size)
                .`as`("check problem count equals expected")
                .isEqualTo(expectedProblemCount)
            assertAll()
        }
    }

    @Test(dataProvider = "stepsWithinBoundsProvider")
    fun `should return same result when all steps are within bounds`(
        testCaseItem: TestCaseItem,
        testResult: ZephyrTestResult
    ) {
        val result = filter.filterTestResult(testResult, testCaseItem)
        with(SoftAssertions()) {
            assertThat(result.testResult)
                .`as`("check test result is unchanged")
                .isEqualTo(testResult)

            assertThat(result.issues)
                .`as`("check problem count equals expected")
                .isEmpty()
            assertAll()
        }
    }

    @Test
    fun `should return expected result when some steps out of bounds and some not`() {
        every { testCaseItemMock.testScript.steps } returns mockk {
            every { size } returns 3
        }
        with(dataSetResultMock) {
            every { failedStepIndex } returns 0
            every { index } returns 0
        }
        val secondDataSet = mockk<TestDataResult> {
            every { failedStepIndex } returns 4
            every { index } returns 2
        }
        val testResult =
            ZephyrTestResult(testDataResults = listOf(dataSetResultMock, secondDataSet), startTime = 1, endTime = 2)
        val result = filter.filterTestResult(testResult, testCaseItemMock)


        with(SoftAssertions()) {
            assertThat(result.testResult)
                .`as`("zephyr result should contain expected datasets")
                .isEqualTo(ZephyrTestResult(testDataResults = listOf(dataSetResultMock), startTime = 1, endTime = 2))

            assertThat(result.issues)
                .`as`("check problem count equals expected")
                .hasSize(1)
            assertAll()
        }
    }

    @DataProvider
    private fun stepsWithinBoundsProvider(): Array<Array<Any?>> {
        every { testCaseItemMock.testScript.steps } returns mockk {
            every { size } returns 3
        }

        with(dataSetResultMock) {
            every { failedStepIndex } returnsMany listOf(0, 1, 2)
            every { index } returnsMany listOf(1, 2, 3)
        }

        return arrayOf(
            arrayOf(
                testCaseItemMock,
                ZephyrTestResult(testDataResults = listOf(dataSetResultMock), startTime = 1, endTime = 2)
            ),
            arrayOf(
                testCaseItemMock,
                ZephyrTestResult(
                    testDataResults = listOf(dataSetResultMock, dataSetResultMock),
                    startTime = 1,
                    endTime = 2
                )
            )
        )
    }

    @DataProvider
    private fun outOfBoundsStepProvider(): Array<Array<Any?>> {
        every { testCaseItemMock.testScript.steps } returns mockk {
            every { size } returns 2
        }

        with(dataSetResultMock) {
            every { failedStepIndex } returnsMany listOf(3, 4, 5)
            every { index } returnsMany listOf(1, 2, 3)
        }
        return arrayOf(
            arrayOf(
                testCaseItemMock,
                ZephyrTestResult(testDataResults = listOf(dataSetResultMock), startTime = 1, endTime = 2)
            ),
            arrayOf(
                testCaseItemMock,
                ZephyrTestResult(
                    testDataResults = listOf(dataSetResultMock, dataSetResultMock),
                    startTime = 1,
                    endTime = 2
                )
            )
        )
    }
}
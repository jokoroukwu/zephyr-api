package io.github.jokoroukwu.zephyrapi.testcyclefilter

import io.github.jokoroukwu.zephyrapi.publication.TestDataResult
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestResult
import io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor.TestCaseItem
import io.github.jokoroukwu.zephyrapi.publication.testcyclefilter.OutOfBoundsTestDataFilter
import io.github.jokoroukwu.zephyrapi.publication.testcyclefilter.TestResultFilter
import io.mockk.*
import org.testng.annotations.AfterClass
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class OutOfBoundsTestDataFilterTest {
    private val nextFilterMock = mockk<TestResultFilter>(relaxed = true)
    private val dataSetMock = mockk<TestDataResult>()
    private val testCaseItemMock = mockk<TestCaseItem>()

    private val testDataFilter = OutOfBoundsTestDataFilter(nextFilterMock)

    @BeforeMethod
    fun setUp() {
    }

    @AfterMethod(alwaysRun = true)
    fun tearDown() {
        unmockkObject(nextFilterMock, dataSetMock, testCaseItemMock)
    }

    @AfterClass(alwaysRun = true)
    fun afterClass() {
        clearAllMocks()
    }

    @Test
    private fun `should call next processor when test result non data driven`() {
        val testResult = ZephyrTestResult(testDataResults = listOf(dataSetMock), startTime = 1, endTime = 2)
        testDataFilter.filterTestResult(testResult, testCaseItemMock)
        verify(exactly = 1) { nextFilterMock.filterTestResult(testResult, testCaseItemMock) }
    }
}
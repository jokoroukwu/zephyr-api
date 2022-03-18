package io.github.jokoroukwu.zephyrapi.integration

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.util.encodeBase64ToString
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.BasicCredentials
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import io.github.jokoroukwu.zephyrapi.publication.TestDataResultBase
import io.github.jokoroukwu.zephyrapi.publication.TestResultBase
import io.github.jokoroukwu.zephyrapi.publication.TestRunBase
import io.github.jokoroukwu.zephyrapi.api.ZephyrClient
import io.github.jokoroukwu.zephyrapi.api.annotations.Step
import io.github.jokoroukwu.zephyrapi.api.annotations.TestCaseKey
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfig
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigLoaderImpl
import io.github.jokoroukwu.zephyrapi.http.AbstractRequestSender.Companion.BASE_API_URL
import io.github.jokoroukwu.zephyrapi.http.JsonMapper
import io.github.jokoroukwu.zephyrapi.integration.util.*
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.*
import io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor.*
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.SerializableTestResult
import io.github.jokoroukwu.zephyrapi.publication.testcyclecreator.CreateTestCycleRequest
import io.github.jokoroukwu.zephyrapi.publication.testcycleupdater.UpdateTestCycleRequest
import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.TestResultStatus
import io.mockk.unmockkAll
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.time.Instant
import java.util.*
import java.util.stream.Collectors

const val DEFAULT_CYCLE_KEY = "test-cycle-key"
const val DEFAULT_PROJECT_ID = 1L
const val DEFAULT_CYCLE_ID = 1L
const val DATA_DRIVEN_TEST_CASE_KEY = "data-driven-test-case-key"
const val NON_DATA_DRIVEN_TEST_CASE = "non-data-driven-test-case-key"

class HappyPathTest {
    private val wireMockPort = 2355
    private val wireMockHttpsPort = 2354
    private val zephyrConfig = ZephyrConfigLoaderImpl.getZephyrConfig()
    private val idOne = 1L
    private val idTwo = 2L

    private val wireMock = WireMockServer(WireMockConfiguration().httpsPort(wireMockHttpsPort).port(wireMockPort))
        .also { configureFor("localhost", wireMockPort) }

    private val testCaseItems = listOf(
        TestCaseItem(
            id = 1,
            key = NON_DATA_DRIVEN_TEST_CASE,
            projectId = DEFAULT_PROJECT_ID,
            testData = listOf(),
            testScript = TestScriptItem(listOf(StepItem(0), StepItem(1)))
        ),
        TestCaseItem(
            id = 2,
            key = DATA_DRIVEN_TEST_CASE_KEY,
            projectId = DEFAULT_PROJECT_ID,
            testData = listOf(TestDataItem(1), TestDataItem(2)),
            testScript = TestScriptItem(listOf(StepItem(0), StepItem(1)))
        )
    )

    private val testRun = TestRunBase(
        name = "test-run-1",
        testResults = listOf(
            TestResultBase(
                NON_DATA_DRIVEN_TEST_CASE,
                listOf(TestDataResultBase(0, false, 1, "step 1 failed")),
                1,
                2
            ),
            TestResultBase(
                DATA_DRIVEN_TEST_CASE_KEY,
                listOf(
                    TestDataResultBase(0, false, 0, "step 0 failed"),
                    TestDataResultBase(1, true, null)
                ),
                3,
                4
            )
        ),
        1,
        4
    )
    private val reportTestResults = listOf(
        ReportTestResult(
            id = idOne,
            testCase = TestCase(idOne, NON_DATA_DRIVEN_TEST_CASE, "non-data-driven-test-case-name"),
            testScriptResults = listOf(ReportStepResult(idOne, 0), ReportStepResult(idTwo, 1))
        ),
        ReportTestResult(
            id = idTwo,
            testCase = TestCase(idTwo, DATA_DRIVEN_TEST_CASE_KEY, "data-driven-test-case-name"),
            testScriptResults = listOf(
                ReportStepResult(3, 0), ReportStepResult(4, 1),
                ReportStepResult(5, 0), ReportStepResult(6, 1)
            )
        )
    )

    private lateinit var createTestCycleStub: UUID
    private lateinit var updateTestCycleStub: UUID
    private lateinit var updateTestResultsStub: UUID
    private lateinit var updateTestScriptResultsStub: UUID
    private lateinit var getDetailedReportStub: UUID


    @BeforeClass
    fun setUp() {
        with(wireMock) {
            start()
            stubGetTestCasesRequest(testCaseItems)
            stubGetTestResultStatusesRequest()
            createTestCycleStub = stubCreateTestCycleRequest()
            updateTestCycleStub = stubUpdateTestCycleRequest()
            getDetailedReportStub = stubGetDetailedReportRequest(TestRunDetailReport(reportTestResults))
            updateTestResultsStub = stubUpdateTestResultsRequest()
            updateTestScriptResultsStub = stubUpdateTestScriptResultsRequest()
        }
        ZephyrClient.publishTestResults(listOf(testRun))
    }


    @Test
    fun `should submit single valid getTestCases request`() {
        val inClause = listOf(DATA_DRIVEN_TEST_CASE_KEY, NON_DATA_DRIVEN_TEST_CASE).joinToString(
            prefix = "('", separator = "','", postfix = "')"
        )
        val url = "$BASE_API_URL/testcase/search?fields=id,key,projectId,testData(id)," +
                "testScript(steps(index))&maxResults=$MAX_TEST_CASE_COUNT&query=testCase.key%20IN$inClause"

        verify(
            exactly(1), getRequestedFor(urlEqualTo(url))
                .withBasicAuth(BasicCredentials(zephyrConfig.username(), zephyrConfig.password()))
        )
    }

    @Test
    fun `should submit single valid getTestResultStatuses request`() {
        val url = "$BASE_API_URL/project/$DEFAULT_PROJECT_ID/testresultstatus"
        verify(
            exactly(1), getRequestedFor(urlEqualTo(url))
                .withBasicAuth(BasicCredentials(zephyrConfig.username(), zephyrConfig.password()))
        )
    }

    @Test
    fun `should submit single valid createTestCycleRequest`() {
        val request = wireMock.assertHasSingleRequest(
            createTestCycleStub, "should have received single CreateTestCycle request"
        )
        softly {
            assertThat(request.header(Headers.AUTHORIZATION).firstValue())
                .`as`("should have expected auth header")
                .isEqualTo(zephyrConfig.basicAuthBase64())


            val actualCreateTestCycleRequest: CreateTestCycleRequest = Json.decodeFromString(request.bodyAsString);
            val expectedStartDate = Instant.ofEpochMilli(testRun.startTime).toString()
            val expectedEndDate = Instant.ofEpochMilli(testRun.endTime).toString()
            val expectedCreateTestCycleRequest =
                CreateTestCycleRequest(DEFAULT_PROJECT_ID, testRun.name, expectedStartDate, expectedEndDate)

            assertThat(actualCreateTestCycleRequest)
                .`as`("should have received expected request")
                .isEqualTo(expectedCreateTestCycleRequest)
            assertAll()
        }
    }

    @Test
    fun `should submit single valid 'update test cycle request'`() {
        val request = wireMock.assertHasSingleRequest(
            updateTestCycleStub, "UpdateTestCycle request count validation"
        )

        softly {
            val description = "Update test cycle request: %s"
            assertThat(request.header(Headers.AUTHORIZATION).firstValue())
                .`as`(description, "basic authorization header")
                .isEqualTo(zephyrConfig.basicAuthBase64())

            assertThat(request.method)
                .`as`(description, "method")
                .isEqualTo(RequestMethod.PUT)

            assertThat(request)
                .`as`(description, "body")
                .satisfies { rq ->
                    val updateTestCycleRequest = Json.decodeFromString<UpdateTestCycleRequest>(rq.bodyAsString)
                    assertThat(updateTestCycleRequest.testRunId)
                        .`as`(description, "test run id")
                        .isEqualTo(DEFAULT_CYCLE_ID)

                    assertThat(updateTestCycleRequest.addedTestRunItems.map { it.lastTestResult.testCaseId })
                        .`as`(description, "test case ids")
                        .containsExactlyInAnyOrder(1, 2)

                    assertThat(updateTestCycleRequest.addedTestRunItems.map { it.index })
                        .`as`(description, "added test run items' indexes")
                        .containsExactlyInAnyOrder(0, 1)

                    assertThat(updateTestCycleRequest.addedTestRunItems.map { it.id })
                        .`as`(description, "added test run items' ids")
                        .containsOnlyNulls()
                }
            assertAll()
        }
    }

    @Test
    fun `should submit single valid getDetailedReportRequest`() {
        val request = wireMock.assertHasSingleRequest(
            getDetailedReportStub, "get detailed report requests count validation"
        )

        softly {
            val description = "GetDetailedReport request: %s"
            assertThat(request.header(Headers.AUTHORIZATION).firstValue())
                .`as`(description, "basic authorization header")
                .isEqualTo(zephyrConfig.basicAuthBase64())

            assertThat(request.method)
                .`as`(description, "method")
                .isEqualTo(RequestMethod.GET)

            assertAll()
        }

    }

    @Test
    fun `should submit single valid updateTestResultsRequest`() {
        val request = wireMock.assertHasSingleRequest(
            updateTestResultsStub, "UpdateTestResult requests count validation"
        )

        softly {
            val description = "UpdateTestResultsRequest request: %s"
            assertThat(request.header(Headers.AUTHORIZATION).firstValue())
                .`as`(description, "basic authorization header")
                .isEqualTo(zephyrConfig.basicAuthBase64())

            assertThat(request.method)
                .`as`(description, "method")
                .isEqualTo(RequestMethod.PUT)

            val actualTestResults =
                JsonMapper.instance.decodeFromString<List<SerializableTestResult>>(request.bodyAsString)

            val testResultOne = testRun.testResults[0]
            val testResultTwo = testRun.testResults[1]
            val expectedTestResults = listOf(
                SerializableTestResult(
                    id = reportTestResults[0].id,
                    testResultStatusId = TestResultStatus.FAIL.ordinal.toLong(),
                    executionTime = testResultOne.endTime - testResultOne.startTime
                ),
                SerializableTestResult(
                    id = reportTestResults[1].id,
                    testResultStatusId = TestResultStatus.FAIL.ordinal.toLong(),
                    executionTime = testResultTwo.endTime - testResultTwo.startTime
                )
            )
            assertThat(actualTestResults)
                .`as`("should contain expected test results")
                .containsExactlyInAnyOrderElementsOf(expectedTestResults)

            assertAll()
        }
    }

    @Test
    fun `should submit single valid updateTestScriptResultsRequest`() {
        val request = wireMock.assertHasSingleRequest(
            updateTestScriptResultsStub, "UpdateTestScriptResultsRequest count validation"
        )

        softly {
            val description = "UpdateTestScriptResultsRequest request: %s"
            assertThat(request.header(Headers.AUTHORIZATION).firstValue())
                .`as`(description, "basic authorization header")
                .isEqualTo(zephyrConfig.basicAuthBase64())

            assertThat(request.method)
                .`as`(description, "method")
                .isEqualTo(RequestMethod.PUT)
            val actualTestScriptResults =
                JsonMapper.instance.decodeFromString<List<TestScriptResult>>(request.bodyAsString)
            val expectedScriptResults = listOf(
                TestScriptResult(1, TestResultStatus.PASS.ordinal.toLong()),
                TestScriptResult(2, TestResultStatus.FAIL.ordinal.toLong(), "step 1 failed"),
                TestScriptResult(3, TestResultStatus.FAIL.ordinal.toLong(), "step 0 failed"),
                TestScriptResult(4, TestResultStatus.BLOCKED.ordinal.toLong()),
                TestScriptResult(5, TestResultStatus.PASS.ordinal.toLong()),
                TestScriptResult(6, TestResultStatus.PASS.ordinal.toLong()),
            )

            assertThat(actualTestScriptResults)
                .`as`("should contain expected test script results ")
                .containsExactlyInAnyOrderElementsOf(expectedScriptResults)

            assertAll()
        }
    }

    @AfterClass(alwaysRun = true)
    fun tearDown() {
        unmockkAll()
        wireMock.stop()
    }

    private fun WireMockServer.assertHasSingleRequest(id: UUID, description: String) = allServeEvents.stream()
        .filter { event -> event.stubMapping.id == id }
        .map(ServeEvent::getRequest)
        .collect(Collectors.toList())
        .also { Assertions.assertThat(it).`as`(description).hasSize(1) }[0]

    private inline fun <T> softly(assertion: SoftAssertions.() -> T) = assertion(SoftAssertions())


    private fun ZephyrConfig.basicAuthBase64() = "Basic ${"${username()}:${password()}".encodeBase64ToString()}"

}
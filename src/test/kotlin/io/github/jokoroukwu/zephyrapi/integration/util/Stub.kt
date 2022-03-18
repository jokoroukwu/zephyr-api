package io.github.jokoroukwu.zephyrapi.integration.util

import com.github.kittinunf.fuel.core.Headers
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfig
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigLoaderImpl
import io.github.jokoroukwu.zephyrapi.http.AbstractRequestSender.Companion.BASE_API_URL
import io.github.jokoroukwu.zephyrapi.integration.*
import io.github.jokoroukwu.zephyrapi.integration.util.CustomRequestMatcher.urlStartsWith
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.GetDetailedReportResponse
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.TestRunDetailReport
import io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor.*
import io.github.jokoroukwu.zephyrapi.publication.testcyclecreator.CreateTestCycleResponse
import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.SerializableTestResultStatusItem
import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.TestResultStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.http.entity.ContentType
import java.util.*

fun WireMockServer.stubUpdateTestScriptResultsRequest(
    config: ZephyrConfig = ZephyrConfigLoaderImpl.getZephyrConfig(),
    response: ResponseDefinitionBuilder = ok().applyResponseHeaders()
): UUID = stubFor(
    put("$BASE_API_URL/testscriptresult")
        .withBasicAuth(config.username(), config.password())
        .willReturn(response)
).id


fun WireMockServer.stubUpdateTestResultsRequest(config: ZephyrConfig = ZephyrConfigLoaderImpl.getZephyrConfig()): UUID =
    stubFor(
        put("$BASE_API_URL/testresult")
            .withBasicAuth(config.username(), config.password())
            .willReturn(ok().applyResponseHeaders())
    ).id


fun WireMockServer.stubGetDetailedReportRequest(
    testRunDetailReport: TestRunDetailReport,
    cycleKey: String = DEFAULT_CYCLE_KEY,
    projectId: Long = DEFAULT_PROJECT_ID,
    config: ZephyrConfig = ZephyrConfigLoaderImpl.getZephyrConfig()
): UUID {
    return stubFor(
        get(
            "$BASE_API_URL/reports/testresults/detailed?projectId=$projectId&tql=testRun.key%20IN('$cycleKey')"
        ).withBasicAuth(config.username(), config.password())
            .willReturn(
                ok().applyResponseHeaders().withBody(
                    Json.encodeToString(
                        GetDetailedReportResponse(
                            listOf(testRunDetailReport)
                        )
                    )
                )
            )
    ).id
}


fun WireMockServer.stubUpdateTestCycleRequest(
    config: ZephyrConfig = ZephyrConfigLoaderImpl.getZephyrConfig(),
    response: ResponseDefinitionBuilder = ok().applyResponseHeaders()
): UUID = stubFor(
    put("$BASE_API_URL/testrunitem/bulk/save")
        .withBasicAuth(config.username(), config.password())
        .willReturn(response)
).id


fun WireMockServer.stubCreateTestCycleRequest(
    config: ZephyrConfig = ZephyrConfigLoaderImpl.getZephyrConfig(),
    responseBody: () -> CreateTestCycleResponse = {
        CreateTestCycleResponse(id = DEFAULT_CYCLE_ID, key = DEFAULT_CYCLE_KEY)
    }

): UUID {
    return stubFor(
        post("$BASE_API_URL/testrun")
            .withBasicAuth(config.username(), config.password())
            .willReturn(ok().applyResponseHeaders().withBody(Json.encodeToString(responseBody())))
    ).id

}


fun WireMockServer.stubGetTestResultStatusesRequest(

    projectId: Long = DEFAULT_PROJECT_ID,
    config: ZephyrConfig = ZephyrConfigLoaderImpl.getZephyrConfig()
) {
    val serializableStatusItems = EnumSet.allOf(TestResultStatus::class.java)
        .mapIndexed { i, status -> SerializableTestResultStatusItem(i.toLong(), status) }
        .let(Json::encodeToString)
    stubFor(
        get(urlEqualTo("$BASE_API_URL/project/$projectId/testresultstatus"))
            .withBasicAuth(config.username(), config.password())
            .willReturn(ok().applyResponseHeaders().withBody(serializableStatusItems))
    )
}


fun WireMockServer.stubGetTestCasesRequest(
    resultItems: List<TestCaseItem>,
    zephyrNgConfig: ZephyrConfig = ZephyrConfigLoaderImpl.getZephyrConfig()
) {
    val response =
        GetTestCasesResponse(
            results = resultItems,
            total = resultItems.size,
            startAt = 0,
            maxResults = MAX_TEST_CASE_COUNT
        )
    val url = "$BASE_API_URL/testcase/search?fields=id,key,projectId,testData(id)," +
            "testScript(steps(index))&maxResults=$MAX_TEST_CASE_COUNT&query=testCase.key%20IN"
    val jsonStub = Json.encodeToString(response)

    stubFor(
        get(anyUrl())
            .withBasicAuth(zephyrNgConfig.username(), zephyrNgConfig.password())
            .andMatching(urlStartsWith(url))
            .willReturn(ok().applyResponseHeaders().withBody(jsonStub))
    )
}


private fun ResponseDefinitionBuilder.applyResponseHeaders() =
    withHeader(Headers.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
        .withHeader("Connection", "keep-alive")


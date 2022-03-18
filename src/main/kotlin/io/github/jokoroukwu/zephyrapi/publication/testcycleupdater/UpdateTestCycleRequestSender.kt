package io.github.jokoroukwu.zephyrapi.publication.testcycleupdater

import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestCycle
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestResult
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigImpl
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigLoaderImpl
import io.github.jokoroukwu.zephyrapi.http.AbstractRequestSender
import io.github.jokoroukwu.zephyrapi.http.JsonMapper
import io.github.jokoroukwu.zephyrapi.http.ZephyrException
import io.github.jokoroukwu.zephyrapi.http.ZephyrResponseDeserializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UpdateTestCycleRequestSender(
    zephyrConfig: ZephyrConfigImpl = ZephyrConfigLoaderImpl.getZephyrConfig(),
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val url = "${baseUrl}/testrunitem/bulk/save"
    private val errorMessageTemplate = "Failed to add test cases to Zephyr test cycle"

    /**
     * Performs an HTTP-request to populate a previously created test cycle with test cases from [zephyrTestCycle]
     *
     * @see [ZephyrTestCycle]
     */
    suspend fun updateTestCycle(zephyrTestCycle: ZephyrTestCycle) {
        requestFactory.runCatching {
            put(url)
                .authentication().basic(zephyrConfig.username(), zephyrConfig.password())
                .jsonBody(
                    jsonMapper.encodeToString(
                        UpdateTestCycleRequest(
                            testRunId = zephyrTestCycle.id,
                            addedTestRunItems = mapTestResultsToTestRunItems(zephyrTestCycle.testResults)
                        )
                    )
                )
                .treatResponseAsValid()
                .await(ZephyrResponseDeserializer)
        }.getOrElse { cause -> throw ZephyrException("$errorMessageTemplate '${zephyrTestCycle.key}'", cause) }
            .validateStatusCode {
                "$errorMessageTemplate: unsuccessful status code: test_cycle: {name: '${zephyrTestCycle.name}, key: '${zephyrTestCycle.key}'}"
            }
    }

    private fun mapTestResultsToTestRunItems(testResults: Collection<ZephyrTestResult>): List<TestRunItem> =
        testResults.mapIndexedTo(ArrayList(testResults.size))
        { i, testResult -> TestRunItem(i, UpdateTestCycleTestResult(testResult.testCaseId)) }

}
package io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor

import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.response
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfig
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigImpl
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigLoaderImpl
import io.github.jokoroukwu.zephyrapi.http.AbstractRequestSender
import io.github.jokoroukwu.zephyrapi.http.JsonMapper
import io.github.jokoroukwu.zephyrapi.http.ZephyrException
import io.github.jokoroukwu.zephyrapi.http.ZephyrResponseDeserializer
import kotlinx.serialization.json.Json

const val MAX_TEST_CASE_COUNT = 99999

class GetTestCasesRequestSender(
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory
) : AbstractRequestSender(jsonMapper, requestFactory) {

    private val urlTemplate = "/testcase/search?fields=id,key,projectId,testData(id)," +
            "testScript(steps(index))&maxResults=$MAX_TEST_CASE_COUNT&query=testCase.key IN"
    private val errorMessageTemplate = "Failed to fetch test cases from Zephyr"

    /**
     * Attempts to fetch test cases from Zephyr by provided [testCaseKeys]
     */
    fun requestTestCases(testCaseKeys: Collection<String>, zephyrConfig: ZephyrConfig): GetTestCasesResponse {
        val url = zephyrConfig.jiraUrl.resolveApiUrl(urlTemplate + testCaseKeys.joinToString("','", "('", "')"))
        return zephyrConfig.runCatching {
            requestFactory.get(url)
                .authentication().basic(username, password)
                .treatResponseAsValid()
                .response(ZephyrResponseDeserializer)
                .third.get()
        }.onFailure { cause -> throw ZephyrException(errorMessageTemplate, cause) }
            .mapCatching { it.getJsonBody<GetTestCasesResponse>() }
            .getOrElse { cause -> throw ZephyrException("$errorMessageTemplate: failed to deserialize body", cause) }
    }
}
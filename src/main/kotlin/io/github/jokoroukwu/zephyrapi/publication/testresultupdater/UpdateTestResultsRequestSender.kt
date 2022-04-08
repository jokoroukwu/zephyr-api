package io.github.jokoroukwu.zephyrapi.publication.testresultupdater

import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfig
import io.github.jokoroukwu.zephyrapi.http.AbstractRequestSender
import io.github.jokoroukwu.zephyrapi.http.JsonMapper
import io.github.jokoroukwu.zephyrapi.http.ZephyrException
import io.github.jokoroukwu.zephyrapi.http.ZephyrResponseDeserializer
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.SerializableTestResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UpdateTestResultsRequestSender(
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory
) : AbstractRequestSender(jsonMapper, requestFactory) {

    private val urlPath = "/testresult"

    suspend fun updateTestResults(testResults: List<SerializableTestResult>, zephyrConfig: ZephyrConfig) {
        zephyrConfig.runCatching {
            requestFactory.put(jiraUrl.resolveApiUrl(urlPath))
                .authentication().basic(username, password)
                .jsonBody(jsonMapper.encodeToString(testResults))
                .await(ZephyrResponseDeserializer)
        }.onFailure { cause -> throw ZephyrException("Failed to update test results", cause) }
    }
}

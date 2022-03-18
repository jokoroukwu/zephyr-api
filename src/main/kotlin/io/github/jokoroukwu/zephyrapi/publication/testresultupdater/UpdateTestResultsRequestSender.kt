package io.github.jokoroukwu.zephyrapi.publication.testresultupdater

import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfig
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigLoaderImpl
import io.github.jokoroukwu.zephyrapi.http.AbstractRequestSender
import io.github.jokoroukwu.zephyrapi.http.JsonMapper
import io.github.jokoroukwu.zephyrapi.http.ZephyrException
import io.github.jokoroukwu.zephyrapi.http.ZephyrResponseDeserializer
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.SerializableTestResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UpdateTestResultsRequestSender(
    zephyrConfig: ZephyrConfig = ZephyrConfigLoaderImpl.getZephyrConfig(),
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val url = "$baseUrl/testresult"

    suspend fun updateTestResults(testResults: List<SerializableTestResult>) {
        requestFactory.runCatching {
            put(url)
                .authentication().basic(zephyrConfig.username(), zephyrConfig.password())
                .jsonBody(jsonMapper.encodeToString(testResults))
                .await(ZephyrResponseDeserializer)
        }.onFailure { cause -> throw ZephyrException("Failed to update test results", cause) }
    }
}

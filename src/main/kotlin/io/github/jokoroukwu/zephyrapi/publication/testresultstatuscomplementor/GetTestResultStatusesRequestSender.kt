package io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor

import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.response
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigImpl
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigLoaderImpl
import io.github.jokoroukwu.zephyrapi.http.AbstractRequestSender
import io.github.jokoroukwu.zephyrapi.http.JsonMapper
import io.github.jokoroukwu.zephyrapi.http.ZephyrException
import io.github.jokoroukwu.zephyrapi.http.ZephyrResponseDeserializer
import kotlinx.serialization.json.Json

class GetTestResultStatusesRequestSender(
    zephyrConfig: ZephyrConfigImpl = ZephyrConfigLoaderImpl.getZephyrConfig(),
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val urlTemplate = "$baseUrl/project/%d/testresultstatus"
    private val errorMessageTemplate = "Failed to fetch test result statuses"

    fun getTestResultStatusesRequest(
        zephyrProjectId: Long
    ): List<SerializableTestResultStatusItem> {
        return zephyrConfig.runCatching {
            requestFactory.get(urlTemplate.format(zephyrProjectId))
                .authentication().basic(username(), password())
                .treatResponseAsValid()
                .response(ZephyrResponseDeserializer)
                .third.get()
        }.getOrElse { cause -> throw ZephyrException(errorMessageTemplate, cause) }
            .validateStatusCode { "${errorMessageTemplate}: unsuccessful status code" }
            .runCatching { getJsonBody<List<SerializableTestResultStatusItem>>() }
            .getOrElse { cause -> throw ZephyrException("${errorMessageTemplate}: deserialization error", cause) }
    }
}
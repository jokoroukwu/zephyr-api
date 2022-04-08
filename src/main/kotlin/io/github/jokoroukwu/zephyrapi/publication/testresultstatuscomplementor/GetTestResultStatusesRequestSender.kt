package io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor

import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.response
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfig
import io.github.jokoroukwu.zephyrapi.http.AbstractRequestSender
import io.github.jokoroukwu.zephyrapi.http.JsonMapper
import io.github.jokoroukwu.zephyrapi.http.ZephyrException
import io.github.jokoroukwu.zephyrapi.http.ZephyrResponseDeserializer
import kotlinx.serialization.json.Json

class GetTestResultStatusesRequestSender(
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory
) : AbstractRequestSender(jsonMapper, requestFactory) {

    private val urlPath = "/project/%d/testresultstatus"
    private val errorMessageTemplate = "Failed to fetch test result statuses"

    fun getTestResultStatusesRequest(
        zephyrProjectId: Long,
        zephyrConfig: ZephyrConfig
    ): List<SerializableTestResultStatusItem> {
        return zephyrConfig.runCatching {
            requestFactory.get(jiraUrl.resolveApiUrl(urlPath.format(zephyrProjectId)))
                .authentication().basic(username, password)
                .treatResponseAsValid()
                .response(ZephyrResponseDeserializer)
                .third.get()
        }.getOrElse { cause -> throw ZephyrException(errorMessageTemplate, cause) }
            .validateStatusCode { "${errorMessageTemplate}: unsuccessful status code" }
            .runCatching { getJsonBody<List<SerializableTestResultStatusItem>>() }
            .getOrElse { cause -> throw ZephyrException("${errorMessageTemplate}: deserialization error", cause) }
    }
}
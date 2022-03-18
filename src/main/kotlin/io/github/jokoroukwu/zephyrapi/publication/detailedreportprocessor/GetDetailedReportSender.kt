package io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor

import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.response
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigImpl
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigLoaderImpl
import io.github.jokoroukwu.zephyrapi.http.*
import io.github.jokoroukwu.zephyrapi.publication.PublicationData
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestCycle
import kotlinx.serialization.json.Json

class GetDetailedReportSender(
    zephyrConfig: ZephyrConfigImpl = ZephyrConfigLoaderImpl.getZephyrConfig(),
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val urlTemplate = "$baseUrl/reports/testresults/detailed?projectId=%d&tql=testRun.key IN%s"
    private val errorMessageTemplate = "Failed to fetch detailed report"

    fun getDetailedReport(publicationData: PublicationData): GetDetailedReportResponse {
        val url: String = urlTemplate.format(publicationData.projectId, publicationData.testCycles.toQueryString())
        return requestFactory.get(url)
            .authentication().basic(zephyrConfig.username(), zephyrConfig.password())
            .treatResponseAsValid()
            .response(ZephyrResponseDeserializer)
            .third
            .fold({ response -> response }, { cause -> throw ZephyrException("$errorMessageTemplate: ", cause) })
            .runCatching<ZephyrResponse, GetDetailedReportResponse>(ZephyrResponse::getJsonBody)
            .getOrElse { cause ->
                throw ZephyrException("$errorMessageTemplate: body deserialization error:", cause)
            }
    }

    private fun Collection<ZephyrTestCycle>.toQueryString() = joinToString(
        separator = "','",
        prefix = "('",
        postfix = "')",
        transform = ZephyrTestCycle::key
    )

}
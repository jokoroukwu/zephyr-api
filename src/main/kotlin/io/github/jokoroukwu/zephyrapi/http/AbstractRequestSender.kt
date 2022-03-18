package io.github.jokoroukwu.zephyrapi.http

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.Response
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfig
import kotlinx.serialization.json.Json
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext


abstract class AbstractRequestSender(
    protected val zephyrConfig: ZephyrConfig,
    protected val jsonMapper: Json,
    protected val requestFactory: RequestFactory.Convenience,
) {
    protected val baseUrl = "${zephyrConfig.jiraUrl()}$BASE_API_URL"

    companion object {
        private val noValidation = { _: Response -> true }
        private const val DEFAULT_TIMEOUT = 10_000
        const val BASE_API_URL = "/rest/tests/1.0"
        val defaultRequestFactory = FuelManager().apply {
            timeoutInMillisecond = DEFAULT_TIMEOUT
            timeoutReadInMillisecond = DEFAULT_TIMEOUT
            socketFactory = with(SSLContext.getInstance("SSL")) {
                init(null, arrayOf(TrustAllCertManager), null)
                socketFactory
            }
            hostnameVerifier = HostnameVerifier { _, _ -> true }
        }
    }

    protected inline fun ZephyrResponse.validateStatusCode(
        validCode: Int = 200,
        errorMessage: () -> String
    ): ZephyrResponse {
        if (statusCode != validCode) {
            throw ZephyrException("${errorMessage()}: {response_message: $responseMessage as server returned status code $statusCode, response_body: ${getStringBody()}}")
        }
        return this
    }

    protected fun Request.treatResponseAsValid(): Request {
        executionOptions.responseValidator = noValidation
        return this
    }
}
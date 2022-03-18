package io.github.jokoroukwu.zephyrapi.publication.testcyclecreator

import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import io.github.jokoroukwu.zephyrapi.publication.TestRun
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigImpl
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigLoaderImpl
import io.github.jokoroukwu.zephyrapi.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

class CreateTestCycleRequestSender(
    zephyrConfig: ZephyrConfigImpl = ZephyrConfigLoaderImpl.getZephyrConfig(),
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory,
) : AbstractRequestSender(zephyrConfig, jsonMapper, requestFactory) {

    private val url = "$baseUrl/testrun"
    private val errorMessageTemplate = "failed to create Zephyr test cycle"

    /**
     * Performs an HTTP request to create a new Zephyr test cycle.
     * The provided [testNgZephyrSuite] name will be joined with start and end time properties
     * and used as test cycle name.
     *
     * @param projectId JIRA project id
     * @param testNgZephyrSuite suite
     */
    suspend fun createTestCycle(
        projectId: Long,
        zephyrTestCycle: TestRun,
    ): CreateTestCycleResponse {
        val testCycleName = zephyrTestCycle.name
        return zephyrConfig.runCatching {
            requestFactory.post(url)
                .authentication().basic(username(), password())
                .jsonBody(
                    jsonMapper.encodeToString(
                        CreateTestCycleRequest(
                            projectId = projectId,
                            name = testCycleName,
                            plannedStartDate = zephyrTestCycle.startTime.millisToString(),
                            plannedEndDate = zephyrTestCycle.endTime.millisToString(),
                        )
                    )
                ).treatResponseAsValid()
                .await(ZephyrResponseDeserializer)
        }.getOrElse { cause -> throw ZephyrException("$errorMessageTemplate $testCycleName", cause) }
            .validateStatusCode { "$errorMessageTemplate $testCycleName: unsuccessful status code" }
            .runCatching { getJsonBody<CreateTestCycleResponse>() }
            .getOrElse { cause ->
                throw ZephyrException(
                    errorMessageTemplate.format(testCycleName, "body deserialization error"), cause
                )
            }
    }

    private fun Long?.millisToString() = this
        ?.let(Instant::ofEpochMilli)
        ?.toString()
}
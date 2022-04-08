package io.github.jokoroukwu.zephyrapi.publication.testcyclecreator

import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfig
import io.github.jokoroukwu.zephyrapi.publication.TestRun
import io.github.jokoroukwu.zephyrapi.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.format.DateTimeFormatter

class CreateTestCycleRequestSender(
    jsonMapper: Json = JsonMapper.instance,
    requestFactory: RequestFactory.Convenience = defaultRequestFactory,
    private val dateTimeFormatter: DateTimeFormatter = DEFAULT_FORMATTER
) : AbstractRequestSender(jsonMapper, requestFactory) {

    companion object {
        val DEFAULT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-dd-MM'T'HH:mm:ss.SSS")

    }

    private val errorMessageTemplate = "Failed to create Zephyr test cycle"

    /**
     * Performs an HTTP request to create a new Zephyr test cycle.
     *
     * @param projectId JIRA project id
     * @param testRun Test run
     */
    suspend fun createTestCycle(
        projectId: Long,
        testRun: TestRun,
        zephyrConfig: ZephyrConfig
    ): CreateTestCycleResponse {

        return zephyrConfig.runCatching {
            requestFactory.post(jiraUrl.resolveApiUrl("/testrun"))
                .authentication().basic(username, password)
                .jsonBody(jsonMapper.encodeToString(toTestCycleRequest(projectId, testRun, zephyrConfig)))
                .treatResponseAsValid()
                .await(ZephyrResponseDeserializer)
        }.getOrElse { cause -> throw ZephyrException("$errorMessageTemplate ${testRun.name}", cause) }
            .validateStatusCode { "$errorMessageTemplate ${testRun.name}: unsuccessful status code" }
            .runCatching { getJsonBody<CreateTestCycleResponse>() }
            .getOrElse { cause ->
                throw ZephyrException("$errorMessageTemplate '${testRun.name}': body deserialization error", cause)
            }
    }

    private fun toTestCycleRequest(
        projectId: Long,
        testRun: TestRun,
        zephyrConfig: ZephyrConfig
    ): CreateTestCycleRequest {
        val startTimeInstant = Instant.ofEpochMilli(testRun.startTime)
        val endTimeInstant = Instant.ofEpochMilli(testRun.endTime)
        val name = String.format(
            "%s (%s - %s)",
            testRun.name,
            startTimeInstant.atZone(zephyrConfig.timeZone).format(dateTimeFormatter),
            endTimeInstant.atZone(zephyrConfig.timeZone).format(dateTimeFormatter)
        )

        return CreateTestCycleRequest(
            projectId = projectId,
            name = name,
            plannedStartDate = startTimeInstant.toString(),
            plannedEndDate = endTimeInstant.toString(),
        )
    }

}
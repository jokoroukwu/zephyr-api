package io.github.jokoroukwu.zephyrapi.config

import java.net.URL
import java.time.ZoneId

data class ZephyrConfigImpl(
    override var timeZone: ZoneId,
    override val jiraUrl: URL,
    override val projectKey: String,
    override val username: String,
    override val password: String
) : ZephyrConfig {

    override fun toString() =
        "{timeZone: $timeZone, jiraUrl: $jiraUrl, projectKey: $projectKey, username: $username, password: ${
            "*".repeat(
                password.length
            )
        }}"

}
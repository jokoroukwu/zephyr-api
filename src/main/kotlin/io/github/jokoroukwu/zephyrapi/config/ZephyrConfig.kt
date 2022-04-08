package io.github.jokoroukwu.zephyrapi.config

import java.net.URL
import java.time.ZoneId

interface ZephyrConfig {
    /**
     * The actual timezone used to display Zephyr test result
     * start and end time
     */
    val timeZone: ZoneId

    /**
     * JIRA server URL e.g https://${your-jira-server-address}
     */
    val jiraUrl: URL

    /**
     * JIRA project key
     */
    val projectKey: String

    /**
     * JIRA login
     */
    val username: String

    /**
     * JIRA password
     */
    val password: String
}
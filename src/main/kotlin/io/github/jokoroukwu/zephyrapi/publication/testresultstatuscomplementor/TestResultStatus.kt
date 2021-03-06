package io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TestResultStatus {

    @SerialName("Not Executed")
    NOT_EXECUTED,

    @SerialName("In Progress")
    IN_PROGRESS,

    @SerialName("Pass")
    PASS,

    @SerialName("Fail")
    FAIL,

    @SerialName("Blocked")
    BLOCKED
}
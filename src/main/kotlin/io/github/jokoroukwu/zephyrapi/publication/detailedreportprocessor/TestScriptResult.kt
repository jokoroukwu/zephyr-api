package io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor

import kotlinx.serialization.Serializable

/**
 * Represents a Test Result's step in Zephyr UI
 */
@Serializable
data class TestScriptResult(
    val id: Long,
    val testResultStatusId: Long,
    val comment: String = ""
)
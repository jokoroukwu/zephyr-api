package io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor

import kotlinx.serialization.Serializable

@Serializable
data class ReportStepResult(val id: Long, val index: Int) {

    fun toTestScriptResult(statusId: Long, comment: String = "") = TestScriptResult(id, statusId, comment)
}
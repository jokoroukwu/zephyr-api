package io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.datasetfinalization

import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.TestScriptResult


data class FinalizationResult(
    val testScriptResults: List<TestScriptResult> = emptyList(),
    val comment: String? = null,
    val warningMessage: String? = null
) {
    inline fun onComment(consumer: (String) -> Unit): FinalizationResult {
        if (comment != null) {
            consumer(comment)
        }
        return this
    }

    inline fun onScriptResults(consumer: (List<TestScriptResult>) -> Unit): FinalizationResult {
        if (testScriptResults.isNotEmpty()) {
            consumer(testScriptResults)
        }
        return this
    }

    inline fun onWarning(consumer: (String) -> Unit): FinalizationResult {
        if (warningMessage != null) {
            consumer(warningMessage)
        }
        return this
    }
}
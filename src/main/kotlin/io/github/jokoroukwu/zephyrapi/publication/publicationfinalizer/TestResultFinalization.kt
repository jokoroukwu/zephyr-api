package io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer

import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.TestScriptResult

class TestResultFinalization(
    val finalizedResult: SerializableTestResult,
    val finalizedSteps: List<TestScriptResult>
)
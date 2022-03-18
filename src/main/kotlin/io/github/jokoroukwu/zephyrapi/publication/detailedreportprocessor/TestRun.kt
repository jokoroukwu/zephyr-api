package io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor

import kotlinx.serialization.Serializable

@Serializable
class TestRun(
    val key: String,
    val name: String
)
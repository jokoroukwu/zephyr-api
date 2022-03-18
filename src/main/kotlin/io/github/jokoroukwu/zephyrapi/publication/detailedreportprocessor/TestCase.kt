package io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor

import kotlinx.serialization.Serializable

@Serializable
class TestCase(
    val id: Long,
    val key: String,
    val name: String,
)
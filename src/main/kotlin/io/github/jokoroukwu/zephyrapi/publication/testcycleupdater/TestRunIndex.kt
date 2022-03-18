package io.github.jokoroukwu.zephyrapi.publication.testcycleupdater

import kotlinx.serialization.Serializable

@Serializable
class TestRunIndex(
    val id: Int,
    val index: Int
)
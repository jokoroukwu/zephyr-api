package io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor

import kotlinx.serialization.Serializable

@Serializable
data class TestScriptItem(val steps: List<StepItem>)

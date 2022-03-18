package io.github.jokoroukwu.zephyrapi.publication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("testDataResult")
data class TestDataResultBase(
    override val index: Int,
    override val isSuccess: Boolean,
    override val failedStepIndex: Int? = null,
    override val failureMessage: String = ""
) : TestDataResult
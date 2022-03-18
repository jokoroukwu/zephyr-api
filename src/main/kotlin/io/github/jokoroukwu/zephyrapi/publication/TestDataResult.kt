package io.github.jokoroukwu.zephyrapi.publication

interface TestDataResult {

    val index: Int

    val isSuccess: Boolean

    val failedStepIndex: Int?

    val failureMessage: String
}
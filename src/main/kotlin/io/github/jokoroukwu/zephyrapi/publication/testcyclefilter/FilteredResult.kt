package io.github.jokoroukwu.zephyrapi.publication.testcyclefilter

import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestResult
import java.util.*

data class FilteredResult(
    val testResult: ZephyrTestResult? = null,
    val issues: List<String> = LinkedList()
)

package io.github.jokoroukwu.zephyrapi.publication.keytoitemmapcomplementor

data class ZephyrProjectWithTestCases(
    val projectId: Long,
    val testCaseKeyToIdMap: Map<String, Long>
)
package io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor

import kotlinx.serialization.Serializable

@Serializable
class SerializableTestResultStatusItem(val id: Long, val name: TestResultStatus)
package io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.commentbuilder

import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.TestResultStatus

data class Comment(val text: String, val status: TestResultStatus)
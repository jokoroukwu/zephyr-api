package io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer

import io.github.jokoroukwu.zephyrapi.publication.StatusMap
import io.github.jokoroukwu.zephyrapi.publication.TestResult
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.ReportTestResult
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.TestScriptResult
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.commentbuilder.Comment
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.commentbuilder.CommentBuilderImpl
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.commentbuilder.TestResultCommentBuilder
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.datasetfinalization.FinalizationStrategyFactory
import io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.datasetfinalization.FinalizationStrategyFactoryImpl
import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.TestResultStatus
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class TestResultFinalizerImpl(
    private val finalizationStrategyFactory: FinalizationStrategyFactory = FinalizationStrategyFactoryImpl,
    private val commentBuilderFactory: () -> TestResultCommentBuilder = ::CommentBuilderImpl,
) : TestResultFinalizer {

    override fun finalizeTestResult(
        reportTestResult: ReportTestResult,
        testResult: TestResult,
        statusMap: StatusMap
    ): TestResultFinalization {
        val testDataResults = testResult.testDataResults
        val stepGroups = reportTestResult.groupSteps()
        val commentBuilder = commentBuilderFactory.invoke()
        val finalizedSteps = ArrayList<TestScriptResult>(reportTestResult.testScriptResults.size)
        for (i in testDataResults.indices) {
            val steps = stepGroups.getOrElse(i) { emptyList() }
            val testDataResult = testDataResults[i]
            val dataSetStatus = if (testDataResult.isSuccess) TestResultStatus.PASS else TestResultStatus.FAIL
            finalizationStrategyFactory
                .finalizationStrategy(testDataResult)
                .finalizeResult(statusMap, steps, testDataResult)
                .onWarning { logWarning(reportTestResult.testCase.key, it) }
                .onScriptResults(finalizedSteps::addAll)
                .onComment { commentBuilder.appendResultComment(Comment(it, dataSetStatus)) }
        }
        val testResultStatus = if (testResult.isSuccess) TestResultStatus.PASS else TestResultStatus.FAIL
        val finalizedTestResult =
            SerializableTestResult(
                id = reportTestResult.id,
                testResultStatusId = statusMap.getValue(testResultStatus),
                executionTime = testResult.endTime - testResult.startTime,
                comment = commentBuilder.toString()
            )

        return TestResultFinalization(finalizedTestResult, finalizedSteps)
    }

    private fun logWarning(testCaseKey: String, message: String) {
        logger.warn { "{test_case_key: $testCaseKey, warning: $message}" }
    }
}
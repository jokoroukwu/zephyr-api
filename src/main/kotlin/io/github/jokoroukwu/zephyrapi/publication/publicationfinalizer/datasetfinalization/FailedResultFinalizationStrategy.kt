package io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.datasetfinalization

import io.github.jokoroukwu.zephyrapi.publication.StatusMap
import io.github.jokoroukwu.zephyrapi.publication.TestDataResult
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.ReportStepResult
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.TestScriptResult
import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.TestResultStatus.*


object FailedResultFinalizationStrategy : DatasetFinalizationStrategy {
    const val warningMessage =
        "failed step (%d) exceeds actual number of steps (%d): this may only happen if a third party had modified test result after its creation but before it was updated"

    override fun finalizeResult(
        statusMap: StatusMap,
        steps: List<ReportStepResult>,
        datasetResultTest: TestDataResult
    ): FinalizationResult {
        val failedStepIndex = datasetResultTest.failedStepIndex
        return when {
            //  in case there's no failed step
            //  we still need to update test result
            //  so just add a comment
            failedStepIndex == null -> FinalizationResult(comment = datasetResultTest.failureMessage)
            //  such inconsistency should rarely happen
            failedStepIndex >= steps.size -> {
                FinalizationResult(
                    comment = datasetResultTest.failureMessage,
                    warningMessage = warningMessage.format(failedStepIndex + 1, steps.size)
                )
            }
            else -> finalizeSteps(datasetResultTest, statusMap, steps)
        }
    }

    private fun finalizeSteps(
        datasetResultTest: TestDataResult,
        statusMap: StatusMap,
        steps: List<ReportStepResult>
    ): FinalizationResult {
        val failedStatusId = statusMap.getValue(FAIL)
        val finalizedSteps = ArrayList<TestScriptResult>(steps.size)
        val failedStepIndex = datasetResultTest.failedStepIndex!!
        //  add the FAILED step first
        finalizedSteps.add(
            steps[failedStepIndex].toTestScriptResult(
                statusId = failedStatusId,
                comment = datasetResultTest.failureMessage
            )
        )
        //  now mark all steps prior to failed one as PASSED and add them
        val passedStatusId = statusMap.getValue(PASS)
        (0 until failedStepIndex)
            .forEach { i -> finalizedSteps.add(steps[i].toTestScriptResult(passedStatusId)) }

        //  finally mark all steps following the failed one as BLOCKED and add them
        val blockedStatusId = statusMap.getValue(BLOCKED)
        (failedStepIndex + 1 until steps.size)
            .forEach { i -> finalizedSteps.add(steps[i].toTestScriptResult(blockedStatusId)) }
        return FinalizationResult(testScriptResults = finalizedSteps)
    }
}

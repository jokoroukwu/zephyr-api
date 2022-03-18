package io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.datasetfinalization

import io.github.jokoroukwu.zephyrapi.publication.TestDataResult
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.ReportStepResult
import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.TestResultStatus

object PassedResultFinalizationStrategy : DatasetFinalizationStrategy {

    override fun finalizeResult(
        statusMap: Map<TestResultStatus, Long>,
        steps: List<ReportStepResult>,
        datasetResultTest: TestDataResult
    ): FinalizationResult {
        return when {
            steps.isEmpty() -> FinalizationResult(comment = TestResultStatus.PASS.toString())
            else -> {
                val passedStatusId = statusMap.getValue(TestResultStatus.PASS)
                FinalizationResult(steps.map { it.toTestScriptResult(passedStatusId) })
            }
        }
    }
}

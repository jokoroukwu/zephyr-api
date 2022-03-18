package io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.datasetfinalization

import io.github.jokoroukwu.zephyrapi.publication.StatusMap
import io.github.jokoroukwu.zephyrapi.publication.TestDataResult
import io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor.ReportStepResult

interface DatasetFinalizationStrategy {

    fun finalizeResult(
        statusMap: StatusMap,
        steps: List<ReportStepResult>,
        datasetResultTest: TestDataResult
    ): FinalizationResult
}
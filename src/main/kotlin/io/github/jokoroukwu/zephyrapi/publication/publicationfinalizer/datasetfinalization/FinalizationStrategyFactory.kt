package io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.datasetfinalization

import io.github.jokoroukwu.zephyrapi.publication.TestDataResult

interface FinalizationStrategyFactory {

    fun finalizationStrategy(testDataResult: TestDataResult): DatasetFinalizationStrategy
}

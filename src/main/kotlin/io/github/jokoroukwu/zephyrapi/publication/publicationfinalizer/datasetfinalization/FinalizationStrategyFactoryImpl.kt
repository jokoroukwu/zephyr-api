package io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.datasetfinalization

import io.github.jokoroukwu.zephyrapi.publication.TestDataResult


object FinalizationStrategyFactoryImpl : FinalizationStrategyFactory {

    override fun finalizationStrategy(testDataResult: TestDataResult) =
        if (testDataResult.isSuccess) PassedResultFinalizationStrategy else FailedResultFinalizationStrategy
}


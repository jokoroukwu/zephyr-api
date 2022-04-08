package io.github.jokoroukwu.zephyrapi.publication

/**
 * Represents test result for a particular data set.
 *  A [TestResult] will be associated with either
 *  a single (in case the test is not data driven) or multiple
 *  (if the test is data driven) test data results.
 *
 */
interface TestDataResult {

    val index: Int

    val isSuccess: Boolean

    val failedStepIndex: Int?

    val failureMessage: String
}
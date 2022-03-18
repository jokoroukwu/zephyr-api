package io.github.jokoroukwu.zephyrapi.publication.detailedreportprocessor

import kotlinx.serialization.Serializable

typealias Steps = List<ReportStepResult>
typealias StepGroups = List<Steps>

@Serializable
class ReportTestResult(
    val id: Long,
    val testCase: TestCase,
    val testScriptResults: List<ReportStepResult>
) {
    fun groupSteps(): StepGroups {
        if (testScriptResults.isEmpty()) {
            return emptyList();
        }
        val stepGroups = ArrayList<List<ReportStepResult>>(5)
        var steps: MutableList<ReportStepResult> = ArrayList()

        var previousScriptResultIndex = -1;
        for (testScriptResult in testScriptResults) {
            if (testScriptResult.index > previousScriptResultIndex) {
                steps.add(testScriptResult)
            } else {
                stepGroups.add(steps)
                steps = ArrayList<ReportStepResult>().apply { add(testScriptResult) }
            }
            previousScriptResultIndex = testScriptResult.index
        }
        stepGroups.add(steps)
        return stepGroups
    }
}

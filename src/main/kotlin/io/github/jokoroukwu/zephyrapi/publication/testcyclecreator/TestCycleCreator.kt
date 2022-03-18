package io.github.jokoroukwu.zephyrapi.publication.testcyclecreator

import io.github.jokoroukwu.zephyrapi.http.SupervisorIOContext
import io.github.jokoroukwu.zephyrapi.publication.PublicationData
import io.github.jokoroukwu.zephyrapi.publication.PublicationDataProcessor
import io.github.jokoroukwu.zephyrapi.publication.ZephyrTestCycle
import io.github.jokoroukwu.zephyrapi.publication.testcycleupdater.TestCycleUpdater
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.util.concurrent.ArrayBlockingQueue

private val logger = KotlinLogging.logger { }

class TestCycleCreator(
    private val createTestCycleRequestSender: CreateTestCycleRequestSender = CreateTestCycleRequestSender(),
    private val nextProcessor: PublicationDataProcessor = TestCycleUpdater()
) : PublicationDataProcessor {
    /**
     * Creates Zephyr tests cycles using [ZephyrTestCycle.name] provided in [publicationData].
     * Then complements [publicationData] test cycles with [ZephyrTestCycle.id] and [ZephyrTestCycle.key]
     */
    override fun process(publicationData: PublicationData): Boolean {
        return publicationData.takeUnless { it.testCycles.isEmpty() }
            ?.let(::asyncCreateCycles)
            ?.let { publicationData.copy(testCycles = it) }
            ?.let(nextProcessor::process)
            ?: false.also { logger.info { "No test cycles to create" } }
    }

    private fun asyncCreateCycles(publicationData: PublicationData): Collection<ZephyrTestCycle> {
        val queue = ArrayBlockingQueue<ZephyrTestCycle>(publicationData.testCycles.size)
        runBlocking {
            launch(SupervisorIOContext) {
                for (testCycle in publicationData.testCycles) {
                    launch {
                        logger.debug { "Creating test cycle: {name: ${testCycle.name}}" }
                        val response =
                            createTestCycleRequestSender.createTestCycle(publicationData.projectId, testCycle)
                        val testCycleKey = response.key
                        logger.debug { "Test cycle successfully created: {name: '${testCycle.name}', key: '$testCycleKey'}" }
                        queue.add(testCycle.copy(id = response.id, key = testCycleKey))
                    }
                }
            }.join()
        }
        return queue
    }
}

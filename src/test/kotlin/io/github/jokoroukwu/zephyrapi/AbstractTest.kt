package io.github.jokoroukwu.zephyrapi

import io.github.jokoroukwu.zephyrapi.config.ZephyrConfig
import io.github.jokoroukwu.zephyrapi.config.ZephyrConfigImpl
import io.mockk.clearAllMocks
import io.mockk.unmockkObject
import org.testng.annotations.AfterClass
import org.testng.annotations.AfterMethod
import java.net.URL
import java.time.ZoneOffset

abstract class AbstractTest {
    protected val dummyConfig: ZephyrConfig = ZephyrConfigImpl(
        ZoneOffset.UTC,
        URL("https://jira.com"),
        "PROJ-123",
        "user",
        "pass"
    )

    @AfterMethod(alwaysRun = true)
    protected open fun baseTearDown() {
        clearAllMocks()
    }

    @AfterClass(alwaysRun = true)
    protected fun baseAfterClass() {
        unmockkObject(getObjectsToUnmock())
    }

    protected abstract fun getObjectsToUnmock(): Array<Any>


}
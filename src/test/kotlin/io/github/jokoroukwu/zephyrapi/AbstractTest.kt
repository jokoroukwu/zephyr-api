package io.github.jokoroukwu.zephyrapi

import io.mockk.clearAllMocks
import io.mockk.unmockkObject
import org.testng.annotations.AfterClass
import org.testng.annotations.AfterMethod

abstract class AbstractTest {

    @AfterMethod(alwaysRun = true)
    protected fun tearDown() {
        clearAllMocks()
    }

    @AfterClass(alwaysRun = true)
    protected fun afterClass() {
        unmockkObject(getObjectsToUnmock())
    }

    protected abstract fun getObjectsToUnmock(): Array<Any>


}
package io.github.jokoroukwu.zephyrapi.annotation

/**
 * Indicates that the method corresponds to a particular
 * step of Zephyr test result.
 *
 * @param value index of the corresponding Zephyr test result step (zero based)
 * @param description step description (optional)
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Step(val value: Int, val description: String = "<none>")

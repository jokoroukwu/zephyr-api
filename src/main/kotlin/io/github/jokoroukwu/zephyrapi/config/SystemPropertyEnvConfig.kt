package io.github.jokoroukwu.zephyrapi.config

import org.snakeyaml.engine.v2.env.EnvConfig
import java.util.*

object SystemPropertyEnvConfig : EnvConfig {
    private const val EMPTY_SAME_AS_NULL = ":"

    /**
     * Attempts to get system property for the provided [name] when either [environment] is null
     * or the [separator] starts with [EMPTY_SAME_AS_NULL],
     * which indicates that empty [environment] is not accepted
     */
    override fun getValueFor(name: String, separator: String, value: String, environment: String?): Optional<String> {
        return when {
            environment == null -> tryGetProperty(name)
            environment.isEmpty() && separator.startsWith(EMPTY_SAME_AS_NULL) -> tryGetProperty(name)
            else -> Optional.of(environment)
        }
    }

    private fun tryGetProperty(name: String) = Optional.ofNullable(System.getProperty(name))
}
package io.github.jokoroukwu.zephyrapi.config

import io.github.jokoroukwu.zephyrapi.config.SystemPropResolvingConstructor.Companion.ENV_OR_PROP_FORMAT
import mu.KotlinLogging
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.resolver.JsonScalarResolver
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.ZoneId
import java.util.*

object ZephyrConfigLoaderImpl : ZephyrConfigLoader {
    private const val fileName = "zephyr-config.yml"
    private const val systemPropName = "zephyr.config"
    private const val envVarName = "ZEPHYR_CONFIG"
    private val lazyZephyrConfig: ZephyrConfigImpl by lazy { loadConfig() }

    override fun getZephyrConfig() = lazyZephyrConfig;

    @Suppress("UNCHECKED_CAST")
    private fun loadConfig(): ZephyrConfigImpl {
        val yamlInputStream = System.getenv(envVarName)?.fileInputStream()
            ?: System.getProperty(systemPropName)?.fileInputStream()
            ?: ClassLoader.getSystemResourceAsStream(fileName)
            ?: throw FileNotFoundException("Zephyr configuration file '$fileName' not found")

        val settings = LoadSettings.builder()
            .setEnvConfig(Optional.of(SystemPropertyEnvConfig))
            .setScalarResolver(JsonScalarResolver().also {
                it.addImplicitResolver(Tag.ENV_TAG, ENV_OR_PROP_FORMAT, "$")
            })
            .setDefaultMap { i -> HashMap<Any?, Any?>(i, 1F) }
            .build()

        return yamlInputStream.use {
            Load(settings, SystemPropResolvingConstructor(settings)).loadFromInputStream(it.buffered())
                .let { yamlObject -> yamlObject as Map<String, Any?> }
                .let(ZephyrConfigLoaderImpl::parseZephyrConfig)
                .also { logger.debug { "ZephyrNg configuration loaded successfully" } }
        }
    }

    private fun parseZephyrConfig(propertyMap: Map<String, Any?>) =
        with(propertyMap) {
            ZephyrConfigImpl(
                timeZone = ZoneId.of(getProperty("time-zone")),
                projectKey = getProperty("project-key"),
                jiraUrl = getProperty("jira-url"),
                username = getProperty("username"),
                password = getProperty("password")
            )
        }

    private fun Map<String, Any?>.getProperty(key: String) =
        get(key) as String? ?: throw NoSuchElementException("Missing mandatory configuration property: $key")

    private fun String.fileInputStream(): InputStream {
        return Paths.get(this).takeIf(Files::exists)?.let(Files::newInputStream)
            ?.also { logger.trace { "ZephyrNG configuration file found: $this" } }
            ?: throw  FileNotFoundException("ZephyrNG configuration file not found: $this")
    }
}

private val logger = KotlinLogging.logger { }


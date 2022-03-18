package io.github.jokoroukwu.zephyrapi.config

interface ZephyrConfigLoader {

    fun getZephyrConfig(): ZephyrConfigImpl
}
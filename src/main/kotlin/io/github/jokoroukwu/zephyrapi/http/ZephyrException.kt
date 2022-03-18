package io.github.jokoroukwu.zephyrapi.http

class ZephyrException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, exception: Throwable?) : super(message, exception)
}
package io.github.jokoroukwu.zephyrapi.http

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

object SupervisorIOContext : CoroutineContext by (SupervisorJob() + Dispatchers.IO)
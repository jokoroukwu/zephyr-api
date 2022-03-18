package io.github.jokoroukwu.zephyrapi

import org.assertj.core.api.SoftAssertions

inline fun softly(assertion: SoftAssertions.() -> Unit) {
    assertion.invoke(SoftAssertions())
}
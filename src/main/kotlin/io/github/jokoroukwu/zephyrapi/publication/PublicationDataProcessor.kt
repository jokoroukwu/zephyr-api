package io.github.jokoroukwu.zephyrapi.publication

interface PublicationDataProcessor {

    fun process(publicationContext: PublicationContext): Boolean
}
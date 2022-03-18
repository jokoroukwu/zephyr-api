package io.github.jokoroukwu.zephyrapi.publication

interface PublicationDataProcessor {

    fun process(publicationData: PublicationData): Boolean
}
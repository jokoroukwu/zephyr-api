package io.github.jokoroukwu.zephyrapi.http

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.Response

object ZephyrResponseDeserializer : Deserializable<ZephyrResponse> {

    override fun deserialize(response: Response): ZephyrResponse =
        ZephyrResponse(response.statusCode, response.responseMessage, response.data)

}
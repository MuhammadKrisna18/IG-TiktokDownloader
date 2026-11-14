package com.portofolio.mediagrabber.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CobaltRequestDto(
    @SerialName("url") val url: String,
    @SerialName("videoQuality") val videoQuality: String = "1080",
    @SerialName("youtubeVideoCodec") val youtubeVideoCodec: String = "h264"
)

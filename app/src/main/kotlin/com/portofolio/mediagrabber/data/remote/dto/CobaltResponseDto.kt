package com.portofolio.mediagrabber.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CobaltResponseDto(
    @SerialName("status") val status: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("filename") val filename: String? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("picker") val picker: List<CobaltPickerItemDto>? = null
)

@Serializable
data class CobaltPickerItemDto(
    @SerialName("type") val type: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("thumb") val thumb: String? = null
)

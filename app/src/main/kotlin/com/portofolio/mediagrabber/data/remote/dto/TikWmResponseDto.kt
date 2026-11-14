package com.portofolio.mediagrabber.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TikWmResponseDto(
    @SerialName("code") val code: Int? = null,
    @SerialName("msg") val msg: String? = null,
    @SerialName("data") val data: TikWmDataDto? = null
)

@Serializable
data class TikWmDataDto(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("cover") val cover: String? = null,
    @SerialName("play") val play: String? = null,
    @SerialName("wmplay") val wmplay: String? = null,
    @SerialName("hdplay") val hdplay: String? = null,
    @SerialName("duration") val duration: Long? = null,
    @SerialName("author") val author: TikWmAuthorDto? = null
)

@Serializable
data class TikWmAuthorDto(
    @SerialName("id") val id: String? = null,
    @SerialName("unique_id") val uniqueId: String? = null,
    @SerialName("nickname") val nickname: String? = null,
    @SerialName("avatar") val avatar: String? = null
)

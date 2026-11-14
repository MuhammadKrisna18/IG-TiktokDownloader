package com.portofolio.mediagrabber.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubReleaseDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("tag_name") val tagName: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("body") val body: String? = null,
    @SerialName("prerelease") val prerelease: Boolean = false,
    @SerialName("assets") val assets: List<GithubAssetDto> = emptyList()
)

@Serializable
data class GithubAssetDto(
    @SerialName("name") val name: String? = null,
    @SerialName("browser_download_url") val browserDownloadUrl: String? = null,
    @SerialName("content_type") val contentType: String? = null,
    @SerialName("size") val size: Long = 0L
)

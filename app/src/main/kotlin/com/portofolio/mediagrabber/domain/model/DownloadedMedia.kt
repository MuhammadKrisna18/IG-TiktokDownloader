package com.portofolio.mediagrabber.domain.model

data class DownloadedMedia(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val platform: PlatformType,
    val contentUri: String,
    val filename: String,
    val downloadedAt: Long = System.currentTimeMillis()
)

package com.portofolio.mediagrabber.domain.model

data class MediaItem(
    val id: String,
    val originalUrl: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val durationSeconds: Long = 0L,
    val platform: PlatformType,
    val downloadOptions: List<DownloadOption>
)

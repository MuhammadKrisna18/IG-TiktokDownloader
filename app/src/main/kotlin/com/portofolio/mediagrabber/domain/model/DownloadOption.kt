package com.portofolio.mediagrabber.domain.model

data class DownloadOption(
    val id: String,
    val title: String,
    val downloadUrl: String,
    val isWatermarkFree: Boolean = true,
    val quality: String = "HD",
    val extension: String = "mp4"
)

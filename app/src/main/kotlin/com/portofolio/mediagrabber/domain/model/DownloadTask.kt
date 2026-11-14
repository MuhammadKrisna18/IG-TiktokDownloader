package com.portofolio.mediagrabber.domain.model

data class DownloadTask(
    val id: String,
    val url: String,
    val filename: String,
    val title: String,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val progressPercent: Int = 0,
    val status: DownloadStatus = DownloadStatus.IDLE,
    val destinationUri: String? = null,
    val errorMessage: String? = null
)
